package com.applikeysolutions.cosmocalendar.model;

import com.applikeysolutions.cosmocalendar.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Month {

    private List<Day> days;
    private Day firstDay;

    public Month(Day firstDay, List<Day> days) {
        this.days = days;
        this.firstDay = firstDay;
    }

    public Day getFirstDay() {
        return firstDay;
    }

    public void setFirstDay(Day firstDay) {
        this.firstDay = firstDay;
    }

    public Calendar getLastDayCalendar(){
        Calendar monthLastDay = Calendar.getInstance();
        monthLastDay.setTime(DateUtils.getLastDayOfMonth(((Calendar) firstDay.getCalendar().clone()).getTime()));
        return monthLastDay;
    }

    public boolean isBefore(Calendar calendar){
        Calendar firstDayCalendar = this.getFirstDay().getCalendar();
        return calendar.get(Calendar.YEAR) > firstDayCalendar.get(Calendar.YEAR)
                || (calendar.get(Calendar.YEAR) == firstDayCalendar.get(Calendar.YEAR) &&  (calendar.get(Calendar.DAY_OF_YEAR) > firstDayCalendar.get(Calendar.DAY_OF_YEAR)));
    }

    public boolean isAfter(Calendar calendar){
        Calendar lastDayCalendar = this.getLastDayCalendar();
        return calendar.get(Calendar.YEAR) < lastDayCalendar.get(Calendar.YEAR)
                || (calendar.get(Calendar.YEAR) == lastDayCalendar.get(Calendar.YEAR) &&  (calendar.get(Calendar.DAY_OF_YEAR) < lastDayCalendar.get(Calendar.DAY_OF_YEAR)));
    }

    public List<Day> getDays() {
        return days;
    }

    /**
     * Returns selected days that belong only to current month
     *
     * @return
     */
    public List<Day> getDaysWithoutTitlesAndOnlyCurrent() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstDay.getCalendar().getTime());
        int currentMonth = calendar.get(Calendar.MONTH);

        List<Day> result = new ArrayList<>();
        for (Day day : days) {
            calendar.setTime(day.getCalendar().getTime());
            if (!(day instanceof DayOfWeek) && calendar.get(Calendar.MONTH) == currentMonth) {
                result.add(day);
            }
        }
        return result;
    }

    public String getMonthName() {
        return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDay.getCalendar().getTime());
    }


}
