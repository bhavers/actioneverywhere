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
import java.util.Calendar;

import nl.handypages.trviewer.parser.TRTopic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ThoughtActivity extends Activity {

	private static final int REQUEST_CODE = 1234;
	private EditText editTextThought;
	private Spinner spinner;
	private String strSentFrom;
	//private String strTopic;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.thought);
        editTextThought = (EditText) findViewById(R.id.editTextThought);
        
        String appName = getString(R.string.app_name);
        PackageInfo pInfo;
        try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			// If package name (including version number) is found, overwrite appName.
			appName = appName + " " + pInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(MainActivity.TAG,"Could not find package version");
		}
		Calendar c = Calendar.getInstance(); 
        strSentFrom = "\n\n\nSent from " + appName + " at " + c.getTime().toString();
        
        spinner = (Spinner) findViewById(R.id.spinnerTopic);
        if (MainActivity.listTopics != null) {
        	// If New Thought button is pressed before parsing of .trx file has completed, than listTopics is still null
	        
	        ArrayAdapter<TRTopic> adapter = new ArrayAdapter<TRTopic>(this, android.R.layout.simple_spinner_item, MainActivity.listTopics);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adapter);
	        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,  View view,  int position, long id) {
                        // onItemSelected not used, only read spinner when sending mail. Left it in for later reuse of code.
                    	/*TRTopic topic = MainActivity.listTopics.get(position);
                        strTopic = topic.toString();*/
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
            );
        }
        

    }
    public void send(View view) {

    	if (MainActivity.prefsEmailForThoughts != "") {
	    	Intent i = new Intent(Intent.ACTION_SEND);
	    	i.setType("text/html");
	    	i.putExtra(Intent.EXTRA_EMAIL  , new String[]{MainActivity.prefsEmailForThoughts});
	    	i.putExtra(Intent.EXTRA_SUBJECT, editTextThought.getText().toString());
	    	i.putExtra(Intent.EXTRA_TEXT   , editTextThought.getText().toString() + "\n\nTopic: " + spinner.getSelectedItem().toString() + strSentFrom );
	    	try {
	    	    startActivity(Intent.createChooser(i, "Send thought via email:"));
	    	} catch (android.content.ActivityNotFoundException ex) {
	    	    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
	    	}
    	} else {
    		Toast.makeText(getApplicationContext(), ((TextView) view).getText() + ": set email in Preferences.", Toast.LENGTH_LONG).show();
    	}

    }
    public void recordVoice(View view) {
    	//Toast.makeText(getApplicationContext(), ((TextView) view).getText() + " not yet implemented.", Toast.LENGTH_SHORT).show();
    	startVoiceRecognitionActivity();
    }

    /**
     * Start voice recognition.
     */
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            editTextThought.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        	//ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,matches));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}