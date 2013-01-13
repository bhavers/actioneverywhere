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

	private Dropbox db;
	private Handler downloadHandler;
	private boolean forceDownload;
	
	public DropboxDownloader(Dropbox dropbox, boolean forceDownload, Handler h) {
		super();
		this.db = dropbox;
		this.forceDownload = forceDownload;
		downloadHandler = h;
	}
	
	@Override
	public void run() {
		db.downloadDropboxFile(forceDownload, downloadHandler);
	}
}