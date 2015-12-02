package cz.pazzi.clockwidget.data;


import android.graphics.Color;

import java.util.Calendar;

/**
 * Created by pavel on 02.10.15.
 */
public class GEvent {
    public String name;
//    public DateTime start;
//    public DateTime end;

    public Calendar start;
    public Calendar end;

    public int backgroundColor;
    public int foregroundColor;

    public GEvent(String name, Calendar start, Calendar end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public void SetBackgroundColor(String str) {
        backgroundColor = Color.parseColor(str);
    }
    public void SetForegroundColor(String str) {
        foregroundColor = Color.parseColor(str);
    }

    public float DurationInMinutes() {
        return (end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60);
    }

    public int StartAtMinutes() {
//        Log.d("aaa", "hours = " + start.get(Calendar.HOUR_OF_DAY) + ", minutes = " + start.get(Calendar.MINUTE));
        return (start.get(Calendar.HOUR_OF_DAY) - 1) * 60 + start.get(Calendar.MINUTE);
    }
}
