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

import nl.handypages.trviewer.dropbox.Dropbox;
import nl.handypages.trviewer.helpers.FilterHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;

public class PrefsActivitity extends PreferenceActivity {
    
	private static final int DIALOG_LOAD_FILE = 0;
	private final int DIALOG_DROPBOX_AUTH_OK = 1;
	private static final String INTERNAL_LOCAL_FILENAME = "dropboxTR.trx";
	public static final String INTERNAL_LOCAL_FILENAME_ACTIONLIST = "ReviewActions.xml";
	//private static final String TR_ACTIONLIST_FILE = "ReviewActions.xml";

	private Dropbox db;
	private DropboxAPI<AndroidAuthSession> mApi;

	AlertDialog dbAuthOKDialog;
	private Preference prefDropboxLogin;
	private Preference prefDropboxRemoteFile;
	private boolean firstAuthentication;
	
	private String[] dropboxFileList;
	private String dropboxSelectedFile; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		db = new Dropbox(this);
		mApi = db.getAPI();
        if (!db.checkAppKeySetup()) finish();
		
		//prefUseDropbox = (CheckBoxPreference) findPreference("checkBoxSyncWithDropbox");

		//prefUseDropbox = (CheckBoxPreference) findPreference("checkBoxSyncWithDropbox");

		prefDropboxLogin = (Preference) findPreference("prefBtnDropboxLogin");
		prefDropboxRemoteFile = (Preference) findPreference("prefBtnDropboxRemoteFile");

		prefDropboxLogin.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				//startActivity(new Intent(getApplicationContext(), PrefsDropboxLoginActivity.class));
				// This logs you out if you're logged in, or vice versa
                if (mApi.getSession().isLinked() == true) {
                    logOut();
                } else {
                    // Start the remote authentication
                    firstAuthentication = true;
                	mApi.getSession().startAuthentication(PrefsActivitity.this);
                }
				return true;
			}

		});
		prefDropboxRemoteFile.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				//db.authenticate();
				showDialog(DIALOG_LOAD_FILE);
				return true;
			}

		});
		
		
		changeUILoggedInState(db.mLoggedIn);

	}
	/**
     * Convenience function to change UI state based on being logged in
     */
    public void changeUILoggedInState(boolean loggedIn) {
    	if (db.getRemoteActionPath() != null && !db.getRemoteActionPath().equalsIgnoreCase("")) {
			prefDropboxRemoteFile.setSummary(getString(R.string.dropbox_base_path) + db.dropboxRemoteActionPath);
		} else {
			prefDropboxRemoteFile.setSummary(R.string.prefs_dropbox_remote_file_summary);
		}

    	if (mApi.getSession().getAccessTokenPair() != null) {
    		prefDropboxLogin.setTitle(R.string.prefs_dropbox_login_loggedin);
			prefDropboxLogin.setSummary(R.string.prefs_dropbox_login_summary_loggedin);
			
    		
    	} else {
    		prefDropboxLogin.setTitle(R.string.prefs_dropbox_login_not_loggedin);
			prefDropboxLogin.setSummary(R.string.prefs_dropbox_login_summary_not_loggedin);
    	}
    }

    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
    	// 20120221: firstAuthentication is a workaround as authenticationSuccessful would otherwise also return 
    	// true on subsequent onResume calls (for example on Select Dropbox File). I have submitted a questions:
    	// http://forums.dropbox.com/topic.php?id=54699&replies=1
        if (session.authenticationSuccessful() && firstAuthentication == true) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                db.storeKeys(tokens.key, tokens.secret);
                changeUILoggedInState(true);
                showDialog(DIALOG_DROPBOX_AUTH_OK);
        	    TextView textView = (TextView) dbAuthOKDialog.findViewById(android.R.id.message);
        	    textView.setTextSize(12);
        	    firstAuthentication = false;

            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(MainActivity.TAG, "Error authenticating", e);
            }
        }
    	changeUILoggedInState(db.mLoggedIn);
    }
    
  
    protected Dialog onCreateDialog(int id){
    	AlertDialog dialog = null;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);


    	switch(id){
    	case DIALOG_LOAD_FILE:
    		
    		dropboxFileList = db.loadFileList(null, dropboxSelectedFile);
    		//builder.setTitle("Dropbox " + mApi.  getSession().db.getParentPath());
    		builder.setTitle("Dropbox " + db.getCurrentDBPath()); 
    		builder.create();
    		builder.setItems(dropboxFileList, new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    				dropboxSelectedFile = dropboxFileList[which];
    				if (db.isSelectionFile(dropboxSelectedFile)) {
    					dialog.dismiss(); //20120217 Why is this in? It doesn't seem to do anything.
    					try {
							// if download is successful, store the new path
							db.storeLocalPath(getBaseContext().getFilesDir().getAbsolutePath() + "/" + INTERNAL_LOCAL_FILENAME, 
									getBaseContext().getFilesDir().getAbsolutePath() + "/" + INTERNAL_LOCAL_FILENAME_ACTIONLIST);
							
							/*
							  After downloading file, reset this activity to reinitialize variables.
							  This should be removed when parsing of actions is replaced by reading
							  actions from database.
							 */

							 // Reset filters of actionlists to default filters.
							 
							ArrayList<String> actionLists = MainActivity.getActionLists(getBaseContext());						
							for (String list : actionLists) {
								//Log.i(MainActivity.TAG,"List: " + list);
								FilterHelper fh = new FilterHelper(getBaseContext(), list);
								fh.createDefaultFilters();
							}
							
							MainActivity.dropboxFileChanged = true;
							finish();
					        Intent intent = new Intent(PrefsActivitity.this, PrefsActivitity.class);
					        startActivity(intent);
							
						//} catch (IOException e) {
						} catch (Exception e) {
							Log.e(MainActivity.TAG,"Failed to download file from Dropbox");
							Toast.makeText(getApplicationContext(), "Failed downloading file", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
    					changeUILoggedInState(db.mLoggedIn);
    					return;
    				} 
    				Log.i(MainActivity.TAG, "Selected File (before onCreateDialog): " + dropboxSelectedFile);
    				onCreateDialog(DIALOG_LOAD_FILE);
    				/*if (!newFile.isDirectory()) {
    					dialog.dismiss();
    		            mLocalFile.setText(mChosenFile);  
    				}
    				onCreateDialog(DIALOG_LOAD_FILE);*/
    			}
    		});
    		break;
    		
    	case DIALOG_DROPBOX_AUTH_OK:
    		dbAuthOKDialog = new AlertDialog.Builder(this).create(); 
    		dbAuthOKDialog.setTitle(getString(R.string.dropbox_auth_ok_title));
    		dbAuthOKDialog.setMessage(getString(R.string.dropbox_auth_ok_body));
    		dbAuthOKDialog.setIcon(R.drawable.ic_menu_help);
    		dbAuthOKDialog.setButton("OK", new android.content.DialogInterface.OnClickListener() {  
    	      public void onClick(android.content.DialogInterface dialog, int which) {  
    	        return;  
    	    } });
    		return dbAuthOKDialog;
    	}
    	dialog = builder.show();
    	return dialog;
    }
    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        db.clearKeys();
        // Change UI state to display logged out version
        changeUILoggedInState(false);
    }
}
