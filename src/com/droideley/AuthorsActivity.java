package com.droideley;

import com.droideley.comm.MendeleyAuthTools;
import com.droideley.comm.SQLiteHelper;
import com.droideley.components.ActionBarWidget;
import com.droideley.tools.DroideleyTools;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class AuthorsActivity extends ListActivity {

	private SQLiteDatabase db;
	private SimpleCursorAdapter adapter;
	private SQLiteCursor docsCursor;
	
	private int index = 0;
	private int top = 0;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		index = getListView().getFirstVisiblePosition();
		View v = getListView().getChildAt(0);
		top = (v == null) ? 0 : v.getTop();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.authors_list);
		ActionBarWidget actionBar = (ActionBarWidget) findViewById(R.id.authors_bar);
		actionBar.setTitle("Authors");
		actionBar.setParentActivity(this);
		actionBar.showSearchButton(true);
		actionBar.setListView(getListView());
	}

	public void onResume() {
		super.onResume();
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(AuthorsActivity.this, DocumentListActivity.class);
				Bundle b = new Bundle();
				b.putInt("FILTER_TYPE", DocumentListActivity.FILTER_AUTHOR);
				b.putLong("ID", id);
				b.putString("BAR_TITLE",
						"By " + ((TextView) view.findViewById(R.id.authorName)).getText());
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		db = new SQLiteHelper(getBaseContext()).getReadableDatabase();
		docsCursor = (SQLiteCursor) db.rawQuery(
				"SELECT author_id AS _id, signature, size FROM authors ORDER BY signature", null);

		adapter = new SimpleCursorAdapter(this, R.layout.authors_list_item, docsCursor,
				new String[] { "signature", "size" },
				new int[] { R.id.authorName, R.id.authorSize });
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			public Cursor runQuery(CharSequence constraint) {
				String escapedConstraint = DatabaseUtils.sqlEscapeString("%"
						+ constraint.toString() + "%");
				Cursor c = (SQLiteCursor) db.rawQuery(
						"SELECT author_id AS _id, signature, size FROM authors WHERE signature LIKE "
								+ escapedConstraint + " ORDER BY signature", null);
				return c;
			}
		});
		adapter.setStringConversionColumn(adapter.getCursor().getColumnIndex("signature"));
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setSelectionFromTop(index, top);
	}

	public void onPause() {
		super.onPause();
		docsCursor.close();
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.authors_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sortBy:
			String sortingOptions[] = { "By name", "By documents count" };

			AlertDialog.Builder builder = new AlertDialog.Builder(AuthorsActivity.this);
			builder.setTitle("Sorting options");
			builder.setItems(sortingOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, final int item) {
					switch (item) {
					case 0:
						docsCursor = (SQLiteCursor) db
								.rawQuery(
										"SELECT author_id AS _id, signature, size FROM authors ORDER BY signature",
										null);
						adapter.changeCursor(docsCursor);
						adapter.notifyDataSetChanged();						
						break;
					case 1:
						docsCursor = (SQLiteCursor) db
								.rawQuery(
										"SELECT author_id AS _id, signature, size FROM authors ORDER BY size DESC",
										null);
						adapter.changeCursor(docsCursor);
						adapter.notifyDataSetChanged();
						break;
					}
					//startManagingCursor(docsCursor);
				}
			});
			// Show the dialog
			AlertDialog alert = builder.create();
			alert.show();
			break;
		}
		return true;
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
}
