package com.droideley.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class MendeleyGroup {

	private int groupId;
	private String name;	
	private int size;
	private String type;
	
	public MendeleyGroup() {
		
	}
	
	public void insertInotDB(SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put("group_id", groupId);
		cv.put("name", name);
		cv.put("size", size);
		cv.put("type", type);
		db.insert("groups", null, cv);
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}	
}
