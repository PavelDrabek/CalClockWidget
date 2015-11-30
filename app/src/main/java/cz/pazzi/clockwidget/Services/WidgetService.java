package cz.pazzi.clockwidget.Services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.List;

import cz.pazzi.clockwidget.WidgetProvider1;
import cz.pazzi.clockwidget.data.GEvent;

/**
 * Created by pavel on 07.10.15.
 */
public class WidgetService extends Service {
    public static final int UPDATE_CLOCK = 0;
    public static final int UPDATE_CALENDAR = 1;

    private List<GEvent> calEvents = null;
    private long lastCalendarCheck = 0;
    private int intervalCalendarCheck = 1 * 60 * 1000;

    private MyHandler handler = new MyHandler();

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case WidgetService.UPDATE_CLOCK:
                    Log.d("WidgetService", "update clock");
                    BroadcastWidgets();
                    break;
            }
        }

        private void BroadcastWidgets() {
            Intent intent = new Intent(getApplicationContext(), WidgetProvider1.class);
            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider1.class));

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    };

    Thread clockThread = new Thread() {
        public boolean canRun = true;

        public void run() {
            synchronized (this) {
                while (canRun)
                {
                    Log.d("thread", "loop");

                    long timeInMs = System.currentTimeMillis();

                    handler.sendMessage(handler.obtainMessage(WidgetService.UPDATE_CLOCK));

//                    if(lastCalendarCheck + intervalCalendarCheck < timeInMs) {
//                        lastCalendarCheck = timeInMs;
//                        handler.sendMessage(handler.obtainMessage(WidgetService.UPDATE_CALENDAR));
//                    }

                    try {
                        int timeToNextMinute = 60000 - (int)(timeInMs % 60000);
                        Log.d("thread", "time to next minute = " + timeToNextMinute);
                        sleep(timeToNextMinute);
                    } catch (Exception e) {
                        Log.e(getPackageName(), "exception = " + e.toString());
                    }
                }
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("WidgetService", "onCreate");

        clockThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("WidgetService", "onDestroy");
        clockThread.interrupt();
        super.onDestroy();
    }
}
