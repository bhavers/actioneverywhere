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
import nl.handypages.trviewer.parser.TRActor;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ActorsDbAdapter {

		// Database fields
		public static final String KEY_ID = "ID";
		public static final String KEY_NAME = "NAME";
		public static final String KEY_EMAIL = "EMAIL";
		public static final String KEY_INACTIVE = "INACTIVE";
		
		public static final String INACTIVE_TRUE = "1";
		public static final String INACTIVE_FALSE = "0";
		
		private static final String DATABASE_TABLE = "actors";
		private Context context;
		private SQLiteDatabase database;
		private GenericDatabaseHelper dbHelper;

		public ActorsDbAdapter(Context context) {
			this.context = context;
		}

		public ActorsDbAdapter open() throws SQLException {
			dbHelper = new GenericDatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
			database.close();
		}

		
		public long create(TRActor actor) {
			
			ContentValues initialValues = createContentValues(
					actor.getId(), 
					actor.getName(),
					actor.getEmail(), 
					((actor.getInactive()) ? INACTIVE_TRUE:INACTIVE_FALSE));
			
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
					KEY_NAME, KEY_EMAIL, KEY_INACTIVE}, null, null, null,
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

		private ContentValues createContentValues(String id, String name, String email, String inactive) {
			ContentValues values = new ContentValues();
			if (id != null) {
				values.put(KEY_ID, id);
			}
			if (name != null) {
				values.put(KEY_NAME, name);
			}
			if (email != null) {
				values.put(KEY_EMAIL, email);
			}
			if (inactive != null) {
				values.put(KEY_INACTIVE, inactive);
			}
			
			return values;
		}
		
	}
