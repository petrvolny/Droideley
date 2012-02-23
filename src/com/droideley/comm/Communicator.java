package com.droideley.comm;

import java.util.Arrays;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.droideley.data.MendeleyDocument;
import com.droideley.data.MendeleyFolder;
import com.droideley.data.MendeleyGroup;
import com.droideley.data.MendeleyProfile;
import com.droideley.tools.DroideleyTools;
import com.droideley.tools.FinishedThreads;

/**
 * 
 * @author Petr (severin) Volny
 * 
 */
public class Communicator {

	private final Context context;
	private final int itemsPerPage = 100;

	public Communicator(Context context) {
		this.context = context;
		// new SQLiteHelper(context);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public MendeleyProfile getProfile() {
		MendeleyProfile mp = new MendeleyProfile();
		try {
			JSONObject profileJSON = MendeleyAuthTools.processRequest(
					"http://www.mendeley.com/oapi/profiles/info/me/", true);
			Log.d("Mendeley Profile", profileJSON.toString(2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mp;
	}

	/**
	 * Makes a call to the Mendeley OAPI and returns all document ids in user's
	 * library
	 * 
	 * @return The String array of all library document ids
	 */
	public long[] getLibrary() {
		try {
			JSONObject libJSON = MendeleyAuthTools.processRequest(
					"http://www.mendeley.com/oapi/library/?items="
							+ itemsPerPage + "&", true);
			Log.d("Library", libJSON.toString(2));
			int totalPages = libJSON.getInt("total_pages");
			int totalResults = libJSON.getInt("total_results");
			int currPage = 0;
			long res[] = new long[totalResults];
			// Fecth all pages
			while (true) {
				JSONArray docIdArray = libJSON.getJSONArray("document_ids");
				for (int i = 0; i < docIdArray.length(); i++) {
					res[currPage * itemsPerPage + i] = docIdArray.getLong(i);
				}
				currPage++;
				// If there is at least one more page call the API and fetch it
				if (currPage < totalPages) {
					libJSON = MendeleyAuthTools.processRequest(
							"http://www.mendeley.com/oapi/library/?items="
									+ itemsPerPage + "&page=" + currPage + "&",
							true);
					// Log.d("Library", libJSON.toString(2));
				} else {
					break;
				}
			}
			return res;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public LinkedList<MendeleyDocument> getGroupDocuments(long groupId) {
		LinkedList<MendeleyDocument> docsList = new LinkedList<MendeleyDocument>();
		try {
			JSONObject groupDocsJSON = MendeleyAuthTools.processRequest(
					"http://www.mendeley.com/oapi/library/groups/" + groupId
							+ "/?items=" + itemsPerPage + "&", true);
			Log.d("GroupDocuments", groupDocsJSON.toString(2));
			int totalPages = groupDocsJSON.getInt("total_pages");
			int totalResults = groupDocsJSON.getInt("total_results");
			int currPage = 0;
			String ids[] = new String[totalResults];
			// Fecth all pages
			while (true) {
				JSONArray docIdArray = groupDocsJSON
						.getJSONArray("document_ids");
				for (int i = 0; i < docIdArray.length(); i++) {
					ids[currPage * itemsPerPage + i] = (String) (docIdArray
							.get(i));
				}
				currPage++;
				// If there is at least one more page call the API and fetch it
				if (currPage < totalPages) {
					groupDocsJSON = MendeleyAuthTools.processRequest(
							"http://www.mendeley.com/oapi/library/groups/"
									+ groupId + "/?items=" + itemsPerPage
									+ "&page=" + currPage + "&", true);
					// Log.d("GroupDocuments", groupDocsJSON.toString(2));
				} else {
					break;
				}
			}
			for (int i = 0; i < ids.length; i++) {
				JSONObject docJSON = MendeleyAuthTools.processRequest(
						"http://api.mendeley.com/oapi/library/groups/"
								+ groupId + "/" + ids[i] + "/", true);
				Log.d("JSON", docJSON.toString(2));
				MendeleyDocument tmp = DroideleyTools.documentFromJSON(docJSON);
				tmp.setGroupId(groupId);
				docsList.add(tmp);
			}

			return docsList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method calls the "User Library Folder Documents" MendeleyAPI method
	 * and returns the result in a form an array of document ids.
	 * 
	 * @param folderId
	 * @return
	 */
	public long[] getFolderDocumentsIds(String folderId) {
		try {
			JSONObject docsJSON = MendeleyAuthTools.processRequest(
					"http://www.mendeley.com/oapi/library/folders/" + folderId
							+ "/?items=" + itemsPerPage + "&", true);
			System.out.println(docsJSON.toString(2));
			// int itemsPerPage = docsJSON.getInt("items_per_page");
			int totalPages = docsJSON.getInt("total_pages");
			int totalResults = docsJSON.getInt("total_results");
			int currPage = 0;
			JSONArray docIdArray = docsJSON.getJSONArray("document_ids");
			// Log.d("Documents in folder",
			// docsJSON.getJSONArray("document_ids").toString(2));
			long res[] = new long[totalResults];

			while (true) {
				for (int i = 0; i < docIdArray.length(); i++) {
					res[currPage * itemsPerPage + i] = docIdArray.getLong(i);
					System.out.println(docIdArray.getLong(i));
				}
				currPage++;
				if (currPage < totalPages) {
					docsJSON = MendeleyAuthTools.processRequest(
							"http://www.mendeley.com/oapi/library/folders/"
									+ folderId + "/?items=" + itemsPerPage
									+ "&page=" + currPage + "&", true);
					docIdArray = docsJSON.getJSONArray("document_ids");
				} else {
					break;
				}
			}
			return res;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static LinkedList<MendeleyDocument> getDocuments(final long[] ids) {
		final FinishedThreads finishedThread = new FinishedThreads();
		final LinkedList<MendeleyDocument> res = new LinkedList<MendeleyDocument>();

		if (ids.length > 15) {
			// if (false) {
			final LinkedList<MendeleyDocument> res1 = new LinkedList<MendeleyDocument>();
			final LinkedList<MendeleyDocument> res2 = new LinkedList<MendeleyDocument>();
			final LinkedList<MendeleyDocument> res3 = new LinkedList<MendeleyDocument>();
			final LinkedList<MendeleyDocument> res4 = new LinkedList<MendeleyDocument>();
			final LinkedList<MendeleyDocument> res5 = new LinkedList<MendeleyDocument>();

			final int idsPerThread = ids.length / 5;

			new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < idsPerThread; i++) {
						// Log.d("THREADS", "Thread 1");
						res1.add(getDocument(ids[i]));
					}
					finishedThread.addCount();
				}
			}).start();
			new Thread(new Runnable() {
				public void run() {
					for (int i = idsPerThread; i < 2 * idsPerThread; i++) {
						// Log.d("THREADS", "Thread 2");
						res2.add(getDocument(ids[i]));
					}
					finishedThread.addCount();
				}
			}).start();
			new Thread(new Runnable() {
				public void run() {
					for (int i = 2 * idsPerThread; i < 3 * idsPerThread; i++) {
						// Log.d("THREADS", "Thread 3");
						res3.add(getDocument(ids[i]));
					}
					finishedThread.addCount();
				}
			}).start();
			new Thread(new Runnable() {
				public void run() {
					for (int i = 3 * idsPerThread; i < 4 * idsPerThread; i++) {
						// Log.d("THREADS", "Thread 3");
						res4.add(getDocument(ids[i]));
					}
					finishedThread.addCount();
				}
			}).start();
			new Thread(new Runnable() {
				public void run() {
					for (int i = 4 * idsPerThread; i < ids.length; i++) {
						// Log.d("THREADS", "Thread 4");
						res5.add(getDocument(ids[i]));
					}
					finishedThread.addCount();
				}
			}).start();

			// Wait for all threads to finish their jobs
			while (finishedThread.getCount() != 5) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			res.addAll(res1);
			res.addAll(res2);
			res.addAll(res3);
			res.addAll(res4);
			res.addAll(res5);
		} else {
			for (int i = 0; i < ids.length; i++) {
				res.add(getDocument(ids[i]));
			}
		}

		return res;
	}

	/**
	 * Fetches the document details through the MendeleyAPI and stores it in a
	 * local MendeleyObject class
	 * 
	 * @param id
	 *            Id of the mendeley document to be fetched
	 * @return MendeleyObject with stored details
	 */
	public static MendeleyDocument getDocument(long id) {
		MendeleyDocument res = null;

		try {
			// Parsing the returned JSON object
			JSONObject docJSON = MendeleyAuthTools.processRequest(
					"http://www.mendeley.com/oapi/library/documents/" + id
							+ "/", true);
			System.out.println(docJSON.toString(2));

			res = DroideleyTools.documentFromJSON(docJSON);
			res.setId(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * This method fetches all user's folders through MendeleyAPI
	 * 
	 * @return List of @see MendeleyFolder objects
	 */
	public static LinkedList<MendeleyFolder> getFolders() {
		LinkedList<MendeleyFolder> res = new LinkedList<MendeleyFolder>();
		try {
			JSONArray docJSON = MendeleyAuthTools
					.processRequestToArray("http://www.mendeley.com/oapi/library/folders/");
			for (int i = 0; i < docJSON.length(); i++) {
				JSONObject colJSON = new JSONObject();
				colJSON = docJSON.getJSONObject(i);
				MendeleyFolder folder = new MendeleyFolder();
				folder.setId(colJSON.getString("id"));
				folder.setName(colJSON.getString("name"));
				folder.setSize(colJSON.getInt("size"));
				res.add(folder);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return res;
	}

	/**
	 * Searches Mendeley Web for the given keyword or keyphrase
	 * 
	 * @param term
	 * @return
	 */
	public static LinkedList<MendeleyDocument> searchMendeley(String term) {
		LinkedList<MendeleyDocument> resList = new LinkedList<MendeleyDocument>();
		term = term.replace(" ", "%20").replace(":", "%3A");
		try {
			JSONObject docsJSON = MendeleyAuthTools.processRequest(
					"http://api.mendeley.com/oapi/documents/search/" + term
							+ "/", false);
			JSONArray docDetailsArray = docsJSON.getJSONArray("documents");
			for (int i = 0; i < docDetailsArray.length(); i++) {
				JSONObject docJSON = docDetailsArray.getJSONObject(i);
				System.out.println(docJSON.toString(2));
				resList.add(DroideleyTools.documentFromJSON(docJSON));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resList;
	}

	/**
	 * Retrn list of related documents to the one which canonical id is given as an argument 
	 * @param canonicalId
	 * @return list of related (@see MendeleyDocument)
	 */
	public static LinkedList<MendeleyDocument> findRelated(String canonicalId) {
		LinkedList<MendeleyDocument> resList = new LinkedList<MendeleyDocument>();
		try {
			JSONObject relatedObj = MendeleyAuthTools
					.processRequest(
							"http://http://api.mendeley.com/oapi/documents/related/"+canonicalId+"/",
							false);
			JSONArray relatedDocs = relatedObj.getJSONArray("documents");
			for (int i = 0; i<relatedDocs.length(); i++) {
				resList.add(DroideleyTools.documentFromJSON(relatedDocs.getJSONObject(i)));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resList;
	}

	/**
	 * Returns List of (@see MendeleyGroup)
	 * @return
	 */
	public static LinkedList<MendeleyGroup> getGroups() {
		LinkedList<MendeleyGroup> resList = new LinkedList<MendeleyGroup>();
		JSONObject groupJSON;
		try {
			JSONArray groupsArrayJSON = MendeleyAuthTools
					.processRequestToArray("http://api.mendeley.com/oapi/library/groups/");
			for (int i = 0; i < groupsArrayJSON.length(); i++) {
				groupJSON = groupsArrayJSON.getJSONObject(i);
				Log.d("JSON", groupJSON.toString(2));
				resList.add(DroideleyTools.groupFromJSON(groupJSON));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resList;
	}

	/**
	 * Syncs only new files that were added to the user's Mendeley Library. It
	 * fetches all ids form the server and finds those that are missing in the
	 * local library and only these are fetched.
	 */
	public void syncNew() {
		long[] serverIds = getLibrary();
		LinkedList<MendeleyDocument> newDocs = new LinkedList<MendeleyDocument>();
		SQLiteDatabase db = new SQLiteHelper(context).getWritableDatabase();
		Cursor localIdsCursor = db.rawQuery("SELECT doc_id FROM documents",
				null);
		long[] localIds = new long[localIdsCursor.getCount()];
		int i = 0;
		while (localIdsCursor.moveToNext()) {
			localIds[i] = localIdsCursor.getLong(0);
			i++;
		}

		Arrays.sort(localIds);
		for (i = 0; i < serverIds.length; i++) {
			if (Arrays.binarySearch(localIds, serverIds[i]) < 0) {
				Log.d("New to sync", "DOC_ID: " + serverIds[i]);
				newDocs.add(getDocument(serverIds[i]));
			}
		}

		LinkedList<MendeleyFolder> folders = getFolders();

		db.beginTransaction();
		try {
			db.execSQL("DELETE FROM documents_folders");
			db.execSQL("DELETE FROM folders");
			// Store documents and their relations (follow insertIntoDB(db));
			for (MendeleyDocument m : newDocs) {
				m.insertIntoDB(db);
			}
			// Store folders and their relations with documents
			ContentValues cv = new ContentValues();
			for (MendeleyFolder f : folders) {
				f.insertIntoDB(db);
				long[] folderDocs = getFolderDocumentsIds(f.getId());
				for (i = 0; i < folderDocs.length; i++) {
					cv.clear();
					cv.put("folder_id", f.getId());
					cv.put("doc_id", folderDocs[i]);
					db.insert("documents_folders", null, cv);
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Syncs all documents and folders with the user's mendeley account
	 * 
	 * @param handler
	 */
	public void syncAll() {

		ContentValues cv = new ContentValues();
		long[] documentIds = getLibrary();
		LinkedList<MendeleyDocument> documents = Communicator
				.getDocuments(documentIds);
		LinkedList<MendeleyFolder> folders = getFolders();
		LinkedList<MendeleyGroup> groups = getGroups();
		SQLiteDatabase db = new SQLiteHelper(context).getWritableDatabase();

		// Everything happens in transaction
		db.beginTransaction();
		try {
			// Delete previous data
			db.execSQL("DELETE FROM documents_folders");
			db.execSQL("DELETE FROM authored");
			db.execSQL("DELETE FROM documents_tags");
			db.execSQL("DELETE FROM documents_outlets");
			db.execSQL("DELETE FROM files");
			db.execSQL("DELETE FROM documents");
			db.execSQL("DELETE FROM folders");
			db.execSQL("DELETE FROM authors");
			db.execSQL("DELETE FROM tags");
			db.execSQL("DELETE FROM outlets");
			db.execSQL("DELETE FROM group_documents");
			db.execSQL("DELETE FROM groups");
			db.execSQL("DELETE FROM contacts");
			// Store documents and their relations (follow insertIntoDB(db));
			for (MendeleyDocument m : documents) {
				m.insertIntoDB(db);
			}
			// Store folders and their relations with documents
			for (MendeleyFolder f : folders) {
				f.insertIntoDB(db);
				long[] folderDocs = getFolderDocumentsIds(f.getId());
				for (int i = 0; i < folderDocs.length; i++) {
					cv.clear();
					cv.put("folder_id", f.getId());
					cv.put("doc_id", folderDocs[i]);
					db.insert("documents_folders", null, cv);
				}
			}
			// Store groups
			for (MendeleyGroup g : groups) {
				g.insertInotDB(db);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Synchronizes groups (not their documents)
	 */
	public void syncGroups() {
		LinkedList<MendeleyGroup> groups = getGroups();

		SQLiteDatabase db = new SQLiteHelper(context).getWritableDatabase();
		db.beginTransaction();

		db.execSQL("DELETE FROM group_documents");
		db.execSQL("DELETE FROM groups");
		for (MendeleyGroup g : groups) {
			g.insertInotDB(db);
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	/**
	 * Synchronizes documents within a given group
	 * 
	 * @param groupId
	 *            Id of the group to be synchronized
	 */
	public void syncGroupDocuments(long groupId) {
		LinkedList<MendeleyDocument> groupDocs = getGroupDocuments(groupId);

		SQLiteDatabase db = new SQLiteHelper(context).getWritableDatabase();
		db.beginTransaction();

		try {
			db.execSQL("DELETE FROM group_files WHERE doc_id IN (SELECT doc_id FROM group_documents WHERE group_id="
					+ groupId + ")");
			db.execSQL("DELETE FROM group_documents WHERE group_id=" + groupId);
			for (MendeleyDocument d : groupDocs) {
				d.insertIntoDB(db);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public void downloadPdfFiles() {

		SQLiteDatabase db = new SQLiteHelper(context).getReadableDatabase();
		SQLiteCursor filesCursor = (SQLiteCursor) db
				.rawQuery(
						"SELECT file_hash, doc_id, extension, date_added FROM files WHERE extension='pdf'",
						null);
		String[] hashes = new String[filesCursor.getCount()];
		int i = 0;
		while (filesCursor.moveToNext()) {
			hashes[i] = filesCursor.getString(0);
			i++;
		}

	}

}
