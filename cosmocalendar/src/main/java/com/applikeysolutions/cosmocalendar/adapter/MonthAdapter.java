package com.applikeysolutions.cosmocalendar.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.applikeysolutions.cosmocalendar.adapter.viewholder.MonthHolder;
import com.applikeysolutions.cosmocalendar.model.Day;
import com.applikeysolutions.cosmocalendar.model.Month;
import com.applikeysolutions.cosmocalendar.selection.BaseSelectionManager;
import com.applikeysolutions.cosmocalendar.settings.lists.DisabledDaysCriteria;
import com.applikeysolutions.cosmocalendar.utils.CalendarUtils;
import com.applikeysolutions.cosmocalendar.utils.DateUtils;
import com.applikeysolutions.cosmocalendar.utils.DayFlag;
import com.applikeysolutions.cosmocalendar.view.CalendarView;
import com.applikeysolutions.cosmocalendar.view.ItemViewType;
import com.applikeysolutions.cosmocalendar.view.delegate.DayDelegate;
import com.applikeysolutions.cosmocalendar.view.delegate.DayOfWeekDelegate;
import com.applikeysolutions.cosmocalendar.view.delegate.MonthDelegate;
import com.applikeysolutions.cosmocalendar.view.delegate.OtherDayDelegate;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MonthAdapter extends RecyclerView.Adapter<MonthHolder> {

    private final List<Month> months;

    private MonthDelegate monthDelegate;

    private CalendarView calendarView;
    private BaseSelectionManager selectionManager;
    private DaysAdapter daysAdapter;

    private MonthAdapter(List<Month> months,
                         MonthDelegate monthDelegate,
                         CalendarView calendarView,
                         BaseSelectionManager selectionManager) {
        setHasStableIds(true);
        this.months = months;
        this.monthDelegate = monthDelegate;
        this.calendarView = calendarView;
        this.selectionManager = selectionManager;
    }

    public void setSelectionManager(BaseSelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    public BaseSelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public MonthHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        daysAdapter = new DaysAdapter.DaysAdapterBuilder()
                .setDayOfWeekDelegate(new DayOfWeekDelegate(calendarView))
                .setOtherDayDelegate(new OtherDayDelegate(calendarView))
                .setDayDelegate(new DayDelegate(calendarView, this))
                .setCalendarView(calendarView)
                .createDaysAdapter();
        return monthDelegate.onCreateMonthHolder(daysAdapter, parent, viewType);
    }

    @Override
    public void onBindViewHolder(MonthHolder holder, int position) {
        final Month month = months.get(position);
        monthDelegate.onBindMonthHolder(month, holder, position);
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ItemViewType.MONTH;
    }

    @Override
    public long getItemId(int position) {
        return months.get(position).getFirstDay().getCalendar().getTimeInMillis();
    }

    public List<Month> getData() {
        return months;
    }

    public void disableDay(Calendar dayToDisableCalendar, boolean disable, boolean belongToMonth) {

        monthsLoop:
        for (Month month : months) {

            Calendar monthFirstDayCalendar = month.getFirstDay().getCalendar();

            if (monthFirstDayCalendar.get(Calendar.YEAR) == dayToDisableCalendar.get(Calendar.YEAR)
                    && monthFirstDayCalendar.get(Calendar.MONTH) == dayToDisableCalendar.get(Calendar.MONTH)) {
                for (Day monthDay : month.getDays()) {
                    if (monthDay.isBelongToMonth() == belongToMonth && monthDay.getCalendar().get(Calendar.YEAR) == dayToDisableCalendar.get(Calendar.YEAR) && monthDay.getCalendar().get(Calendar.DAY_OF_YEAR) == dayToDisableCalendar.get(Calendar.DAY_OF_YEAR)) {
                        monthDay.setDisabled(disable);
                        notifyItemChanged(months.indexOf(month));
                        break monthsLoop;
                    }
                }
            }
        }
    }

    public static class MonthAdapterBuilder {

        private List<Month> months;
        private MonthDelegate monthDelegate;
        private CalendarView calendarView;
        private BaseSelectionManager selectionManager;

        public MonthAdapterBuilder setMonths(List<Month> months) {
            this.months = months;
            return this;
        }

        public MonthAdapterBuilder setMonthDelegate(MonthDelegate monthHolderDelegate) {
            this.monthDelegate = monthHolderDelegate;
            return this;
        }

        public MonthAdapterBuilder setCalendarView(CalendarView calendarView) {
            this.calendarView = calendarView;
            return this;
        }

        public MonthAdapterBuilder setSelectionManager(BaseSelectionManager selectionManager) {
            this.selectionManager = selectionManager;
            return this;
        }

        public MonthAdapter createMonthAdapter() {
            return new MonthAdapter(months,
                    monthDelegate,
                    calendarView,
                    selectionManager);
        }
    }

    public void setWeekendDays(Set<Long> weekendDays) {
        setDaysAccordingToSet(weekendDays, DayFlag.WEEKEND);
    }

    public void setDisabledDays(Set<Long> disabledDays) {
        setDaysAccordingToSet(disabledDays, DayFlag.DISABLED);
    }

    public void setConnectedCalendarDays(Set<Long> connectedCalendarDays) {
        setDaysAccordingToSet(connectedCalendarDays, DayFlag.FROM_CONNECTED_CALENDAR);
    }

    public void setDisabledDaysCriteria(DisabledDaysCriteria criteria) {
        for (Month month : months) {
            for (Day day : month.getDays()) {
                if (!day.isDisabled()) {
                    day.setDisabled(CalendarUtils.isDayDisabledByCriteria(day, criteria));
                }
            }
        }
        notifyDataSetChanged();
    }

    public void resetDisabledDays() {
        for (Month month : months) {
            for (Day day : month.getDays()) {
                day.setDisabled(false);
            }
        }
        notifyDataSetChanged();
    }

    private void setDaysAccordingToSet(Set<Long> days, DayFlag dayFlag) {
        if (days != null && !days.isEmpty()) {
            for (Month month : months) {
                for (Day day : month.getDays()) {
                    switch (dayFlag) {
                        case WEEKEND:
                            day.setWeekend(days.contains(day.getCalendar().get(Calendar.DAY_OF_WEEK)));
                            break;

                        case DISABLED:
                            day.setDisabled(CalendarUtils.isDayInSet(day, days));
                            break;

                        case FROM_CONNECTED_CALENDAR:
                            day.setFromConnectedCalendar(CalendarUtils.isDayInSet(day, days));
                            break;
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public void setMinDate(Calendar minDate) {
        setMinDate(minDate, false);
    }

    public void setMinDate(Calendar minDate, boolean limitCalendarToMinDate) {
        Calendar minDateFirstDayOfMonth = Calendar.getInstance();
        minDateFirstDayOfMonth.setTime(DateUtils.getFirstDayOfMonth(((Calendar) minDate.clone()).getTime()));

        Iterator<Month> monthIterator = months.iterator();
        while (monthIterator.hasNext()){
            Month month = monthIterator.next();

            if(limitCalendarToMinDate && month.isBefore(minDateFirstDayOfMonth)){
                monthIterator.remove();
                continue;
            }

            for (Day day : month.getDays()) {
                if (!day.isDisabled()) {
                    day.setDisabled(CalendarUtils.isDayDisabledByMinDate(day, minDate));
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setMaxDate(Calendar maxDate) {
        setMaxDate(maxDate, false);
    }

    public void setMaxDate(Calendar maxDate, boolean limitCalendarToMaxDate) {
        Calendar maxDateLastDayOfMonth = Calendar.getInstance();
        maxDateLastDayOfMonth.setTime(DateUtils.getLastDayOfMonth(((Calendar) maxDate.clone()).getTime()));

        Iterator<Month> monthIterator = months.iterator();
        while (monthIterator.hasNext()) {
            Month month = monthIterator.next();

            if(limitCalendarToMaxDate && month.isAfter(maxDateLastDayOfMonth)){
                monthIterator.remove();
                continue;
            }

            for (Day day : month.getDays()) {
                if (!day.isDisabled()) {
                    day.setDisabled(CalendarUtils.isDayDisabledByMaxDate(day, maxDate));
                }
            }
        }
        notifyDataSetChanged();
    }
}
