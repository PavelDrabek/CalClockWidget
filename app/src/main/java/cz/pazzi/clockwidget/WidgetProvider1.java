package cz.pazzi.clockwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.GridView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Pazzi on 16.9.2015.
 */
public class WidgetProvider1 extends AppWidgetProvider {

    private GridView gridView;
    protected Handler handler = new Handler();

    public WidgetProvider1() {
        super();
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        UpdateClock(remoteViews);
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
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

    private void UpdateClock(RemoteViews remoteViews) {
        remoteViews.setTextViewText(R.id.text_date, GetDateString());
        remoteViews.setTextViewText(R.id.text_time, GetTimeString());
    }
}
