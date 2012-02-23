package com.droideley.components;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.droideley.DashboardActivity;
import com.droideley.DocumentListActivity;
import com.droideley.R;
import com.droideley.RelatedDocsActivity;
import com.droideley.tools.DroideleyTools;

/**
 * This class encapsulates the Droideley's action bar widget functionality
 * @author Petr (severin) Volny
 *
 */
public class ActionBarWidget extends LinearLayout {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	
	private LinearLayout barView;
	private TextView titleView;
	private ImageButton btnMendeley;
	private ImageButton btnSearch;
	private ImageButton btnShare;
	private ImageButton btnVoice;
	private ImageButton btnRelated;
	private String stringToShare;
	private String relatedId = null;
	private String relatedDocTitle = null;
	private TextView voiceOutput;
	private Activity parentActivity;
	private View listView;
	private File file;
	//private final Context thisContext;

	public ActionBarWidget(Context context) {
		super(context);
		//thisContext = context;
		init(context);
	}

	public ActionBarWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		//thisContext = context;
		init(context);	

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ActionBar);
		CharSequence title = a.getString(R.styleable.ActionBar_title);		
		if (title != null) {
			setTitle(title);
		}
		a.recycle();
	}
	
	/**
	 * Does the basic inflating of this action bar wrapper
	 * @param context
	 */
	private void init(Context context) {
		barView = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.action_bar, this);
		titleView = (TextView) barView.findViewById(R.id.barTitle);
		btnMendeley = (ImageButton) barView.findViewById(R.id.barHome);
		btnSearch = (ImageButton) barView.findViewById(R.id.barSearch);
		btnShare = (ImageButton) barView.findViewById(R.id.barShare);
		btnVoice = (ImageButton) barView.findViewById(R.id.barVoice);
		btnRelated = (ImageButton) barView.findViewById(R.id.barRelated);
		
		btnSearch.setVisibility(GONE);
		btnShare.setVisibility(GONE);
		btnVoice.setVisibility(GONE);		
		btnRelated.setVisibility(GONE);
		
		PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
    		btnVoice.setOnClickListener(new OnClickListener() {
    			
    			public void onClick(View v) {    				
    				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    		                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    		        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Droideley");    		        
    		        getParentActivity().startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    			}
    		});
        } else {
        	btnVoice.setVisibility(GONE);
            System.out.println("Voice recognizer not present");
        }

		btnMendeley.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getContext(),
						DashboardActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getContext().startActivity(intent);
			}
		});		
		
		btnShare.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, stringToShare);
                //sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

				getContext().startActivity(Intent.createChooser(sharingIntent,"Share using"));				
			}
		});
		
		btnSearch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				InputMethodManager inputMethodManager=(InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
			    inputMethodManager.toggleSoftInputFromWindow(listView.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
				//Intent intent = new Intent(getContext(),
				//		SearchHubActivity.class);				
				//getContext().startActivity(intent);				
			}
		});
		
		btnRelated.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				if (!DroideleyTools.checkConnection(getContext())) {
					Toast.makeText(getContext(), "Device needs to be connected to the Internet to find related docs...",
							Toast.LENGTH_LONG).show();
				} else {
					Intent relatedIntent = new Intent(getContext(), RelatedDocsActivity.class);
					Bundle b = new Bundle();
					b.putString("RELATED_ID", ""+relatedId);
					b.putString("BAR_TITLE", "Related to "+relatedDocTitle);
					relatedIntent.putExtras(b);
					getContext().startActivity(relatedIntent);
				}
			}
		});
	}	
	

	public void setTitle(CharSequence title) {
		titleView.setText(title);
	}

	public void setTitle(int resId) {
		titleView.setText(resId);
	}
	
	public void setRelatedDocTitle(String relatedDocTitle) {
		this.relatedDocTitle = relatedDocTitle;
	}

	public void setBtnMendeleyDisabled(boolean disabled) { 
		btnMendeley.setClickable(!disabled);
	}

	public void showSearchButton(boolean show) {
		if (show) {
			btnSearch.setVisibility(VISIBLE);
		} else {
			btnSearch.setVisibility(GONE);
		}
	}
	
	public void showShareButton(boolean show) {
		if (show) {
			btnShare.setVisibility(VISIBLE);
		} else {
			btnShare.setVisibility(GONE);
		}
	}
	
	public void showVoiceButton(boolean show) {
		if (show) {
			btnVoice.setVisibility(VISIBLE);
		} else {
			btnVoice.setVisibility(GONE);
		}
	}
	
	public void showRelatedButton(boolean show) {
		if (show) {
			btnRelated.setVisibility(VISIBLE);
		} else {
			btnRelated.setVisibility(GONE);
		}
	}
	
	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}

	public void setStringToShare(String stringToShare) {
		this.stringToShare = stringToShare;
	}

	public String getStringToShare() {
		return stringToShare;
	}
	
	public void removeShareBtn() {
		btnShare.setVisibility(GONE);		
	}
	
	public void removeSearchBtn() {
		btnSearch.setVisibility(GONE);		
	}

	public void setVoiceOutput(TextView voiceOutput) {
		this.voiceOutput = voiceOutput;
	}

	public TextView getVoiceOutput() {
		return voiceOutput;
	}

	public void setParentActivity(Activity parentActivity) {
		this.parentActivity = parentActivity;
	}

	public Activity getParentActivity() {
		return parentActivity;
	}
	
	public void setListView(ListView listView) {
		this.listView = listView;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
