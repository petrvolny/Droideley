package com.droideley.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Class that encapsulates Mendeley File data structure and db related methods
 * @author Petr (severin) Volny 
 */
public class MendeleyFile {

	private long docId;
	private String hash;
	private String dateAdded;
	private String extension;
	private int size;

	public MendeleyFile(String hash, String dateAdded, String extension) {
		this.hash = hash;
		this.dateAdded = dateAdded;
		this.extension = extension;
	}

	public MendeleyFile(String hash, String dateAdded, String extension,
			long docId) {
		this(hash, dateAdded, extension);
		this.docId = docId;
	}

	public MendeleyFile(String hash, String dateAdded, String extension,
			long docId, int size) {
		this(hash, dateAdded, extension, docId);
		this.size = size;
	}

	/**
	 * 
	 * @param db SQLiteDatabse to store this object in
	 * @param isGroupFile true for storing it to "group_files" db table; false for storing it to "files" table
	 */
	public void insertIntoDB(SQLiteDatabase db, boolean isGroupFile) {
		ContentValues cv = new ContentValues();
		cv.put("file_hash", hash);
		cv.put("date_added", dateAdded);
		cv.put("extension", extension);
		cv.put("size", size);
		cv.put("doc_id", docId);
		if (!isGroupFile) {
			db.insert("files", null, cv);
		} else {
			db.insert("group_files", null, cv);
		}
	}
}
