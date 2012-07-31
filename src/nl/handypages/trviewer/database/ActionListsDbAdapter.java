/*******************************************************************************
 * Copyright (c) 2012 Handypages.nl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.handypages.trviewer.database;
import nl.handypages.trviewer.parser.TRActionList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ActionListsDbAdapter {

		// Database fields
		public static final String KEY_LISTNAME = "LISTNAME";
		public static final String KEY_WEIGHT = "WEIGHT";
		public static final String KEY_CUSTOM = "CUSTOM";
		public static final String KEY_FILTERDONE = "FILTERDONE";
		private static final String DATABASE_TABLE = "actionlists";
		
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public ActionListsDbAdapter(Context context) {
			this.context = context;
		}

		public ActionListsDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		/**
		 * Create a new actionlist If the action list is successfully created return the new
		 * listId for that note, otherwise return a -1 to indicate failure.
		 */
		public long create(String listname, String weight, String custom, String filterDone) {
			ContentValues initialValues = createContentValues(listname, weight, custom, filterDone);

			return database.insert(DATABASE_TABLE, null, initialValues);
		}
		/**
		 * Create a new actionlist from an TRActionList object If the action list is successfully created return the new
		 * listId for that note, otherwise return a -1 to indicate failure.
		 */
		public long create(TRActionList list) {
			
			ContentValues initialValues = createContentValues(list.getName(), Integer.toString(list.getWeight()), 
					Integer.toString(list.getCustomInt()), list.getFilterDone());

			return database.insert(DATABASE_TABLE, null, initialValues);
		}

		/**
		 * Update the Actionlist
		 */
		public boolean update(String listname, String weight, String custom, String filterDone) {
			ContentValues updateValues = createContentValues(listname, weight, custom, filterDone);
			return database.update(DATABASE_TABLE, updateValues, KEY_LISTNAME + "='" + listname + "'", null) > 0;
		}

		/**
		 * Deletes ActionList
		 */
		public boolean delete(String listname) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "'", null) > 0;
		}
		/**
		 * Deletes all custom lists (false = non-custom (synced from TR app), true = custom lists)
		 * Returns true if list are deleted  
		 * Return false if lists are not deleted
		 */
		public boolean deleteAllCustom(boolean custom) {
			if (custom == true) {
				//Log.i(MainActivity.TAG,"Deleting lists where CUSTOM=1");
				return database.delete(DATABASE_TABLE, KEY_CUSTOM + "='1'", null) > 0;
			} else {
				//Log.i(MainActivity.TAG,"Deleting lists where CUSTOM='0'");
				return database.delete(DATABASE_TABLE, KEY_CUSTOM + "=0", null) > 0;
			}
			
		}
		/**
		 * Deletes all ActionLists
		 */
		public boolean deleteAll() {
			return database.delete(DATABASE_TABLE, null, null) > 0;
		}
		/**
		 * Return a Cursor over the list of all ActionLists in the database
		 * 
		 * @return Cursor over all notes
		 */
		public Cursor fetchAll() {
			return database.query(DATABASE_TABLE, new String[] { KEY_LISTNAME, 
					KEY_WEIGHT, KEY_CUSTOM, KEY_FILTERDONE}, null, null, null,
					null, KEY_WEIGHT + " ASC");
		}

		
		/**
		 * Return a Cursor positioned at the defined ActionList
		 */
		public Cursor fetchActionList(String listname) throws SQLException {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_LISTNAME, KEY_WEIGHT, KEY_CUSTOM, KEY_FILTERDONE},
					KEY_LISTNAME + "='" + listname + "'", null, null, null, KEY_WEIGHT + " ASC", null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}
		
		/**
		 * Return a Cursor for a list of all custom or non-custom action lists
		 */
		public Cursor fetchAllCustom(boolean custom) throws SQLException {
			String customStr = Integer.toString(TRActionList.CUSTOM_FALSE);
			if (custom == true) {
				customStr = Integer.toString(TRActionList.CUSTOM_TRUE);
			}
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_LISTNAME, KEY_WEIGHT, KEY_CUSTOM, KEY_FILTERDONE},
					KEY_CUSTOM + "='" + customStr + "'", null, null, null, KEY_WEIGHT + " ASC", null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}

		private ContentValues createContentValues(String listname, String weight, String custom, String filterDone) {
			ContentValues values = new ContentValues();
			if (listname != null) {
				values.put(KEY_LISTNAME, listname);
			}
			if (weight != null) {
				values.put(KEY_WEIGHT, weight);
			}
			if (custom != null) {
				values.put(KEY_CUSTOM, custom);
			}
			if (filterDone != null) {
				values.put(KEY_FILTERDONE, filterDone);
			}
			return values;
		}
		
	}
