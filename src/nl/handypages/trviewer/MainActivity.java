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
import java.util.Date;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.GoogleAnalytics.AppOptOutCallback;

import nl.handypages.trviewer.dropbox.Dropbox;
import nl.handypages.trviewer.dropbox.DropboxDownloader;
import nl.handypages.trviewer.helpers.ActionHelper;
import nl.handypages.trviewer.helpers.ActionListHelper;
import nl.handypages.trviewer.helpers.ActorHelper;
import nl.handypages.trviewer.helpers.ContextHelper;
import nl.handypages.trviewer.helpers.Eula;
import nl.handypages.trviewer.helpers.ProjectHelper;
import nl.handypages.trviewer.helpers.TopicHelper;
import nl.handypages.trviewer.parser.TRAction;
import nl.handypages.trviewer.parser.TRActionList;
import nl.handypages.trviewer.parser.TRActor;
import nl.handypages.trviewer.parser.TRContext;
import nl.handypages.trviewer.parser.TRParser;
import nl.handypages.trviewer.parser.TRProject;
import nl.handypages.trviewer.parser.TRTopic;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	
	public static final String TAG = "TRV_Main";
	static final int PARSING_PROGRESS_DIALOG = 0;
	static final int DOWNLOADING_PROGRESS_DIALOG = 1;
	static final int ABOUT_DIALOG = 2;
	static final int HELP_DIALOG = 3;
	
	public static final int PARSING_PROGRESS_START = 0;
	public static final int PARSING_PROGRESS_CONTEXTS = 10;
	public static final int PARSING_PROGRESS_TOPICS = 20;
	public static final int PARSING_PROGRESS_PROJECTS = 30;
	public static final int PARSING_PROGRESS_ACTORS = 45;
	public static final int PARSING_PROGRESS_ACTIONS = 65;
	public static final int PARSING_PROGRESS_DB_WRITE = 80;
	public static final int PARSING_PROGRESS_ACTIONLISTS = 90;
	public static final int PARSING_PROGRESS_FINISH = 100;
	private ListView lv1;
	private TextView textViewMainRefreshTime;
	// listActions, listContext. listActors and listTopics are populate by the TRParser thread.
	public static ArrayList<TRAction> listActions = null;
	public static ArrayList<TRContext> listContexts = null;
	public static ArrayList<TRTopic> listTopics = null;
	public static ArrayList<TRProject> listProjects = null;
	public static ArrayList<TRActor> listActors = null;
	public static ArrayList<TRActionList> listActionLists = null; 
	public static boolean dropboxFileChanged = false;
	 
	public static String[] LISTACTIONSTATE = null;
	SharedPreferences prefs;
	private Boolean prefsUseDropbox = null;
	public static String prefsSyncInterval = null;
	private String prefsFilelastModDateStr = null; 
	private String prefsDropboxLastChecked = null;
	public static String prefsEmailForThoughts = null;
		
	private static ActionListHelper actionListHelper;
	private static ActionHelper actionHelper;
	public static ProjectHelper projectHelper;
	private static ActorHelper actorHelper;
	private static ContextHelper contextHelper;
	private static TopicHelper topicHelper;
	
	private TRParser parsingProgressThread;
	private DropboxDownloader dbDownloaderThread;
    ProgressDialog parsingProgressDialog;
    ProgressDialog downloadingProgressDialog;
    AlertDialog helpDialog;
    private Dialog aboutDialog;
    private TextView tvHelpTitle;
    private TextView tvHelpBody;
    private Tracker mGaTracker;

    public static Dropbox db;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //googleAnalytics = GoogleAnalytics.getInstance(getApplicationContext());
        //mGaTracker = googleAnalytics.getDefaultTracker();
        //mGaTracker.sendEvent("ui_action", "button_press", "test", null);
        
        synchronized (this) {
        	new Eula(this).show();
		}
        setContentView(R.layout.main);
        
        actionListHelper = new ActionListHelper(this);
        actionHelper = new ActionHelper(this);
        projectHelper = new ProjectHelper(this);
        actorHelper = new ActorHelper(this);
        contextHelper = new ContextHelper(this);
        topicHelper = new TopicHelper(this);
        
        textViewMainRefreshTime = (TextView) findViewById(R.id.textViewMainRefreshTime);
        tvHelpTitle = (TextView) findViewById(R.id.textViewMainHelpTitle);
        tvHelpBody = (TextView) findViewById(R.id.textViewMainHelp);
        lv1 = (ListView)findViewById(R.id.listViewMain);  
        tvHelpTitle = (TextView) findViewById(R.id.textViewMainHelpTitle);
        tvHelpBody = (TextView) findViewById(R.id.textViewMainHelp);
      
        lv1.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (listActions != null) {
					Intent intent = new Intent(getApplicationContext(), ActionListActivity.class);
	            	Bundle bundle = new Bundle();
	            	bundle.putInt("actionlistPos",position);
	            	ListView tmpLv = (ListView)parent;
	            	String tmp = (String)tmpLv.getItemAtPosition(position);
	            	bundle.putString("actionList", tmp);
	            	
	            	intent.putExtras(bundle);
	            	startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "No actions available", Toast.LENGTH_LONG).show();
				}
		    }
		});

		setActionsFromDb();

	    // Action states are hard coded in TR; retrieve from Array in R.
        LISTACTIONSTATE = getResources().getStringArray(R.array.action_status);
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
      mGaTracker = EasyTracker.getTracker();
      setDevelopmentSetting();
    }
 
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); 
    }
 
    /**
     * Change settings when in development mode. Only works from API Level 17 onwards.
     * http://stackoverflow.com/questions/13990391/disable-google-analytics-when-in-development
     */
    private void setDevelopmentSetting() {
    	GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getApplicationContext());
    	if(BuildConfig.DEBUG) {
    		Log.i(MainActivity.TAG,"=== App running in debug mode ===");
    	    //googleAnalytics.setAppOptOut(true);
    	    googleAnalytics.setDebug(true);
    	} else {
    		Log.i(MainActivity.TAG,"=== App NOT running in debug mode ===");
    		googleAnalytics.setAppOptOut(false);
    		googleAnalytics.setDebug(false);
    	}
		// Get the app opt out preference using an AppOptOutCallback.
    	googleAnalytics.requestAppOptOut(new AppOptOutCallback() {
			@Override
			public void reportAppOptOut(boolean optOut) {
				if (optOut) {
					Log.i(MainActivity.TAG,"Google Analytics state = off.");
				} else {
					Log.i(MainActivity.TAG,"Google Analytics state = on.");
				}

			}
		});
    }
    
    /**
     * Parses the downloaded file and writes content to database. 
     */
    private void parseXMLtoDb() {
    	if (db.getLocalActionPath() != "" && db.existsLocalFile()) {
        	showDialog(PARSING_PROGRESS_DIALOG);
	        parsingProgressThread.start();
	    }
    }
   /**
    * Updates the label at the top of the main activity. It shows number of actions, datetime of actions, datetime of last check.
    */
    private void updateRefreshLabel() {
    	if ((listActions != null) && (listActions.size() > 0)) {
    		textViewMainRefreshTime.setText(Integer.toString(listActions.size()) + " actions: " + getLastUpdate(prefsFilelastModDateStr) + "\n");
    		if ((prefsDropboxLastChecked != null) && (!prefsDropboxLastChecked.equalsIgnoreCase(""))) {
    			// If available include last checked date.
    			textViewMainRefreshTime.setText(textViewMainRefreshTime.getText() + getString(R.string.update_last_checked) + " " + getLastUpdate(prefsDropboxLastChecked));
    		}
    		//lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getActionLists(getBaseContext())));
		}
    }
    
    /**
     * Converts the date to a human readable 'last updated' date. Eg. 1 minute ago, 12 minutes ago, 3 hours ago, yesterday 12:33PM
     * @return
     */
    private String getLastUpdate(String date) {
    	try {
			Date updateDate = new Date(date);
			Date systemDate = new Date();
			if (updateDate != null || systemDate != null) {
				if (systemDate.before(updateDate)) {
					Log.i(MainActivity.TAG,"Strange, the file update date if after the date/time of the device");
					return "0" + " " + getString(R.string.update_minutes_ago);
				} else {
					long minutes = ((systemDate.getTime()/60000) - (updateDate.getTime()/60000));
					if ((minutes / 60) > 6) {
						// More than six hour ago, show date and time
						return updateDate.toLocaleString();
					}
					if ((minutes / 60) > 0) {
						// More than one hour ago, show hours since last update
						return minutes / 60 + " " + getString(R.string.update_hours_ago);
					}
					if ((minutes / 60) < 1) {
						// Less than one hour ago, show hours since last update
						return minutes + " " + getString(R.string.update_minutes_ago);
					}
				}	
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return ""; // will never happen
    }
    
    /*
	 * Update the main activity (if no action lists have been created, show setup instructions)
	 */
    private void updateListGUI() {
        if (lv1.getAdapter().getCount() == 0) {
        	tvHelpTitle.setVisibility(View.VISIBLE);
        	tvHelpBody.setVisibility(View.VISIBLE);
        	tvHelpBody.setTextSize(12);
	    } else {
	    	tvHelpTitle.setVisibility(View.GONE);
	    	tvHelpBody.setVisibility(View.GONE);
	    }
    }
    
	public static ArrayList<String> getActionLists(Context ctx) {
    	ArrayList<String> displayLists = new ArrayList<String>(); // empty, will be build in refreshLists()
		listActionLists = actionListHelper.getActionListCustom(false); // first add lists from TR-app
    	listActionLists.addAll(actionListHelper.getActionListCustom(true)); // than add custom lists (defined by user) at the bottom (if available). 
    	
    	// Now create list to display (only names of action lists instead of objects).
    	for (int i = 0; i < listActionLists.size(); i++) {
    		displayLists.add(listActionLists.get(i).getName());
		} 
    	actionListHelper.close();
		return displayLists;
	}
	/*
	 * Reads Actions, Actors, Contexts and Topics from database and fills local variables.
	 */
	private void setActionsFromDb() {
		listActions = null;
    	listContexts = null;
    	listTopics = null;
    	listProjects = null;
    	listActors = null;
    	
		listActions = actionHelper.getActions();
		listProjects = projectHelper.getProjects();
		listActors = actorHelper.getActors();
		listContexts = contextHelper.getContexts();
		listTopics = topicHelper.getTopics();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		refresh();
		// When a new file on Dropbox is selected in preferences, it should automatically be downloaded.
		if (dropboxFileChanged) {
			dropboxFileChanged = false;
			download();
		}
	} 

	/**
	 * @return
	 */
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
        case PARSING_PROGRESS_DIALOG:
            parsingProgressDialog = new ProgressDialog(MainActivity.this);
            parsingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            parsingProgressDialog.setMessage("Loading...");
            return parsingProgressDialog;
        case DOWNLOADING_PROGRESS_DIALOG:
        	downloadingProgressDialog = new ProgressDialog(MainActivity.this);
        	downloadingProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	downloadingProgressDialog.setMessage(getString(R.string.dropbox_downloading));
        	return downloadingProgressDialog;
        case ABOUT_DIALOG:
        	aboutDialog = new Dialog(this);
            aboutDialog.setContentView(R.layout.about_dialog);
            aboutDialog.setTitle(getString(R.string.menu_item_about) + " " + getString(R.string.app_name));

            TextView textViewAboutAppVersion = (TextView) aboutDialog.findViewById(R.id.textViewAboutAppVersion);
            PackageInfo pInfo;
            String appVersion = new String("Unknown");
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				appVersion = pInfo.versionName;
			} catch (NameNotFoundException e) {
				Log.i(TAG,"Could not find package version");
			}
            //version = pInfo.versionName;

            textViewAboutAppVersion.setText(getString(R.string.about_version_label) + appVersion +"\n \n" + getString(R.string.app_description_short));
            WebView webViewAbout = (WebView) aboutDialog.findViewById(R.id.webViewAbout);
            webViewAbout.loadData(getString(R.string.acknowledgements), "text/html", null);
            ImageView image = (ImageView) aboutDialog.findViewById(R.id.imageAbout);
            image.setImageResource(R.drawable.launcher_trv);
            return aboutDialog;
        case HELP_DIALOG:
	        helpDialog = new AlertDialog.Builder(this).create(); 
		    helpDialog.setTitle(getString(R.string.help_title));
		    helpDialog.setMessage(getString(R.string.help_text));
		    helpDialog.setIcon(R.drawable.ic_menu_help);
		    helpDialog.setButton("OK", new android.content.DialogInterface.OnClickListener() {  
		      public void onClick(android.content.DialogInterface dialog, int which) {  
		        return;  
		    } });
		    return helpDialog;
        
        default:
            return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
        	case PARSING_PROGRESS_DIALOG:
        		parsingProgressDialog.setProgress(0);
	            if (db.getLocalActionPath() != null) {
	            	parsingProgressThread = new TRParser(getBaseContext(), db.getLocalActionPath(), 
	            			db.getLocalActionListPath(), parserHandler);
	            }
        	case DOWNLOADING_PROGRESS_DIALOG:
        		
		}
    	
    }

    final Handler parserHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		/*
    		 * Handles the messages that are return by the parsing thread (TRParse) to update the ProgressDialog
    		 */
    		int total = msg.arg1;
    		parsingProgressDialog.setProgress(total);
    		
    		if (total >= 100){
    			removeDialog(PARSING_PROGRESS_DIALOG);
    			refresh();
    		}
    	}
    };
    
    final Handler downloadHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		/*
    		 * Handles the messages that are return by the DropboxDownloader thread 
    		 * Currently it is not updated from DropboxDownloader, maybe in the 
    		 * future to present more details while downloading.
    		 */
    		int total = msg.arg1;
    		
    		if (total == Dropbox.DOWNLOAD_PROGRESS_DROPBOXIOEXCEPTION) {
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			Toast.makeText(getApplicationContext(), getString(R.string.progress_dropboxexception), Toast.LENGTH_LONG).show();
    		}
    		if (total == Dropbox.DOWNLOAD_PROGRESS_IOEXCEPTION) {
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			Toast.makeText(getApplicationContext(), getString(R.string.progress_ioexception), Toast.LENGTH_LONG).show();
    		}
    		if (total == Dropbox.DOWNLOAD_PROGRESS_EXCEPTION) {
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			Toast.makeText(getApplicationContext(), getString(R.string.progress_exception), Toast.LENGTH_LONG).show();
    		}
    		if (total == Dropbox.DOWNLOAD_PROGRESS_EXCEPTION_MISSING_FILE) {
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			Toast.makeText(getApplicationContext(), getString(R.string.progress_exception_missing_file), Toast.LENGTH_LONG).show();
    		}
    		if (total == Dropbox.DOWNLOAD_PROGRESS_FINISHED_SUCCESS_MODIFIED){
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			//updateRefreshLabel();
    			/*Intent intent = new Intent(MainActivity.this, MainActivity.class);
    			finish();
    	        startActivity(intent);*/
    			parseXMLtoDb();
    		}
    		if (total == Dropbox.DOWNLOAD_PROGRESS_FINISHED_SUCCESS_NOT_MODIFIED){
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			refresh();
    		}
    		if (total == Dropbox.DOWNLOAD_PROGRESS_FINISHED_ERROR){
    			dismissDialog(DOWNLOADING_PROGRESS_DIALOG);
    			refresh();
    		}
    	}
    };
    
    protected void onDestroy() {
    	super.onDestroy();
    	listActions = null;
    	listContexts = null;
    	listTopics = null;
    	listActionLists = null;
    	listActors = null;
    	Log.d(TAG,"MainActivity.onDestroy(): actionListHelper.close()");
    	actionListHelper.close();
    };
    
    private void getPreferences(){
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    
		prefsUseDropbox = prefs.getBoolean("checkBoxSyncWithDropbox", false);
		prefsSyncInterval = prefs.getString("prefBtnSync", null);
		prefsFilelastModDateStr = db.getFromSharedPrefs(Dropbox.DROPBOX_ACTION_FILE_MODIFICATION_DATE);
		prefsDropboxLastChecked = db.getFromSharedPrefs(Dropbox.DROPBOX_CHECKED);
	    prefsEmailForThoughts = prefs.getString("EditTextPrefsEmail", "");
    }
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Create preferences menu
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.main_menu, menu);
	     return true;
	}
    private void trackBtnPress(String msg) {
    	mGaTracker.sendEvent("ui_action", "button_press", "main_menu_editlist_button", null);
    }
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection in the preferences menu
	    switch (item.getItemId()) {
	    case R.id.refreshBtn:
	    	mGaTracker.sendEvent("ui_action", "button_press", "main_menu_download_button", null);
	    	download();
	        return true;
	    case R.id.prefsBtn:
	    	mGaTracker.sendEvent("ui_action", "button_press", "main_menu_preferences_button", null);
	    	Intent settingsActivity = new Intent(getBaseContext(), PrefsActivitity.class);
	    	startActivity(settingsActivity);
	        return true;
	    case R.id.editListBtn:
	    	trackBtnPress("main_menu_editlist_button");
	    	Intent editListActivity = new Intent(getBaseContext(), EditListsActivity.class);
	    	startActivity(editListActivity);
	        return true;
	    case R.id.helpBtn:
	    	mGaTracker.sendEvent("ui_action", "button_press", "main_menu_help_button", null);
	    	showDialog(HELP_DIALOG);
	    	TextView tvHelpDialog= (TextView) helpDialog.findViewById(android.R.id.message);
	    	tvHelpDialog.setTextSize(12);
	    	return true;
	    case R.id.aboutBtn:
	    	mGaTracker.sendEvent("ui_action", "button_press", "main_menu_about_button", null);
	    	showDialog(ABOUT_DIALOG);
	    	return true;
	    case R.id.exitBtn:
	    	this.finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    /*
     * Downloads the latest files from Dropbox.
     */
    private void download() {
    	if (prefsUseDropbox == true && db.isLinked()) {
    		showDialog(DOWNLOADING_PROGRESS_DIALOG);
           	dbDownloaderThread = new DropboxDownloader(db, true, downloadHandler);
           	dbDownloaderThread.start();
    	} else {
    		Toast.makeText(getApplicationContext(), "Can not refresh actions, Dropbox preferences not set.", Toast.LENGTH_SHORT).show();
    	}
    	//refresh();
    }
    public void newThought(View view) {
    	if (prefsEmailForThoughts != "") {
    		Intent intent = new Intent(getApplicationContext(), ThoughtActivity.class);
    		startActivity(intent);
    	} else {
    		Toast.makeText(getApplicationContext(), ((TextView) view).getText() + ": set email in Preferences.", Toast.LENGTH_LONG).show();
    	}
    }

	/**
	 * Re-reads preferences, actions and actionlists from db and refreshes the GUI
	 */
	private void refresh() {
		db = new Dropbox(this);
		getActionLists(getApplicationContext()); // update the action list display when parsing of lists has finished.
		setActionsFromDb();
		getPreferences();
		updateRefreshLabel();
		lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getActionLists(getBaseContext())));
		updateListGUI();
	}

}