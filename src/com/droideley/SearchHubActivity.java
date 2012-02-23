package com.droideley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.droideley.comm.Communicator;
import com.droideley.components.ActionBarWidget;
import com.droideley.data.MendeleyDocument;
import com.droideley.tools.DroideleyTools;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * This activity should serve as a starting point for searching in external
 * sources (Mendeley catalogue, PLoS etc.)
 * 
 * @author Petr (severin) Volny
 */
public class SearchHubActivity extends ListActivity {

	ArrayList<HashMap<String, String>> adapterList;

	private EditText inputSearch;
	private Button btnSearch;
	private SimpleAdapter sa;
	private ListView lv;

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// comm = new Communicator(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.searchhub);
		ListView lv = getListView();
		ActionBarWidget actionBar = (ActionBarWidget) findViewById(R.id.searchhub_bar);
		inputSearch = (EditText) findViewById(R.id.inputSearch);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		actionBar.setTitle("Search Hub");
		actionBar.removeSearchBtn();
		actionBar.removeShareBtn();
		actionBar.showVoiceButton(true);
		actionBar.setParentActivity(this);

		// Show document details on item click
		getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				System.out.println("Row ID: " + id);
				Intent intent = new Intent(SearchHubActivity.this,
						SearchedDocumentDetailsActivity.class);
				Bundle b = new Bundle();
				b.putString("DOC_TITLE", adapterList.get(position).get("title"));
				b.putString("DOC_AUTHORS", adapterList.get(position).get("authors"));
				b.putString("DOC_YEAR", adapterList.get(position).get("year"));
				// b.putString("DOC_ABSTRACT",
				// adapterList.get(position).get("abstract"));
				b.putString("DOC_MENDELEY_URL", adapterList.get(position).get("mendeley_url"));
				intent.putExtras(b);
				startActivity(intent);

			}
		});

		// Perform the search
		btnSearch.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (DroideleyTools.checkConnection(getBaseContext())) {
					final ProgressDialog dialog = ProgressDialog.show(SearchHubActivity.this, "",
							"Searching...", true);
					final Handler handler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							setListAdapter(sa);
							dialog.dismiss();
						}
					};

					new Thread(new Runnable() {

						public void run() {
							dialog.show();
							adapterList = new ArrayList<HashMap<String, String>>();
							LinkedList<MendeleyDocument> docs = Communicator
									.searchMendeley(inputSearch.getText().toString());
							for (MendeleyDocument d : docs) {
								adapterList.add(d.toHashMap());
							}
							sa = new SimpleAdapter(getBaseContext(), adapterList,
									R.layout.documents_list_item,
									new String[] { "title", "authors" }, new int[] {
											R.id.docItemTitle, R.id.docItemAuthors });
							handler.sendEmptyMessage(0);
						}
					}).start();

				} else {
					Toast.makeText(getApplicationContext(),
							"You need to be connected to the Internet to perform this action...",
							Toast.LENGTH_LONG).show();
				}

			}
		});
	}

	public void onResume() {
		super.onResume();
	}

	/**
	 * Process the result of the google voice recognizer
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			// If the google was sure and returned only one choice
			if (matches.size() == 1) {
				inputSearch.setText(matches.get(0));
			} else {
				// else we will let user to choose the correct sentence
				final CharSequence[] items = new CharSequence[matches.size()];
				matches.toArray(items);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// builder.setTitle("");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						inputSearch.setText(items[item]);
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			// mList.setAdapter(new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1,
			// matches));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
