/* ********************************************************************************************** *
 * ********************************************************************************************** *
 *                                                                                                *
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                                      *
 *                                                                                                *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software  *
 * and associated documentation files (the "Software"), to deal in the Software without           *
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,     *
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the  *
 * Software is furnished to do so, subject to the following conditions:                           *
 *                                                                                                *
 * The above copyright notice and this permission notice shall be included in all copies or       *
 * substantial portions of the Software.                                                          *
 *                                                                                                *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING  *
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND     *
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,   *
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.        *
 *                                                                                                *
 * ********************************************************************************************** *
 * ********************************************************************************************** */

package com.einzig.ipst2.Utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import com.einzig.ipst2.Activities.MainActivity;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.sun.mail.imap.IMAPStore;

import java.util.ArrayList;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
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
    /**
     * The URL to get the OAuth token from
     */
    private static final String AUTH_URL = "oauth2:https://mail.google.com/";
    /**
     * Tag used for logging.
     */
    private static final String TAG = "IPST:EmailParseTask";

    private final List<PortalAccepted> acceptedPortals;
    private final List<PortalSubmission> pendingPortals;
    private final List<PortalRejected> rejectedPortals;

    /**
     * The user's email account that portal submission emails go to
     */
    private Account account;
    /**
     * Does the actual parsing of emails
     */
    private EmailParser parser;
    /**
     * The calling activity. Used to update UI elements
     */
    private MainActivity activity;
    /**
     * Display parsing progress
     */
    private ProgressDialog dialog;

    /**
     * Initialize a new EmailParseActivity to asynchronously parse new portal submission emails.
     *
     * @param activity The calling activity.
     * @param account The email account to parse from.
     */
    public EmailParseTask(MainActivity activity, Account account) {
        this.acceptedPortals = new ArrayList<>();
        this.account = account;
        this.activity = activity;
        this.pendingPortals = new ArrayList<>();
        this.rejectedPortals = new ArrayList<>();
        this.parser = new EmailParser(acceptedPortals, pendingPortals, rejectedPortals);
        addMailcaps();
        initProgressDialog();
        System.getProperties().setProperty("mail.store.protocol", "imaps");
    }

    /**
     * Is this necessary?
     */
    private void addMailcaps() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
    }

    /**
     * Authenticate with GMail.
     *
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
        Log.d(TAG, "Parsing email");
        String token = authenticate();
        Message messages[] = null;
        OAuth2Authenticator sender = new OAuth2Authenticator();
        IMAPStore store = sender.testImap(account.name, token);
        try {
            Folder inbox = store.getFolder("[Gmail]/All Mail");
            inbox.open(Folder.READ_ONLY);
            messages = searchMailbox(inbox);
            fetchMessages(inbox, messages);
            dialog.setMax(messages.length);
            printAllMessages(messages);
            inbox.close(true);
            store.close();
        }  catch (MessagingException e) {
            Log.e(TAG, e.toString());
        }
        return messages == null ? 0 : messages.length;
    }

    private void fetchMessages(Folder inbox, Message[] messages) {
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        try {
            Log.d(TAG, "Fetching messages");
            inbox.fetch(messages, fp);
        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void initProgressDialog() {
        this.dialog = new ProgressDialog(this.activity);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Parsing email");
    }

    private void printAllMessages(Message[] msgs) {
        for (int i = 0; i < msgs.length; i++) {
            parser.parseEmail(msgs[i]);
            publishProgress(i, msgs.length);
            if (isCancelled())
                break;
        }
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected void onPostExecute(Integer result) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.dismiss();
        // TODO Add These functions
        activity.onEmailParse();
        new UpdateDatabaseTask(activity, acceptedPortals, pendingPortals, rejectedPortals, true).execute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0] + 1);
        Log.d(TAG, "Parsing" + dialog.getProgress() + " / " + dialog.getMax());
    }

    private Message[] searchMailbox(Folder inbox) {
        SearchTerm searchTerm = new SubjectTerm("Ingress Portal");
        ReceivedDateTerm minDateTerm = new ReceivedDateTerm(ComparisonTerm.GT,
                activity.getMostRecentDate());
        searchTerm = new AndTerm(searchTerm, minDateTerm);
        try {
            return inbox.search(searchTerm);
        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
