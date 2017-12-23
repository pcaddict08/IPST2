/******************************************************************************
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
 ******************************************************************************/

package com.einzig.ipst2.activities;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseHelper;
import com.einzig.ipst2.oauth.OAuth2Authenticator;
import com.einzig.ipst2.parse.EmailParser;
import com.einzig.ipst2.parse.FolderGetter;
import com.einzig.ipst2.parse.MailBundle;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.DialogHelper;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.PreferencesHelper;
import com.einzig.ipst2.util.ThemeHelper;
import com.sun.mail.imap.IMAPStore;

import org.joda.time.LocalDate;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import static com.einzig.ipst2.database.DatabaseHelper.DATE_FORMATTER;

/**
 * @author Ryan Porterfield
 * @since 2017-12-22
 */

public class EmailParseActivity extends AppCompatActivity {
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
    /** App preferences */
    final private SharedPreferences preferences;
    /** OAuth token */
    final private String token;

    /**
     * Initialize a new EmailParseActivity to asynchronously getPortal new portal submission emails.
     *
     * @param bundle
     */
    public EmailParseActivity(MailBundle bundle) {
        this.bundle = bundle;
        this.db = new DatabaseHelper(this);
        this.messages = bundle.getMessages();
        this.parser = new EmailParser();
        this.helper = new PreferencesHelper(this);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.token = token;
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

    /**
     *
     */
    private void doInBackground() {
        Logger.d("Parsing email");
        LocalDate now = LocalDate.now();
        for (int i = 0; i < messages.length; i++) {
            PortalSubmission p = parser.getPortal(messages[i]);
            addPortal(p);
            publishProgress(i, messages.length);
        }
        onEmailParse(now);
        bundle.cleanup();
    }

    protected MailBundle doInBackground(Void... voids) {
        OAuth2Authenticator sender = new OAuth2Authenticator();
        IMAPStore store = sender.getIMAPStore(account.name, token);
        if (store == null) {
            DialogHelper.showSimpleDialog(R.string.invalidtokentitle_getmailtask,
                                          R.string.invalidtokenmessage_getmailtask, activity);
            AccountManager.get(activity).invalidateAuthToken("com.google", token);
        } else {
            try {
                Folder folder = getFolder(store);
                if (folder != null) {
                    folder.open(Folder.READ_ONLY);
                    Message[] messages = searchMailbox(folder);
                    fetchMessages(folder, messages);
                    return new MailBundle(folder, messages, store);
                }
            } catch (MessagingException e) {
                Logger.e(e.toString());
            }
        }
        return null;
    }

    /**
     * Fetch envelope and content info of messages
     *
     * @param folder   Folder messages are contained in
     * @param messages Array of messages that matched the search
     * @see FetchProfile
     */
    private void fetchMessages(Folder folder, Message[] messages) {
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        Logger.d("Fetching messages");
        try {
            folder.fetch(messages, fp);
        } catch (MessagingException e) {
            Logger.e(e.toString());
        }
    }

    private Folder getFolder(IMAPStore store) throws MessagingException {
        Folder[] folders = store.getDefaultFolder().list();
        Folder folder = new FolderGetter(this, folders, preferences).getFolder();
        if (folder != null) {
            PreferencesHelper helper = new PreferencesHelper(activity.getApplicationContext());
            Logger.d(helper.folderKey() + " -> " + folder.getFullName());
            helper.set(helper.folderKey(), folder.getFullName());
        }
        return folder;
    }

    /**
     * Get the last date email was parsed
     *
     * @param dateStr String representation of the last parse date
     * @return Date the email was parsed if previously parsed, otherwise the date Ingress launched
     */
    private LocalDate getLastParseDate(String dateStr) {
        LocalDate d;
        try {
            d = DATE_FORMATTER.parseLocalDate(dateStr);
        } catch (IllegalArgumentException e) {
            d = new LocalDate(2012, 10, 15);
        }
        return d;
    }

    /**
     * Get SearchTerm to find relevant emails
     *
     * @param lastParseDate Date of previous parse for ReceivedDateTerm
     * @param anySource     boolean that decides if the email search terms 'from' are included.
     * @return Search term that will find all portal submission emails
     */
    private SearchTerm getSearchTerm(LocalDate lastParseDate, boolean anySource) {
        SearchTerm portalTerm = new SubjectTerm("ingress portal");
        SearchTerm reviewTerm = new SubjectTerm("portal review");
        SearchTerm submissionTerm = new SubjectTerm("portal submission");
        SearchTerm submittedTerm = new SubjectTerm("portal submitted");
        SearchTerm subjectTerm = new OrTerm(new SearchTerm[]{portalTerm, reviewTerm,
                submissionTerm, submittedTerm});
        ReceivedDateTerm minDateTerm = new ReceivedDateTerm(ComparisonTerm.GT, lastParseDate.toDate());
        SearchTerm invalidTerm = new NotTerm(new SubjectTerm("invalid"));
        SearchTerm editTerm = new NotTerm(new SubjectTerm("edit"));
        SearchTerm editsTerm = new NotTerm(new SubjectTerm("edits"));
        SearchTerm photoTerm = new NotTerm(new SubjectTerm("photo"));
        SearchTerm superOpsTerm = new FromStringTerm("super-ops@google.com");
        SearchTerm iSupportTerm1 = new FromStringTerm("ingress-support@google.com");
        SearchTerm iSupportTerm2 = new FromStringTerm("ingress-support@nianticlabs.com");
        Logger.d("Last Parse Date: " + lastParseDate.toString());
        SearchTerm fromTerm = new OrTerm(new SearchTerm[]
                                                 {superOpsTerm, iSupportTerm1, iSupportTerm2});
        if (anySource)
            return new AndTerm(new SearchTerm[]
                                       {subjectTerm, minDateTerm, invalidTerm, editTerm, editsTerm, photoTerm});
        else
            return new AndTerm(new SearchTerm[]
                                       {subjectTerm, minDateTerm, invalidTerm, editTerm, editsTerm, photoTerm,
                                               fromTerm});
    }

    /**
     * Initialize the progress dialog
     */
    private void initProgressDialog() {
        dialog = new ProgressDialog(this, ThemeHelper.getDialogTheme(this));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(this.getString(R.string.parsing_email));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMax(messages.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);


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
    protected void onPostExecute(Void voids) {
        Logger.d("Accepted portals: " + db.getAcceptedCount());
        Logger.d("Pending portals: " + db.getPendingCount());
        Logger.d("Rejected portals: " + db.getRejectedCount());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.dismiss();
        this.buildUIAfterParsing();
    }

    /*
     * Populate messages array and display progress dialog
     */
    protected void onPreExecute() {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.show();
    }

    /*
     * Update progress dialog
     */
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0] + 1);
        Logger.v("Parsing: " + dialog.getProgress() + " / " + dialog.getMax());
    }

    /**
     * Search a mail folder for portal submission and response emails
     *
     * @param folder Mail folder containing portal submission emails
     * @return All emails matching the search terms
     * @throws MessagingException if the library encounters an error
     */
    private Message[] searchMailbox(Folder folder) throws MessagingException {
        PreferencesHelper helper = new PreferencesHelper(getApplicationContext());
        LocalDate lastParseDate = getLastParseDate(helper.get(helper.parseDateKey()));
        return folder.search(getSearchTerm(lastParseDate, false));
    }
}
