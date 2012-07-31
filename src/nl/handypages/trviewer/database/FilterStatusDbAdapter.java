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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FilterStatusDbAdapter {

		// Database fields
		public static final String KEY_LISTNAME = "LISTNAME";
		public static final String KEY_STATUS_ID = "STATUS_ID";
		private static final String DATABASE_TABLE = "filter_status";
		public static final String LIST_ALL_ID = "-999";
		
		/**
		 * The numbers map to the array index of R.string.action_status
		 */
		public static int stateActionASAP = 1;
		public static int stateActionStateInactive = 2;
		public static int stateActionScheduled = 3;
		public static int stateActionDelegated = 4;
		
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public FilterStatusDbAdapter(Context context) {
			this.context = context;
		}

		public FilterStatusDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		/**
		 * Insert a new status filter for this list.
		 * 
		 * @return Row number at successful, otherwise -1 to indicate failure.
		 */
		public long createFilterStatus(String listname, String statusId) {
			ContentValues initialValues = createContentValues(listname, statusId);

			return database.insert(DATABASE_TABLE, null, initialValues);
		}

		
		/**
		 * Deletes a status filter from the list.
		 */
		public boolean deleteStatusFilter(String listname, String statusId) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "' AND "+ KEY_STATUS_ID + "='" + statusId +"'", null) > 0;
		}
		/**
		 * Deletes all status filters for this list
		 */
		public boolean deleteAllStatusFilters(String listname) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "'", null) > 0;
		}
		/**
		 * Return a Cursor over the list of all status filters in the database
		 * 
		 * @return Cursor over all status filters
		 */
		public Cursor fetchAllFilterStatus(String listname) {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_LISTNAME, KEY_STATUS_ID},
					KEY_LISTNAME + "='" + listname + "'", null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}


		private ContentValues createContentValues(String listname, String statusId) {
			ContentValues values = new ContentValues();
			values.put(KEY_LISTNAME, listname);
			values.put(KEY_STATUS_ID, statusId);
			return values;
		}
	}
