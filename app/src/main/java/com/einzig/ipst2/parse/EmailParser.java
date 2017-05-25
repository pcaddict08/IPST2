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
     * @throws MessagingException Thrown by email library
     * @throws IOException Thrown by email library
     */
    private String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            return (String) p.getContent();
        } else { // TODO: Bug test this change
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
                String from = address.toString();
                if (from.equalsIgnoreCase("super-ops@google.com") ||
                        from.equalsIgnoreCase("ingress-support@google.com") ||
                        from.equalsIgnoreCase("ingress-support@nianticlabs.com"))
                    return true;
            }
        } catch (MessagingException | NullPointerException e) {
            return false;
        }
        return false;
    }

    /**
     * Returns true if the email is relevant to IPST and needs to be parsed.
     *
     * @param message The email being parsed.
     * @return true if the email is a submission or review and needs to be parsed, otherwise false.
     */
    private boolean isEmailRelevant(Message message, String subject) {
        return isEmailFromNiantic(message) && !subject.toLowerCase().contains("edit") &&
                !subject.toLowerCase().contains("photo");
    }

    /** TODO: Speed up parsing
     * Parse a portal submission or portal review email from Niantic.
     * @param message A Message being parsed.
     */
    PortalSubmission parse(Message message) {
        String messageString, subject;
        Date receivedDate;
        try {
            subject = message.getSubject();
            receivedDate = message.getReceivedDate();
            messageString = getText(message);
        } catch (IOException | MessagingException e) {
            return null;
        }

        subject = trimSubject(subject);

        if (!isEmailRelevant(message, subject))
            return null;
        return parse(subject, messageString, receivedDate);
    }

    private PortalSubmission parse(String subject, String message, Date receivedDate) {
        String portalName = getPortalName(subject).trim();
        // TODO: Fix bug with not parsing subject "review complete"
        if (subject.toLowerCase().contains("submitted")) {
            return submissionBuilder.build(portalName, receivedDate, message);
        } else if (subject.toLowerCase().contains("portal live") ||
                subject.toLowerCase().contains(" *success!*")) {
            return acceptedBuilder.build(portalName, receivedDate, message);
        } else if (subject.toLowerCase().contains("rejected") || subject.toLowerCase().contains("duplicate")) {
            return rejectedBuilder.build(portalName, receivedDate, message);
        } else if (subject.toLowerCase().contains("portal review complete")) {

        }
        return null;
    }

    private String trimSubject(String subject) {
        if(subject.endsWith("."))
            subject = subject.substring(0, subject.length() - 1);
        subject = subject.replaceAll("  " ," ");
        return subject;
    }
}
