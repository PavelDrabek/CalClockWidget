package cz.pazzi.clockwidget.Interfaces;

import java.util.List;

import cz.pazzi.clockwidget.data.GCalendar;

/**
 * Created by pavel on 03.11.15.
 */
public interface ICalendarList {
    void OnCalendarsDownloaded(List<GCalendar> calendars);
}
