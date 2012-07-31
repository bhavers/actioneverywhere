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
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
public class ActionListSortActivity extends Activity {
	//private String[] lists;
	SharedPreferences listValues;
	SharedPreferences.Editor listEditor;
	private ArrayList<String> displayLists; // the lists to display, is a copy of storedLists
	private Map<String,Integer> storedLists; // the lists retrieved from SharedPreferences
	private ListView lv;
	/**
	 * 
	 */

	/**
	 * TODO:
	 * - Add New thought button
	 * - Add generic screen for all lists, instead of PhoneActivity
	 */
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Retrieve all lists from SharedPreferences 'lists' (stored as lists.xml
         * in /data/data/nl.handypages.trviewer/shared_prefs/lists.xml).
         * Only accessible via DDMS File Explorer on the emulator; the real phone is protected.
         */
        listValues = getBaseContext().getSharedPreferences("lists", Context.MODE_PRIVATE);
		listEditor = listValues.edit();
		
		// Example commented out below how to get Arrays from R.
        //List<String> tempLists = Arrays.asList(getResources().getStringArray(R.array.edit_lists));
        //displayLists = new ArrayList<String>(tempLists);
		displayLists = new ArrayList<String>(); // empty, will be build in refreshLists()
		
        setContentView(R.layout.edit_list);
        lv = (ListView)findViewById(R.id.listEditLists);
	    //refreshLists();
        lv.setAdapter(new ArrayAdapter<String>(this, 
	    		android.R.layout.simple_list_item_multiple_choice, MainActivity.getActionLists(getBaseContext())));
        //lv.setAdapter(new ArrayAdapter<String>(this, 
	    //		android.R.layout.simple_list_item_multiple_choice, displayLists));
	    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


	    
        /**PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName("trv_lists");
        addPreferencesFromResource(R.xml.lists);**/
    }
	public void removeList(View view) {
	     /** 
	      * See this post on how to implement handling multiple selected entries:
	      * http://bestsiteinthemultiverse.com/2009/12/android-selected-state-listview-example/
	      * This method is defined in edit_list.xml
	      */
		int pos = lv.getCheckedItemPosition();
		if (pos == -1) {
			// Nothing selected, do not add anything.
			return;
		}
		listEditor.remove(displayLists.get(pos));
		listEditor.commit();
		MainActivity.getActionLists(getBaseContext());
	 }
	public void orderUp(View view) {
		int pos = lv.getCheckedItemPosition();
		if (pos == -1 || pos == 0) {
			// Nothing selected, do not add anything.
			return;
		}
		listEditor.putInt(displayLists.get(pos),pos - 1);
		listEditor.putInt(displayLists.get(pos-1),pos);
		listEditor.commit();
		MainActivity.getActionLists(getBaseContext());
	}
	public void orderDown(View view) {
		int pos = lv.getCheckedItemPosition();
		if (pos == -1 || pos == displayLists.size() - 1) {
			// Nothing selected, do not add anything.
			return;
		}
		listEditor.putInt(displayLists.get(pos),pos + 1);
		listEditor.putInt(displayLists.get(pos+1),pos);
		listEditor.commit();
		MainActivity.getActionLists(getBaseContext());
	}
	public void addList(View view) {
	     /** 
	      * Adds the list to SharedPreferences and refreshes the ListView to display the list.
	      * This method is defined in edit_list.xml.
	      * 
	      */
		TextView listToAdd = (TextView)findViewById(R.id.editListEditTextAdd);

		if (listToAdd.getText().toString().equals("")) {
			// Nothing in field, do not add anything.
			return;
		}
		
		int listSize = storedLists.size(); // needed to add new list at the end of list (+1)

	    listEditor.putInt(listToAdd.getText().toString(),listSize);
	    Log.i("TRV_Main","listSize = " + Integer.toString(listSize));
	    
	    listEditor.commit();

	    MainActivity.getActionLists(getBaseContext());
		listToAdd.setText("");
		listToAdd.clearFocus();
		
	 }
	
}
