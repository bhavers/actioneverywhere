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
import java.util.Date;

import nl.handypages.trviewer.database.ActionsDbAdapter;
import nl.handypages.trviewer.parser.TRAction;
import android.content.Context;
import android.database.Cursor;

public class ActionHelper {

	
	private Context context;
	private ActionsDbAdapter actionDbAdapter;

	/**
	 * This is a helper class to handle Actions, Contexts, Topics and Actors. Mainly reading and writing these 
	 * to the database. 
	 */
	
	public ActionHelper(Context context) {
		super();
		this.context = context;
		actionDbAdapter = new ActionsDbAdapter(this.context);
	}
	public boolean deleteAll() {
		actionDbAdapter.open();
		boolean result = actionDbAdapter.deleteAll();
		actionDbAdapter.close();
		return result;
	}
	
	public void create(ArrayList<TRAction> actions) {
		actionDbAdapter.open();
		for (int i = 0; i < actions.size(); i++) {
			TRAction action = actions.get(i);
			actionDbAdapter.create(action);
		}
		actionDbAdapter.close();
	}
	/**
	 * Returns an ArrayList with current action lists from the database.
	 * The parameter custom determines if custom lists (defined by the user) should be returned,
	 * or non-custom (from TR-app) lists.
	 * The ArrayList can directly be used in a ListView to enumerate the users action lists.
	 */
	public ArrayList<TRAction> getActions() {
		
		actionDbAdapter.open();
		ArrayList<TRAction> actions = new ArrayList<TRAction>(); 
		
		Cursor cur = actionDbAdapter.fetchAll();
		//startManagingCursor(cur); 
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			TRAction action = new TRAction();
			action.setId(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_ID)));
			if (!cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_CREATED)).equalsIgnoreCase("")) {
				action.setCreated(new Date(cur.getLong(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_CREATED))));
			}
			action.setDescription(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_DESCRIPTION)));
			action.setTopicIndex(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_TOPICINDEX)));
			action.setTopicId(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_TOPICID)));
			action.setContextIndex(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_CONTEXTINDEX)));
			action.setContextId(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_CONTEXTID)));
			action.setState(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_STATE)));
			action.setNotes(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_NOTES)));
			action.setDoneAsStr(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_DONE)));
			if (!cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_MODIFIED)).equalsIgnoreCase("")) {
				action.setModified(new Date(cur.getLong(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_MODIFIED))));
			}
			if (!cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_DUEDATE)).equalsIgnoreCase("")) {
				action.setDueDate(new Date(cur.getLong(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_DUEDATE))));
			}
			if (!cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_SCHEDULEDDATE)).equalsIgnoreCase("")) {
				action.setScheduledDate(new Date(cur.getLong(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_SCHEDULEDDATE))));
			}
			action.setScheduledRecurringAsStr(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_SCHEDULEDRECURRING)));
			action.setDelegatedTo(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_DELEGATEDTO)));
			if (!cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_CHASEDATE)).equalsIgnoreCase("")) {
				action.setChaseDate(new Date(cur.getLong(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_CHASEDATE))));	
			}
			action.setProjectId(cur.getString(cur.getColumnIndexOrThrow(ActionsDbAdapter.KEY_PROJECTID)));
			
			actions.add(action);
			cur.moveToNext();
		}
		//actions.trimToSize(); // not really needed.
		cur.close();
		actionDbAdapter.close();
		return actions;
	}
}
