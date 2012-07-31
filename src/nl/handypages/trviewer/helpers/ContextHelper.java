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
package nl.handypages.trviewer.helpers;

import java.util.ArrayList;

import nl.handypages.trviewer.database.ContextsDbAdapter;
import nl.handypages.trviewer.parser.TRContext;
import android.content.Context;
import android.database.Cursor;

public class ContextHelper {

	
	private Context context;
	private ContextsDbAdapter contextDbAdapter;

	/**
	 * This is a helper class to handle Actions, Contexts, Topics and Actors. Mainly reading and writing these 
	 * to the database. 
	 */
	
	public ContextHelper(Context context) {
		super();
		this.context = context;
		contextDbAdapter = new ContextsDbAdapter(this.context);
	}
	public boolean deleteAll() {
		contextDbAdapter.open();
		boolean result = contextDbAdapter.deleteAll();
		contextDbAdapter.close();
		return result;
	}
	
	public void create(ArrayList<TRContext> contexts) {
		contextDbAdapter.open();
		for (int i = 0; i < contexts.size(); i++) {
			TRContext context = contexts.get(i);
			contextDbAdapter.create(context);
		}
		contextDbAdapter.close();
	}

	public ArrayList<TRContext> getContexts() {
		
		contextDbAdapter.open();
		ArrayList<TRContext> contexts = new ArrayList<TRContext>(); 
		
		Cursor cur = contextDbAdapter.fetchAll();
		//startManagingCursor(cur); 
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			TRContext context = new TRContext();
			context.setId(cur.getString(cur.getColumnIndexOrThrow(ContextsDbAdapter.KEY_ID)));
			context.setName(cur.getString(cur.getColumnIndexOrThrow(ContextsDbAdapter.KEY_NAME)));
			context.setDescription(cur.getString(cur.getColumnIndexOrThrow(ContextsDbAdapter.KEY_DESCRIPTION)));
			if (!cur.getString(cur.getColumnIndexOrThrow(ContextsDbAdapter.KEY_CONTEXTINDEX)).equalsIgnoreCase("")) {
				context.setIndex(cur.getInt(cur.getColumnIndexOrThrow(ContextsDbAdapter.KEY_CONTEXTINDEX)));
			}
			
			
			contexts.add(context);
			cur.moveToNext();
		}
		//actions.trimToSize(); // not really needed.
		cur.close();
		contextDbAdapter.close();
		return contexts;
	}
}
