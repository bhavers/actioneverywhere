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


import com.google.analytics.tracking.android.EasyTracker;

import nl.handypages.trviewer.database.FilterActionDateDbAdapter;
import nl.handypages.trviewer.helpers.FilterHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author bhavers
 *
 */
public class ActionListFilterActivity extends Activity {
	private ListView lvFilters;
	private String actionList = null;
	private FilterHelper filterHelper;

	static final int DIALOG_CONTEXT_ID = 0;
	static final int DIALOG_TOPIC_ID = 1;
	static final int DIALOG_STATUS_ID = 2;
	static final int DIALOG_ACTION_DATE_AFTER_ID = 3;
	static final int DIALOG_ACTION_DATE_BEFORE_ID = 4;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	
        setContentView(R.layout.filter_list);
        lvFilters = (ListView)findViewById(R.id.listFilters);
        String[] filters = getResources().getStringArray(R.array.action_list_filters);
		lvFilters.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filters));
	    lvFilters.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			    int position, long id) {
				switch(position) {
                case DIALOG_CONTEXT_ID:
                	showDialog(DIALOG_CONTEXT_ID);
                	break;
                case DIALOG_TOPIC_ID:
                	showDialog(DIALOG_TOPIC_ID);
                	break;
                case DIALOG_STATUS_ID:
                	showDialog(DIALOG_STATUS_ID);
                	break;	
                case DIALOG_ACTION_DATE_AFTER_ID:
                	showDialog(DIALOG_ACTION_DATE_AFTER_ID);
                	break;
                case DIALOG_ACTION_DATE_BEFORE_ID:
                	showDialog(DIALOG_ACTION_DATE_BEFORE_ID);
                	break;
                default:
                	break;
				  }
			    }
		});
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
 
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
    }
 
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_CONTEXT_ID:
	    	//final CharSequence[] items = {"None", "Phone", "Office", "Shop"};
	    	//final CharSequence[] contextItems = contextsList.toArray(new CharSequence[contextsList.size()]);
	    	final CharSequence[] contextItems = filterHelper.getContextsList().toArray(new CharSequence[filterHelper.getContextsListSize()]);

	    	//final CharSequence[] items = contextsList.
		    //boolean[] checkedItems = {false, true, true, false, false, false, false};
	    	//boolean[] cCheckedItems = contextsCheckedItems; 
	    		
		    AlertDialog.Builder contextBuilder = new AlertDialog.Builder(this);
		    contextBuilder.setTitle("Select contexts to include in list");
		    contextBuilder.setMultiChoiceItems(contextItems, filterHelper.getContextsCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
		        public void onClick(DialogInterface dialog, int item, boolean checks) {
		            if (item == 0) {
		            	if (checks == true) {
							filterHelper.deleteAllContexts();
							filterHelper.createContext(item);	
		            	} else {
		            		filterHelper.deleteAllContexts();
		            	}
		            }
		        	if (item != 0) {
		        		if (checks == true) {
		            		filterHelper.deleteContext(0);
		        			filterHelper.createContext(item);
			            } else {
			            	filterHelper.deleteContext(item);
			            }
		        	}
		        	dialog.dismiss();
					onResume();
					onCreateDialog(DIALOG_CONTEXT_ID);
		        }
		    });
		    AlertDialog contextAlert = contextBuilder.create();
	    	contextAlert.show();
	        break;
	    case DIALOG_TOPIC_ID:
	    	//final CharSequence[] items = {"None", "Phone", "Office", "Shop"};
	    	final CharSequence[] topicItems = filterHelper.getTopicsList().toArray(new CharSequence[filterHelper.getTopicsListSize()]);
	    		
		    AlertDialog.Builder topicBuilder = new AlertDialog.Builder(this);
		    topicBuilder.setTitle("Select topics to include in list");
		    topicBuilder.setMultiChoiceItems(topicItems, filterHelper.getTopicsCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
		        public void onClick(DialogInterface dialog, int item, boolean checks) {
		        	if (item == 0) {
		            	if (checks == true) {
							filterHelper.deleteAllTopics();
							filterHelper.createTopic(item);	
		            	} else {
		            		filterHelper.deleteAllTopics();
		            	}
		            }
		        	if (item != 0) {
		        		if (checks == true) {
		            		filterHelper.deleteTopic(0);
		        			filterHelper.createTopic(item);
			            } else {
			            	filterHelper.deleteTopic(item);
			            }
		        	}
		        	dialog.dismiss();
					onResume();
					onCreateDialog(DIALOG_TOPIC_ID);
		        }
		    });
		    AlertDialog topicAlert = topicBuilder.create();
	    	topicAlert.show();
	        break;
	    case DIALOG_STATUS_ID:
	    	//final CharSequence[] statusItems = actionStateList.toArray(new CharSequence[actionStateList.size()]);
	    	//final CharSequence[] statusItems = ;
	    		
		    AlertDialog.Builder statusBuilder = new AlertDialog.Builder(this);
		    statusBuilder.setTitle("Select action state to include in list");
		    statusBuilder.setMultiChoiceItems(filterHelper.getActionStateList(), filterHelper.getActionStateCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
		        public void onClick(DialogInterface dialog, int item, boolean checks) {
		        	if (item == 0) {
		            	if (checks == true) {
							filterHelper.deleteAllActionState();
							filterHelper.createActionState(item);	
		            	} else {
		            		filterHelper.deleteAllActionState();
		            	}
		            }
		        	if (item != 0) {
		        		if (checks == true) {
		            		filterHelper.deleteActionState(0);
		        			filterHelper.createActionState(item);
			            } else {
			            	filterHelper.deleteActionState(item);
			            }
		        	}
		        	dialog.dismiss();
					onResume();
					onCreateDialog(DIALOG_STATUS_ID);
		        }
		    });
		    AlertDialog statusAlert = statusBuilder.create();
	    	statusAlert.show();
	        break;
	    case DIALOG_ACTION_DATE_AFTER_ID:
	    	//final CharSequence[] statusItems = actionStateList.toArray(new CharSequence[actionStateList.size()]);
	    	//final CharSequence[] statusItems = ;
	    		
		    AlertDialog.Builder actionAfterBuilder = new AlertDialog.Builder(this);
		    actionAfterBuilder.setTitle("Actions scheduled after...");
		    actionAfterBuilder.setSingleChoiceItems(filterHelper.getActionDateAfterList(), filterHelper.getActionDateAfterSelectedItem(), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int item) {
		        	filterHelper.deleteActionDate(FilterActionDateDbAdapter.ID_AFTER);
		        	filterHelper.createActionDate(FilterActionDateDbAdapter.ID_AFTER, filterHelper.getActionDateAfterDaysList()[item],FilterActionDateDbAdapter.TYPE_DAYS);
					onResume();
		        }
		    });
		    AlertDialog actionAfterAlert = actionAfterBuilder.create();
	    	actionAfterAlert.show();
	        break;
	    case DIALOG_ACTION_DATE_BEFORE_ID:
	    	//final CharSequence[] statusItems = actionStateList.toArray(new CharSequence[actionStateList.size()]);
	    	//final CharSequence[] statusItems = ;
	    		
		    AlertDialog.Builder actionBeforeBuilder = new AlertDialog.Builder(this);
		    actionBeforeBuilder.setTitle("Actions scheduled up to...");
		    actionBeforeBuilder.setSingleChoiceItems(filterHelper.getActionDateBeforeList(), filterHelper.getActionDateBeforeSelectedItem(), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int item) {
		        	filterHelper.deleteActionDate(FilterActionDateDbAdapter.ID_BEFORE); 
		        	filterHelper.createActionDate(FilterActionDateDbAdapter.ID_BEFORE, filterHelper.getActionDateBeforeDaysList()[item],FilterActionDateDbAdapter.TYPE_DAYS);
		        	onResume();
		        }
		    });
		    AlertDialog actionBeforeAlert = actionBeforeBuilder.create();
	    	actionBeforeAlert.show();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		actionList = bundle.getString("actionList");
		filterHelper = new FilterHelper(this, actionList);
		//Log.i(TAG,"ActionListFilter - listName: " + actionList);
	}
	
	
}
