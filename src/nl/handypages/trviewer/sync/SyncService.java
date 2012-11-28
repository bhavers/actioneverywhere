package nl.handypages.trviewer.sync;

import nl.handypages.trviewer.MainActivity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service
{
    AlarmReceiver alarm = new AlarmReceiver();
    public void onCreate()
    {
        super.onCreate();       
    }

    public void onStart(Context context,Intent intent, int startId)
    {
    	alarm.SetAlarm(context);
        Log.d(MainActivity.TAG, "SyncService.onStart");
    }

    @Override
    public IBinder onBind(Intent intent) 
    {
        return null;
    }
}
