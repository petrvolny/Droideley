package com.droideley.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class MendeleyFolder {
	private String id;
	private String name;
	private int size;
	
	public MendeleyFolder() {
		
	}

	public void insertIntoDB(SQLiteDatabase db) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			cv.put("folder_id", id);
			cv.put("name", name);
			cv.put("size", size);
			db.insert("folders", null, cv);
		}		
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
}
