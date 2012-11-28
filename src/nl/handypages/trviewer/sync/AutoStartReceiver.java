package nl.handypages.trviewer.sync;

import nl.handypages.trviewer.MainActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * This BroadcastReceiver is called at boot time. It will instantiate AlarmReceiver to set the period alarm
 * for synchronization with Dropbox.
 * @author nl34904
 *
 */
public class AutoStartReceiver extends BroadcastReceiver
{   
    AlarmReceiver alarm = new AlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent)
    {   
    	Log.d(MainActivity.TAG,"AutoStartReceiver.onReceive()");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
        	alarm.SetAlarm(context);
        }
    }
}
