package com.droideley.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpException;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.droideley.comm.MendeleyAuthTools;

/**
 * This class handles work with files on a physical level. It provides methods
 * for downloading and storing of files in the Mendeley library.
 * 
 * @author Petr (severin) Volny
 */
public class FileManager {

	public static final int LOCAL_STORAGE = 0;
	public static final int EXTERNAL_STORAGE = 1;

	Activity activity;

	public FileManager(Activity activity) {
		this.activity = activity;
	}

	public boolean isStoredLocaly(String fileName) {
		return (new File(activity.getFilesDir(), fileName).exists()) ? true
				: false;
	}

	/**
	 * Returns File instance for the given filename.
	 * 
	 * @param fileName
	 *            path to a file
	 * @return An instance of File or null if there is no physical file on the
	 *         given path.
	 */
	public File getStoredFile(String fileName) {
		File file;
		file = new File(activity.getFilesDir(), fileName);
		if (file.exists()) {
			return file;
		} else {
			if (checkExternalStorageState()) {
				file = new File(getExternalFilesDir(), fileName);
				if (file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * Downloads the file identified with a given hash and associated to a given
	 * file. Than it stores the file to the local/external storage of a device.
	 * 
	 * @param docId
	 *            Associated document
	 * @param hash
	 *            Mendeley hash of the file
	 * @param extension
	 *            Extension of the file (pdf, txt etc)
	 * @param whereToStore
	 *            Determines, where the file is tored. Expected values are
	 *            FileManager.LOCAL_STORAGE and FileManager.EXTERNAL_STORAGE
	 * @param handler
	 *            An instance of Handler that should be notified about the
	 *            progress of the download.
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws OAuthCommunicationException 
	 * @throws OAuthExpectationFailedException 
	 * @throws OAuthMessageSignerException 
	 */
	public void downloadAndStore(String docId, String hash, String extension,
			int whereToStore, Handler handler) 
					throws OAuthMessageSignerException, 
					OAuthExpectationFailedException, 
					OAuthCommunicationException, 
					HttpException, IOException {
		String fileName = hash + "." + extension; // files are named by their
													// hashes and extensions
		FileOutputStream fos;
		switch (whereToStore) {
		case LOCAL_STORAGE:
			fos = activity
					.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
			break;
		case EXTERNAL_STORAGE:
			fos = new FileOutputStream(
					new File(getExternalFilesDir(), fileName));
			break;
		default:
			return;
		}
		// Download file
		MendeleyAuthTools.downloadFile(docId, hash, extension, fos, handler);
	}

	public boolean isUpToDate(String docId, String hash) {
		return true;
	}

	/**
	 * Returns the apps default directory on the an external storage.
	 * 
	 * @return directory on an external storage where we can put our files
	 */
	public File getExternalFilesDir() {
		String packageName = activity.getPackageName();
		File externalPath = Environment.getExternalStorageDirectory();
		Log.d("DEBUG", "External Path: " + externalPath.getAbsolutePath());
		File appFiles = new File(externalPath.getAbsolutePath()
				+ "/Android/data/" + packageName + "/files/");
		if (!appFiles.exists()) {
			appFiles.mkdirs();
		}
		return appFiles;
	}

	/**
	 * Checks the availability of an external storage.
	 * 
	 * @return True if the external storage is properly mounted and is writable.
	 *         False otherwise.
	 */
	public boolean checkExternalStorageState() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// We can read and write the media
			return true;
		} else {
			return false;
		}
	}
}
