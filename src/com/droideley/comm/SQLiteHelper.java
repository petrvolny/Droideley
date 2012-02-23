package com.droideley.comm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "MendeleyDB";
	private static final int DB_VERSION = 9;
	// public static final String TITLE = "title";
	// public static final String TITLE = "authors";
	// public static final String TITLE = "abstract";
	String sqlDocuments = "CREATE TABLE documents ("
			+ "doc_id INTEGER NOT NULL PRIMARY KEY,"
			+ "canonical_id STRING DEFAULT NULL," + "title TEXT NOT NULL ,"
			+ "uuid TEXT DEFAULT NULL," + "year INTEGER DEFAULT NULL,"
			+ "abstract TEXT DEFAULT NULL,"
			+ "authors_string TEXT DEFAULT NULL," + "doi TEXT DEFAULT NULL,"
			+ "isbn TEXT DEFAULT NULL," + "mendeley_url TEXT DEFAULT NULL,"
			+ "url TEXT DEFAULT NULL);";

	String sqlFolders = "CREATE TABLE folders ("
			+ "folder_id INTEGER NOT NULL PRIMARY KEY,"
			+ "name TEXT NOT NULL ," + "size INTEGER NOT NULL" + ") ";

	String sqlTags = "CREATE TABLE tags ("
			+ "tag_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
			+ "size INTEGER DEFAULT NULL," + "tag TEXT NOT NULL " + ") ";

	String sqlAuthors = "CREATE TABLE authors ("
			+ "author_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
			+ "first_name TEXT DEFAULT NULL," + "last_name TEXT NOT NULL ,"
			+ "signature TEXT DEFAULT NULL," + "size INTEGER DEFAULT NULL"
			+ ") ";

	String sqlOutlets = "CREATE TABLE outlets ("
			+ "outlet_id INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, "
			+ "name TEXT NOT NULL, size INTEGER DEFAULT NULL) ";

	String sqlFiles = "CREATE TABLE files ("
			+ "file_hash TEXT NOT NULL PRIMARY KEY,"
			+ "extension TEXT DEFAULT NULL," + "date_added TEXT DEFAULT NULL,"
			+ "size INTEGER DEFAULT NULL,"
			+ "doc_id INETEGER NOT NULL REFERENCES documents (doc_id))";

	// ============================================================================

	String sqlDocsOutlets = "CREATE TABLE documents_outlets ("
			+ "outlet_id INTEGER NOT NULL REFERENCES outlets (outlet_id),"
			+ "doc_id INTEGER NOT NULL REFERENCES documents (doc_id),"
			+ "PRIMARY KEY (outlet_id, doc_id)" + ");";

	String sqlAuthored = "CREATE TABLE authored ("
			+ "author_id INTEGER NOT NULL REFERENCES authors (author_id),"
			+ "doc_id INTEGER NOT NULL REFERENCES documents (doc_id),"
			+ "PRIMARY KEY (author_id, doc_id)" + ");";

	String sqlDocsTags = "CREATE TABLE documents_tags ("
			+ "doc_id INTEGER NOT NULL  REFERENCES documents (doc_id),"
			+ "tag_id INTEGER NOT NULL  REFERENCES tags (tag_id),"
			+ "PRIMARY KEY (doc_id, tag_id)" + ") ";

	String sqlKeys = "CREATE TABLE keys (" + "type STRING PRIMARY KEY,"
			+ "value STRING NOT NULL " + ") ";

	String sqlDocsFolders = "CREATE TABLE documents_folders ("
			+ "doc_id INTEGER NOT NULL REFERENCES documents (doc_id) ,"
			+ "folder_id INTEGER NOT NULL  REFERENCES folders (folder_id),"
			+ "PRIMARY KEY (doc_id, folder_id)" + ") ";

	String sqlContacts = "CREATE TABLE contacts ("
			+ "contact_id INTEGER NOT NULL  PRIMARY KEY,"
			+ "name TEXT DEFAULT NULL" + ") ";

	String sqlGroups = "CREATE TABLE groups ("
			+ "group_id INTEGER NOT NULL  PRIMARY KEY,"
			+ "name TEXT DEFAULT NULL," + "size INTEGER DEFAULT NULL,"
			+ "type TEXT DEFAULT NULL" + ");";

	String sqlGroupDocuments = "CREATE TABLE group_documents ("
			+ "doc_id INTEGER NOT NULL PRIMARY KEY,"
			+ "canonical_id STRING DEFAULT NULL," + "title TEXT NOT NULL ,"
			+ "uuid TEXT DEFAULT NULL," + "year INTEGER DEFAULT NULL,"
			+ "abstract TEXT DEFAULT NULL,"
			+ "authors_string TEXT DEFAULT NULL," + "pdf TEXT DEFAULT NULL,"
			+ "doi TEXT DEFAULT NULL," + "isbn TEXT DEFAULT NULL,"
			+ "mendeley_url TEXT DEFAULT NULL," + "url TEXT DEFAULT NULL,"
			+ "outlet TEXT DEFAULT NULL," + " authors TEXT DEFAULT NULL,"
			+ " group_id INTEGER NOT NULL REFERENCES groups (group_id)" + ");";

	String sqlGroupFiles = "CREATE TABLE group_files ("
			+ "file_hash TEXT NOT NULL PRIMARY KEY,"
			+ "extension TEXT DEFAULT NULL," + "date_added TEXT DEFAULT NULL,"
			+ "size INTEGER DEFAULT NULL,"
			+ "doc_id INETEGER NOT NULL REFERENCES group_documents (doc_id))";

	/*
	 * String sqlKeys =
	 * "CREATE TABLE keys(type STRING PRIMARY KEY, value STRING NOT NULL);";
	 * String sqlDocuments = "CREATE TABLE documents(doc_id INT PRIMARY KEY" +
	 * ", title TEXT NOT NULL, authors TEXT, year INT, abstract TEXT," +
	 * "coll_id INT, FOREIGN KEY(coll_id) REFERENCES collections(coll_id));";
	 * String sqlCollections =
	 * "CREATE TABLE collections(coll_id INT PRIMARY KEY, name TEXT NOT NULL, size INT);"
	 * ;
	 */

	public SQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("Before DB creation...");
		db.execSQL(sqlKeys);
		db.execSQL(sqlDocuments);
		db.execSQL(sqlFolders);
		db.execSQL(sqlAuthors);
		db.execSQL(sqlTags);
		db.execSQL(sqlFiles);
		db.execSQL(sqlOutlets);
		db.execSQL(sqlContacts);
		db.execSQL(sqlGroups);
		db.execSQL(sqlGroupDocuments);
		db.execSQL(sqlDocsFolders);
		db.execSQL(sqlDocsOutlets);
		db.execSQL(sqlAuthored);
		db.execSQL(sqlDocsTags);
		db.execSQL(sqlGroupFiles);

		System.out.println("After DB creation...");

		// db.execSQL(sqlKeys);
		// db.execSQL(sqlCollections);
		// db.execSQL(sqlDocuments);

		db.execSQL("INSERT INTO keys(type, value) VALUES('ACCESS_KEY', 'none');");
		db.execSQL("INSERT INTO keys(type, value) VALUES('ACCESS_SECRET', 'none');");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		/*
		 * System.out.println("Before DB upgrade..."); //db.execSQL(
		 * "ALTER TABLE documents ADD COLUMN canonical_id STRING DEFAULT NULL");
		 * db.execSQL("DROP TABLE IF EXISTS group_files");
		 * db.execSQL("DROP TABLE IF EXISTS group_documents");
		 * db.execSQL(sqlGroupDocuments); db.execSQL(sqlGroupFiles);
		 * //db.execSQL(
		 * "ALTER TABLE group_documents ADD COLUMN canonical_id STRING DEFAULT NULL"
		 * ); //db.execSQL(sqlGroupFiles);
		 * System.out.println("DB upgrade OK..."); //
		 * db.execSQL("ALTER TABLE outlets ADD size INTEGER DEFAULT NULL");
		 */

		db.execSQL("DROP TABLE IF EXISTS contacts");
		db.execSQL("DROP TABLE IF EXISTS documents_folders");
		db.execSQL("DROP TABLE IF EXISTS documents_tags");
		db.execSQL("DROP TABLE IF EXISTS documents_outlets");
		db.execSQL("DROP TABLE IF EXISTS authored");
		db.execSQL("DROP TABLE IF EXISTS folders");
		db.execSQL("DROP TABLE IF EXISTS files");
		db.execSQL("DROP TABLE IF EXISTS documents");
		db.execSQL("DROP TABLE IF EXISTS authors");
		db.execSQL("DROP TABLE IF EXISTS tags");
		db.execSQL("DROP TABLE IF EXISTS outlets");
		db.execSQL("DROP TABLE IF EXISTS group_files");
		db.execSQL("DROP TABLE IF EXISTS group_documents");
		db.execSQL("DROP TABLE IF EXISTS groups");

		db.execSQL(sqlDocuments);
		db.execSQL(sqlFolders);
		db.execSQL(sqlAuthors);
		db.execSQL(sqlTags);
		db.execSQL(sqlFiles);
		db.execSQL(sqlOutlets);
		db.execSQL(sqlContacts);
		db.execSQL(sqlGroups);
		db.execSQL(sqlGroupDocuments);
		db.execSQL(sqlDocsFolders);
		db.execSQL(sqlDocsOutlets);
		db.execSQL(sqlAuthored);
		db.execSQL(sqlDocsTags);
		db.execSQL(sqlGroupFiles);
	}

}
