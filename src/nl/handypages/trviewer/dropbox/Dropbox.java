/*
 * Copyright (c) 2010-11 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.handypages.trviewer.dropbox;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.PrefsActivitity;
import nl.handypages.trviewer.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

/*
 * Substantial code has been reused from the Dropbox API example, see license above.
 */

public class Dropbox {
	
    ///////////////////////////////////////////////////////////////////////////
    //                      Your app-specific settings.                      //
    ///////////////////////////////////////////////////////////////////////////

    // Replace this with your app key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    final static private String APP_KEY = "0eaqdthbpmxv38n";
    final static private String APP_SECRET = "ha1ftk0sknf98ap";

    // If you'd like to change the access type to the full Dropbox instead of
    // an app folder, change this value.
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    ///////////////////////////////////////////////////////////////////////////
    //                      End app-specific settings.                       //
    ///////////////////////////////////////////////////////////////////////////

    // You don't need to change these, leave them alone.
    final static private String DROPBOX_ACCOUNT_PREFS_NAME = "dropbox_prefs";
    final static private String DROPBOX_ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String DROPBOX_FILE_MODIFICATION_DATE = "FILE_MODIFIED";
    final static private String DROPBOX_ACCESS_SECRET_NAME = "ACCESS_SECRET";
    final static public String DROPBOX_REMOTE_ACTION_PATH = "DROPBOX_REMOTE_ACTION_PATH";
    final static public String DROPBOX_REMOTE_ACTIONLIST_PATH = "DROPBOX_REMOTE_ACTIONLIST_PATH";
    final static public String DROPBOX_LOCAL_ACTION_PATH = "DROPBOX_LOCAL_ACTION_PATH";
    final static public String DROPBOX_LOCAL_ACTIONLIST_PATH = "DROPBOX_LOCAL_ACTIONLIST_PATH";
    
    public boolean mLoggedIn;	
    private DropboxAPI<AndroidAuthSession> mApi;
    
    private List<Entry> dbFoldercontents;
    private String[] dropboxFileList;
	public String dropboxRemoteActionPath;
	public String dropboxRemoteActionListPath;
	public String dropboxLocalActionPath;
	public String dropboxLocalActionListPath;
	//private File dropboxLocalFileObject;
	private Context ctx;


    

    //private Handler progressHandler;

    public Dropbox(Context ctx) {
		super();
		this.ctx = ctx;
		
		Log.i(MainActivity.TAG,"Initializing Dropbox settings...");
		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        
	}
    
    public String getRemoteActionPath() {
		return dropboxRemoteActionPath;
	}
    public String getRemoteActionListPath() {
		return dropboxRemoteActionListPath;
	}

	public String getLocalActionPath() {
		return dropboxLocalActionPath;
	}
	public String getLocalActionListPath() {
		return dropboxLocalActionListPath;
	}
	/**Return true if the local file exists.
	 * 
	 * @return boolean (true if exists)
	 */
	public boolean existsLocalFile() {
		if (dropboxLocalActionPath != null && dropboxLocalActionPath != "") {
			File file = new File(dropboxLocalActionPath);
			return file.exists();
		} else {
			return false;
		}
	}
	
    public boolean checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            Log.e(MainActivity.TAG,"You must apply for an app key and secret from developers.dropbox.com, and add them to the app before trying it.");
            return false;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = ctx.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
        	Log.e(MainActivity.TAG,"URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            return false;
        }
        return true;
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(DROPBOX_ACCESS_KEY_NAME, null);
        String secret = prefs.getString(DROPBOX_ACCESS_SECRET_NAME, null);
        dropboxRemoteActionPath = prefs.getString(DROPBOX_REMOTE_ACTION_PATH, "");
        dropboxRemoteActionListPath = prefs.getString(DROPBOX_REMOTE_ACTIONLIST_PATH, "");
    	dropboxLocalActionPath = prefs.getString(DROPBOX_LOCAL_ACTION_PATH, "");
    	dropboxLocalActionListPath = prefs.getString(DROPBOX_LOCAL_ACTIONLIST_PATH, "");
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
    public String getFileLastModified () {
        SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Log.i(MainActivity.TAG,"Last modified retrieved from prefs: " + prefs.getString(DROPBOX_FILE_MODIFICATION_DATE, ""));
        return prefs.getString(DROPBOX_FILE_MODIFICATION_DATE, "");
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(DROPBOX_ACCESS_KEY_NAME, key);
        edit.putString(DROPBOX_ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    public void storeFileModificationDate(String date) {
        // Save the access key for later
        SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(DROPBOX_FILE_MODIFICATION_DATE, date);
        edit.commit();
    }

    public void clearKeys() {
        SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    public AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    public void storeRemotePath(String remoteActionPath, String remoteActionListPath) {
    	SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
    	edit.putString(DROPBOX_REMOTE_ACTION_PATH, remoteActionPath);
    	edit.putString(DROPBOX_REMOTE_ACTIONLIST_PATH, remoteActionListPath);
        edit.commit();
        getKeys(); // getKeys() refreshes the internal variables from the SharedPreferences
    }
    public void storeLocalPath(String localActionPath, String localActionListPath) {
    	SharedPreferences prefs = ctx.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
    	edit.putString(DROPBOX_LOCAL_ACTION_PATH, localActionPath);
    	edit.putString(DROPBOX_LOCAL_ACTIONLIST_PATH, localActionListPath);
        edit.commit();
        getKeys(); // getKeys() refreshes the internal variables from the SharedPreferences
    }
    /**
     * @param dropboxPath is the absolute path at dropbox should load a list of files from.
     * If this is null it will load the dropbox root folder, unless dropboxSelection is provided.
     * dropboxPath is stateless.
     * @param dropboxSelection is the selected file or directory at dropbox. Only provide this
     * parameter if you have previously provided a dropbox path. Internally this class keeps
     * a record of the previous selected folder. This is needed to check if the selected file
     * is a directory or a file with isSelectionFile().
     * dropboxSelection keeps state.
     * @return String[] with list of files in the selected directory.
     */
    public String[] loadFileList(String dropboxPath, String dropboxSelection) {
    	//FileDownload fd;
    	Entry dropboxFileListEntry;
    	try {
			if (dropboxPath == null ) {
				if (dropboxSelection == null) {
					// Load directory listing of the the root of dropbox
					dropboxFileListEntry = mApi.metadata("/", 100, null, true, null);
					if (dropboxFileListEntry.contents.size() == 0) {
						Log.e(MainActivity.TAG,"No file listing returned by Dropbox. Dropbox service might be down.");
					}
				} else {
					// Check previous selection and set the absolute dropboxPath
					dropboxPath = dbFoldercontents.get(0).parentPath() + dropboxSelection;
					Log.i(MainActivity.TAG, "Path to open: " + dropboxPath);
					dropboxFileListEntry = mApi.metadata(dropboxPath, 100, null, true, "");
					//dropboxFileListEntry = mApi.metadata("dropbox", dropboxPath, 100, "", true);
				}
			} else {
				dropboxFileListEntry = mApi.metadata(dropboxPath, 100, null, true, "");
				//dropboxFileListEntry = mApi.metadata("dropbox", dropboxPath, 100, "", true);
			}
    	dbFoldercontents = dropboxFileListEntry.contents;
    	} catch (DropboxException e) {
			Log.e(MainActivity.TAG, "Something went wrong while getting folder list of files of Dropbox.");
		}
    	if (dbFoldercontents != null) {
	    	dropboxFileList = new String[dbFoldercontents.size()];
	    	for (int i = 0; i < dbFoldercontents.size(); i++) {
	    		
	    		dropboxFileList[i] = dbFoldercontents.get(i).fileName();
				/*if (dropboxSelection == null) {
					//dropboxFileList[i] = contents.get(i).parentPath() + contents.get(i).fileName();
					dropboxFileList[i] = contents.get(i).pathNoInitialSlash();
				} else {
					dropboxFileList[i] = contents.get(i).fileName();
				}*/
			}
    	} else {
    		dropboxFileList= new String[0];
    	}
    	return dropboxFileList;
    }
    /**
     * Downloads a file to Dropbox to the local device.
     * This only works if dropbox has been configured (user can log in, and dropbox and local path 
     * has been set (with storeLocalPath).
     * @return
     * @throws Exception 
     */
    public boolean downloadDropboxFile() throws Exception{
		/*
		 * If you do not authenticate() than you will get a NullPointerException in
		 * DropboxAPI.getFile() at line 446. Authenticate makes sure some internal
		 * variables are set.
		 */
    	if (!mApi.getSession().isLinked()) {
			return false;
		} 
		
    	if (getRemoteActionPath() == null || getLocalActionPath() == null) {
    		Log.e(MainActivity.TAG,"Dropbox: either the remote or local path has not been set.");
    		return false;
    	}
    	BufferedOutputStream bw = null;
    	File dropboxLocalFileObject = new File(getLocalActionPath());
    	
    	try {
			Log.i(MainActivity.TAG, "Reading remote path: " + getRemoteActionPath());
			bw = new BufferedOutputStream(new FileOutputStream(dropboxLocalFileObject));
			DropboxFileInfo info = mApi.getFile(getRemoteActionPath(), null, bw, null);
			Log.i(MainActivity.TAG, "The file's rev is: " + info.getMetadata().rev);
			Log.i(MainActivity.TAG, "The file's modification date is: " + info.getMetadata().modified);
			storeFileModificationDate(info.getMetadata().modified);
		} catch (Exception e) {
    		Log.e(MainActivity.TAG, e.getMessage() + ": Could not create local file or download file from Dropbox. Rethrowing...");
			e.printStackTrace();
			throw e;
		} finally {
    		if (bw != null) {
    			bw.close();
    		}
		}
		bw = null;
    	dropboxLocalFileObject = new File(getLocalActionListPath());
    	
    	try {
			Log.i(MainActivity.TAG, "Reading remote path: " + getRemoteActionListPath());
			bw = new BufferedOutputStream(new FileOutputStream(dropboxLocalFileObject));
			DropboxFileInfo info = mApi.getFile(getRemoteActionListPath(), null, bw, null);
			Log.i(MainActivity.TAG, "The file's rev is: " + info.getMetadata().rev);
			Log.i(MainActivity.TAG, "The file's modification date is: " + info.getMetadata().modified);
			//storeFileModificationDate(info.getMetadata().modified);
    	} catch (Exception e) {
    		Log.e(MainActivity.TAG, "Could not create local file or download file from Dropbox.");
			e.printStackTrace();
		} finally {
    		if (bw != null) {
    			bw.close();
    		}
		}
    	
    	return true;
    }
    public String getCurrentDBPath() {
    	if (dbFoldercontents.size() == 0) return "";
    	else return ctx.getString(R.string.dropbox_base_path) + dbFoldercontents.get(0).parentPath();
    }
    public boolean isSelectionFile (String dropboxSelection) {
    	
    	if (dropboxSelection != null) {
    		for (Entry file_entry:dbFoldercontents) {
    			if (file_entry.fileName().equalsIgnoreCase(dropboxSelection)) {
		    		if (file_entry.isDir) {
		    			return false; // selection is a directory
    				} else { 
		    			storeRemotePath(file_entry.path, file_entry.parentPath() + PrefsActivitity.INTERNAL_LOCAL_FILENAME_ACTIONLIST);
		    			return true; // selection is a file
	    			}
    			}
	    	}
    		return false; // no match found
    	} else {
    		Log.e(MainActivity.TAG,"No selection provided to isSelectionFile(), returning false.");
    		return false; // no selection provided
    	}
    }
    /**
     * This lets us use the Dropbox API from the DropboxLoginAsyncTask
     */
    public DropboxAPI<AndroidAuthSession> getAPI() {
    	return mApi;
    }
    public boolean isLinked() {
    	return getAPI().getSession().isLinked();
    }
} 

    
