package com.droideley;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

public class GroupsActivity extends ListActivity {

	private Communicator comm;
	private SQLiteDatabase db;
	private SimpleCursorAdapter adapter;
	private SQLiteCursor groupsCursor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.groups_list);
		ActionBarWidget actionBar = (ActionBarWidget) findViewById(R.id.groups_bar);
		actionBar.setTitle("Groups");
		actionBar.setParentActivity(this);
		actionBar.showSearchButton(true);
		actionBar.setListView(getListView());
	}

	public void onResume() {
		super.onResume();
		comm = new Communicator(this);
		ListView lv = this.getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(GroupsActivity.this, GroupDocumentsListActivity.class);
				Bundle b = new Bundle();
				// b.putInt("FILTER_TYPE", DocumentListActivity.FILTER_AUTHOR);
				b.putLong("ID", id);
				b.putString("BAR_TITLE",
						"" + ((TextView) view.findViewById(R.id.groupName)).getText());
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		db = new SQLiteHelper(getBaseContext()).getReadableDatabase();
		groupsCursor = (SQLiteCursor) db.rawQuery(
				"SELECT group_id AS _id, name, size FROM groups ORDER BY name", null);

		adapter = new SimpleCursorAdapter(this, R.layout.groups_list_item, groupsCursor,
				new String[] { "name", "size" }, new int[] { R.id.groupName, R.id.groupSize });
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			public Cursor runQuery(CharSequence constraint) {
				String escapedConstraint = new String("%" + constraint.toString() + "%");
				escapedConstraint = DatabaseUtils.sqlEscapeString(escapedConstraint);
				Cursor c = (SQLiteCursor) db.rawQuery(
						"SELECT group_id AS _id, name, size FROM groups WHERE name LIKE "
								+ escapedConstraint + " ORDER BY name", null);				
				return c;
			}
		});
		adapter.setStringConversionColumn(adapter.getCursor().getColumnIndex("signature"));
		setListAdapter(adapter);
		this.getListView().setTextFilterEnabled(true);
	}

	public void onPause() {
		super.onPause();
		groupsCursor.close();		
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
		inflater.inflate(R.menu.groups_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sortBy:
			String sortingOptions[] = { "By name", "By documents count" };

			AlertDialog.Builder builder = new AlertDialog.Builder(GroupsActivity.this);
			builder.setTitle("Sorting options");
			builder.setItems(sortingOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, final int item) {
					switch (item) {
					case 0:
						groupsCursor = (SQLiteCursor) db.rawQuery(
								"SELECT group_id AS _id, name, size FROM groups ORDER BY name",
								null);
						adapter.changeCursor(groupsCursor);
						adapter.notifyDataSetChanged();
						break;
					case 1:
						groupsCursor = (SQLiteCursor) db
								.rawQuery(
										"SELECT group_id AS _id, name, size FROM groups ORDER BY size DESC",
										null);
						adapter.changeCursor(groupsCursor);
						adapter.notifyDataSetChanged();
						break;
					}
				}
			});
			// Show the dialog
			AlertDialog alert = builder.create();
			alert.show();
			break;
		case R.id.reload:
			if (DroideleyTools.checkConnection(this)) {
				refreshGroups();
			} else {
				Toast.makeText(getBaseContext(), "Device is not connected to the Internet...",
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return true;
	}

	/**
	 * Helper method for starting a thread for synchronization of the local DB
	 */
	private void refreshGroups() {
		final ProgressDialog progressDialog = ProgressDialog.show(GroupsActivity.this, "",
				"Fetching groups...", true);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
						// adapter.notifyDataSetChanged();
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
					comm.syncGroups();
					handler.sendEmptyMessage(0);
				} catch (Exception e) {
					// handler.sendEmptyMessage(1);
					// showDialog(SYNC_FAILED);
					Log.e("Syncing", "Syncing failed...", e);
				}
			}
		}).start();
	}

}
