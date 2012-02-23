package com.droideley;

import com.droideley.components.ActionBarWidget;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class SearchedDocumentDetailsActivity extends Activity {

	
	public static final int DOC_TITLE = 0;
	public static final int DOC_AUTHORS = 1;
	public static final int DOC_YEAR = 2;
	public static final int DOC_ABSTRACT = 3;
	public static final int DOC_MENDELEY_URL = 4;	
	
	
	private ActionBarWidget actionBar;
	private TextView tvTitle;
	private TextView tvAuthors;
	private TextView tvYear;
	private TextView tvAbstract;
	private TextView tvMendeleyUrl;
	//private Button pdfButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.document_details);
		actionBar = (ActionBarWidget) findViewById(R.id.details_bar);
		actionBar.setTitle("Document details");
		actionBar.showShareButton(false);		
		
		tvTitle = (TextView) findViewById(R.id.detailName);
		tvAuthors = (TextView) findViewById(R.id.detailAuthors);
		tvYear = (TextView) findViewById(R.id.detailYear);
		tvAbstract = (TextView) findViewById(R.id.detailAbstract);
		tvMendeleyUrl = (TextView) findViewById(R.id.mendeleyUrl);						
	}

	@Override
	public void onResume() {
		super.onResume();
		tvTitle.setText(getIntent().getExtras().getString("DOC_TITLE"));
		tvAuthors.setText(getIntent().getExtras().getString("DOC_AUTHORS"));
		tvYear.setText(getIntent().getExtras().getString("DOC_YEAR"));
		tvAbstract.setText(getIntent().getExtras().getString("DOC_ABSTRACT"));
		tvMendeleyUrl.setText(getIntent().getExtras().getString("DOC_MENDELEY_URL"));
		Linkify.addLinks(((TextView) findViewById(R.id.mendeleyUrl)),
				Linkify.ALL);
	}
}
