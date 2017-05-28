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

import com.einzig.ipst2.activities.MainActivity;
import com.sun.mail.imap.IMAPStore;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * @author Ryan Porterfield
 * @since 2017-05-28
 */

public class MailBundle {
    private final Folder folder;
    private final Message[] messages;
    private final IMAPStore store;

    public MailBundle(Folder folder, Message[] messages, IMAPStore store) {
        this.folder = folder;
        this.messages = messages;
        this.store = store;
    }

    void cleanup() {
        try {
            folder.close(true);
            store.close();
        } catch (MessagingException e) {
            Log.e(MainActivity.TAG, e.toString());
        }
    }

    Message[] getMessages() {
        return messages;
    }
}
