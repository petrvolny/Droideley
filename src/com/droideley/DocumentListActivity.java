package com.droideley;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.droideley.comm.SQLiteHelper;
import com.droideley.components.ActionBarWidget;

public class DocumentListActivity extends ListActivity {

	public static final int FILTER_NONE = 0;
	public static final int FILTER_FOLDER = 1;
	public static final int FILTER_AUTHOR = 2;
	public static final int FILTER_TAG = 3;

	private SQLiteDatabase db;
	private SQLiteCursor docsCursor;
	private String id;
	private ActionBarWidget actionBar;
	private SimpleCursorAdapter adapter;
	
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
		System.out.println("onCreate(): called...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.documents_list);
		actionBar = (ActionBarWidget) findViewById(R.id.documents_bar);
		actionBar.setTitle("All documents");
		actionBar.setParentActivity(this);
		actionBar.setListView(getListView());
		actionBar.showSearchButton(true);

		ListView lv = getListView();
		lv.setSaveEnabled(true);
		// registerForContextMenu(lv);
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(DocumentListActivity.this, DocumentDetailsActivity.class);
				Bundle b = new Bundle();
				b.putString("ID", Long.toString(id));
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}

/*	
	public void onRestart() {
		super.onRestart();
		System.out.println("onRestart(): called...");
		getListView().setSelectionFromTop(index, top);	
		int selection = getListView().getSelectedItemPosition();
		System.out.println("Last Selected: " + selection);
		getListView().setSelection(selection);
		
	}
*/	

	@Override
	public void onResume() {
		super.onResume();
		System.out.println("onResume(): called...");		
		final String dbQuery;
		String barTitle = "";
		int filterType = FILTER_NONE;
		long id = -1;

		getListView().clearTextFilter();
		filterType = getIntent().getExtras().getInt("FILTER_TYPE");
		if (getIntent().hasExtra("ID")) {
			id = getIntent().getExtras().getLong("ID");
		}
		if (getIntent().hasExtra("BAR_TITLE")) {
			barTitle = getIntent().getExtras().getString("BAR_TITLE");
		}

		switch (filterType) {
		case FILTER_NONE:
			dbQuery = "SELECT doc_id AS _id, title, authors_string FROM documents";
			break;
		case FILTER_FOLDER:
			dbQuery = "SELECT documents.doc_id AS _id, title, authors_string FROM documents, documents_folders WHERE documents.doc_id=documents_folders.doc_id AND folder_id="
					+ id;
			break;
		case FILTER_AUTHOR:
			dbQuery = "SELECT documents.doc_id AS _id, title, authors_string FROM documents, authored WHERE documents.doc_id=authored.doc_id AND author_id="
					+ id;
			break;
		case FILTER_TAG:
			dbQuery = "SELECT documents.doc_id AS _id, title, authors_string FROM documents, documents_tags WHERE documents.doc_id=documents_tags.doc_id AND tag_id="
					+ id;
			break;
		default:
			// TODO Throw some exception
			dbQuery = null;
			System.out.println("Neeeeeee=============Neeeeeeeeeee");
			break;
		}

		Log.d("query", dbQuery);
		// TextView barTitleView = (TextView) findViewById(R.id.barTitle);
		actionBar.setTitle(barTitle);
		db = new SQLiteHelper(getBaseContext()).getReadableDatabase();
		docsCursor = (SQLiteCursor) db.rawQuery(dbQuery + " ORDER BY title", null);		

		adapter = new SimpleCursorAdapter(this, R.layout.documents_list_item, docsCursor,
				new String[] { "title", "authors_string" }, new int[] { R.id.docItemTitle,
						R.id.docItemAuthors });
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			public Cursor runQuery(CharSequence constraint) {
				//docsCursor.close();
				System.out.println();
				if (constraint.equals("")) {
					Cursor c = db.rawQuery(dbQuery + " ORDER BY title", null);
					//startManagingCursor(c);
					return c;
				}
				if (dbQuery.contains("WHERE")) {
					String filterQuery = dbQuery + " AND title LIKE '%" + constraint
							+ "%' ORDER BY title";
					System.out.println("Filter Query: " + filterQuery);
					Cursor c = db.rawQuery(filterQuery, null);
					//startManagingCursor(c);
					return c;
				} else {
					String filterQuery = dbQuery + " WHERE title LIKE '%" + constraint
							+ "%' ORDER BY title";
					System.out.println("Filter Query: " + filterQuery);
					Cursor c = db.rawQuery(filterQuery, null);
					//startManagingCursor(c);
					return c;
				}
			}
		});
		adapter.setStringConversionColumn(adapter.getCursor().getColumnIndex("title"));
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);	
		getListView().setSelectionFromTop(index, top);
	}

	public void onPause() {
		super.onPause();		
		//stopManagingCursor(adapter.getCursor());
		//adapter.getCursor().close();
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
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.doc_list_menu,
	 * menu); return true; }
	 * 
	 * @Override public void onCreateContextMenu(ContextMenu menu, View v,
	 * ContextMenuInfo menuInfo) { super.onCreateContextMenu(menu, v, menuInfo);
	 * MenuInflater inflater = getMenuInflater();
	 * inflater.inflate(R.menu.doc_list_context, menu); }
	 */
}
