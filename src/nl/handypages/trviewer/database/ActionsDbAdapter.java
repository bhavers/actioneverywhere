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
import nl.handypages.trviewer.parser.TRAction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ActionsDbAdapter {

		// Database fields
		public static final String KEY_ID = "ID";
		public static final String KEY_CREATED = "CREATED";
		public static final String KEY_DESCRIPTION = "DESCRIPTION";
		public static final String KEY_TOPICINDEX = "TOPICINDEX";
		public static final String KEY_TOPICID = "TOPICID";
		public static final String KEY_CONTEXTINDEX = "CONTEXTINDEX";
		public static final String KEY_CONTEXTID = "CONTEXTID";
		public static final String KEY_STATE = "STATE";
		public static final String KEY_NOTES = "NOTES";
		public static final String KEY_DONE = "DONE";
		public static final String KEY_MODIFIED = "MODIFIED";
		public static final String KEY_DUEDATE = "DUEDATE";
		public static final String KEY_SCHEDULEDDATE = "SCHEDULEDDATE";
		public static final String KEY_SCHEDULEDRECURRING = "SCHEDULEDRECURRING";
		public static final String KEY_DELEGATEDTO = "DELEGATEDTO";
		public static final String KEY_CHASEDATE = "CHASEDATE";
		public static final String KEY_PROJECTID = "PROJECTID";
		
		public static final String ACTION_TRUE = "1";
		public static final String ACTION_FALSE = "0";
		
		private static final String DATABASE_TABLE = "actions";
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public ActionsDbAdapter(Context context) {
			this.context = context;
		}

		public ActionsDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		/**
		 * Create a new action. 
		 */
		/*public long create(String id, String created, String description, String topicIndex, String topicId, String contextIndex, String contextId,
				String state, String notes, String done, String modified, String dueDate, String scheduledDate, String scheduledRecurring,
				String delegatedTo, String chaseDate) {
			ContentValues initialValues = createContentValues(id, created, description, topicIndex, topicId, contextIndex, contextId,
					state, notes, done, modified, dueDate, scheduledDate, scheduledRecurring, delegatedTo, chaseDate);

			return database.insert(DATABASE_TABLE, null, initialValues);
		}*/
		/**
		 * Create a new actionlist from an TRActionList object If the action list is successfully created return the new
		 * listId for that note, otherwise return a -1 to indicate failure.
		 */
		
		public long create(TRAction action) {
			
			ContentValues initialValues = createContentValues(
					action.getId(), 
					((action.getCreatedAsLong() == null) ? "":action.getCreatedAsLong().toString()),
					action.getDescription(), 
					action.getTopicIndex(), 
					action.getTopicId(), 
					action.getContextIndex(),
					action.getContextId(), 
					action.getState(), 
					action.getNotes(), 
					action.getDoneAsStr(), 
					Long.toString(action.getModifiedAsLong()), 
					((action.getDueDateAsLong() == null) ? "":action.getDueDateAsLong().toString()), 
					((action.getScheduledDateAsLong() == null) ? "":action.getScheduledDateAsLong().toString()),
					action.getScheduledRecurringAsStr(), 
					action.getDelegatedTo(), 
					((action.getChaseDateAsLong() == null) ? "":action.getChaseDateAsLong().toString()),
					action.getProjectId());
			
			return database.insert(DATABASE_TABLE, null, initialValues);
		}

		/**
		 * Deletes ActionList
		 */
		public boolean delete(String id) {
			return database.delete(DATABASE_TABLE, KEY_ID + "='" + id + "'", null) > 0;
		}


		public boolean deleteAll() {
			return database.delete(DATABASE_TABLE, null, null) > 0;
		}
		/**
		 * Return a Cursor over the list of all actions in the database
		 * 
		 * @return Cursor over all notes
		 */
		public Cursor fetchAll() {
			return database.query(DATABASE_TABLE, new String[] { KEY_ID, 
					KEY_CREATED, KEY_DESCRIPTION, KEY_TOPICINDEX, KEY_TOPICID, KEY_CONTEXTINDEX, KEY_CONTEXTID,
					KEY_STATE, KEY_NOTES, KEY_DONE, KEY_MODIFIED, KEY_DUEDATE, KEY_SCHEDULEDDATE, KEY_SCHEDULEDRECURRING,
					KEY_DELEGATEDTO, KEY_CHASEDATE, KEY_PROJECTID}, null, null, null,
					null, null);
		}

		
		/**
		 * Return a Cursor positioned at the defined Action
		 */
		public Cursor fetchAction(String id) throws SQLException {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_ID},
					KEY_ID + "='" + id + "'", null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}

		private ContentValues createContentValues(String id, String created, String description, String topicIndex, String topicId, String contextIndex, String contextId,
				String state, String notes, String done, String modified, String dueDate, String scheduledDate, String scheduledRecurring,
				String delegatedTo, String chaseDate, String projectId) {
			ContentValues values = new ContentValues();
			if (id != null) {
				values.put(KEY_ID, id);
			}
			if (created != null) {
				values.put(KEY_CREATED, created);
			}
			if (description != null) {
				values.put(KEY_DESCRIPTION, description);
			}
			if (topicIndex != null) {
				values.put(KEY_TOPICINDEX, topicIndex);
			}
			if (topicId!= null) {
				values.put(KEY_TOPICID, topicId);
			}
			if (contextIndex != null) {
				values.put(KEY_CONTEXTINDEX, contextIndex);
			}
			if (contextId!= null) {
				values.put(KEY_CONTEXTID, contextId);
			}
			if (state != null) {
				values.put(KEY_STATE, state);
			}
			if (notes!= null) {
				values.put(KEY_NOTES, notes);
			}
			if (done != null) {
				values.put(KEY_DONE, done);
			}
			if (modified != null) {
				values.put(KEY_MODIFIED, modified);
			}
			if (dueDate != null) {
				values.put(KEY_DUEDATE, dueDate);
			}
			if (scheduledDate != null) {
				values.put(KEY_SCHEDULEDDATE, scheduledDate);
			}
			if (scheduledRecurring != null) {
				values.put(KEY_SCHEDULEDRECURRING, scheduledRecurring);
			}
			if (delegatedTo != null) {
				values.put(KEY_DELEGATEDTO, delegatedTo);
			}
			if (chaseDate != null) {
				values.put(KEY_CHASEDATE, chaseDate);
			}
			if (projectId != null) {
				values.put(KEY_PROJECTID, projectId);
			}
			return values;
		}
		
	}
