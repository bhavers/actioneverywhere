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
package nl.handypages.trviewer.dropbox;

import java.io.IOException;

import com.dropbox.client2.exception.DropboxIOException;

import nl.handypages.trviewer.MainActivity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * DownloadHelper for Dropbox that runs in a separate thread.
 * @author bhavers
 * @version 1.0
 */

public class DropboxDownloader extends Thread {


	String fileName = null;
	private Dropbox db;
	Handler mHandler; // used to handle message exchange with the calling activity
	
	public DropboxDownloader(Dropbox dropbox, String fileTRX, Handler h) {
		super();
		//Log.i(MainActivity.TAG, "DropboxDownloader thread initialized...");
		this.db = dropbox;
		mHandler = h;
		fileName = fileTRX;
	}
	
	@Override
	public void run() {
		try {
			Log.i(MainActivity.TAG, "DropboxDownloader thread run() started.");
			if (fileName != "") {
				if (db.getAPI().getSession().isLinked()) {
		        	Log.i(MainActivity.TAG,"Start downloading file from Dropbox");
					db.downloadDropboxFile();
					Log.i(MainActivity.TAG,"Finished downloading file from Dropbox");
		    	} 
			} else {
				Log.e(MainActivity.TAG, "No file supplied to DropboxDownloader." );
			}
			updateProgress(100);
		} catch (DropboxIOException e) {
			Log.e(MainActivity.TAG, "DropboxIOException: is the network down?");
			updateProgress(MainActivity.PROGRESS_DROPBOXIOEXCEPTION);
		} catch (IOException e) {
	    	Log.e(MainActivity.TAG, "IOException: Could not read or write the file downloaded from Dropbox.");
			updateProgress(MainActivity.PROGRESS_IOEXCEPTION);
	    } catch (Exception e) {
			Log.e(MainActivity.TAG, "Exception: in DropboxDownloader thread.");
			updateProgress(MainActivity.PROGRESS_EXCEPTION);
		}
	}
    

    private void updateProgress(int progressPercentage) {
    	// Sends update message to calling Activity via Handler with the total update progress in percentage
    	Message msg = mHandler.obtainMessage();
    	msg.arg1 = progressPercentage;
    	mHandler.sendMessage(msg);
    }


}