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

public class FilterTopicDbAdapter {

		// Database fields
		public static final String KEY_LISTNAME = "LISTNAME";
		public static final String KEY_TOPIC_ID = "TOPIC_ID";
		private static final String DATABASE_TABLE = "filter_topic";
		public static final String LIST_ALL_ID = "-999";
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public FilterTopicDbAdapter(Context context) {
			this.context = context;
		}

		public FilterTopicDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		/**
		 * Insert a new topic filter for this list.
		 * 
		 * @return Row number at successful, otherwise -1 to indicate failure.
		 */
		public long createFilterTopic(String listname, String topicId) {
			ContentValues initialValues = createContentValues(listname, topicId);

			return database.insert(DATABASE_TABLE, null, initialValues);
		}

		
		/**
		 * Deletes a topic filter from the list.
		 */
		public boolean deleteTopicFilter(String listname, String topicId) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "' AND "+ KEY_TOPIC_ID + "='" + topicId +"'", null) > 0;
		}
		/**
		 * Deletes all topic filters for this list
		 */
		public boolean deleteAllTopicFilters(String listname) {
			return database.delete(DATABASE_TABLE, KEY_LISTNAME + "='" + listname + "'", null) > 0;
		}
		/**
		 * Return a Cursor over the list of all topic filters in the database
		 * 
		 * @return Cursor over all topic filters
		 */
		public Cursor fetchAllFilterTopics(String listname) {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_LISTNAME, KEY_TOPIC_ID},
					KEY_LISTNAME + "='" + listname + "'", null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}


		private ContentValues createContentValues(String listname, String topicId) {
			ContentValues values = new ContentValues();
			values.put(KEY_LISTNAME, listname);
			values.put(KEY_TOPIC_ID, topicId);
			return values;
		}
	}
