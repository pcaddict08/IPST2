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

import android.util.Log;

import com.einzig.ipst2.activities.MainActivity;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.portal.PortalSubmission;

import java.io.IOException;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

/**
 * Parses portal submission and review emails
 *
 * @author Ryan Porterfield
 * @since 2017-05-17
 */
class EmailParser {
    final private PortalBuilder acceptedBuilder;
    final private PortalBuilder rejectedBuilder;
    final private PortalBuilder submissionBuilder;

    /**
     * Create a new EmailParser
     */
    EmailParser(DatabaseInterface db) {
        acceptedBuilder = new PortalAcceptedBuilder(db);
        rejectedBuilder = new PortalRejectedBuilder(db);
        submissionBuilder = new PortalSubmissionBuilder(db);
    }

    private String getPortalName(String subject) {
        return subject.substring(subject.indexOf(":") + 2);
    }

    /**
     * Does a thing maybe.
     *
     * @param p The body of the message.
     * @return A String representation of the message body.
     */
    private String getText(Part p) {
        try {
            if (p.isMimeType("text/*")) {
                return (String) p.getContent();
            } else {
                Multipart mp = (Multipart) p.getContent();
                String text = null;

                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    // prefer html text over plain text
                    if (bp.isMimeType("text/html")) {
                        text = getText(bp);
                        if (text != null)
                            return text;
                    } else {
                        text = getText(bp);
                    }
                }
                return text;
            }
        } catch (MessagingException | IOException e) {
            Log.e(MainActivity.TAG, e.toString());
        }
        return null;
    }

    /**
     * Returns true if the email is from Niantic.
     *
     * @param message The email being parsed.
     * @return true if the email is from Niantic, otherwise false.
     */
    private boolean isEmailFromNiantic(Message message) {
        try {
            Address[] addresses = message.getFrom();
            for (Address address : addresses) {
                String from = address.toString().toLowerCase();
                if (from.endsWith("<super-ops@google.com>") ||
                        from.endsWith("<ingress-support@google.com>") ||
                        from.endsWith("<ingress-support@nianticlabs.com>"))
                    return true;
            }
        } catch (MessagingException | NullPointerException e) {
            return false;
        }
        return false;
    }

    /**
     * Get a portal object from an email
     * @param message A Message being parsed.
     * @return
     */
    PortalSubmission getPortal(Message message) {
        String messageString, subject;
        Date receivedDate;
        try {
            subject = message.getSubject();
            receivedDate = message.getReceivedDate();
        } catch (MessagingException e) {
            return null;
        }
        if (!isEmailFromNiantic(message))
            return null;
        messageString = getText(message);
        return parse(subject, messageString, receivedDate);
    }

    /** TODO: Speed up parsing
     *
     * @param subject
     * @param message
     * @param receivedDate
     * @return
     */
    private PortalSubmission parse(String subject, String message, Date receivedDate) {
        Log.d(MainActivity.TAG, "Parsing: " + subject);
        String portalName = getPortalName(subject).trim();
        subject = subject.toLowerCase();
        if (subject.contains("submitted"))
            return submissionBuilder.build(portalName, receivedDate, message);
        else if (subject.contains("portal live") ||
                subject.contains(" *success!*"))
            return acceptedBuilder.build(portalName, receivedDate, message);
        else if (subject.contains("rejected") || subject.contains("duplicate"))
            return rejectedBuilder.build(portalName, receivedDate, message);
        else
            return parseNewFormat(portalName, message, receivedDate, subject);
    }

    private PortalSubmission parseNewFormat(String portalName, String message, Date receivedDate, String subject) {
        if (message.contains("not to accept") || message.contains("duplicate"))
            return rejectedBuilder.build(portalName, receivedDate, message);
        else if (message.contains("accepted"))
            return acceptedBuilder.build(portalName, receivedDate, message);
        Log.e(MainActivity.TAG, "Couldn't getPortal message:\n" + subject);
        return null;
    }
}
