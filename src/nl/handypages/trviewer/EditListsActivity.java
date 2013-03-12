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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

import nl.handypages.trviewer.helpers.ActionListHelper;
import nl.handypages.trviewer.parser.TRActionList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author bhavers
 *
 */
public class EditListsActivity extends Activity {
	
	private ArrayList<String> customLists;
	private ListView lv;
	private ActionListHelper actionListHelper;
	private Tracker mGaTracker;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		actionListHelper = new ActionListHelper(this);
		
        setContentView(R.layout.edit_list);
        lv = (ListView)findViewById(R.id.listEditLists);
        getCustomActionLists(); 
         
        if (customLists != null) {
        	lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, customLists));
        }
	    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
      mGaTracker = EasyTracker.getTracker();
    }
 
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
    }
 
	/**
	 * Retrieves all custom defined lists from database and populates the global
	 * customLists ArrayList<String>. 
	 */
	private void getCustomActionLists() {
		ArrayList<TRActionList> trActionLists = actionListHelper.getActionListCustom(true); // only retrieve custom lists, the others can not be editted
        // Now create list to display (only names of action lists instead of objects).
    	customLists = new ArrayList<String>();
		for (int i = 0; i < trActionLists.size(); i++) {
    		customLists.add(trActionLists.get(i).getName());
		}
	}

	public void removeList(View view) {
	     /** 
	      * See this post on how to implement handling multiple selected entries:
	      * http://bestsiteinthemultiverse.com/2009/12/android-selected-state-listview-example/
	      * This method is defined in edit_list.xml
	      */
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_removelist_try_button", null);
		int pos = lv.getCheckedItemPosition();
		if (pos == -1) {
			// Nothing selected, do not add anything.
			return;
		}
		
		actionListHelper.removeList(customLists.get(pos));
		
		getCustomActionLists();
		lv.setAdapter(new ArrayAdapter<String>(this, 
	    		android.R.layout.simple_list_item_multiple_choice, customLists));
		
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_removelist_ok_button", null);
	 }

	public void orderUp(View view) {
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_orderup_try_button", null);
		int pos = lv.getCheckedItemPosition();

		//Log.i(MainActivity.TAG,"Pos = " + Integer.toString(pos));
		if (pos == -1 || pos == 0) {
			// Nothing selected, do not add anything.
			return;
		}
		actionListHelper.orderUp(customLists.get(pos));
		
		getCustomActionLists();
		lv.setAdapter(new ArrayAdapter<String>(this, 
	    		android.R.layout.simple_list_item_multiple_choice, customLists));
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_orderup_ok_button", null);
	}

	public void orderDown(View view) {
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_orderdown_try_button", null);
		int pos = lv.getCheckedItemPosition();
		if (pos == -1 || pos == customLists.size() - 1) {
			// Nothing selected, do not add anything.
			return;
		}
		actionListHelper.orderDown(customLists.get(pos));
		
		getCustomActionLists();
		lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, customLists));
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_orderup_ok_button", null);
	}
	
	public void addList(View view) {
	     /** 
	      * Adds the list to SharedPreferences and refreshes the ListView to display the list.
	      * This method is defined in edit_list.xml.
	      */
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_addlist_try_button", null);
		TextView listToAdd = (TextView)findViewById(R.id.editListEditTextAdd);
		
		if (listToAdd.getText().toString().equals("")) {
			// Nothing in field, do not add anything.
			return;
		}
		
		//int listSize = actionListAdapter.fetchAll().getCount(); // needed to add new list at the end of list (+1)

		// Do not add list if it already exists. No duplicate list names.
		for (int i = 0; i < customLists.size(); i++) {
			if (customLists.get(i).equalsIgnoreCase(listToAdd.getText().toString())) {
				Log.i(MainActivity.TAG,"List " + listToAdd.getText().toString() + " already exists.");
				listToAdd.setText("");
				listToAdd.clearFocus();
				return;
			}
		}
		actionListHelper.addList(listToAdd.getText().toString());
		
		getCustomActionLists();
		lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, customLists));
		
		listToAdd.setText("");
		listToAdd.clearFocus();
		mGaTracker.sendEvent("ui_action", "button_press", "editlist_addlist_ok_button", null);
	 }
}
