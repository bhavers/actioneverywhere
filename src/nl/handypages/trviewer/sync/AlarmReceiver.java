package nl.handypages.trviewer.sync;

import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.dropbox.Dropbox;
import nl.handypages.trviewer.dropbox.DropboxDownloader;
import nl.handypages.trviewer.parser.TRParser;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver 
{
	private Dropbox db;
	private DropboxDownloader dbDownloaderThread;
	private TRParser parsingThread;
	private PowerManager.WakeLock wl;
	private Context context;
	
     @Override
     public void onReceive(Context context, Intent intent) 
     {   
    	 Log.d(MainActivity.TAG,"AlarmReceiver.onReceive()");
    	 this.context = context;
    	 PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
         downloadFiles(); 
         /** parseXMLtoDb will automatically be called by downloadHandler if download is successful.
          * The handlers will also call finish at the right time to close the WakeLock.
          */
     }

     /**
      * Cleanup method that should be called if the processed has finished (either ended successfully or with an error).
      * Will release the Wake Lock for the device.
      */
     private void finished() {
    	 Log.d(MainActivity.TAG, "Cleaning up, releasing WakeLock.");
    	 context = null;
    	 wl.release();
     }
     
     private boolean downloadFiles() {
    	 db = new Dropbox(context);
    	 if (db.getLocalActionPath() != null) {
    		dbDownloaderThread = new DropboxDownloader(db, db.getLocalActionPath(), downloadHandler);
         	dbDownloaderThread.start();
         }
    	 return true;
     }
     
     /**
      * Parses the downloaded the file and writes content to database.
      * A 
      */
     private void parseXMLtoDb() {
     	//getPreferences(); // After downloading the file modification date has changed, which is read in getPreferences()
     	if (db.getLocalActionPath() != "" && db.existsLocalFile()) {
            parsingThread = new TRParser(context, db.getLocalActionPath(), db.getLocalActionListPath(), parsingHandler);
            parsingThread.start();
 	    }
     }
     
public void SetAlarm(Context context)
 {
	Log.d(MainActivity.TAG,"AlarmReceiver.setAlarm()");
     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
     Intent i = new Intent(context, AlarmReceiver.class);
     PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
     
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	 Boolean prefsUseDropbox = prefs.getBoolean("checkBoxSyncWithDropbox", false);
	 String prefsSyncInterval = prefs.getString("prefBtnSync", null);
	 
	 Log.d(MainActivity.TAG,"prefsSyncInterval = " + prefsSyncInterval);
	 if (prefsUseDropbox == true) {
		  Log.d(MainActivity.TAG, "Alarm onReceive, interval: " + prefsSyncInterval);
		  //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute
		  // !!! Use AlarmManager.INTERVAL_* for energy optimization + type should maybe be ELAPSED_REALTIME_WAKEUP
		  // Now every minute for testing.
		  //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pi); // Millisec * Second * Minute
		  if (prefsSyncInterval.equals("NO_INTERVAL")) {
			  CancelAlarm(context);
			  Log.d(MainActivity.TAG,"Sync interval removed.");
		  }
		  if (prefsSyncInterval.equals("INTERVAL_FIFTEEN_MINUTES")) {
			  am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 15, pi); // Millisec * Second * Minute
			  Log.d(MainActivity.TAG,"Sync interval set to: 15 minutes");
		  }
		  if (prefsSyncInterval.equals("INTERVAL_HALF_HOUR")) {
			  am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 30, pi);
			  Log.d(MainActivity.TAG,"Sync interval set to: 30 minutes");
		  }
		  if (prefsSyncInterval.equals("INTERVAL_HOUR")) {
			  am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60, pi);
			  Log.d(MainActivity.TAG,"Sync interval set to: 60 minutes");
		  } 
		  if (prefsSyncInterval.equals("INTERVAL_HALF_DAY")) {
			  am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 12, pi);
			  Log.d(MainActivity.TAG,"Sync interval set to: 12 hours");
		  } 
		  if (prefsSyncInterval.equals("INTERVAL_DAY")) {
			  am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, pi);
			  Log.d(MainActivity.TAG,"Sync interval set to: 24 hours");
		  } 
	 } else {
		Log.d(MainActivity.TAG,"Alarm not set because Dropbox sync was disabled.");
	 }
 }

 public void CancelAlarm(Context context)
 {
     Intent intent = new Intent(context, AlarmReceiver.class);
     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     alarmManager.cancel(sender);
     Log.d(MainActivity.TAG, "Alarm cancelled...");
 }
 
 final Handler downloadHandler = new Handler() {
	 	@Override
	 	public void handleMessage(Message msg) {
	 		/*
	 		 * Handles the messages that are return by the DropboxDownloader thread 
	 		 * Currently it is not updated from DropboxDownloader, maybe in the 
	 		 * future to present more details while downloading.
	 		 */

	 		int total = msg.arg1;
	 		
	 		if (total == MainActivity.PROGRESS_DROPBOXIOEXCEPTION) {
	 			Log.e(MainActivity.TAG, "AlarmReceiver: could not download file");
	 			finished();
	 		}
	 		if (total == MainActivity.PROGRESS_IOEXCEPTION) {
	 			Log.e(MainActivity.TAG, "AlarmReceiver: IOException");
	 			finished();
    		}
    		if (total == MainActivity.PROGRESS_EXCEPTION) {
    			Log.e(MainActivity.TAG, "AlarmReceiver: could not finish downloading.");
	 			finished();
    		}
	 		if (total >= MainActivity.PROGRESS_FINISH){
	 			Log.i(MainActivity.TAG, "AlarmReceiver: file up to date with Dropbox");
	 			parseXMLtoDb();
	 		}
	 	}
	 };
	 final Handler parsingHandler = new Handler() {
	    	@Override
	    	public void handleMessage(Message msg) {
	    		/*
	    		 * Handles the messages that are return by the parsing thread (TRParser)
	    		 */
	    		int total = msg.arg1;
	    		
	    		if (total >= 100){
	    			finished();
	    			/** Update the GUI
	    			MainActivity.getActionLists(MainActivity.getApplicationContext()); // update the action list display when parsing of lists has finished.
	    			setActionsFromDb();
	    			updateRefreshLabel();
	    			updateListGUI();
	    			**/
	    		}
	    	}
	    };
}

