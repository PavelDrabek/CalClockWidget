package cz.pazzi.clockwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.GridView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Pazzi on 16.9.2015.
 */
public class WidgetProvider1 extends AppWidgetProvider {

    private GridView gridView;

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

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        UpdateClock(views);
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private String GetTimeString() {
        Date date = Calendar.getInstance().getTime();
        String str = String.format("%02d", date.getHours()) + ":" + String.format("%02d", date.getMinutes());
        return str;
    }

    private String GetDateString() {
        Date date = Calendar.getInstance().getTime();
        String str = String.format("%d.%d.%d", date.getDay(), date.getMonth(), 1900 + date.getYear());
        return str;
    }

    private void UpdateClock(RemoteViews views) {
        views.setTextViewText(R.id.text_date, GetDateString());
        views.setTextViewText(R.id.text_time, GetTimeString());
    }
}
