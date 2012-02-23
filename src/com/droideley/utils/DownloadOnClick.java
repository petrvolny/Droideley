package com.droideley.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.droideley.DocumentDetailsActivity;
import com.droideley.tools.FileManager;

public class DownloadOnClick implements OnClickListener {
	
	private String docId;
	private String hash;
	private String extension;	
	private FileManager fileManager;
	private Handler handler;
	
	public DownloadOnClick(String docId, String hash, String extension, FileManager fileManager, Handler handler) {
		this.docId = docId;
		this.hash = hash;
		this.extension = extension;
		this.fileManager = fileManager;
		this.handler = handler;
	}

	public void onClick(View v) {		
		
	}
}
