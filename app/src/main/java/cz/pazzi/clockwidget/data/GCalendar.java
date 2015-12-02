package cz.pazzi.clockwidget.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavel on 03.11.15.
 */
public class GCalendar {
    public String id;
    public String summary;
    public String backgroundColor;
    public String foregroundColor;

    public List<GEvent> events;

    public GCalendar(String id, String summary, String backgroundColor, String foregroundColor, List<GEvent> events) {
        this.id = id;
        this.summary = summary;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.events = events;
    }

    public GCalendar(String id, String summary, String backgroundColor, String foregroundColor) {
        this(id, summary, backgroundColor, foregroundColor, new ArrayList<GEvent>());
    }
}
