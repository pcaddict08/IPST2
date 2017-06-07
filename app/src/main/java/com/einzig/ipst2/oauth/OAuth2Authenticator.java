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

package com.einzig.ipst2.oauth;


import android.util.Log;

import com.einzig.ipst2.activities.MainActivity;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.URLName;


/**
 * Performs OAuth2 authentication.
 *
 * <p>Before using this class, you must call {@code initialize} to install the
 * OAuth2 SASL provider.
 */
public class OAuth2Authenticator {
	/**
	 * Installs the OAuth2 SASL provider. This must be called exactly once before
	 * calling other methods on this class.
	 */
	private void initialize() {
		Security.addProvider(new OAuth2Provider());
	}

	/**
	 * Connects and authenticates to an IMAP server with OAuth2. You must have
	 * called {@code initialize}.
	 *
	 * @param host Hostname of the imap server, for example {@code
	 *     imap.googlemail.com}.
	 * @param port Port of the imap server, for example 993.
	 * @param userEmail Email address of the user to authenticate, for example
	 *     {@code oauth@gmail.com}.
	 * @param oauthToken The user's OAuth token.
	 * @param debug Whether to enable debug logging on the IMAP connection.
	 *
	 * @return An authenticated IMAPStore that can be used for IMAP operations.
	 */
	private static IMAPStore connectToImap(String host,
			int port,
			String userEmail,
			String oauthToken,
			boolean debug) throws Exception {
		Properties props = new Properties();
		props.put("mail.imaps.sasl.enable", "true");
		props.put("mail.imaps.sasl.mechanisms", "XOAUTH2");
		props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
		Session session = Session.getInstance(props);
		session.setDebug(debug);

		final URLName unusedUrlName = null;
		IMAPSSLStore store = new IMAPSSLStore(session, unusedUrlName);
		final String emptyPassword = "";
		store.connect(host, port, userEmail, emptyPassword);
		return store;
	}

	public synchronized IMAPStore getIMAPStore(String user, String oauthToken) {
		try {
			initialize();
			return connectToImap("imap.gmail.com", 993, user, oauthToken, false);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(MainActivity.TAG, e.toString());
		}
		return null;
	}

	private static final class OAuth2Provider extends Provider {
		private static final long serialVersionUID = 1L;

		OAuth2Provider() {
			super("Google OAuth2 Provider", 1.0,
					"Provides the XOAUTH2 SASL Mechanism");
			put("SaslClientFactory.XOAUTH2", OAuth2SaslClientFactory.class.getCanonicalName());
		}
	}
}
