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
package nl.handypages.trviewer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import nl.handypages.trviewer.database.FilterActionDateDbAdapter;
import nl.handypages.trviewer.helpers.FilterHelper;
import nl.handypages.trviewer.parser.TRAction;
import nl.handypages.trviewer.parser.TRContext;
import nl.handypages.trviewer.parser.TRProject;
import nl.handypages.trviewer.parser.TRTopic;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActionListActivity extends ListActivity {
	//String[] listItems = {"exploring", "android", "list", "activities"};
	String actionList = null;
	int actionListPos;
    ArrayList<TRAction> listActions = null;
    ArrayList<TRAction> listActionsSelected = null; // subset of listAction that only contains object where Context=Phone
    //ArrayList<TRProject> listProjectsSelected = null; // subset of listProjects that is synced with listActionsSelected.
    ArrayList<TRProject> listProjects = null;
    ArrayList<TRContext> listContexts = null;
    ArrayList<TRTopic> listTopics = null;
    FilterHelper filterHelper = null;
    Calendar afterDate; // reused in filterDates method.
    Calendar beforeDate; // reused in filterDates method. 
    boolean filterAfterDate;
    boolean filterBeforeDate;
 	Date todayDate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.action_list);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		actionList = bundle.getString("actionList");
		actionListPos = bundle.getInt("actionlistPos");
		filterHelper = new FilterHelper(this, actionList);
		setTodayDate();
		rebuildActionsLists();	
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(getApplicationContext(), ActionDetailActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putString("actionDescription", listActionsSelected.get(position).getDescription());
    	bundle.putString("actionDetails", listActionsSelected.get(position).getNotes());
    	intent.putExtras(bundle);
    	startActivity(intent);
	}
	/**
	 * Sets the todayDate to the last minute of the previous day. todayDate can be used to identify
	 * actions that are before today.
	 */
	private void setTodayDate() {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR) -1);
		today.set(Calendar.HOUR_OF_DAY, 23);
		today.set(Calendar.MINUTE, 59);
		today.set(Calendar.SECOND, 59);
		todayDate = new Date(today.getTimeInMillis());
	}
    
	private void rebuildActionsLists() {
	    if (MainActivity.listActions != null) {
	    	listActions = MainActivity.listActions;
		    listActionsSelected = new ArrayList<TRAction>();
		    listContexts = MainActivity.listContexts;
		    listProjects = MainActivity.listProjects;
		    //listProjectsSelected = new ArrayList<TRProject>();
		    listTopics = MainActivity.listTopics;
		    	
		    Iterator<TRAction> iterator = listActions.iterator();
		    //TRAction tra = new TRAction();
		    TRAction tra = null;
		    if ((filterHelper.getActionDateAfterSelection() != null) && (filterHelper.getActionDateBeforeSelection() != null)) {
		    	setFilterDates(Integer.parseInt(filterHelper.getActionDateBeforeSelection()), Integer.parseInt(filterHelper.getActionDateAfterSelection()));
		    }
		    
			while(iterator.hasNext()) {
				tra = iterator.next();
				if (filterContext(tra)) {
					if (filterTopics(tra)) { 
						if (filterActionState(tra)) {
							if (filterDates(tra)) {
								listActionsSelected.add(tra);
		    				}
						}
			    	}
		    	}
				tra = null;
			}
			
			// Convert actionlist to array, to be able to use sort() method.
			TRAction[] tra_array = new TRAction[listActionsSelected.size()];
			tra_array = listActionsSelected.toArray(tra_array);
			Arrays.sort(tra_array); // Sort method will use overriden TRAction.compareTo() method.
			
			// Convert back to ArrayList for use in ListAdapter
			listActionsSelected.clear();
			listActionsSelected.addAll(Arrays.asList(tra_array));
			
			/**
			 *  Loop below creates a listProjectsSelected that is in sync (1 on 1 map) with listActionsSelected.
			 *  Removed, because the project is now read directly in ActionListArrayAdapter.getView because
			 *  it is simpler. The difference is that with loop below all processing is done before view
			 *  is rendered. If you do it in getView it is processed when scrolling through the list.
			 *
			 *
			for (int i = 0; i < listActionsSelected.size(); i++) {
				Log.i(MainActivity.TAG,"action projectid = " + listActionsSelected.get(i).getProjectId() + " " + listActionsSelected.get(i).getDescription());
				TRProject p = MainActivity.projectHelper.getProject(listActionsSelected.get(i).getProjectId());
				if (p != null) {
					if (p.getDescription() != null) {
						Log.i(MainActivity.TAG,"project descr = " + p.getDescription());
					} else {
						Log.i(MainActivity.TAG,"project descr is null");
					}
				} else {
					Log.i(MainActivity.TAG,"TRProject is null"); 
				}
			
				listProjectsSelected.add(MainActivity.projectHelper.getProject(listActionsSelected.get(i).getProjectId()));
				
			}*/
			setListAdapter(new ActionListArrayAdapter(this,R.layout.action_list_row, listActionsSelected));
		    this.setTitle(actionList + " list (" + Integer.toString(listActionsSelected.size()) + " actions)");
	    } else {
	    	listActions = null;
		    listActionsSelected = null;
		    listContexts = null;
		    listProjects = null;
		    //listProjectsSelected = null;
		    listTopics = null;
	    }
	}
     
	/**
	 * This method is used to initialize the before and after dates for the filterDates method.
	 * Sets the number of days before the the current date (daysBefore) and number of days after the current date (daysAfter).
	 * 
	 * @param daysBefore Number of days before the current date 
	 * @param daysAfter Number of days after the current date 
	 * 
	 * @see filterDates
	 */
	private void setFilterDates(int daysBefore, int daysAfter)
	{
		afterDate = Calendar.getInstance();
		beforeDate = Calendar.getInstance();
		filterBeforeDate = true;
		filterAfterDate = true;
		
		if (daysBefore != Integer.parseInt(FilterActionDateDbAdapter.LIST_ALL_ID)) {
			beforeDate.add(Calendar.DATE, daysBefore); 
		} else {
			filterBeforeDate = false;
		}
		if (daysAfter != Integer.parseInt(FilterActionDateDbAdapter.LIST_ALL_ID)) {
			//afterDate.add(Calendar.DATE, 0 - daysAfter); // 0 - inverts daysBefore, so 3 becomes -3.	
			afterDate.add(Calendar.DATE, daysAfter);
		} else {
			filterAfterDate = false;
		}
	}
	
	/**
	 * Filters the actions on the number of days before the current date (daysBefore) and number of days after the current date (daysAfter).
	 * Set the daysBefore and daysAfter with setFilterDates (because of performance reasons).
	 * beforeDate en afterDate need to be set outside 
	 * @param actionDate A Date object with the action date
	 
	 * @return true if this actionDate is within boundaries of daysBefore and daysAfter (and should therefore be included in the action list)
	 */
	private boolean filterDates(TRAction tra)
	{
		/**
		 * Dates are available for actions where the status is:
		 * - Scheduled
		 * - Inactive (due)
		 * - Delegated (follow-up and due); think due date can be skipped
		 * - ASAP (due)
		 */
		Date actionDate[] = new Date[3];
		
		actionDate[0] = tra.getScheduledDate();
		actionDate[1] = tra.getDueDate();
		actionDate[2] = tra.getChaseDate();
		
		for (int i = 0; i < actionDate.length; i++) {
			if (actionDate[i] != null) {
				//d.setTime( d.getTime() + days*1000*60*60*24 );
				if (filterBeforeDate == true) {
					if (actionDate[i].getTime() <= beforeDate.getTimeInMillis()) {
						//Log.i(TAG,"- Yes this one is before the beforeDate");
						// Do nothing
					} else {
						//Log.i(TAG,"- Not included, action date NOT before set before-date");
						return false;
					}
				} else {
					//Log.i(TAG,"- No before filter set, including this");
				}
				if (filterAfterDate == true) {
					if (actionDate[i].getTime() >= afterDate.getTimeInMillis()) {
						//Log.i(TAG,"- Yes this one is before the afterDate");
						// Do nothing
					} else {
						//Log.i(TAG,"- Do not include; after the current date");
						return false;
					}
				} else {
					//Log.i(TAG,"- No after filter set, including this");
				}
			} else {
				//Log.i(TAG,"- Date = null, not including this");
				//return false;
			}
		}	
		return true;
	}
	private boolean filterContext(TRAction tra) {
		if (tra.getContextIndex() != null) {
			if (filterHelper.getContextsCheckedItems()[0] == true) {
				// Include all was selected, always return true;
				return true;
			}
			for (int i = 0; i < filterHelper.getContextsListSize(); i++) {
				if ((filterHelper.getContextsCheckedItems()[i] == true) && (tra.getContextId().equalsIgnoreCase(filterHelper.getContextsListIds().get(i)))) {
					// listContexts.get(Integer.parseInt(tra.getContextIndex())- 1).getId().equalsIgnoreCase(filterHelper.getContextsListIds().get(i)) is an alternative for the second evaluation.
					return true;
				}
			}
		}
		return false;
		
	}
	
	private boolean filterTopics(TRAction tra) {
		if (tra.getTopicIndex() != null) {
			if (filterHelper.getTopicsCheckedItems()[0] == true) {
				// Include all was selected, always return true;
				return true;
			}
			for (int j = 0; j < filterHelper.getTopicsListSize(); j++) {
				//Log.i(TAG,"Topic: " + tra.getDescription() + " ==> " + tra.getTopicIndex() + " - " + tra.getTopicId() + " --> " + filterHelper.getTopicsListIds().get(j) + " ::: Size: " + filterHelper.getTopicsCheckedItems()[j]);
				if ((filterHelper.getTopicsCheckedItems()[j] == true) && (tra.getTopicId() != null) && (tra.getTopicId().equalsIgnoreCase(filterHelper.getTopicsListIds().get(j)))) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean filterActionState(TRAction tra) {
		if (tra.getState() != null) {

			/*
			 * Code below not working well. The intention is that when actions are filter on a day 'after' the 
			 * selected day. Than it should never show ASAPs, etc. without an actual due date set.
			 * This is the way TR works. But this code below does not seem to work.
			 */
			/*if (!filterHelper.getActionDateAfterSelection().equalsIgnoreCase(ActionListsFilterActionDateDbAdapter.LIST_ALL_ID)) {
				if (tra.getChaseDate() == null && tra.getScheduledDate() == null && tra.getDueDate() == null) {
					Log.i(MainActivity.TAG,"Excluding: " + tra.getDescription() + " " + tra.getScheduledDateStr());
					return false;
				} else {
					Log.i(MainActivity.TAG,"Including: " + tra.getDescription() + " " + tra.getScheduledDateStr());
				}
			}*/
			
			if (filterHelper.getActionStateCheckedItems()[0] == true) {
				// Include all was selected, always return true;
				return true;
			}
			for (int k = 0; k < filterHelper.getActionStateListSize(); k++) {
				//Log.i(TAG,"actionState: " + tra.getState() + " --> " + filterHelper.getActionStateListIds()[k] + " ::: Size: " + filterHelper.getActionStateCheckedItems()[k]);
				if ((filterHelper.getActionStateCheckedItems()[k] == true) && (tra.getState().equalsIgnoreCase(filterHelper.getActionStateListIds()[k]))) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * @return
	 */
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.filterListBtn:
	    	if (MainActivity.listActionLists.get(actionListPos).getCustom()) {
	    		Intent filterListActivity = new Intent(getBaseContext(), ActionListFilterActivity.class);
		    	//Intent filterListActivity = new Intent(getBaseContext(), ActionListFilterPrefsActivitity.class);
		    	Bundle bundle = new Bundle();
		    	bundle.putString("actionList", actionList);
		    	filterListActivity.putExtras(bundle);
		    	startActivity(filterListActivity);	
	    	} else {
	    		Toast.makeText(getApplicationContext(), "Can not edit filters for Thinking Rock lists, " +
	    				"only for custom added lists (see help).", Toast.LENGTH_LONG).show();
	    	}
	    	
	        return true;
	    /**case R.id.sortListBtn:
	    	Intent sortListActivity = new Intent(getBaseContext(), ActionListSortActivity.class);
	    	startActivity(sortListActivity);
	        return true; */
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.action_list_menu, menu);
	     return true;
	 }


	private class ActionListArrayAdapter extends ArrayAdapter<TRAction> {

        private ArrayList<TRAction> items;

        public ActionListArrayAdapter(Context context, int textViewResourceId, ArrayList<TRAction> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.action_list_row, null);
                }
                TRAction action = items.get(position);
                if (action != null && action.getDone() == false) {
                        TextView tt = (TextView) v.findViewById(R.id.toptext);
                        TextView pt = (TextView) v.findViewById(R.id.project);
                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                        TextView btd = (TextView) v.findViewById(R.id.bottomtext_date);
                        tt.setTextColor(Color.WHITE);
                        pt.setTextColor(Color.WHITE);
                        bt.setTextColor(Color.WHITE);
                        btd.setTextColor(Color.WHITE);
                        if (tt != null) {
                        	//When using pre-processed listProjectsSelected list (see also commented out code above):
                        	//String project = listProjectsSelected.get(position).getDescription();
                        	//tt.setText((project == null ? project : "") + ": " + action.getDescription());
                        	
                        	// For including Project before action:
                        	//String project = MainActivity.projectHelper.getProject(action.getProjectId()).getDescription();
                        	//tt.setText((project != null ? project + ": " : "") + action.getDescription());
                        	
                        	tt.setText(action.getDescription());
                        }
                        if(bt != null && action.getContextIndex() != null){
                        	String project = MainActivity.projectHelper.getProject(action.getProjectId()).getDescription();
                        	if (project != null) { 
                        		pt.setText(project);
                        	} else {
                        		pt.setText("No project");
                        		//pt.setVisibility(View.GONE);	
                        	}
                        	
                        	
                        	// index in TRX starts with 1 (not 0),there add - 1;
                        	TRContext aContext = listContexts.get(Integer.parseInt(action.getContextIndex()) - 1);
                        	TRTopic aTopic = listTopics.get(Integer.parseInt(action.getTopicIndex()) - 1 );
                        	//bt.setText("Context: " + aContext.getName() + ", Topic: " + aTopic.getName());
                        	bt.setText("@" + aContext.getName() + " (" + aTopic.getName() + ")");
                        	                        	
                        	if (action.getState().equals(TRAction.stateActionASAP)) {
	                    		if (action.getDueDate() != null) {
	                        		if (action.getDueDate().before(todayDate)) {
	                        			btd.setTextColor(Color.RED);
	                        		} 
	                        		btd.setText("ASAP (" + action.getDueDateStr() + ")");
	                    		} else {
	                    			btd.setText("ASAP");
	                    		}
                        	}
                        	if (action.getState().equals(TRAction.stateActionScheduled)) {
                        		if (action.getScheduledDate() != null) {
                        			if (action.getScheduledDate().before(todayDate)) {
                        				btd.setTextColor(Color.RED);
                        			}
                        			btd.setText("Scheduled (" + action.getScheduledDateStr() + ")") ;
	                        	} else {
	                    			btd.setText("Scheduled");
	                    		}
                        	}
                        	if (action.getState().equals(TRAction.stateActionDelegated)) {
                        		if (action.getChaseDate() != null) {
                        			if (action.getChaseDate().before(todayDate)) {
                        				btd.setTextColor(Color.RED);
                        			}
                        			btd.setText("Delegated to " + (action.getDelegatedTo() != null ? action.getDelegatedTo() : "nobody") + " (" + action.getChaseDateStr() + ")");
                        		} else {
	                    			btd.setText("Delegated to " + (action.getDelegatedTo() != null ? action.getDelegatedTo() : "nobody"));
	                    		}
                        	}
                        	if (action.getState().equals(TRAction.stateActionStateInactive)) {
                        		if (action.getDueDate() != null) {
                        			if (action.getDueDate().before(todayDate)) {
                        				btd.setTextColor(Color.RED);
                        			}
                        			btd.setText("Inactive (" + action.getDueDateStr() + ")");
                        		} else {
	                    			btd.setText("Inactive");
	                    		}
                        	}
                    		
                        }
                }
                return v;
        }
}
}
