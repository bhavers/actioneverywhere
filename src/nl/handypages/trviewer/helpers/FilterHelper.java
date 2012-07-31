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
import java.util.Arrays;

import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.R;
import nl.handypages.trviewer.database.FilterActionDateDbAdapter;
import nl.handypages.trviewer.database.FilterContextDbAdapter;
import nl.handypages.trviewer.database.FilterStatusDbAdapter;
import nl.handypages.trviewer.database.FilterTopicDbAdapter;
import nl.handypages.trviewer.parser.TRContext;
import nl.handypages.trviewer.parser.TRTopic;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Filters are used in multiple activities. This class help in retrieving and saving 
 * filters.
 * @author bhavers
 *
 */
public class FilterHelper {
	private ArrayList<String> contextsList; // the list with contexts
	private ArrayList<String> contextsListIds; //same list and order as contextsList, but this contains the contextIds. 
	private boolean[] contextsCheckedItems;
	private ArrayList<String> topicsList; // the list with topics
	private ArrayList<String> topicsListIds; // same list and order as topicsList, but this contains the topicIds.
	private boolean[] topicsCheckedItems;
	private String[] actionStateList; // the list with state of actions
	private String[] actionStateListIds; // same list and order as actionStateList, but this contains the actionIds.
	private boolean[] actionStateCheckedItems;
	private String[] actionDateAfterList; // the filter list descriptions for actions after.
	private String[] actionDateAfterDaysList; // the filter list with the number of days for actions after. Same size and order as actionDateAfterList
	private String actionDateAfterSelection; // Filter for actions after certain date. --> this could also be declared only in setLists i guess
	private int actionDateAfterSelectedItem; // same list and order as actionDateAfterList, but this contains the selected option.
	private String[] actionDateBeforeList; // the list with of actions for which the dates needs to be lesser or equals to.
	private String[] actionDateBeforeDaysList; // the filter list with the number of days for actions before. Same size and order as actionDateBeforeList
	
	private String actionDateBeforeSelection; // Filter for actions before a certain date. --> this could also be declared only in setLists i guess
	private int actionDateBeforeSelectedItem; // same list and order as actionDateBeforeList, but this contains the selected option.
	private String actionList = null;
	
	private FilterContextDbAdapter contextDbAdapter;
	private FilterTopicDbAdapter topicDbAdapter;
	private FilterStatusDbAdapter statusDbAdapter;
	private FilterActionDateDbAdapter actionDateDbAdapter;
	
	private Context context;
	
	public FilterHelper(Context context, String actionList) {
		this.context = context;
		this.actionList = actionList;
		contextDbAdapter = new FilterContextDbAdapter(context);
    	topicDbAdapter = new FilterTopicDbAdapter(context);
    	statusDbAdapter = new FilterStatusDbAdapter(context);
    	actionDateDbAdapter = new FilterActionDateDbAdapter(context);
    	//openDbConnections();
    	
		contextsList = new ArrayList<String>();
		contextsListIds = new ArrayList<String>();
		topicsList = new ArrayList<String>();
		topicsListIds = new ArrayList<String>();
		
		setLists();
	}
	public void closeDbConnections() {
		contextDbAdapter.close();
		topicDbAdapter.close();
		statusDbAdapter.close();
		actionDateDbAdapter.close();
	}
	public void openDbConnections() {
		contextDbAdapter.open();
    	topicDbAdapter.open();
    	statusDbAdapter.open();
    	actionDateDbAdapter.open();
	}
	public String[] getActionDateAfterDaysList() {
		return actionDateAfterDaysList;
	}

	public void setActionDateAfterDaysList(String[] actionDateAfterDaysList) {
		this.actionDateAfterDaysList = actionDateAfterDaysList;
	}

	public String[] getActionDateBeforeDaysList() {
		return actionDateBeforeDaysList;
	}

	public void setActionDateBeforeDaysList(String[] actionDateBeforeDaysList) {
		this.actionDateBeforeDaysList = actionDateBeforeDaysList;
	}
	public void createDefaultFilters() {
		this.deleteAllTopics();
		this.deleteAllContexts();
		this.deleteAllActionState();
		this.deleteAllActionDate();

		this.createTopic(0); // Include All
		this.createContext(0); // Include All
		this.createActionState(FilterStatusDbAdapter.stateActionASAP); // ASAP
		this.createActionState(FilterStatusDbAdapter.stateActionScheduled); // Scheduled
		this.createActionState(FilterStatusDbAdapter.stateActionDelegated); // Delegated
		this.createActionDate(FilterActionDateDbAdapter.ID_AFTER, FilterActionDateDbAdapter.LIST_ALL_ID, FilterActionDateDbAdapter.TYPE_DAYS); // Include All
		this.createActionDate(FilterActionDateDbAdapter.ID_BEFORE, FilterActionDateDbAdapter.LIST_ALL_ID, FilterActionDateDbAdapter.TYPE_DAYS); // Include All
		
	}
	/** Populates the filter lists
	 * It reads the contexts and topics from the TRActions (TRContext and TRTopic). It adds an
	 * 'Include all' at the beginning of the list. Than it reads the database to find which 
	 * filters are actually set. 
	 */
	private void setLists() {
		
		openDbConnections();
	
		contextsList.clear();
		contextsListIds.clear();
		contextsList.add("Include All");
		contextsListIds.add(TRContext.LIST_ALL_ID);
		Cursor cur = contextDbAdapter.fetchAllFilterContexts(actionList);
		//startManagingCursor(cur);
		int sizeList = 0;
		
		int counter = 1; // pos 0 already taken by Include All
		if (MainActivity.listContexts == null) {
			contextsCheckedItems = new boolean[1];
			//contextsCheckedItems[0] = true;
		} else {
			sizeList = MainActivity.listContexts.size();
			contextsCheckedItems = new boolean[sizeList+1];
			/** Sort context.
			 * Could be improved by putting None as the second entry after All.
			 */
			TRContext[] trc = new TRContext[MainActivity.listContexts.size()];
			trc = MainActivity.listContexts.toArray(trc);
			Arrays.sort(trc);
			
			for (TRContext trContext : trc) {
				contextsList.add(trContext.getName());
				contextsListIds.add(trContext.getId());
				contextsCheckedItems[counter] = false;
				
				cur.moveToFirst();
				
				while (!cur.isAfterLast()) {
					if (cur.getString(cur.getColumnIndexOrThrow(FilterContextDbAdapter.KEY_CONTEXT_ID)).equalsIgnoreCase(TRContext.LIST_ALL_ID)) {
						contextsCheckedItems[0] = true;
					}
					//Log.i(TAG,"List in db: " + cur.getString(cur.getColumnIndexOrThrow(ActionListsFilterContextDbAdapter.KEY_CONTEXT_ID)) + " compared to list: " + trContext.getId()); 
					if (cur.getString(cur.getColumnIndexOrThrow(FilterContextDbAdapter.KEY_CONTEXT_ID)).equalsIgnoreCase(trContext.getId())) {
						//Log.i(TAG, "FOUND! " + contextsList.get(counter));
						contextsCheckedItems[counter] = true;
					}
					cur.moveToNext();
				}
				counter++;
				//Log.i(TAG,"Context: " + trContext.getName());
			}
		}
		cur.close();
		
		cur = null;
		sizeList = 0;
		
		
		topicsList.clear();
		topicsListIds.clear();
		topicsList.add("Include All");
		topicsListIds.add(TRTopic.LIST_ALL_ID);
		
		cur = topicDbAdapter.fetchAllFilterTopics(actionList);
		//startManagingCursor(cur); 
		
		if (MainActivity.listTopics == null) {
			topicsCheckedItems = new boolean[1];
			//topicsCheckedItems[0] = true;
		} else {
		
			sizeList = MainActivity.listTopics.size();
			topicsCheckedItems = new boolean[sizeList+1];
			counter = 1; // pos 0 already taken by Include All
			
			TRTopic[] trp = new TRTopic[MainActivity.listTopics.size()];
			trp = MainActivity.listTopics.toArray(trp);
			Arrays.sort(trp);
			
			for (TRTopic trTopic: trp) {
				topicsList.add(trTopic.getName());
				topicsListIds.add(trTopic.getId());
				topicsCheckedItems[counter] = false;
				cur.moveToFirst();
				
				while (!cur.isAfterLast()) { 
					if (cur.getString(cur.getColumnIndexOrThrow(FilterTopicDbAdapter.KEY_TOPIC_ID)).equalsIgnoreCase(TRTopic.LIST_ALL_ID)) {
						topicsCheckedItems[0] = true;
					}
					if (cur.getString(cur.getColumnIndexOrThrow(FilterTopicDbAdapter.KEY_TOPIC_ID)).equalsIgnoreCase(trTopic.getId())) {
						topicsCheckedItems[counter] = true;
					}
					cur.moveToNext();
				}
				counter++;
				//Log.i(TAG,"Context: " + trContext.getName());
			}
		}
		cur.close();
		
		cur = null;
		sizeList = 0;
		counter = 0;
		/*
		 * No need to add Include all, that is done in R.String for actionState.
		 */
		actionStateList = null;
		actionStateListIds = null;
		
		actionStateList = context.getResources().getStringArray(R.array.action_status);
		actionStateListIds = context.getResources().getStringArray(R.array.action_status_id);
		// Replace the first action state in the list by the id set in code (to keep it consistent)
		actionStateListIds[0] = FilterActionDateDbAdapter.LIST_ALL_ID;
		//actionStateList = (ArrayList<String>)Arrays.asList(getResources().getStringArray(R.array.action_status));
		//actionStateListIds = (ArrayList<String>)Arrays.asList(getResources().getStringArray(R.array.action_status_id));
		
		cur = statusDbAdapter.fetchAllFilterStatus(actionList);
		//startManagingCursor(cur); 
		sizeList = actionStateList.length;
		actionStateCheckedItems = new boolean[sizeList];
		for (counter = 0; counter < sizeList; counter++) {
			actionStateCheckedItems[counter] = false;
			cur.moveToFirst();
			while (!cur.isAfterLast()) { 
				if (cur.getString(cur.getColumnIndexOrThrow(FilterStatusDbAdapter.KEY_STATUS_ID)).equalsIgnoreCase(actionStateListIds[counter])) {
					actionStateCheckedItems[counter] = true;
				}
				cur.moveToNext();
			}
		}
		cur.close();
		
		cur = null;
		sizeList = 0;
		counter = 0;

		actionDateAfterSelection = null;
		actionDateBeforeSelection = null;
		actionDateBeforeSelectedItem = -1;
		actionDateAfterSelectedItem = -1;
		
		actionDateAfterList = context.getResources().getStringArray(R.array.action_after_description);
		actionDateAfterDaysList = context.getResources().getStringArray(R.array.action_after_days);
		actionDateBeforeList = context.getResources().getStringArray(R.array.action_before_description);
		actionDateBeforeDaysList = context.getResources().getStringArray(R.array.action_before_days);
		
		cur = actionDateDbAdapter.fetchAllFilterActionDate(actionList);
		//startManagingCursor(cur); 
		sizeList = actionDateAfterList.length;
		for (counter = 0; counter < sizeList; counter++) {
			cur.moveToFirst();
			while (!cur.isAfterLast()) { 
				if (cur.getString(cur.getColumnIndexOrThrow(FilterActionDateDbAdapter.KEY_ACTION_DATE_ID)).equalsIgnoreCase(FilterActionDateDbAdapter.ID_AFTER)) {
					actionDateAfterSelection = cur.getString(cur.getColumnIndexOrThrow(FilterActionDateDbAdapter.KEY_ACTION_DATE_VALUE));
					if (actionDateAfterSelection.equalsIgnoreCase(actionDateAfterDaysList[counter])) { 
						actionDateAfterSelectedItem = counter;
					}
				}
				cur.moveToNext();
			}
		}
		sizeList = actionDateBeforeList.length;
		for (counter = 0; counter < sizeList; counter++) {
			cur.moveToFirst();
			while (!cur.isAfterLast()) { 
				if (cur.getString(cur.getColumnIndexOrThrow(FilterActionDateDbAdapter.KEY_ACTION_DATE_ID)).equalsIgnoreCase(FilterActionDateDbAdapter.ID_BEFORE)) {
					actionDateBeforeSelection = cur.getString(cur.getColumnIndexOrThrow(FilterActionDateDbAdapter.KEY_ACTION_DATE_VALUE));
					if (actionDateBeforeSelection.equalsIgnoreCase(actionDateBeforeDaysList[counter])) { 
						actionDateBeforeSelectedItem = counter;
					}
				}
		cur.moveToNext();
			}
		}
		cur.close();
		closeDbConnections();
	}

	public String getActionDateAfterSelection() {
		return actionDateAfterSelection;
	}

public void setActionDateGreaterSelection(String actionDateGreaterSelection) {
	this.actionDateAfterSelection = actionDateGreaterSelection;
}

public String getActionDateBeforeSelection() {
	return actionDateBeforeSelection;
}

public void setActionDateBeforeSelection(String actionDateLesserSelection) {
	this.actionDateBeforeSelection = actionDateLesserSelection;
}

	public String[] getActionDateAfterList() {
	return actionDateAfterList;
}

public void setActionDateAfterList(String[] actionDateAfterList) {
	this.actionDateAfterList = actionDateAfterList;
}

public String[] getActionDateBeforeList() {
	return actionDateBeforeList;
}

public void setActionDateBeforeList(String[] actionDateBeforeList) {
	this.actionDateBeforeList = actionDateBeforeList;
}


	public int getActionDateAfterSelectedItem() {
	return actionDateAfterSelectedItem;
}

public void setActionDateAfterSelectedItem(int actionDateAfterSelectedItem) {
	this.actionDateAfterSelectedItem = actionDateAfterSelectedItem;
}

public int getActionDateBeforeSelectedItem() {
	return actionDateBeforeSelectedItem;
}

public void setActionDateBeforeSelectedItem(int actionDateBeforeSelectedItem) {
	this.actionDateBeforeSelectedItem = actionDateBeforeSelectedItem;
}

	public ArrayList<String> getContextsList() {
		return contextsList;
	}
	public int getContextsListSize() {
		return contextsList.size();
	}
	
	public void setContextsList(ArrayList<String> contextsList) {
		this.contextsList = contextsList;
	}
	
	public ArrayList<String> getContextsListIds() {
		return contextsListIds;
	}
	
	public void setContextsListIds(ArrayList<String> contextsListIds) {
		this.contextsListIds = contextsListIds;
	}
	
	public ArrayList<String> getTopicsList() {
		return topicsList;
	}
	public int getTopicsListSize() {
		return topicsList.size();
	}
	
	public void setTopicsList(ArrayList<String> topicsList) {
		this.topicsList = topicsList;
	}
	
	public ArrayList<String> getTopicsListIds() {
		return topicsListIds;
	}
	
	public void setTopicsListIds(ArrayList<String> topicsListIds) {
		this.topicsListIds = topicsListIds;
	}
	
	public String[] getActionStateList() {
		return actionStateList;
	}
	public int getActionStateListSize() {
		return actionStateList.length;
	}
	
	public void setActionStateList(String[] actionStateList) {
		this.actionStateList = actionStateList;
	}
	
	public String[] getActionStateListIds() {
		return actionStateListIds;
	}
	
	public void setActionStateListIds(String[] actionStateListIds) {
		this.actionStateListIds = actionStateListIds;
	}
	
	public boolean[] getContextsCheckedItems() {
		return contextsCheckedItems;
	}
	
	public void setContextsCheckedItems(boolean[] contextsCheckedItems) {
		this.contextsCheckedItems = contextsCheckedItems;
	}
	
	public boolean[] getTopicsCheckedItems() {
		return topicsCheckedItems;
	}
	
	public void setTopicsCheckedItems(boolean[] topicsCheckedItems) {
		this.topicsCheckedItems = topicsCheckedItems;
	}
	
	public boolean[] getActionStateCheckedItems() {
		return actionStateCheckedItems;
	}
	
	public void setActionStateCheckedItems(boolean[] actionStateCheckedItems) {
		this.actionStateCheckedItems = actionStateCheckedItems;
	}

	public long createContextById (String id) {
		openDbConnections();
		long retCode = contextDbAdapter.createFilterContext(actionList, id);
	    if (retCode == -1) {
	    	Log.e(MainActivity.TAG, "Unable to add context to database");
	    } else {
	    	//Log.i(MainActivity.TAG,"Inserted row: " + Long.toString(retCode));
	    }
	    closeDbConnections();
	    return retCode;
	}
	/*
	 * See comment at createTopic. Same thing applies here.
	 */
	public long createContext(int item) {
		return this.createContextById(contextsListIds.get(item));
	}
	public void deleteContext(int item) {
		openDbConnections();
		contextDbAdapter.deleteContextFilter(actionList, contextsListIds.get(item));
		closeDbConnections();
	}
	public void deleteAllContexts() {
		openDbConnections();
		contextDbAdapter.deleteAllContextFilters(actionList);
		closeDbConnections();
	}
	public void deleteAllTopics() {
		openDbConnections();
		topicDbAdapter.deleteAllTopicFilters(actionList);
		closeDbConnections();
	}
	public void deleteAllActionState() {
		openDbConnections();
		statusDbAdapter.deleteAllStatusFilters(actionList);
		closeDbConnections();
	}
	public void deleteAllActionDate() {
		openDbConnections();
		actionDateDbAdapter.deleteAllActionDateFilters(actionList);
		closeDbConnections();
	}
	
	public long createTopicById(String id) {
		openDbConnections();
		long retCode = topicDbAdapter.createFilterTopic(actionList, id);
	    if (retCode == -1) {
	    	Log.e(MainActivity.TAG, "Unable to add topic to database");
	    } else {
	    	//Log.i(TAG,"Inserted row: " + Long.toString(retCode));
	    }
	    closeDbConnections();
	    return retCode;
	}
	/*
	 * item below is the index of the ArrayList that contains the TRTopics in MainActivity.
	 * Seems to be a tricky approach, will need to clean this up later.
	 */
	public long createTopic(int item) {
		return this.createTopicById(topicsListIds.get(item));
	}
	public void deleteTopic(int item) {
		openDbConnections();
		topicDbAdapter.deleteTopicFilter(actionList, topicsListIds.get(item));
		closeDbConnections();
	}
	public long createActionStateById(String actionState) {
		openDbConnections();
		long retCode = statusDbAdapter.createFilterStatus(actionList, actionState);
	    if (retCode == -1) {
	    	Log.e(MainActivity.TAG, "Unable to add status to database");
	    } else {
	    	//Log.i(TAG,"Inserted row: " + Long.toString(retCode));
	    }
	    closeDbConnections();
	    return retCode;
	}
	
	public long createActionState(int item) {
		return createActionStateById(actionStateListIds[item]);
	}
	public void deleteActionState(int item) {
		openDbConnections();
		statusDbAdapter.deleteStatusFilter(actionList, actionStateListIds[item]);
		closeDbConnections();
	}
	/**
	 * @param beforeAfter Use ActionListsFilterActionDateDbAdapter.ID_BEFORE or .ID_AFTER
	 * @param value The value to be inserted, meaning depends on type (e.g. number of days)
	 * @param type The type of data that value represents, use ActionListsFilterActionDateDbAdapter.TYPE_*
	 * @return Result of create action. -1 is a problem, other numbers indicate success
	 */
	public long createActionDate(String beforeAfter, String value, String type) {
		openDbConnections();
		long retCode = actionDateDbAdapter.createFilterActionDate(actionList, beforeAfter, value, type);
	    if (retCode == -1) {
	    	Log.e(MainActivity.TAG, "Unable to add action filter to database");
	    } else {
	    	//Log.i(TAG,"Inserted row: " + Long.toString(retCode));
	    }
	    closeDbConnections();
	    return retCode;
	}
	public void deleteActionDate(String beforeAfter) {
		openDbConnections();
		actionDateDbAdapter.deleteActionDateFilter(actionList, beforeAfter);
		closeDbConnections();
	}
}
