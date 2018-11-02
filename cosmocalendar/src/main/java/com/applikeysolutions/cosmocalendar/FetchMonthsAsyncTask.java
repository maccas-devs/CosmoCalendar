package com.applikeysolutions.cosmocalendar;

import android.os.AsyncTask;
import android.util.Log;

import com.applikeysolutions.cosmocalendar.adapter.MonthAdapter;
import com.applikeysolutions.cosmocalendar.model.Month;
import com.applikeysolutions.cosmocalendar.settings.SettingsManager;
import com.applikeysolutions.cosmocalendar.utils.CalendarUtils;
import com.applikeysolutions.cosmocalendar.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by leonardo on 08/10/17.
 */

public class FetchMonthsAsyncTask extends AsyncTask<FetchMonthsAsyncTask.FetchParams, Void, List<Month>> {

    private boolean future;
    private MonthAdapter monthAdapter;
    private int defaultMonthCount;

    @Override
    protected List<Month> doInBackground(FetchParams... fetchParams) {
        FetchParams params = fetchParams[0];
        Month month = params.month;
        future = params.future;
        SettingsManager settingsManager = params.settingsManager;
        monthAdapter = params.monthAdapter;
        defaultMonthCount = params.defaultMonthCount;

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(month.getFirstDay().getCalendar().getTime());
        final List<Month> result = new ArrayList<>();

        Calendar minDateFirstDayOfMonth = null;
        if(settingsManager != null && settingsManager.getMinDate() != null){
            minDateFirstDayOfMonth = Calendar.getInstance();
            minDateFirstDayOfMonth.setTime(DateUtils.getFirstDayOfMonth(((Calendar) settingsManager.getMinDate().clone()).getTime()));
        }

        Calendar maxDateLastDayOfMonth = null;
        if(settingsManager != null && settingsManager.getMaxDate() != null){
            maxDateLastDayOfMonth = Calendar.getInstance();
            maxDateLastDayOfMonth.setTime(DateUtils.getLastDayOfMonth(((Calendar) settingsManager.getMaxDate().clone()).getTime()));
        }

        for (int i = 0; i < SettingsManager.DEFAULT_MONTH_COUNT; i++) {
            if(isCancelled())
                break;

            calendar.add(Calendar.MONTH, future ? 1 : -1);
            Month newMonth = CalendarUtils.createMonth(calendar.getTime(), settingsManager);
            if (future) {
                if(maxDateLastDayOfMonth != null &&  newMonth.getLastDayCalendar().compareTo(maxDateLastDayOfMonth) > 0){
                    break;
                }
                result.add(newMonth);
            } else {
                if(minDateFirstDayOfMonth != null &&  newMonth.getFirstDay().getCalendar().compareTo(minDateFirstDayOfMonth) < 0){
                    break;
                }
                result.add(0, newMonth);
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Month> months) {
        if (!months.isEmpty()) {
            if (future) {
                monthAdapter.getData().addAll(months);
                monthAdapter.notifyItemRangeInserted(monthAdapter.getData().size() - 1, defaultMonthCount);
            } else {
                monthAdapter.getData().addAll(0, months);
                monthAdapter.notifyItemRangeInserted(0, defaultMonthCount);
            }
        }
    }

    public static class FetchParams {
        private final boolean future;
        private final Month month;
        private final SettingsManager settingsManager;
        private final MonthAdapter monthAdapter;
        private final int defaultMonthCount;

        public FetchParams(boolean future, Month month, SettingsManager settingsManager, MonthAdapter monthAdapter, int defaultMonthCount) {
            this.future = future;
            this.month = month;
            this.settingsManager = settingsManager;
            this.monthAdapter = monthAdapter;
            this.defaultMonthCount = defaultMonthCount;
        }
    }
}
