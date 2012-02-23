package com.droideley.comm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.util.Log;

public class PlosTools {

	private static final String CONSUMER_KEY = "cFDs1Y5OCA3Rtwx";

	public static JSONObject processRequest(String urlString) throws Exception {
		Log.d("Droideley", "PlosTools.processRequest() called...");

		urlString += "?consumer_key=" + CONSUMER_KEY;

		URL url = new URL(urlString);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();

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

			in.close();
			return new JSONObject(strResponse);
		} else {
			throw new Exception("Plos Server returned "
					+ request.getResponseCode() + ": "
					+ request.getResponseMessage());
		}
	}
}
