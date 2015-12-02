package cz.pazzi.clockwidget.Services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.List;

import cz.pazzi.clockwidget.Interfaces.ICalendarListWatcher;
import cz.pazzi.clockwidget.Providers.WidgetProvider1;
import cz.pazzi.clockwidget.Providers.GoogleProvider;
import cz.pazzi.clockwidget.data.GCalendar;

/**
 * Created by pavel on 07.10.15.
 */
public class WidgetService extends Service implements ICalendarListWatcher {
    public static final int UPDATE_CLOCK = 0;
    public static final int UPDATE_CALENDAR = 1;

    private long lastCalendarCheck = 0;
    private int intervalCalendarCheck = 1 * 60 * 1000;

    private MyHandler handler = new MyHandler();

    // TODO: Service jako singleton?

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case WidgetService.UPDATE_CLOCK:
                    Log.d("WidgetService", "update clock");
                    BroadcastWidgets();
                    break;
                case WidgetService.UPDATE_CALENDAR:
                    GoogleProvider.getInstance().DownloadCalendars();
                    break;
            }
        }

        public void BroadcastWidgets() {
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

                    if(lastCalendarCheck + intervalCalendarCheck < timeInMs) {
                        lastCalendarCheck = timeInMs;
                        handler.sendMessage(handler.obtainMessage(WidgetService.UPDATE_CALENDAR));
                    }

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

        Context context = getBaseContext();
        GoogleProvider gProvider = GoogleProvider.getInstance();
        gProvider.Init(context);
        gProvider.AddListener(this);

        if(gProvider.IsAccountSelected()) {
            gProvider.DownloadCalendars();
        } else {
            gProvider.ShowChooseAccount();
        }

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

    @Override
    public void OnCalendarsDownloaded(List<GCalendar> calendars) {
        handler.BroadcastWidgets();
    }

    @Override
    public void OnCalendarsError(String error) {

    }
}
