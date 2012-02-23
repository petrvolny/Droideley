package com.droideley.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.droideley.R;
import com.droideley.comm.SQLiteHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;

/**
 * This class encapsulates mendeley document db data
 * 
 * @author Petr (severin) Volny
 */
public class MendeleyDocument {

	private long id;
	private long groupId = -1; // If (groupId != -1) than treat this document as
								// a group related document (store it to the
								// other table)
	private String uuid = null;
	private String canonicalId;
	private String title;
	private String docAbstract;
	private int year;
	private List<MendeleyAuthor> authors = new LinkedList<MendeleyAuthor>();
	private List<String> tags = new LinkedList<String>();
	private List<MendeleyFile> files = new LinkedList<MendeleyFile>();
	private String outlet;
	private String pdf = null;
	private String doi = null;
	private String isbn = null;
	private String mendeleyUrl = null;
	private String url = null;
	private String file = null;
	private String authorsString = "";

	public MendeleyDocument() {
	}

	public MendeleyDocument(String title, List<String> authors) {
		this.setTitle(title);
		// this.setAuthors(authors);
	}

	public MendeleyDocument(String title, List<String> authors, String docAbstract) {
		this(title, authors);
		this.setDocAbstract(docAbstract);
	}

	public void insertIntoDB(SQLiteDatabase db) {
		for (MendeleyAuthor author : getAuthors()) {
			setAuthorsString(getAuthorsString() + (author + "; "));
		}
		if (!getAuthorsString().equals("")) {
			setAuthorsString(getAuthorsString().substring(0, getAuthorsString().length() - 2));
		}
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			cv.put("doc_id", id);
			cv.put("uuid", uuid);
			cv.put("canonical_id", canonicalId);
			cv.put("title", title);
			cv.put("year", year);
			cv.put("abstract", docAbstract);
			cv.put("authors_string", getAuthorsString());
			//cv.put("pdf", pdf);
			cv.put("mendeley_url", mendeleyUrl);
			cv.put("url", url);
			cv.put("doi", doi);
			cv.put("isbn", isbn);
			// If we are going to store group document
			if (getGroupId() != -1) {				
				cv.put("group_id", getGroupId());
				cv.put("outlet", outlet);
				cv.put("authors", authorsString);
				db.insert("group_documents", null, cv);
				storeFiles(db, true);
			} else { 
				// If it is library document
				db.insert("documents", null, cv);

				storeAuthors(db);
				storeTags(db);
				storeFiles(db, false);
				storeOutlet(db);
			}
		} else {
			// TODO report bug
		}
	}

	/*
	 * public static MendeleyDocument fetchDocFromDB(SQLiteDatabase db, String
	 * docId) { MendeleyDocument res = new MendeleyDocument(); //db = new
	 * SQLiteHelper().getReadableDatabase(); //docId =
	 * this.getIntent().getExtras().getString("ID"); // Read document details
	 * Cursor c = db.rawQuery(
	 * "SELECT title, abstract, year, mendeley_url, url, doi, isbn FROM documents WHERE doc_id="
	 * + docId, null); // c.moveToFirst(); res.setTitle(c.getString(0));
	 * res.setDocAbstract(c.getString(1)); res.setYear(c.getInt(2));
	 * res.setMendeleyUrl(c.getString(3)); res.setUrl(c.getString(4));
	 * res.setDoi(c.getString(5)); res.setIsbn(c.getString(6));
	 * 
	 * //actionBar.setStringToShare("#Droideley "+c.getString(3)); // Create an
	 * authors string Cursor a = db.rawQuery(
	 * "SELECT author_id, signature, first_name, last_name, size FROM authors, authored WHERE authors.author_id=authored.author_id AND doc_id="
	 * + docId, null); if (a.moveToFirst()) { authors += a.getString(0); while
	 * (a.moveToNext()) { authors += ", " + a.getString(0); } } a.close();
	 * tvAuthors.setText(authors);
	 * 
	 * // Read all files attached to this document Cursor filesCursor =
	 * db.rawQuery("SELECT file_hash, extension FROM files WHERE doc_id = " +
	 * docId, null);
	 * 
	 * final String[] filesHashes = new String[filesCursor.getCount()]; final
	 * String[] filesExtensions = new String[filesCursor.getCount()]; int i = 0;
	 * while (filesCursor.moveToNext()) { filesHashes[i] =
	 * filesCursor.getString(0); filesExtensions[i] = filesCursor.getString(1);
	 * i++; } filesCursor.close(); return res; }
	 */

	private void storeAuthors(SQLiteDatabase db) {

		ContentValues cv = new ContentValues();
		int authorId = -1;

		if (!getAuthors().isEmpty()) {
			for (MendeleyAuthor author : getAuthors()) {
				String authorSignature = DatabaseUtils.sqlEscapeString(author.getSignature());
				Cursor knownAuthor = db.rawQuery(
						"SELECT author_id, signature FROM authors WHERE signature LIKE "
								+ authorSignature, null);
				if (knownAuthor.getCount() != 0) {
					knownAuthor.moveToFirst();
					authorId = knownAuthor.getInt(0);
					db.execSQL("UPDATE authors SET size=size+1 WHERE author_id=" + authorId);
				} else {
					/*
					cv.clear();
					cv.put("first_name", author.getForename());
					cv.put("last_name", author.getSurname());
					cv.put("signature", );
					cv.put("size", 1);				
					authorId = (int) db.insert("authors", null, cv);
					*/
					authorId = author.insertIntoDB(db);
				}
				knownAuthor.close();
				if (authorId != -1) {
					cv.clear();
					cv.put("doc_id", id);
					cv.put("author_id", authorId);
					db.insert("authored", null, cv);
				} else {
					System.out.println("Inserting author failed...");
				}
			}
		}
	}

	/**
	 * Helper method for storing tags of this document into the given DB
	 * @param db
	 */
	private void storeTags(SQLiteDatabase db) {

		ContentValues cv = new ContentValues();
		int tagId = -1;

		if (!getTags().isEmpty()) {
			for (String tag : getTags()) {
				String escapedTag = DatabaseUtils.sqlEscapeString(tag);
				Cursor knownTag = db.rawQuery("SELECT tag_id, size FROM tags WHERE tag="
						+ escapedTag, null);
				if (knownTag.getCount() != 0) {
					knownTag.moveToFirst();
					tagId = knownTag.getInt(0);
					db.execSQL("UPDATE tags SET size=size+1 WHERE tag_id=" + tagId);
				} else {
					cv.clear();
					cv.put("tag", tag);
					cv.put("size", 1);
					tagId = (int) db.insert("tags", null, cv);
				}
				knownTag.close();
				if (tagId != -1) {
					cv.clear();
					cv.put("doc_id", id);
					cv.put("tag_id", tagId);
					db.insert("documents_tags", null, cv);
				} else {
					System.out.println("Inserting tag failed...");
				}
			}
		}
	}

	/**
	 * Helper method for storing documents
	 * @param db
	 */
	private void storeOutlet(SQLiteDatabase db) {
		int outletId = -1;
		ContentValues cv = new ContentValues();

		if (outlet != null) {
			String escapedOutlet = DatabaseUtils.sqlEscapeString(outlet);
			Cursor knownOutlet = db.rawQuery("SELECT outlet_id FROM outlets WHERE name="
					+ escapedOutlet, null);
			if (knownOutlet.getCount() != 0) {
				knownOutlet.moveToFirst();
				outletId = knownOutlet.getInt(0);
				db.execSQL("UPDATE outlets SET size=size+1 WHERE outlet_id=" + outletId);
			} else {
				cv.clear();
				cv.put("name", outlet);
				cv.put("size", 1);
				outletId = (int) db.insert("outlets", null, cv);
			}
			knownOutlet.close();
			if (outletId != -1) {
				cv.clear();
				cv.put("doc_id", id);
				cv.put("outlet_id", outletId);
				db.insert("documents_outlets", null, cv);
			} else {
				System.out.println("Inserting outlet failed...");
			}
		}
	}

	private void storeFiles(SQLiteDatabase db, boolean isGroupFile) {
		for (MendeleyFile f : files) {
			f.insertIntoDB(db, isGroupFile);
		}
	}

	public HashMap<String, String> toHashMap() {
		HashMap<String, String> res = new HashMap<String, String>();
		res.put("uuid", uuid);
		res.put("title", title);
		res.put("authors", authorsString);
		res.put("year", String.valueOf(year));
		res.put("abstract", docAbstract);
		res.put("mendeley_url", mendeleyUrl);
		return res;
	}

	/*
	 * public String getAuthorsAsString() { String res = ""; for (String s:
	 * authors) { res += s+" "; } return res; }
	 */

	public void setDocAbstract(String docAbstract) {
		this.docAbstract = docAbstract;
	}

	public String getDocAbstract() {
		return docAbstract;
	}

	public String getCanonicalId() {
		return canonicalId;
	}

	public void setCanonicalId(String canonicalId) {
		this.canonicalId = canonicalId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String toString() {
		return title;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}

	public String getPdf() {
		return pdf;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getDoi() {
		return doi;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setMendeleyUrl(String mendeleyUrl) {
		this.mendeleyUrl = mendeleyUrl;
	}

	public String getMendeleyUrl() {
		return mendeleyUrl;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return file;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getYear() {
		return year;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setAuthorsString(String authorsString) {
		this.authorsString = authorsString;
	}

	public String getAuthorsString() {
		return authorsString;
	}

	public List<MendeleyFile> getFiles() {
		return files;
	}

	public void setFiles(List<MendeleyFile> files) {
		this.files = files;
	}

	public List<MendeleyAuthor> getAuthors() {
		return authors;
	}

	public void setAuthors(List<MendeleyAuthor> authors) {
		this.authors = authors;
	}

	public String getOutlet() {
		return outlet;
	}

	public void setOutlet(String outlet) {
		this.outlet = outlet;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
}
