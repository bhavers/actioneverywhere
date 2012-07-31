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
import nl.handypages.trviewer.parser.TRProject;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ProjectsDbAdapter {

		// Database fields
		public static final String KEY_ID = "ID";
		public static final String KEY_PARENTID = "PARENTID";
		public static final String KEY_DONE = "DONE";
		public static final String KEY_DESCRIPTION = "DESCRIPTION";
		public static final String KEY_TYPE = "TYPE";
		
		public static final String DONE_TRUE = "1";
		public static final String DONE_FALSE = "0";
		
		public static final String TYPE_CURRENT = "0";
		public static final String TYPE_FUTURE = "1";
		public static final String TYPE_UNDEFINED = "2";
		
		private static final String DATABASE_TABLE = "projects";
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public ProjectsDbAdapter(Context context) {
			this.context = context;
		}

		public ProjectsDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		
		public long create(TRProject project) {
						
			ContentValues initialValues = createContentValues(
					project.getId(),
					project.getParentId(), 
					((project.getDone()) ? DONE_TRUE:DONE_FALSE),
					project.getDescription(),
					project.getType());
			
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
		 * Return a Cursor over the list of all projects in the database
		 * 
		 * @return Cursor over all notes
		 */
		public Cursor fetchAll() {
			return database.query(DATABASE_TABLE, new String[] { KEY_ID, 
					KEY_PARENTID, KEY_DONE, KEY_DESCRIPTION, KEY_TYPE}, null, null, null,
					null, null);
		}

		
		/**
		 * Return a Cursor positioned at the defined Project
		 */
		public Cursor fetchProject(String id) throws SQLException {
			Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
					KEY_ID, KEY_PARENTID, KEY_DONE, KEY_DESCRIPTION, KEY_TYPE},
					KEY_ID + "='" + id + "'", null, null, null, null, null);
			if (mCursor != null) {
				if (!mCursor.moveToFirst()) {
					return null;
				}
			} else {
				return null;
			}
			return mCursor;
		}

		private ContentValues createContentValues(String id, String parentid, String done, String description, String type) {
			ContentValues values = new ContentValues();
			if (id != null) {
				values.put(KEY_ID, id);
			}
			if (parentid != null) {
				values.put(KEY_PARENTID, parentid);
			}
			if (done != null) {
				values.put(KEY_DONE, done);
			}
			if (description != null) {
				values.put(KEY_DESCRIPTION, description);
			}
			if (type != null) {
				values.put(KEY_TYPE, type);
			}
			
			return values;
		}
		
	}
