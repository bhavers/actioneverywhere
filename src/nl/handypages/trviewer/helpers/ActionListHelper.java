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

import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.database.ActionListsDbAdapter;
import nl.handypages.trviewer.database.FilterActionDateDbAdapter;
import nl.handypages.trviewer.database.FilterContextDbAdapter;
import nl.handypages.trviewer.database.FilterStatusDbAdapter;
import nl.handypages.trviewer.database.FilterTopicDbAdapter;
import nl.handypages.trviewer.parser.TRActionList;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ActionListHelper {

	private ActionListsDbAdapter actionListDbAdapter;
	private Context context;

	
	/**
	 * This is a helper class to handle ActionLists. It is intended to be used by Activities and it will use
	 * the dbadapters to save and retrieve information about actionlists in/from the database.
	 */
	public ActionListHelper(Context context) {
		super();
		this.context = context;
		actionListDbAdapter = new ActionListsDbAdapter(this.context);
	}

	/**
	 * Returns an ArrayList with current action lists from the database.
	 * The parameter custom determines if custom lists (defined by the user) should be returned,
	 * or non-custom (from TR-app) lists.
	 * The ArrayList can directly be used in a ListView to enumerate the users action lists.
	 */
	public ArrayList<TRActionList> getActionListCustom(boolean custom) {
		
		actionListDbAdapter.open();
		ArrayList<TRActionList> actionLists = new ArrayList<TRActionList>(); // empty, will be build in refreshLists()
		
		Cursor cur = actionListDbAdapter.fetchAllCustom(custom);
		//startManagingCursor(cur); 
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			TRActionList actionList = new TRActionList();
			//Log.i(TAG,"List: " + cur.getString(cur.getColumnIndexOrThrow(ActionListsDbAdapter.KEY_LISTNAME))); // List all lists that are retrieved.
			//actionList.add(cur.getString(cur.getColumnIndexOrThrow(ActionListsDbAdapter.KEY_LISTNAME)));
			actionList.setName(cur.getString(cur.getColumnIndexOrThrow(ActionListsDbAdapter.KEY_LISTNAME)));
			actionList.setWeightAsStr(cur.getString(cur.getColumnIndexOrThrow(ActionListsDbAdapter.KEY_WEIGHT)));
			actionList.setCustomAsInt(cur.getInt(cur.getColumnIndexOrThrow(ActionListsDbAdapter.KEY_CUSTOM)));
			actionList.setFilterDone(cur.getString(cur.getColumnIndexOrThrow(ActionListsDbAdapter.KEY_FILTERDONE)));
			
			actionLists.add(actionList);
			cur.moveToNext();
		}
		actionLists.trimToSize(); // not really needed.
		cur.close();
		actionListDbAdapter.close();
		return actionLists;
	}
	
	public void close() {
	    actionListDbAdapter.close();
	}

	public boolean deleteAllCustom(boolean custom) {
		actionListDbAdapter.open();
		boolean result = actionListDbAdapter.deleteAllCustom(custom);
		actionListDbAdapter.close();
		return result;
	}
	/*public long create(TRActionList list) {
		actionListDbAdapter.open();
		long result = actionListDbAdapter.create(list);
		actionListDbAdapter.close();
		return result;
	}*/
	public void create(ArrayList<TRActionList> lists) {
		actionListDbAdapter.open();
		for (int i = 0; i < lists.size(); i++) {
			// Create the list in the database
			TRActionList list = lists.get(i);
			actionListDbAdapter.create(list);
			// Create the filters for this list in the database.
			FilterHelper fHelper = new FilterHelper(context, list.getName());
			for (int j = 0; j < list.getFilterTopicIds().size(); j++) {
				fHelper.createTopicById(list.getFilterTopicIds().get(j));
			}
			if (list.getFilterTopicIds().size() == 0) {
				fHelper.createTopicById(FilterTopicDbAdapter.LIST_ALL_ID);
			}
			
			for (int k = 0; k < list.getFilterContextIds().size(); k++) {
				fHelper.createContextById(list.getFilterContextIds().get(k));
			}
			if (list.getFilterContextIds().size() == 0) {
				fHelper.createContextById(FilterContextDbAdapter.LIST_ALL_ID);
			}
			
			for (int l = 0; l < list.getFilterStatesIds().size(); l++) {
				fHelper.createActionStateById(list.getFilterStatesIds().get(l));
			}
			if (list.getFilterStatesIds().size() == 0) {
				fHelper.createActionState(FilterStatusDbAdapter.stateActionASAP);
				fHelper.createActionState(FilterStatusDbAdapter.stateActionStateInactive);
				fHelper.createActionState(FilterStatusDbAdapter.stateActionScheduled);
				fHelper.createActionState(FilterStatusDbAdapter.stateActionDelegated);
			}
			/**
			 * Only support 'Days from now' filter, not the special filters like 'end of next week'. 
			 */
			if (list.getFilterActionFromType() == TRActionList.FILTER_FROMTO_DAYS) {
				fHelper.createActionDate(FilterActionDateDbAdapter.ID_AFTER, list.getFilterActionFromValueStr(), FilterActionDateDbAdapter.TYPE_DAYS);
			} else {
				fHelper.createActionDate(FilterActionDateDbAdapter.ID_AFTER, FilterActionDateDbAdapter.LIST_ALL_ID, FilterActionDateDbAdapter.TYPE_DAYS);
			}
			if (list.getFilterActionToType() == TRActionList.FILTER_FROMTO_DAYS) {
				fHelper.createActionDate(FilterActionDateDbAdapter.ID_BEFORE, list.getFilterActionToValueStr(), FilterActionDateDbAdapter.TYPE_DAYS);
			} else {
				fHelper.createActionDate(FilterActionDateDbAdapter.ID_BEFORE, FilterActionDateDbAdapter.LIST_ALL_ID, FilterActionDateDbAdapter.TYPE_DAYS);
			}
		}
		actionListDbAdapter.close();
	}
	
	public void removeList(String listname) {
	     /** 
	      * See this post on how to implement handling multiple selected entries:
	      * http://bestsiteinthemultiverse.com/2009/12/android-selected-state-listview-example/
	      * This method is defined in edit_list.xml
	      */
		actionListDbAdapter.open();
		boolean delResult = actionListDbAdapter.delete(listname);
	    if (delResult == false) {
	    	Log.e(MainActivity.TAG, "Problem deleting action lists from database");
	    } 
	    actionListDbAdapter.close();
	    /*
	     * Change the order of all list to be all in sequence again.
	     */
	    ArrayList<TRActionList> customLists = getActionListCustom(true); 
	    
	    actionListDbAdapter.open();
	    delResult = actionListDbAdapter.deleteAllCustom(true);
	    if (delResult == false) {
	    	Log.e(MainActivity.TAG, "Problem deleting action lists from database");
	    } 
		for (int i = 0; i < customLists.size(); i++) {
			long retCode = actionListDbAdapter.create(customLists.get(i).getName(), Integer.toString(i), Integer.toString(TRActionList.CUSTOM_TRUE), Integer.toString(TRActionList.FILTER_DONE_TODO));
		    if (retCode == -1) {
		    	Log.e(MainActivity.TAG, "Unable to add list to database");
		    } 
		}
		actionListDbAdapter.close();
		
	 }

	public void orderUp(String listname) {

		ArrayList<TRActionList> customLists = getActionListCustom(true);
		
		actionListDbAdapter.open();
		int pos = -1;
		for (int i = 0; i < customLists.size(); i++) {
			if (customLists.get(i).getName().equals(listname)) pos = i; // get position of actionlist between other lists.
		}
		if (pos != -1) {
			actionListDbAdapter.update(customLists.get(pos).getName(), Integer.toString(pos-1), Integer.toString(TRActionList.CUSTOM_TRUE), null);
			actionListDbAdapter.update(customLists.get(pos-1).getName(), Integer.toString(pos), Integer.toString(TRActionList.CUSTOM_TRUE), null);
		}
		actionListDbAdapter.close();
	}

	public void orderDown(String listname) {
				
		ArrayList<TRActionList> customLists = getActionListCustom(true);
		actionListDbAdapter.open();
		int pos = -1;
		for (int i = 0; i < customLists.size(); i++) {
			if (customLists.get(i).getName().equals(listname)) pos = i; // get position of actionlist between other lists.
		}
		if (pos != -1) {
			actionListDbAdapter.update(customLists.get(pos).getName(), Integer.toString(pos+1), Integer.toString(TRActionList.CUSTOM_TRUE), null);
			actionListDbAdapter.update(customLists.get(pos+1).getName(), Integer.toString(pos), Integer.toString(TRActionList.CUSTOM_TRUE), null);
		}
		actionListDbAdapter.close();
	}
	
	public void addList(String listname) {
		
		
		if (listname.equals("")) {
			// Nothing in field, do not add anything.
			return;
		}
		ArrayList<TRActionList> customLists = getActionListCustom(true);
		
		
		// Do not add list if it already exists. No duplicate list names.
		for (int i = 0; i < customLists.size(); i++) {
			if (customLists.get(i).getName().equalsIgnoreCase(listname)) {
				Log.i(MainActivity.TAG,"List " + listname + " already exists (not added again).");
				return;
			}
		}
		
		int listSize = customLists.size() + 1; // needed to add new list at the end of list (+1)
		// Misschien dat getCount automatisch +1 doet.
		// int listSize = actionListAdapter.fetchAll().getCount(); // needed to add new list at the end of list (+1)
		
		actionListDbAdapter.open();
	    long retCode = actionListDbAdapter.create(listname, Integer.toString(listSize), Integer.toString(TRActionList.CUSTOM_TRUE), Integer.toString(TRActionList.FILTER_DONE_TODO));
	    if (retCode == -1) {
	    	Log.e(MainActivity.TAG, "Unable to add list to database");
	    } else {
	    	// List is created, now set default filters (if a file is loaded)
	    	if (MainActivity.listActions != null) {
	    		FilterHelper filterHelper = new FilterHelper(this.context, listname);
	    		filterHelper.createDefaultFilters();
	    	}
	    }
	    actionListDbAdapter.close();
	 }
	
}
