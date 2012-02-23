package com.droideley;

import java.io.File;
import java.io.IOException;

import com.droideley.comm.Communicator;
import com.droideley.comm.MendeleyAuthTools;
import com.droideley.comm.SQLiteHelper;
import com.droideley.tools.DroideleyTools;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The very first activity that handles the initialization of the app It should
 * be started off the activity stack.
 * 
 * @author Petr (severin) Volny
 */
public class MainActivity extends Activity {

	private final int NETWORK_UNREACHABLE = 0;
	private final int FATAL_DIALOG = 1;
	private final int LOGIN_DIALOG = 2;

	private String oauthException = null;

	private boolean loggedIn = false;
	// private Handler handler;
	private Communicator comm;
	private SQLiteDatabase db;

	// private int progress = 0;
	// private ProgressBar mainProgress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// comm = new Communicator(this);
		Log.d("Droideley", "MainActivity.onCreate() called...");
		setContentView(R.layout.main);
		// mainProgress = (ProgressBar) findViewById(R.id.mainProgressbar);
	}

	@Override
	public void onResume() {
		Log.d("Droideley", "DashboardActivity.onResume() called...");
		super.onResume();
		/**
		 * File droideleyDir = new
		 * File(Environment.getExternalStorageDirectory() .getAbsolutePath() +
		 * "/Droideley"); if (!droideleyDir.exists()) { droideleyDir.mkdirs(); }
		 */

		db = new SQLiteHelper(this).getWritableDatabase();
/*
		final Handler finishHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				db.close();
				switch (msg.what) {
				case 1:
					showDialog(NETWORK_UNREACHABLE);
					break;
				case 10:
					oauthException = "OAuthMessageSignerException";
					showDialog(FATAL_DIALOG);
					break;
				case 11:
					oauthException = "OAuthNotAuthorizedException";
					showDialog(FATAL_DIALOG);
					break;
				case 12:
					oauthException = "OAuthExpectationFailedException";
					showDialog(FATAL_DIALOG);
					break;
				case 13:
					oauthException = "OAuthCommunicationException";
					showDialog(FATAL_DIALOG);
					break;
				default:
					new Communicator(MainActivity.this).getProfile();
					startActivity(new Intent(getBaseContext(), DashboardActivity.class));
					db.close();
					finish();
					break;
				}
			}
		};
*/		

		// This thread handles the login process

		// Fetch mendeley credentials for the local db
		Cursor keys = db.rawQuery("SELECT * FROM keys ORDER BY type", null);
		System.out.println("Fetching keys from DB...");
		keys.moveToFirst();
		if (keys.getCount() == 2) {
			MendeleyAuthTools.ACCESS_KEY = keys.getString(1);
			keys.moveToNext();
			MendeleyAuthTools.ACCESS_SECRET = keys.getString(1);
		}
		keys.close();


		System.out.println("ACCESS_KEY: " + MendeleyAuthTools.ACCESS_KEY);
		System.out.println("ACCESS_SECRET: " + MendeleyAuthTools.ACCESS_SECRET);
		// If they are none saved credentials
		if (MendeleyAuthTools.ACCESS_KEY.equals("none")
				|| MendeleyAuthTools.ACCESS_SECRET.equals("none")) {
			loggedIn = false;
			Uri uri = getIntent().getData();
			Log.d("Droideley", "intent.getData()");
			// Do we have credentials already returned in uri?
			if (uri != null) {
				// Login was successful
				MendeleyAuthTools.callbackHandler(uri, db);
				loggedIn = true;
				//finishHandler.sendEmptyMessage(0);
				new Communicator(MainActivity.this).getProfile();
				startActivity(new Intent(getBaseContext(), DashboardActivity.class));				
				finish();
			} else {
				// If not, we need to log in through mendeley oauth provider
				if (DroideleyTools.checkConnection(getBaseContext())) {					
					showDialog(LOGIN_DIALOG);
					//===============
					/*
					Intent browserOAuth;
					try {
						browserOAuth = new Intent(Intent.ACTION_VIEW, Uri.parse(MendeleyAuthTools
								.userAuthentication()));
						browserOAuth.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						browserOAuth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(browserOAuth);
					} catch (OAuthMessageSignerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthNotAuthorizedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // check on null
					*/
					//================

				} else {
					// No connection
					//finishHandler.sendEmptyMessage(1);
					showDialog(NETWORK_UNREACHABLE);
				}
			}
		} else {
			// Already logged in
			MendeleyAuthTools.updateConsumerKeys();
			loggedIn = true;
			//finishHandler.sendEmptyMessage(0);
			db.close();
			new Communicator(MainActivity.this).getProfile();
			startActivity(new Intent(getBaseContext(), DashboardActivity.class));			
			finish();
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case NETWORK_UNREACHABLE:
			dialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("Connection problems")
					.setMessage(
							"Droideley requires internet connection to be able to login to Mendeley. Please check your connection settings and start Droideley again when connected.")
					.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create();
			break;
		case FATAL_DIALOG:
			dialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle(oauthException)
					.setMessage(
							"Oops! Something went wrong. This could be a problem of the Mendeley OAPI. Please try again later or report a bug on droideley@gmail.com.")
					.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create();
			break;
		case LOGIN_DIALOG:
			dialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle(oauthException)
					.setTitle("Login")
					.setMessage(
							"Droideley needs granted acces to your Mendeley resources. "
									+ "Now, you will be redirected to the Mendeley login page.")
					.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
							try {
								Intent browserOAuth;
								browserOAuth = new Intent(Intent.ACTION_VIEW, Uri.parse(MendeleyAuthTools
										.userAuthentication())); // check on null
								browserOAuth.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
								browserOAuth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(browserOAuth);
							} catch (OAuthMessageSignerException e) {
								e.printStackTrace();
								oauthException = "OAuthMessageSignerException";
								showDialog(FATAL_DIALOG);								
								//finishHandler.sendEmptyMessage(10);
							} catch (OAuthNotAuthorizedException e) {
								e.printStackTrace();
								oauthException = "OAuthNotAuthorizedException";
								showDialog(FATAL_DIALOG);
								//finishHandler.sendEmptyMessage(11);
							} catch (OAuthExpectationFailedException e) {
								e.printStackTrace();
								oauthException = "OAuthExpectationFailedException";
								showDialog(FATAL_DIALOG);
								//finishHandler.sendEmptyMessage(12);
							} catch (OAuthCommunicationException e) {
								e.printStackTrace();
								oauthException = "OAuthCommunicationException";
								showDialog(FATAL_DIALOG);
								//finishHandler.sendEmptyMessage(13);
							} finally {
								db.close();
							}
						}
					}).create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}