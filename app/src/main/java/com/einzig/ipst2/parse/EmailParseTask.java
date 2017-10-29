/******************************************************************************
 *                                                                            *
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                  *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        *
 * DEALINGS IN THE SOFTWARE.                                                  *
 *                                                                            *
 ******************************************************************************/

package com.einzig.ipst2.parse;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.WindowManager;

import com.einzig.ipst2.R;
import com.einzig.ipst2.activities.MainActivity;
import com.einzig.ipst2.database.DatabaseHelper;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.PreferencesHelper;
import com.einzig.ipst2.util.ThemeHelper;

import org.joda.time.LocalDate;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.MessagingException;

import static com.einzig.ipst2.database.DatabaseHelper.DATE_FORMATTER;

/**
 * Asynchronously parses the user's emails to update portal submission activity.
 *
 * @author Ryan Porterfield
 * @since 2015-07-30
 */
public class EmailParseTask extends AsyncTask<Void, Integer, Void> {
    /** The calling activity. Used to update UI elements */
    final private MainActivity activity;
    /** Wrapper class for IMAPStore, Folder, and Message[] resources */
    final private MailBundle bundle;
    /** Database for adding portals */
    final private DatabaseHelper db;
    /** Array of messages that match the search terms */
    final private Message[] messages;
    /** Does the actual parsing of emails */
    final private EmailParser parser;
    /** App preferences */
    final private PreferencesHelper helper;
    /** Display parsing progress */
    private ProgressDialog dialog;

    /**
     * Initialize a new EmailParseActivity to asynchronously getPortal new portal submission emails.
     *
     * @param activity The calling activity.
     */
    public EmailParseTask(MainActivity activity, MailBundle bundle) {
        this.activity = activity;
        this.bundle = bundle;
        this.db = new DatabaseHelper(activity);
        this.messages = bundle.getMessages();
        this.parser = new EmailParser();
        this.helper = new PreferencesHelper(activity);
        addMailcaps();
        initProgressDialog();
        System.getProperties().setProperty("mail.store.protocol", "imaps");
    }

    /**
     * Add mailcaps for the mail library.
     */
    private void addMailcaps() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap(
                "message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    /**
     * Add a portal to the database
     *
     * @param p Instance of PortalSubmission or subclass to add to the database
     */
    private void addPortal(PortalSubmission p) {
        if (p instanceof PortalAccepted && !db.containsAccepted(p.getPictureURL(), p.getName())) {
            addPortalAccepted((PortalAccepted) p);
        } else if (p instanceof PortalRejected && !db.containsRejected(p.getPictureURL(), p.getName())) {
            addPortalRejected((PortalRejected) p);
        } else if (p != null && !db.containsPending(p.getPictureURL(), p.getName())
                && !db.containsRejected(p.getPictureURL(), p.getName())
                && !db.containsAccepted(p.getPictureURL(), p.getName()))
            addPortalSubmission(p);
    }

    /**
     * Add a portal to the database
     *
     * @param portal Instance of PortalAccepted to add to the database
     */
    public void addPortalAccepted(PortalAccepted portal) {
        PortalSubmission pending = db.getPendingPortal(portal.getPictureURL(), portal.getName());
        if (pending != null) {
            portal.setDateSubmitted(pending.getDateSubmitted());
            db.deletePending(pending);
        } else {
            portal.setDateSubmitted(portal.getDateResponded());
        }
        db.addPortalAccepted(portal);
    }

    /**
     * Add a portal to the database
     *
     * @param portal Instance of PortalRejected to add to the database
     */
    private void addPortalRejected(PortalRejected portal) {
        PortalSubmission pending = db.getPendingPortal(portal.getPictureURL(), portal.getName());
        if (pending != null) {
            portal.setDateSubmitted(pending.getDateSubmitted());
            db.deletePending(pending);
        } else {
            portal.setDateSubmitted(portal.getDateResponded());
        }
        db.addPortalRejected(portal);
    }

    /**
     * Add a portal to the database
     *
     * @param portal Instance of PortalSubmission (but not a subclass) to add to the database
     */
    private void addPortalSubmission(PortalSubmission portal) {
        db.addPortalSubmission(portal);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Logger.d("Parsing email");
        LocalDate now = LocalDate.now();
        for (int i = 0; i < messages.length; i++) {
            PortalSubmission p = parser.getPortal(messages[i]);
            addPortal(p);
            publishProgress(i, messages.length);
            if (isCancelled()) {
                try {
                    now = new LocalDate(messages[i].getReceivedDate());
                } catch (MessagingException e) {
                    Logger.e(e.toString());
                }
                break;
            }
        }
        onEmailParse(now);
        bundle.cleanup();
        return null;
    }

    /**
     * Initialize the progress dialog
     */
    private void initProgressDialog() {
        dialog = new ProgressDialog(activity, ThemeHelper.getDialogTheme(activity));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(activity.getString(R.string.parsing_email));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMax(messages.length);
    }

    /**
     * Update the mostRecentDate preference after email has been parsed.
     *
     * @param parseDate Last time email was parsed
     */
    private void onEmailParse(LocalDate parseDate) {
        String dateString = DATE_FORMATTER.print(parseDate);
        Logger.d(helper.parseDateKey() + " -> " + dateString);
        helper.set(helper.parseDateKey(), dateString);
    }

    /*
     * Dismiss progress dialog and print some debug info
     */
    @Override
    protected void onPostExecute(Void voids) {
        Logger.d("Accepted portals: " + db.getAcceptedCount());
        Logger.d("Pending portals: " + db.getPendingCount());
        Logger.d("Rejected portals: " + db.getRejectedCount());
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.dismiss();
        activity.buildUIAfterParsing();
    }

    /*
     * Populate messages array and display progress dialog
     */
    @Override
    protected void onPreExecute() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.show();
    }

    /*
     * Update progress dialog
     */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0] + 1);
        Logger.v("Parsing: " + dialog.getProgress() + " / " + dialog.getMax());
    }
}
