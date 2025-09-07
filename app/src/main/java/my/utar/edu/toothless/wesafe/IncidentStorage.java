package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class IncidentStorage {
    private static final String PREF_NAME = "WeSafeIncidents";
    private static final String KEY_INCIDENTS = "incidents";
    private static final String KEY_TOTAL_COUNT = "total_count";
    private static final String KEY_TODAY_COUNT = "today_count";
    private static final String KEY_LAST_COUNT_DATE = "last_count_date";
    
    private final SharedPreferences preferences;
    private final Gson gson;

    public IncidentStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        checkAndResetDailyCount();
    }

    public void saveIncident(Incident incident) {
        List<Incident> incidents = getIncidents();
        incidents.add(incident);
        
        // Update counts
        int totalCount = preferences.getInt(KEY_TOTAL_COUNT, 0) + 1;
        int todayCount = preferences.getInt(KEY_TODAY_COUNT, 0) + 1;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_INCIDENTS, gson.toJson(incidents));
        editor.putInt(KEY_TOTAL_COUNT, totalCount);
        editor.putInt(KEY_TODAY_COUNT, todayCount);
        editor.apply();
    }

    public List<Incident> getIncidents() {
        String json = preferences.getString(KEY_INCIDENTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Incident>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public int getTotalCount() {
        return preferences.getInt(KEY_TOTAL_COUNT, 0);
    }

    public int getTodayCount() {
        checkAndResetDailyCount();
        return preferences.getInt(KEY_TODAY_COUNT, 0);
    }

    public void incrementIncidentCount() {
        checkAndResetDailyCount();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_TOTAL_COUNT, preferences.getInt(KEY_TOTAL_COUNT, 0) + 1);
        editor.putInt(KEY_TODAY_COUNT, preferences.getInt(KEY_TODAY_COUNT, 0) + 1);
        editor.apply();
    }

    private void checkAndResetDailyCount() {
        long lastCountDate = preferences.getLong(KEY_LAST_COUNT_DATE, 0);
        long today = getDayStart(new Date().getTime());
        
        if (lastCountDate < today) {
            // Reset daily count if it's a new day
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY_TODAY_COUNT, 0);
            editor.putLong(KEY_LAST_COUNT_DATE, today);
            editor.apply();
        }
    }

    private long getDayStart(long timestamp) {
        // Get the start of the day (midnight) for the given timestamp
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
