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

import android.util.Log;

import com.einzig.ipst2.database.PortalAcceptedBuilder;
import com.einzig.ipst2.database.PortalBuilder;
import com.einzig.ipst2.database.PortalRejectedBuilder;
import com.einzig.ipst2.database.PortalSubmissionBuilder;
import com.einzig.ipst2.portal.PortalSubmission;

import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import static com.einzig.ipst2.activities.MainActivity.TAG;

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
    EmailParser() {
        acceptedBuilder = new PortalAcceptedBuilder();
        rejectedBuilder = new PortalRejectedBuilder();
        submissionBuilder = new PortalSubmissionBuilder();
    }

    /**
     * Get a portal object from an email
     *
     * @param message A Message being parsed.
     * @return PortalSubmission or subclass if the email can be parsed, otherwise null
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
        /*if (!isEmailFromNiantic(message))
            return null;*/
        messageString = getText(message);
        return parse(subject, messageString, receivedDate);
    }

    /**
     * Get portal name from email subject
     *
     * @param subject Email subject line
     * @return portal name from email subjec
     */
    private String getPortalName(String subject) {
        return subject.substring(subject.indexOf(":") + 2);
    }

    /**
     * Get body of the email
     *
     * @param p The body of the message.
     * @return A String representation of the email body.
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
            Log.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * TODO: Speed up parsing
     * Parse a portal submission email
     *
     * @param subject      Email subject line
     * @param message      Email body
     * @param receivedDate Date the email was delivered
     * @return PortalSubmission or subclass if the email can be parsed, otherwise null
     */
    private PortalSubmission parse(String subject, String message, Date receivedDate) {
        Log.d(TAG, "Parsing: " + subject);
        String portalName = getPortalName(subject).trim();
        subject = subject.toLowerCase();
        if (subject.contains("submitted") || subject.contains("submission"))
            return submissionBuilder.build(portalName, receivedDate, message);
        else if (subject.contains("portal live") ||
                subject.contains(" *success!*"))
            return acceptedBuilder.build(portalName, receivedDate, message);
        else if (subject.contains("rejected") || subject.contains("duplicate"))
            return rejectedBuilder.build(portalName, receivedDate, message);
        else
            return parseNewFormat(portalName, message, receivedDate);
    }

    /**
     * Parse the new portal submission email format
     *
     * @param portalName   Name of the portal
     * @param message      Email body
     * @param receivedDate Date the email was delivered
     * @return PortalSubmission or subclass if the email can be parsed, otherwise null
     */
    private PortalSubmission parseNewFormat(String portalName, String message, Date receivedDate) {
        if (message != null) {
            if (message.contains("not to accept") || message.contains("duplicate"))
                return rejectedBuilder.build(portalName, receivedDate, message);
            else if (message.contains("accepted"))
                return acceptedBuilder.build(portalName, receivedDate, message);
        }
        return null;
    }
}
