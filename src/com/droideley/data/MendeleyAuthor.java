package com.droideley.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author Petr (severin) Volny
 * 
 */
public class MendeleyAuthor {

	private String forename;
	private String surname;	

	public MendeleyAuthor(String forename, String surname) {
		this.setForename(forename);
		this.setSurname(surname);
	}

	public int insertIntoDB(SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put("first_name", getForename());
		cv.put("last_name", getSurname());
		cv.put("signature", getSignature());
		cv.put("size", 1);
		return (int) db.insert("authors", null, cv);
	}	

	public String toString() {
		return getForename() +" "+ getSurname();
	}

	public String getForename() {
		return forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	public String getSignature() {
		return surname +", "+forename;
	}
}
