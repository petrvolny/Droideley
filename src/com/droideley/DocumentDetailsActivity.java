package com.droideley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.droideley.comm.MendeleyAuthTools;
import com.droideley.comm.SQLiteHelper;
import com.droideley.components.ActionBarWidget;
import com.droideley.tools.DroideleyTools;
import com.droideley.tools.FileManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class DocumentDetailsActivity extends Activity {

	static final int PROGRESS_DIALOG = 0;

	private SQLiteDatabase db;
	private FileManager fileManager;
	private String docId;
	private String canonicalId = null;
	private boolean groupDoc = false;
	// private String fileToDownload = null;
	private TextView tvAuthors;
	private TextView tvOutlet;
	private String pdfPath;
	private ActionBarWidget actionBar;
	private ProgressDialog downloadProgressDialog;

	/**
	 * Handler for progressbar
	 */
	final Handler downloadProgressHandler = new Handler() {
		public void handleMessage(Message msg) {
			downloadProgressDialog.setProgress(msg.arg1);
			// If something went wrong
			if (msg.arg1 < 0) {
				switch (msg.arg1) {
				case -1:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(DocumentDetailsActivity.this, "Can not access the sdcard...",
							Toast.LENGTH_SHORT).show();
					return;
				case -10:
					return;
				case -11:
					return;
				case -12:
					return;
				default:
					break;
				}
			}
			// Finished?
			if (msg.arg1 >= 100) {
				if (downloadProgressDialog.isShowing()) {
					dismissDialog(PROGRESS_DIALOG);
				}
				downloadProgressDialog.setProgress(0);
				File file = fileManager.getStoredFile(msg.getData().getString("DownloadedFile"));
				if (file.exists()) {
					Uri path = Uri.fromFile(file);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(path, "application/"
							+ msg.getData().getString("FileType"));
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(
								DocumentDetailsActivity.this,
								"No application available to view "
										+ msg.getData().getString("FileType"), Toast.LENGTH_SHORT)
								.show();
					}
				}
			// Update progress
			} else {
				downloadProgressDialog.setProgress(msg.arg1);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fileManager = new FileManager(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.document_details);
		// Setup action bar
		actionBar = (ActionBarWidget) findViewById(R.id.details_bar);
		actionBar.setTitle("Document details");
		actionBar.showShareButton(true);

		tvAuthors = (TextView) findViewById(R.id.detailAuthors);
		tvOutlet = (TextView) findViewById(R.id.detailOutlet);
	}

	@Override
	public void onResume() {
		super.onResume();
		String authors = "";
		db = new SQLiteHelper(this).getReadableDatabase();
		docId = getIntent().getExtras().getString("ID");
		// Read document details
		Cursor c;
		// Are we going to show group document?
		if (getIntent().hasExtra("GROUP_DOC")) {
			groupDoc = true;
		} else {
			groupDoc = false;
		}
		// If we are processing a group document
		if (groupDoc) {
			c = db.rawQuery(
					"SELECT title, abstract, year, mendeley_url, url, authors, outlet FROM group_documents WHERE doc_id="
							+ docId, null); //
		} else {
			// Normal document
			// Fetching authors
			Cursor a = db
					.rawQuery(
							"SELECT first_name, last_name FROM authors, authored WHERE authors.author_id=authored.author_id AND doc_id="
									+ docId, null);
			if (a.moveToFirst()) {
				authors += a.getString(0) + " " + a.getString(1);
				while (a.moveToNext()) {
					authors += "; " + a.getString(0) + " " + a.getString(1);
				}
			}
			a.close();
			tvAuthors.setText(authors);
			// Fetching outlet
			Cursor outlet = db
					.rawQuery(
							"SELECT name FROM outlets, documents_outlets WHERE documents_outlets.outlet_id=outlets.outlet_id AND doc_id="
									+ docId, null);
			if (outlet.moveToFirst()) {
				tvOutlet.setText(outlet.getString(0));
			}
			outlet.close();

			c = db.rawQuery(
					"SELECT title, abstract, year, mendeley_url, url, canonical_id FROM documents WHERE doc_id="
							+ docId, null); //
		}
		// Setting common fields
		c.moveToFirst();
		((TextView) findViewById(R.id.detailName)).setText(c.getString(0));
		((TextView) findViewById(R.id.detailAbstract)).setText(c.getString(1));
		((TextView) findViewById(R.id.detailYear)).setText(c.getString(2));
		((TextView) findViewById(R.id.mendeleyUrl)).setText(c.getString(3));
		((TextView) findViewById(R.id.url)).setText(c.getString(4));
		canonicalId = c.getString(5);
		Log.d("DEBUG", "Canonical ID: "+canonicalId);
		if (getIntent().hasExtra("GROUP_DOC")) {
			tvAuthors.setText(c.getString(5));
			tvOutlet.setText(c.getString(6));
		}
/**		
		if (canonicalId != null) {
			actionBar.setRelatedId(canonicalId);
			actionBar.setRelatedDocTitle(c.getString(0));
			actionBar.showRelatedButton(true);
		} else {
			actionBar.showRelatedButton(false);
		}

		actionBar.setStringToShare("#Droideley " + c.getString(3));
*/		

		// Get all files attached to this document
		Cursor filesCursor;
		if (groupDoc) {
			filesCursor = db.rawQuery(
					"SELECT file_hash, extension, date_added FROM group_files WHERE doc_id = " + docId, null);
		} else {
			filesCursor = db.rawQuery(
					"SELECT file_hash, extension, date_added FROM files WHERE doc_id = " + docId, null);
		}

		final String[] filesHashes = new String[filesCursor.getCount()];
		final String[] filesExtensions = new String[filesCursor.getCount()];
		int i = 0;
		// Store hashes and extensions to the arrays
		while (filesCursor.moveToNext()) {
			filesHashes[i] = filesCursor.getString(0);
			filesExtensions[i] = filesCursor.getString(1);
			i++;
		}
		filesCursor.close();
		((LinearLayout) findViewById(R.id.files)).removeAllViews(); // Remove all buttons from the layout
		for (i = 0; i < filesExtensions.length; i++) {
			final String hash = filesHashes[i];
			final String extension = filesExtensions[i];
			Button fileBtn = new Button(this);
			fileBtn.setGravity(Gravity.CENTER);
			fileBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, 1));
			fileBtn.setText(filesExtensions[i]);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					DocumentDetailsActivity.this);
			// If we don't have the file stored, we need to download it first
			if (fileManager.getStoredFile(hash + "." + extension) == null) {
				fileBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						if (!DroideleyTools.checkConnection(DocumentDetailsActivity.this)) {
							Toast.makeText(getBaseContext(),
									"Droideley needs internet connection to download the file.",
									Toast.LENGTH_LONG).show();
							return;
						}
						if (fileManager.checkExternalStorageState()) {
							builder.setTitle("Download to");
							String[] downloadingOptions = { "Local storage", "External storage" };
							builder.setItems(downloadingOptions,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, final int item) {
											Log.d("Downloading file...", "Show dialog...");
											showDialog(PROGRESS_DIALOG);
											// Start the thread to download the
											// chosen file											
											downloadFile(hash, extension, item);
										}
									});
							// Show the dialog
							AlertDialog alert = builder.create();
							alert.show();
						} else {
							Log.d("Downloading file...", "Show dialog...");
							showDialog(PROGRESS_DIALOG);
							downloadFile(hash, extension, FileManager.LOCAL_STORAGE);
						}
					}
				});
				// Else, we can open it
			} else {
				fileBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						String[] downloadingOptions = { "View file", "Reload file from Mendeley" };
						builder.setTitle("View or reload?");
						builder.setItems(downloadingOptions, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, final int item) {
								if (item == 0) {
									Uri path = Uri.fromFile(fileManager.getStoredFile(hash + "."
											+ extension));
									Intent intent = new Intent(Intent.ACTION_VIEW);
									intent.setDataAndType(path, "application/" + extension);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

									try {
										startActivity(intent);
									} catch (ActivityNotFoundException e) {
										Toast.makeText(
												DocumentDetailsActivity.this,
												"No application available to view " + extension
														+ " file", Toast.LENGTH_SHORT).show();
									}
								} else {
									if (!DroideleyTools.checkConnection(DocumentDetailsActivity.this)) {
										Toast.makeText(getBaseContext(),
												"Droideley needs internet connection to download the file.",
												Toast.LENGTH_LONG).show();
										return;
									}
									Log.d("Downloading file...", "Show dialog...");
									showDialog(PROGRESS_DIALOG);
									// Start the thread to download the
									// chosen file
									if (fileManager.isStoredLocaly(hash + "." + extension)) {
										downloadFile(hash, extension, FileManager.LOCAL_STORAGE);
									} else {
										downloadFile(hash, extension, FileManager.EXTERNAL_STORAGE);
									}
								}
							}
						});
						// Show the dialog
						AlertDialog alert = builder.create();
						alert.show();
					}
				});
			}
			((LinearLayout) findViewById(R.id.files)).addView(fileBtn);
		}

		// Make url links clickable
		Linkify.addLinks(((TextView) findViewById(R.id.mendeleyUrl)), Linkify.ALL);
		Linkify.addLinks(((TextView) findViewById(R.id.url)), Linkify.ALL);

		c.close();
	}

	public void onPause() {
		super.onPause();
		db.close();
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			downloadProgressDialog = new ProgressDialog(this);
			downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			downloadProgressDialog.setMessage("Loading...");
			return downloadProgressDialog;
		default:
			return downloadProgressDialog;
		}
	}

	private void downloadFile(final String fileHash, final String fileExtension,
			final int whereToStore) {
		// Start the thread to download the chosen file
		Thread t = new Thread() {
			public void run() {
				try {

					fileManager.downloadAndStore(docId, fileHash, fileExtension, whereToStore,
							downloadProgressHandler);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	/*
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case R.id.share: break; case R.id.searchRelated:
	 * break; } return true; }
	 * 
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.details_menu,
	 * menu); return true; }
	 */
}
