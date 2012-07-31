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

public class FilterActionDateDbAdapter {

		// Database fields
		public static final String KEY_LISTNAME = "LISTNAME";
		public static final String KEY_ACTION_DATE_ID = "ACTION_DATE_ID";
		public static final String KEY_ACTION_DATE_VALUE = "ACTION_DATE_VALUE";
		public static final String KEY_ACTION_DATE_TYPE = "ACTION_DATE_TYPE";
		private static final String DATABASE_TABLE = "filter_action_date";
		public static final String LIST_ALL_ID = "-999";
		public static final String ID_BEFORE = "0";
		public static final String ID_AFTER = "1";
		// The TYPE field indicates what the value field stands for.
		public static final String TYPE_DAYS = "0";
		public static final String TYPE_DATE = "1";
		public static final String TYPE_FROM_START_OF_NEXT_WEEK = "2";
		public static final String TYPE_FROM_START_OF_THIS_WEEK = "3";
		public static final String TYPE_FROM_START_OF_LAST_WEEK = "4";
		public static final String TYPE_TO_END_OF_LAST_WEEK = "5";
		public static final String TYPE_TO_END_OF_THIS_WEEK = "6";
		public static final String TYPE_TO_END_OF_NEXT_WEEK = "7";
		
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public FilterActionDateDbAdapter(Context context) {
			this.context = context;
		}

		public FilterActionDateDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		/**
		 * Insert a new "action date" filter for this list.
		 * 
		 * @param listname The name of the list to add this filter to
		 * @param beforeAfter possible values ID_BEFORE (action date to) and ID_AFTER (action date from)
		 * @param value meaning of this field depends on param type (number of days, specific date or special type).
		 * @param type possible values see TYPE_*. determins meaning of value.
		 * @return Row number at successful, otherwise -1 to indicate failure.
		 */
		
		public long createFilterActionDate(String listname, String beforeAfter, String value, String type) {
			ContentValues initialValues = createContentValues(listname, beforeAfter, value, type);

			return database.insert(DATABASE_TABLE, null, initialValues);
		}

		
		/**
		 * Deletes a "action date" filter from the list.
		 */
		public boolean deleteActionDateFilter(String listname, String actionDateId) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "' AND "+ KEY_ACTION_DATE_ID + "='" + actionDateId +"'", null) > 0;
		}
		/**
		 * Deletes all "action date" filters for this list
		 */
		public boolean deleteAllActionDateFilters(String listname) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "'", null) > 0;
		}
		/**
		 * Return a Cursor over the list of all "action date" filters in the database
		 * 
		 * @return Cursor over all status filters
		 */
		public Cursor fetchAllFilterActionDate(String listname) {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_LISTNAME, KEY_ACTION_DATE_ID, KEY_ACTION_DATE_VALUE},
					KEY_LISTNAME + "='" + listname + "'", null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}


		private ContentValues createContentValues(String listname, String beforeAfter, String value, String type) {
			ContentValues values = new ContentValues();
			values.put(KEY_LISTNAME, listname);
			values.put(KEY_ACTION_DATE_ID, beforeAfter);
			values.put(KEY_ACTION_DATE_VALUE, value);
			values.put(KEY_ACTION_DATE_TYPE, type);
			return values;
		}
	}
