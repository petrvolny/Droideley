package com.droideley;

import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.droideley.comm.Communicator;
import com.droideley.comm.SQLiteHelper;
import com.droideley.components.ActionBarWidget;
import com.droideley.tools.DroideleyTools;
import com.droideley.tools.FileManager;

public class DashboardActivity extends Activity {

	private final int SYNC_FAILED_DIALOG = 0;
	private final int FIRST_START_DIALOG = 1;
	private final int SYNC_PROGRESS_DIALOG = 2;
	private final int DOWNLOAD_PROGRESS_DIALOG = 3;
	private final int DOWNLOAD_FAILED_DIALOG = 4;

	private final int SYNC_ALL = 0;
	private final int SYNC_NEW = 1;

	private Communicator comm;
	// private SQLiteDatabase db;
	// private boolean loggedIn = false;
	private boolean showWelcome = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		comm = new Communicator(this);
		setContentView(R.layout.dashboard);
		ActionBarWidget actionBar = (ActionBarWidget) findViewById(R.id.dashboard_bar);
		actionBar.setTitle("Dashboard");
		actionBar.setBtnMendeleyDisabled(true);
		// actionBar.showSearchButton(true);

		Button btnAllDocs = (Button) findViewById(R.id.dash_btn_alldocs);
		btnAllDocs.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						DocumentListActivity.class);
				Bundle b = new Bundle();
				b.putInt("FILTER_TYPE", DocumentListActivity.FILTER_NONE);
				b.putString("BAR_TITLE", "All documents");
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		Button btnFolders = (Button) findViewById(R.id.dash_btn_folders);
		btnFolders.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						FoldersActivity.class);
				startActivity(intent);
			}
		});

		Button btnAuthors = (Button) findViewById(R.id.dash_btn_authors);
		btnAuthors.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						AuthorsActivity.class);
				startActivity(intent);
			}
		});

		Button btnTags = (Button) findViewById(R.id.dash_btn_tags);
		btnTags.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), TagsActivity.class);
				startActivity(intent);
			}
		});

		Button btnSearchHub = (Button) findViewById(R.id.dash_btn_searchhub);
		btnSearchHub.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						SearchHubActivity.class);
				startActivity(intent);
			}
		});

		Button btnGroups = (Button) findViewById(R.id.dash_btn_groups);
		btnGroups.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						GroupsActivity.class);
				startActivity(intent);
			}
		});

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (!pref.contains("DroideleyFirst")) {
			showWelcome = true;
			SharedPreferences.Editor edit = pref.edit();
			edit.putBoolean("DroideleyFirst", true);
			edit.commit();
		}

		/*
		 * ImageButton btnLogin = (ImageButton) findViewById(R.id.loginButton);
		 * btnLogin.setOnClickListener(new View.OnClickListener() {
		 * 
		 * public void onClick(View v) {
		 * 
		 * } });
		 */
		// ========================================================
		// ========================================================
	}

	@Override
	public void onResume() {
		super.onResume();
		if (showWelcome) {
			showWelcome = false;
			showDialog(FIRST_START_DIALOG);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dashboard_menu, menu);
		return true;
	}

	public boolean checkConnection() {
		final ConnectivityManager connMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable() || mobile.isAvailable()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Handles menu items selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				DashboardActivity.this);
		final FileManager fileManager = new FileManager(this);
		switch (item.getItemId()) {
		case R.id.sync:
			// DroideleyTools.lockScreenOrientation(this, true);
			// Raise dialog to let user choose what he wants to synchronize
			if (DroideleyTools.checkConnection(this)) {
				builder.setTitle("What to synchronize?");
				String[] downloadingOptions = { "Sync all docs",
						"Sync new only", "Download all attached pdfs" };
				builder.setItems(downloadingOptions,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									final int item) {
								switch (item) {
								case 0:
									synchronize(SYNC_ALL);
									break;
								case 1:
									synchronize(SYNC_NEW);
									break;
								case 2:
									if (fileManager.checkExternalStorageState()) {
										builder.setTitle("Download to");
										String[] downloadingOptions = {
												"Local storage",
												"External storage (recommended)" };
										builder.setItems(
												downloadingOptions,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															final int item) {
														if (item == 0) {
															downloadPdfs(
																	SYNC_ALL,
																	FileManager.LOCAL_STORAGE);
														} else {
															downloadPdfs(
																	SYNC_ALL,
																	FileManager.EXTERNAL_STORAGE);
														}
													}
												});
										// Show the dialog
										AlertDialog alert = builder.create();
										alert.show();
									} else {
										downloadPdfs(SYNC_ALL,
												FileManager.LOCAL_STORAGE);
									}
									// downloadPdfs(SYNC_ALL,
									// FileManager.EXTERNAL_STORAGE);
									break;
								}
							}
						});

				// Show the dialog
				AlertDialog alert = builder.create();
				alert.show();
				// syncAll();

			} else {
				Toast.makeText(getBaseContext(),
						"Device is not connected to the Internet...",
						Toast.LENGTH_LONG).show();
			}
			break;
		/*
		 * case R.id.settings: startActivity(new Intent(getBaseContext(),
		 * SettingsActivity.class)); break;
		 */
		/*
		 * case R.id.settings: startActivity(new Intent(getBaseContext(),
		 * SettingsActivity.class)); break;
		 */
		}
		return true;
	}

	/**
	 * Helper method for starting a thread for synchronization of the local DB
	 */
	private void synchronize(final int syncType) {
		showDialog(SYNC_PROGRESS_DIALOG);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					dismissDialog(SYNC_PROGRESS_DIALOG);
					break;
				case 1:
					showDialog(SYNC_FAILED_DIALOG);
					break;
				}
			}
		};

		new Thread(new Runnable() {

			public void run() {
				try {
					if (syncType == SYNC_ALL) {
						comm.syncAll();
					} else if (syncType == SYNC_NEW) {
						comm.syncNew();
					}
					handler.sendEmptyMessage(0);

				} catch (Exception e) {
					handler.sendEmptyMessage(1);
					// showDialog(SYNC_FAILED);
					Log.e("Syncing", "Syncing failed...", e);
				}
				// DroideleyTools.pairDocsWithPdfs(getBaseContext());

			}
		}).start();
	}

	/**
	 * Helper method for starting a thread for downloading pdf files from
	 * Mendeley server
	 * 
	 * @param syncType
	 */
	private void downloadPdfs(final int syncType, final int whereToStore) {
		showDialog(DOWNLOAD_PROGRESS_DIALOG);
		final Thread t;
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					dismissDialog(DOWNLOAD_PROGRESS_DIALOG);
					break;
				case 1:
					showDialog(SYNC_FAILED_DIALOG);
					break;
				case -1:
					dismissDialog(DOWNLOAD_PROGRESS_DIALOG);
					showDialog(DOWNLOAD_FAILED_DIALOG);
					break;
				}
			}
		};

		t = new Thread(new Runnable() {

			public void run() {
				FileManager fileManager = new FileManager(
						DashboardActivity.this);
				SQLiteDatabase db = new SQLiteHelper(DashboardActivity.this)
						.getReadableDatabase();
				SQLiteCursor filesCursor = (SQLiteCursor) db
						.rawQuery(
								"SELECT file_hash, doc_id, extension, date_added FROM files WHERE extension='pdf'",
								null);
				//String[] hashes = new String[filesCursor.getCount()];
				try {
					while (filesCursor.moveToNext()) {
						Log.d("PDF DOWNLOAD", "Going to download "
								+ filesCursor.getString(0) + ".pdf");
					
						fileManager.downloadAndStore(filesCursor.getString(1),
								filesCursor.getString(0), "pdf", whereToStore,
								null);
					}
					handler.sendEmptyMessage(0);
				} catch (OAuthMessageSignerException e) {
						handler.sendEmptyMessage(1);
						e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
						handler.sendEmptyMessage(1);
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						handler.sendEmptyMessage(1);
						e.printStackTrace();
					} catch (HttpException e) {
						e.printStackTrace();
					} catch (IOException e) {
						handler.sendEmptyMessage(-1);
						e.printStackTrace();
					} finally {
					  filesCursor.close();
					  db.close();
					}
			}
		});
		t.start();
	}

	/**
	 * Handles creating of dialogs
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DOWNLOAD_FAILED_DIALOG:
			dialog = new AlertDialog.Builder(DashboardActivity.this)
					.setTitle("Storage problems")
					.setMessage("Oops! Somwthing went wrong. Maybe you are running out of storage space...")
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									dialog.dismiss();
								}
							}).create();
			break;
		case SYNC_FAILED_DIALOG:
			dialog = new AlertDialog.Builder(DashboardActivity.this)
					.setTitle("Synchronization problems")
					.setMessage(
							"Oops! Something went wrong. This could be a problem of the Mendeley OAPI. Please try again later or report a bug on droideley@gmail.com.")
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).create();
			break;
		case FIRST_START_DIALOG:
			dialog = new AlertDialog.Builder(DashboardActivity.this)
					.setTitle("Welcome to Droideley!")
					.setMessage(
							"First thing you should do is to synchronize Droideley with your mendeley account."
									+ " You can always do it by pressing the Menu button and choosing the Sync option. Would you like to synchronize now?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									synchronize(SYNC_ALL);
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
			break;
		case SYNC_PROGRESS_DIALOG:
			dialog = new ProgressDialog(DashboardActivity.this);
			((ProgressDialog) dialog).setMessage("Synchronizing...");
			break;
		case DOWNLOAD_PROGRESS_DIALOG:
			dialog = new ProgressDialog(DashboardActivity.this);
			((ProgressDialog) dialog).setMessage("Downloading...");
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}
