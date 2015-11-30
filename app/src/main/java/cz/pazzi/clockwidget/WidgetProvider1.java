package cz.pazzi.clockwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.GridView;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import cz.pazzi.clockwidget.Activities.WidgetPreference;
import cz.pazzi.clockwidget.AsyncTasks.EventListAsyncTask;
import cz.pazzi.clockwidget.Interfaces.IEventListWatcher;
import cz.pazzi.clockwidget.Services.WidgetService;
import cz.pazzi.clockwidget.data.GEvent;

/**
 * Created by Pazzi on 16.9.2015.
 */
public class WidgetProvider1 extends AppWidgetProvider implements IEventListWatcher {

    private GridView gridView;

    private static final String ONCLICK_CLOCK = "clickOnClock";

    public WidgetProvider1() {
        super();
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WidgetProvider", "onEnabled");

        context.startService(new Intent(context, WidgetService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("WidgetProvider", "updating");
//        Toast.makeText(context, "onUpdate clock " + Arrays.toString(appWidgetIds), Toast.LENGTH_SHORT).show();

//        List<GEvent> events = new ArrayList<GEvent>();
//        events.add(new GEvent("test", new DateTime(1241000), new DateTime(1241521)));
//        events.add(new GEvent("test2", new DateTime(1241000), new DateTime(1241521)));

        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//            Toast.makeText(context, "onUpdate widget id is: " + widgetId, Toast.LENGTH_SHORT).show();
            remoteViews.setOnClickPendingIntent(R.id.imgSettings, getPendingSelfIntent(context, widgetId, ONCLICK_CLOCK));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        new EventListAsyncTask(context, this, appWidgetIds).execute();

        UpdateClock(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WidgetProvider", "onReceive: " + intent.getAction());

        if(ONCLICK_CLOCK.equals(intent.getAction())) {
            int rnd = new Random().nextInt(100);
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//            Toast.makeText(context, "onclick clock " + widgetId + " " + rnd, Toast.LENGTH_SHORT).show();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//            remoteViews.setTextViewText(R.id.txtRandom, String.valueOf(rnd));
            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
        }
        super.onReceive(context, intent);
    }

    private String GetTimeString() {
        Calendar cal = Calendar.getInstance();
//        String str = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        String myString = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
        return myString;
    }

    private String GetDateString() {
        Calendar cal = Calendar.getInstance();
//        String str = String.format("%2d.%2d.%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
//        SimpleDateFormat date = new SimpleDateFormat("EEE, MMMM d", Locale.getDefault());
        String myString = DateFormat.getDateInstance(DateFormat.LONG).format(cal.getTime());
        return myString;

//        return date.format(cal.getTime());
    }

    private void UpdateClock(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        remoteViews.setTextViewText(R.id.text_date, GetDateString());
        remoteViews.setTextViewText(R.id.text_time, GetTimeString());

        ComponentName widgetComponent = new ComponentName(context, WidgetProvider1.class);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetComponent, remoteViews);
    }

    private void UpdateTimeLine(Context context, int widgetId, List<GEvent> events) {

        Bitmap bitmap = GetTimelineBitmap(events);

        DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int boundingX = Math.round(480 * metrics.density);
        int boundingY = Math.round(40f * metrics.density);
        float xScale = ((float) boundingX) / width;
        float yScale = ((float) boundingY) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix , false);


        BitmapDrawable bd = new BitmapDrawable(context.getResources(), scaledBitmap);
        bd.setAntiAlias(false);

        remoteViews.setImageViewBitmap(R.id.imgTimeline, bd.getBitmap());

        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
    }

    protected PendingIntent getPendingSelfIntent(Context context, int widgetId, String action) {
        Intent intent = new Intent(context, WidgetPreference.class);
//        Intent intent = new Intent(context, WidgetProvider1.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
//        Log.d("WidgetProvider", "setting intent id = " + widgetId);
//        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    Bitmap GetTimelineBitmap(List<GEvent> events) {
        int bitmapSize = 240;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, 1, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);

        float density = bitmapSize / (float)(24 * 60);
        for(GEvent e : events) {
            AddEventToBitmap(bitmap, e, density);
        }
        return bitmap;
    }

    void AddEventToBitmap(Bitmap bitmap, GEvent event, float densityPerMinute) {
        int pixelCount = (int)(densityPerMinute * event.DurationInMinutes());
        int pixelOffset = (int)(densityPerMinute * event.StartAtMinutes());
        Log.d("addEventToBitmap", "event duration = " + event.DurationInMinutes());
        Log.d("addEventToBitmap", "density per minute = " + densityPerMinute);
        Log.d("addEventToBitmap", "pixel count = " + pixelCount);
        Log.d("addEventToBitmap", "pixel offset = " + pixelOffset);
        Log.d("addEventToBitmap", "event color = " + event.backgroundColor);

        for(int i = 0; i < pixelCount; i++) {
            bitmap.setPixel(pixelOffset + i, 0, event.backgroundColor);
//            bitmap.setPixel(i, 0, Color.BLUE);
        }
    }

    @Override
    public void OnEventsDownloaded(List<GEvent> events, Context context, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            UpdateTimeLine(context, widgetId, events);
        }
        Log.d("asyncEvents", "onDownloaded - event count = " + events.size());
    }

    @Override
    public void OnEventsError(String error) {

    }
}
