package com.droideley;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.droideley.comm.Communicator;
import com.droideley.comm.SQLiteHelper;
import com.droideley.components.ActionBarWidget;
import com.droideley.tools.DroideleyTools;

public class GroupDocumentsListActivity extends ListActivity {

	private SQLiteDatabase db;
	private SQLiteCursor docsCursor;
	private SimpleCursorAdapter adapter;
	ActionBarWidget actionBar;
	Communicator comm;
	private long id;

	private int index = 0;
	private int top = 0;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		index = getListView().getFirstVisiblePosition();
		View v = getListView().getChildAt(0);
		top = (v == null) ? 0 : v.getTop();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		comm = new Communicator(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.documents_list);
		actionBar = (ActionBarWidget) findViewById(R.id.documents_bar);
		actionBar.setTitle("Group Docs");
		actionBar.setParentActivity(this);
		actionBar.showSearchButton(true);

		ListView lv = this.getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(GroupDocumentsListActivity.this, DocumentDetailsActivity.class);
				Bundle b = new Bundle();
				b.putInt("FILTER_TYPE", DocumentListActivity.FILTER_NONE);
				b.putBoolean("GROUP_DOC", true);
				b.putString("ID", Long.toString(id));
				// b.putString("BAR_TITLE",
				// "By "+((TextView)view.findViewById(R.id.authorName)).getText());
				intent.putExtras(b);
				startActivity(intent);

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		id = getIntent().getExtras().getLong("ID");
		actionBar.setTitle(getIntent().getExtras().getString("BAR_TITLE"));
		actionBar.setListView(getListView());

		db = new SQLiteHelper(getBaseContext()).getReadableDatabase();
		docsCursor = (SQLiteCursor) db.rawQuery(
				"SELECT doc_id AS _id, title, authors FROM group_documents WHERE group_id=" + id + " ORDER BY title",
				null);

		if (docsCursor.getCount() == 0) {
			if (DroideleyTools.checkConnection(this)) {
				refreshDocuments();
			} else {
				Toast.makeText(getBaseContext(), "Device is not connected to the Internet...", Toast.LENGTH_LONG)
						.show();
			}
		}

		adapter = new SimpleCursorAdapter(this, R.layout.documents_list_item, docsCursor, new String[] { "title",
				"authors" }, new int[] { R.id.docItemTitle, R.id.docItemAuthors });
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			public Cursor runQuery(CharSequence constraint) {
				// Log.d("HURAYYY", "========a=a=a=a===========");
				// docsCursor.close();
				if (constraint.equals("")) {
					Cursor c = (SQLiteCursor) db.rawQuery(
							"SELECT doc_id AS _id, title, authors FROM group_documents WHERE group_id=" + id
									+ " ORDER BY title", null);

					startManagingCursor(c);
					return c;
				} else {
					String escapedConstraint = new String("%" + constraint.toString() + "%");
					Cursor c = (SQLiteCursor) db.rawQuery(
							"SELECT doc_id AS _id, title, authors FROM group_documents WHERE group_id=" + id
									+ " AND title LIKE '" + escapedConstraint + "' ORDER BY title", null);
					startManagingCursor(c);
					return c;
				}
			}
		});
		adapter.setStringConversionColumn(adapter.getCursor().getColumnIndex("title"));
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setSelectionFromTop(index, top);
	}

	/**
	 * Helper method for starting a thread for synchronization of the local DB
	 */
	private void refreshDocuments() {
		final ProgressDialog progressDialog = ProgressDialog.show(GroupDocumentsListActivity.this, "",
				"Fetching documents...", true);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
						adapter.getCursor().requery();
					}
					break;
				/*
				 * case 1: showDialog(SYNC_FAILED); break;
				 */
				}
			}
		};

		new Thread(new Runnable() {

			public void run() {
				try {
					comm.syncGroupDocuments(id);
					handler.sendEmptyMessage(0);
				} catch (Exception e) {
					// handler.sendEmptyMessage(1);
					// showDialog(SYNC_FAILED);
					Log.e("Syncing", "Syncing failed...", e);
				}
			}
		}).start();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopManagingCursor(adapter.getCursor());
		docsCursor.close();
		db.close();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			InputMethodManager inputMethodManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.toggleSoftInputFromWindow(getListView().getWindowToken(),
					InputMethodManager.SHOW_FORCED, 0);
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.group_docs_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reload:
			if (DroideleyTools.checkConnection(this)) {
				refreshDocuments();
			} else {
				Toast.makeText(getBaseContext(), "Device is not connected to the Internet...", Toast.LENGTH_LONG)
						.show();
			}
			break;
		}
		return true;
	}

}
