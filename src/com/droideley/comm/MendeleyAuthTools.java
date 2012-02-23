package com.droideley.comm;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MendeleyAuthTools {

	public static final String CONSUMER_KEY = "af19d5f600c2192d3a41556785cc71a804d36cddb";
	public static final String CONSUMER_SECRET = "e49565ce855163aaae575c55f16f37d9";
	public static String ACCESS_KEY = null;
	public static String ACCESS_SECRET = null;
	public static final String REQUEST_URL = "http://www.mendeley.com/oauth/request_token/";
	public static final String ACCEST_TOKEN_URL = "http://www.mendeley.com/oauth/access_token/";
	public static final String AUTH_URL = "http://www.mendeley.com/oauth/authorize/";
	public static final String CALLBACK_URL = "droideley://mend";

	private static DefaultOAuthConsumer mendeleyConsumer = new DefaultOAuthConsumer(
			CONSUMER_KEY, CONSUMER_SECRET);
	// private static CommonsHttpOAuthConsumer mendeleyConsumer = new
	// CommonsHttpOAuthConsumer(CONSUMER_KEY,
	// CONSUMER_SECRET);
	private static DefaultOAuthProvider mendeleyProvider = new DefaultOAuthProvider(
			REQUEST_URL, ACCEST_TOKEN_URL, AUTH_URL);

	// private static CommonsHttpOAuthProvider mendeleyProvider = new
	// CommonsHttpOAuthProvider(REQUEST_URL,
	// ACCEST_TOKEN_URL, AUTH_URL);

	/**
	 * 
	 * @return
	 * @throws OAuthCommunicationException
	 * @throws OAuthExpectationFailedException
	 * @throws OAuthNotAuthorizedException
	 * @throws OAuthMessageSignerException
	 */
	public static String userAuthentication()
			throws OAuthMessageSignerException, OAuthNotAuthorizedException,
			OAuthExpectationFailedException, OAuthCommunicationException {

		Log.d("Droideley", "userAuthentication() =========");
		mendeleyProvider.setOAuth10a(true);
		Log.d("Droideley", "setOAuth10a");
		String authURL = mendeleyProvider.retrieveRequestToken(
				mendeleyConsumer, CALLBACK_URL);
		Log.d("Droideley", authURL);
		return authURL;
	}

	public static void updateConsumerKeys() {
		mendeleyConsumer.setTokenWithSecret(ACCESS_KEY, ACCESS_SECRET);
	}

	/**
	 * 
	 * @param uri
	 */
	public static void callbackHandler(Uri uri, SQLiteDatabase db) {
		Log.d("Droideley", "MendeleyAuthTools.callbackHandler() called...");
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
			try {

				Log.d("Droideley", uri.toString());
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				Log.d("Droideley", verifier); // TODO check verifier string on
												// null
				mendeleyProvider.setOAuth10a(true);
				mendeleyProvider
						.retrieveAccessToken(mendeleyConsumer, verifier);
				ACCESS_KEY = mendeleyConsumer.getToken();
				ACCESS_SECRET = mendeleyConsumer.getTokenSecret();
				db.execSQL("UPDATE keys SET value='" + ACCESS_KEY
						+ "' WHERE type='ACCESS_KEY'");
				db.execSQL("UPDATE keys SET value='" + ACCESS_SECRET
						+ "' WHERE type='ACCESS_SECRET'");
				Log.d("MendeleyDroid", ACCESS_KEY);
				Log.d("MendeleyDroid", ACCESS_SECRET);
			} catch (OAuthMessageSignerException ex) {
				Log.e("MendeleyAuthTools", "OAuthMessageSignerException", ex);
			} catch (OAuthNotAuthorizedException ex) {
				Log.e("MendeleyAuthTools", "OAuthNotAuthorizedException", ex);
			} catch (OAuthExpectationFailedException ex) {
				Log.e("MendeleyAuthTools", "OAuthExpectationFailedException",
						ex);
			} catch (OAuthCommunicationException ex) {
				Log.e("MendeleyAuthTools", "OAuthCommunicationException", ex);
			}
		} else {
			Log.e("MendeleyAuthTools",
					"CallbackHandler: Uri is null or does not start with CALLBACK_URL");
			// TODO Show BugReport dialog
		}
	}

	/**
	 * 
	 * @param docId
	 * @param hash
	 * @param fileName
	 * @param fileSize
	 * @param handler
	 * @throws IOException
	 * @throws HttpException
	 * @throws OAuthCommunicationException
	 * @throws OAuthExpectationFailedException
	 * @throws OAuthMessageSignerException
	 * @throws Exception
	 */
	public static void downloadFile(String docId, String hash,
			String extension, FileOutputStream fos, Handler handler) 
					throws HttpException, IOException, 
					OAuthMessageSignerException, 
					OAuthExpectationFailedException, 
					OAuthCommunicationException {
		Log.d("Downloading file...", "downLoadFile(...) entered");
		// File file = new File(fileName);

		// Check whether I can write to the external storage
		// Log.d("Writing to the file...", "");
		/*
		 * if (!file.canWrite()) { Message msgFinished = Message.obtain();
		 * msgFinished.arg1 = -1; handler.sendMessage(msgFinished); return; }
		 */
		String urlString = "http://www.mendeley.com/oapi/library/documents/"
				+ docId + "/file/" + hash + "/";
		Log.d("Downloading file...", urlString);

		URL url = new URL(urlString);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET);
		consumer.setTokenWithSecret(mendeleyConsumer.getToken(),
				mendeleyConsumer.getTokenSecret());
		Log.d("Droideley",
				"MendeleyAuthTools.processRequest(): signing request...");
		consumer.sign(request);
		Log.d("Droideley", "MendeleyAuthTools.processRequest(): connecting...");

		request.connect();

		if (request.getResponseCode() == 200) {
			Log.d("Downloading file...", "Opening file for writing...");
			int totalSize = request.getContentLength();
			int downloadedSize = 0;
			int buffLength = 0;
			BufferedInputStream bis = new BufferedInputStream(
					request.getInputStream());
			// FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			Log.d("Downloading file...", "Total size: " + totalSize);
			while ((buffLength = bis.read(buffer)) > 0) {
				fos.write(buffer, 0, buffLength);
				downloadedSize += buffLength;
				// Log.d("Downloading file...", "Downloaded: " +
				// downloadedSize);
				if (handler != null) {
					Message msg = Message.obtain();
					msg.arg1 = (int) (((float) downloadedSize / (float) totalSize) * 98);
					// Log.d("Downloading file...",
					// "Sending msg to handler: " +
					// msg.arg1);
					handler.sendMessage(msg);
				}
			}
			fos.flush();
			fos.close();
			bis.close();
			// Log.d("Downloading file...", "Expected size: " + fileSize);
			Log.d("Downloading file...", "Downloaded: " + downloadedSize
					+ " from stream of size " + totalSize);
			if (handler != null) {
				Message msgFinished = Message.obtain();
				msgFinished.arg1 = 101;
				Bundle b = new Bundle();
				b.putString("DownloadedFile", hash + "." + extension);
				b.putString("FileType", extension);
				msgFinished.setData(b);
				handler.sendMessage(msgFinished);
			}
		} else {
			throw new HttpException("Mendeley Server returned "
					+ request.getResponseCode() + ": "
					+ request.getResponseMessage());
		}
	}

	/**
	 * 
	 * @param urlString
	 * @param userSpecific
	 * @return
	 * @throws Exception
	 */
	public static JSONObject processRequest(String urlString,
			boolean userSpecific) throws Exception {
		Log.d("Droideley", "MendeleyAuthTools.processRequest() called...");
		if (!userSpecific) {
			urlString += "?consumer_key=" + CONSUMER_KEY;
		}

		URL url = new URL(urlString);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();

		if (userSpecific) {
			OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY,
					CONSUMER_SECRET);
			consumer.setTokenWithSecret(mendeleyConsumer.getToken(),
					mendeleyConsumer.getTokenSecret());
			Log.d("Droideley",
					"MendeleyAuthTools.processRequest(): signing request...");
			consumer.sign(request);
			Log.d("Droideley",
					"MendeleyAuthTools.processRequest(): connecting...");
		}
		System.out.println(request.toString());
		request.connect();

		if (request.getResponseCode() == 200) {
			String strResponse = "";
			InputStreamReader in = new InputStreamReader(
					(InputStream) request.getContent());
			BufferedReader reader = new BufferedReader(in);

			String line;
			while ((line = reader.readLine()) != null) {
				strResponse += line + "\n";
			}
			reader.close();
			in.close();
			return new JSONObject(strResponse);
		} else {
			throw new Exception("Mendeley Server returned "
					+ request.getResponseCode() + ": "
					+ request.getResponseMessage());
		}
	}

	/**
	 * Returns the result of the given request as a @see(JSONArray)
	 * 
	 * @param urlString
	 * @return
	 * @throws Exception
	 */
	public static JSONArray processRequestToArray(String urlString)
			throws Exception {
		Log.d("Droideley", "MendeleyAuthTools.processRequest() called...");
		URL url = new URL(urlString);

		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		Log.d("Droideley",
				"MendeleyAuthTools.processRequest(): signing request...");
		mendeleyConsumer.sign(request);
		Log.d("Droideley", "MendeleyAuthTools.processRequest(): connecting...");
		request.connect();

		if (request.getResponseCode() == 200) {
			String strResponse = "";
			InputStreamReader in = new InputStreamReader(
					(InputStream) request.getContent());
			BufferedReader reader = new BufferedReader(in);

			String line;
			while ((line = reader.readLine()) != null) {
				strResponse += line + "\n";
			}
			reader.close();
			in.close();
			return new JSONArray(strResponse);
		} else {
			throw new Exception("Mendeley Server returned "
					+ request.getResponseCode() + ": "
					+ request.getResponseMessage());
		}
	}
}
