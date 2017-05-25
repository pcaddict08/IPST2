/*
 *  ********************************************************************************************** *
 *  * ********************************************************************************************** *
 *  *                                                                                                *
 *  * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                                      *
 *  *                                                                                                *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software  *
 *  * and associated documentation files (the "Software"), to deal in the Software without           *
 *  * restriction, including without limitation the rights to use, copy, modify, merge, publish,     *
 *  * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the  *
 *  * Software is furnished to do so, subject to the following conditions:                           *
 *  *                                                                                                *
 *  * The above copyright notice and this permission notice shall be included in all copies or       *
 *  * substantial portions of the Software.                                                          *
 *  *                                                                                                *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING  *
 *  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND     *
 *  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,   *
 *  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.        *
 *  *                                                                                                *
 *  * ********************************************************************************************** *
 *  * **********************************************************************************************
 */

package com.einzig.ipst2.parse;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import com.einzig.ipst2.activities.MainActivity;
import com.einzig.ipst2.oauth.OAuth2Authenticator;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.sun.mail.imap.IMAPStore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

/**
 * Asynchronously parses the user's emails to update portal submission activity.
 *
 * @author Ryan Porterfield
 * @since 2015-07-30
 */
public class EmailParseTask extends AsyncTask<String, Integer, Integer> {
    /** The URL to get the OAuth token from */
    static final private String AUTH_URL = "oauth2:https://mail.google.com/";
    /** The user's email account that portal submission emails go to */
    final private Account account;
    /** Format for parsing and printing dates */
    final private DateFormat dateFormat;
    /**  */
    final private DatabaseInterface db;
    /** Does the actual parsing of emails */
    final private EmailParser parser;
    final private SharedPreferences preferences;
    /** The calling activity. Used to update UI elements */
    final private MainActivity activity;
    /**
     * Display parsing progress
     */
    private ProgressDialog dialog;

    /**
     * Initialize a new EmailParseActivity to asynchronously getPortal new portal submission emails.
     *
     * @param activity The calling activity.
     * @param account The email account to getPortal from.
     */
    public EmailParseTask(MainActivity activity, Account account) {
        this.account = account;
        this.activity = activity;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        this.db = new DatabaseInterface(activity);
        this.parser = new EmailParser(db);
        this.preferences = activity.getPreferences(MainActivity.MODE_PRIVATE);
        addMailcaps();
        initProgressDialog();
        System.getProperties().setProperty("mail.store.protocol", "imaps");
    }

    /**
     *
     */
    private void addMailcaps() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
    }

    private void addPortal(PortalSubmission p) {
        if (p instanceof PortalAccepted)
            addPortalAccepted((PortalAccepted) p);
        else if (p instanceof PortalRejected)
            addPortalRejected((PortalRejected) p);
        else if (p != null)
            addPortalSubmission(p);
    }

    /**
     *
     */
    private void addPortalAccepted(PortalAccepted portal) {
        Log.d(MainActivity.TAG, "Adding approved portal: " + portal.getName());
        PortalSubmission pending = db.getPendingPortal(portal.getPictureURL());
        if (pending != null)
            db.deletePending(pending);
        db.addPortalAccepted(portal);
    }

    /**
     *
     */
    private void addPortalSubmission(PortalSubmission portal) {
        Log.d(MainActivity.TAG, "Adding submitted portal: " + portal.getName());
        db.addPortalSubmission(portal);
    }

    /**
     *
     */
    private void addPortalRejected(PortalRejected portal) {
        Log.d(MainActivity.TAG, "Adding rejected portal: " + portal.getName());
        PortalSubmission pending = db.getPendingPortal(portal.getPictureURL());
        if (pending != null)
            db.deletePending(pending);
        db.addPortalRejected(portal);
    }

    /**
     * Authenticate with GMail.
     * @return the OAuth token as a string.
     */
    private String authenticate() {
        AuthToken authToken = new AuthToken();
        AccountManager.get(activity).getAuthToken(account, AUTH_URL, null, activity, authToken, null);
        return authToken.getToken();
    }

    /*
     *
     */
    @Override
    protected Integer doInBackground(String... params) {
        Log.d(MainActivity.TAG, "Parsing email");
        Log.d(MainActivity.TAG, "Account name: " + account.name);
        String token = authenticate();
        Message messages[] = null;
        OAuth2Authenticator sender = new OAuth2Authenticator();
        IMAPStore store = sender.getIMAPStore(account.name, token);
        try {
            Folder inbox = store.getFolder("[Gmail]/All Mail");
            inbox.open(Folder.READ_ONLY);
            messages = searchMailbox(inbox);
            fetchMessages(inbox, messages);
            dialog.setMax(messages.length);
            parseAllMessages(messages);
            inbox.close(true);
            store.close();
        }  catch (MessagingException e) {
            Log.e(MainActivity.TAG, e.toString());
        }
        return messages == null ? 0 : messages.length;
    }

    /**
     *
     * @param inbox
     * @param messages
     */
    private void fetchMessages(Folder inbox, Message[] messages) {
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        try {
            Log.d(MainActivity.TAG, "Fetching messages");
            inbox.fetch(messages, fp);
        } catch (MessagingException e) {
            Log.e(MainActivity.TAG, e.toString());
        }
    }

    /**
     *
     * @param dateStr
     * @return
     */
    private Date getLastParseDate(String dateStr) {
        Date d;
        try {
            d = dateFormat.parse(dateStr);
        } catch (ParseException e) {Calendar c = Calendar.getInstance();
            // Remember the first day of Ingress? Pepperidge Farm remembers.
            c.set(Calendar.MONTH, Calendar.NOVEMBER);
            c.set(Calendar.DAY_OF_MONTH, 15);
            c.set(Calendar.YEAR, 2012);
            d = c.getTime();
        }
        return d;
    }

    /**
     *
     */
    private void initProgressDialog() {
        this.dialog = new ProgressDialog(this.activity);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Parsing email");
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     *
     * @param msgs
     */
    private void parseAllMessages(Message[] msgs) {
        Date parseDate = Calendar.getInstance().getTime();
        for (int i = 0; i < msgs.length; i++) {
            PortalSubmission p = parser.getPortal(msgs[i]);
            addPortal(p);
            publishProgress(i, msgs.length);
            if (isCancelled()) {
                try {
                    parseDate = msgs[i].getReceivedDate();
                } catch (MessagingException e) {
                    Log.e(MainActivity.TAG, e.toString());
                }
                break;
            }
        }
        onEmailParse(parseDate);
    }

    /**
     * Update the mostRecentDate preference after email has been parsed.
     */
    private void onEmailParse(Date parseDate) {
        String dateString = dateFormat.format(parseDate.getTime());
        Log.d(MainActivity.TAG, MainActivity.MOST_RECENT_DATE_KEY + " -> " + dateString);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MainActivity.MOST_RECENT_DATE_KEY, dateString);
        editor.apply();
    }

    @Override
    protected void onPreExecute() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Integer result) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.dismiss();
        Log.d(MainActivity.TAG, "Accepted portals: " + db.getAcceptedCount());
        Log.d(MainActivity.TAG, "Pending portals: " + db.getPendingCount());
        Log.d(MainActivity.TAG, "Rejected portals: " + db.getRejectedCount());
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0] + 1);
        Log.v(MainActivity.TAG, "Parsing: " + dialog.getProgress() + " / " + dialog.getMax());
    }

    /**
     *
     * @param inbox
     * @return
     * @throws MessagingException
     */
    private Message[] searchMailbox(Folder inbox) throws MessagingException {
        SearchTerm portalTerm = new SubjectTerm("ingress portal");
        SearchTerm reviewTerm = new SubjectTerm("portal review");
        SearchTerm subjectTerm = new OrTerm(portalTerm, reviewTerm);
        Date lastParseDate = getLastParseDate(
                preferences.getString(MainActivity.MOST_RECENT_DATE_KEY, MainActivity.NULL_KEY));
        ReceivedDateTerm minDateTerm = new ReceivedDateTerm(ComparisonTerm.GT, lastParseDate);
        SearchTerm invalidTerm = new NotTerm(new SubjectTerm("invalid"));
        SearchTerm editTerm = new NotTerm(new SubjectTerm("edit"));
        SearchTerm editsTerm = new NotTerm(new SubjectTerm("edits"));
        SearchTerm photoTerm = new NotTerm(new SubjectTerm("photo"));
        portalTerm = new AndTerm(new SearchTerm[]
                {subjectTerm, minDateTerm, invalidTerm, editTerm, editsTerm, photoTerm});
        return inbox.search(portalTerm);
    }
}
