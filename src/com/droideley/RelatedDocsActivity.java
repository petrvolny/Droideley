package com.droideley;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.droideley.comm.Communicator;
import com.droideley.components.ActionBarWidget;

public class RelatedDocsActivity extends ListActivity {
	
	private String url;
	private SQLiteDatabase db;
	private String canonicalId;
	private Communicator comm;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.documents_list);
		ActionBarWidget actionBar = (ActionBarWidget) findViewById(R.id.documents_bar);
		actionBar.setTitle(getIntent().getExtras().getString("BAR_TITLE"));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		comm = new Communicator(this);		
		
		ListView lv = this.getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			}
		});
		
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		//docsCursor.close();
		//db.close();
	}
	
	/**
	 * Helper method for starting thread for getting related from Mendeley OAPI
	 */
	private void findRelated() {
		
	}
}
