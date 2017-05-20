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
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    final private DatabaseInterface db;
    /**
     * Create a new EmailParser
     */
    EmailParser(DatabaseInterface db) {
        this.db = db;
    }

    /**
     * Create a new PortalAccepted and add it to the list of accepted portals.
     *
     * @param name The portal name.
     * @param dateResponded The date the portal was accepted.
     * @param pictureURL The URL to the portal picture.
     * @param message The body of the email as a String for parsing.
     */
    private PortalAccepted buildAccepted(String name, Date dateResponded, String pictureURL,
                                          String message) {
        Date dateSubmitted = dateResponded;
        PortalSubmission submission = findPortal(pictureURL);
        if (submission != null)
            dateSubmitted = submission.getDateSubmitted();
        String address = parseLiveAddress(message);
        String intelLink = parseIntelLink(message);
        return new PortalAccepted(name, dateSubmitted, pictureURL, dateResponded, address, intelLink);
    }

    /**
     * Create a new PortalRejected and add it to the list of rejected portals.
     *
     * @param name The portal name.
     * @param dateResponded The date the portal was rejected.
     * @param pictureURL The URL to the portal picture.
     * @param message The body of the email as a String for parsing.
     */
    private PortalRejected buildRejected(String name, Date dateResponded, String pictureURL, String message) {
        Date dateSubmitted = dateResponded;
        PortalSubmission submission = findPortal(pictureURL);
        if (submission != null)
            dateSubmitted = submission.getDateSubmitted();
        String rejectionReason = parseRejectionReason(message);
        return new PortalRejected(name, dateSubmitted, pictureURL, dateResponded, rejectionReason);
    }


    /**
     * Create a new PortalSubmitted and add it to the list of pending portals.
     *
     * @param name The portal name.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL The URL to the portal picture.
     */
    private PortalSubmission buildSubmission(String name, Date dateSubmitted, String pictureURL) {
        return new PortalSubmission(name, dateSubmitted, pictureURL);
    }

    private PortalSubmission findPortal(String pictureURL) {
        return db.getPendingPortal(pictureURL);
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
        } else if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
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
        Address[] addresses;
        try {
            addresses = message.getFrom();
            if (addresses == null)
                return false;
            for (Address address : addresses) {
                String from = address.toString();
                if (from.contains("super-ops@google.com") ||
                        from.contains("ingress-support@google.com"))
                    return true;
            }
        } catch (MessagingException e) {
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

    /**
     * Parse a portal submission or portal review email from Niantic.
     *
     * @param message A Message being parsed.
     */
    PortalSubmission parse(Message message) {
        String messageString, pictureURL, portalName, subject;
        Date receivedDate;

        try {
            subject = message.getSubject();
            receivedDate = message.getReceivedDate();
            messageString = getText(message);
        } catch (IOException | MessagingException e) {
            return null;
        }

        if(subject.endsWith("."))
            subject = subject.substring(0, subject.length() - 1);
        subject = subject.replaceAll("  " ," ");

        if (!isEmailRelevant(message, subject))
            return null;

        pictureURL = parsePictureURL(messageString);
        portalName = subject.substring(subject.indexOf(":") + 2);
        portalName = portalName.trim();

        if(subject.toLowerCase().contains("submitted")) {
            return buildSubmission(portalName, receivedDate, pictureURL);
        } else if(subject.toLowerCase().contains("portal live") ||
                subject.toLowerCase().contains(" *success!*")) {
            return buildAccepted(portalName, receivedDate, pictureURL, messageString);
        } else if(subject.toLowerCase().contains("rejected") || subject.toLowerCase().contains("duplicate")) {
            return buildRejected(portalName, receivedDate, pictureURL, messageString);
        }
        return null;
    }

    /**
     * Parse the intel link for an accepted portal from the email.
     *
     * @param messageString The body of the email as a String for parsing.
     * @return Ingress Intel map link for the portal.
     */
    private String parseIntelLink(String messageString) {
        String intelLinkURL;
        Pattern p = Pattern.compile("href=\"(.*?)\"");
        Matcher m = p.matcher(messageString);
        if (m.find()) {
            intelLinkURL = m.group(1);
        }
        else
            intelLinkURL = "N/A";
        return intelLinkURL;
    }

    /**
     * Parse the address of an accepted portal from the email.
     *
     * @param messageString The body of the email as a String for parsing.
     * @return address of the portal.
     */
    private String parseLiveAddress(String messageString) {
        String liveAddress;
        Pattern titleFinder = Pattern.compile("<a[^>]*>(.*?)</a>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(messageString);
        if (regexMatcher.find()) {
            liveAddress = regexMatcher.group(1);
        }
        else
            liveAddress = "N/A";
        return liveAddress;
    }

    /**
     * Parse the URL of the portal picture from the email.
     * @param messageString The body of the email as a String for parsing.
     * @return the URL of the portal picture.
     */
    private String parsePictureURL(String messageString) {
        String pictureURL;
        Pattern imgfinder = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher2 = imgfinder.matcher(messageString);
        if (regexMatcher2.find())
            pictureURL = regexMatcher2.group(1);
        else
            pictureURL = "No Picture Found";
        return pictureURL;
    }

    /**
     * Parse the reason a portal was rejected.
     * @param messageString The body of the email as a String for parsing.
     * @return the reason the portal was rejected.
     */
    private String parseRejectionReason(String messageString) {
        String rejectionReason = "N/A";
        if (messageString.contains("does not meet the criteria"))
            rejectionReason = "Does not meet portal criteria";
        if (messageString.contains("duplicate"))
            rejectionReason = "Duplicate of another portal";
        if (messageString.contains("too close")) {
            if(rejectionReason.equalsIgnoreCase("N/A"))
                rejectionReason = "Too Close to another portal";
            else
                rejectionReason = rejectionReason + " or too close to another portal";
        }
        return rejectionReason;
    }
}
