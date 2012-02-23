package com.droideley;

import com.droideley.comm.SQLiteHelper;
import com.droideley.components.ActionBarWidget;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author Petr (severin) Volny
 *
 */
public class FoldersActivity extends ListActivity {

	private SQLiteDatabase db;
	private SimpleCursorAdapter adapter;
	private SQLiteCursor docsCursor;
	
	private int index = 0;
	private int top = 0;

	/**
	 * Restores last known postition of the list
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		index = getListView().getFirstVisiblePosition();
		View v = getListView().getChildAt(0);
		top = (v == null) ? 0 : v.getTop();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.folders_list);
		ActionBarWidget actionBar = (ActionBarWidget) findViewById(R.id.folders_bar);
		actionBar.setTitle("Folders");
	}
	
	/**
	 * Populates the list with folders
	 */
	@Override
	public void onResume() {
		super.onResume();
		ListView lv = this.getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(FoldersActivity.this,
						DocumentListActivity.class);
				Bundle b = new Bundle();
				b.putInt("FILTER_TYPE", DocumentListActivity.FILTER_FOLDER);				
				b.putLong("ID", id);
				b.putString("BAR_TITLE", ""+((TextView)view.findViewById(R.id.colItemName)).getText());
				intent.putExtras(b);
				startActivity(intent);
			}
		});
		
		db = new SQLiteHelper(getBaseContext()).getReadableDatabase();
		docsCursor = (SQLiteCursor) db.rawQuery("SELECT folder_id AS _id, name, size FROM folders ORDER BY name", null);
		
		adapter = new SimpleCursorAdapter(this, 
				R.layout.folders_list_item, 
				docsCursor, 
				new String[] {"name", "size"},
				new int[] {R.id.colItemName, R.id.colItemSize});
		setListAdapter(adapter);	
		getListView().setSelectionFromTop(index, top);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		docsCursor.close();
		db.close();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.folders_menu, menu);
		return true;
	}		
	
	/**
	 * Handling mednu selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sortBy:
			String sortingOptions[] = {"By name", "By documents count"};
			
			AlertDialog.Builder builder = new AlertDialog.Builder(FoldersActivity.this);
			builder.setTitle("Sorting options");
			builder.setItems(sortingOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, final int item) {
					switch (item) {
					case 0:
						docsCursor = (SQLiteCursor) db.rawQuery("SELECT folder_id AS _id, name, size FROM folders ORDER BY name", null);
						adapter.changeCursor(docsCursor);
						adapter.notifyDataSetChanged();						
						break;
					case 1:
						docsCursor = (SQLiteCursor) db.rawQuery("SELECT folder_id AS _id, name, size FROM folders ORDER BY size DESC", null);
						adapter.changeCursor(docsCursor);
						adapter.notifyDataSetChanged();
						break;
					}
				}
			});
			// Show the dialog
			AlertDialog alert = builder.create();
			alert.show();
			break;			
		}
		return true;
	}	
}
