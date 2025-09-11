package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager handles the application's theme settings
 * Supports System Default, Light, and Dark themes
 */
public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String THEME_KEY = "selected_theme";
    
    // Theme constants
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    
    private static ThemeManager instance;
    private SharedPreferences preferences;
    
    private ThemeManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get singleton instance of ThemeManager
     */
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Get the currently selected theme
     */
    public String getCurrentTheme() {
        return preferences.getString(THEME_KEY, THEME_SYSTEM);
    }
    
    /**
     * Set and apply the selected theme
     */
    public void setTheme(String theme) {
        preferences.edit().putString(THEME_KEY, theme).apply();
        applyTheme(theme);
    }
    
    /**
     * Apply the theme to the application
     */
    public void applyTheme(String theme) {
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    /**
     * Apply the current saved theme
     */
    public void applyCurrentTheme() {
        applyTheme(getCurrentTheme());
    }
    
    /**
     * Check if dark theme is currently active
     */
    public boolean isDarkTheme(Context context) {
        String currentTheme = getCurrentTheme();
        if (THEME_DARK.equals(currentTheme)) {
            return true;
        } else if (THEME_LIGHT.equals(currentTheme)) {
            return false;
        } else {
            // System default - check system setting
            int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
    }
    
    /**
     * Get the theme name for display
     */
    public String getThemeDisplayName(Context context, String theme) {
        switch (theme) {
            case THEME_LIGHT:
                return context.getString(R.string.theme_light);
            case THEME_DARK:
                return context.getString(R.string.theme_dark);
            case THEME_SYSTEM:
            default:
                return context.getString(R.string.theme_system);
        }
    }
    
    /**
     * Get the index of current theme in the theme arrays
     */
    public int getCurrentThemeIndex() {
        String currentTheme = getCurrentTheme();
        switch (currentTheme) {
            case THEME_SYSTEM:
                return 0;
            case THEME_LIGHT:
                return 1;
            case THEME_DARK:
                return 2;
            default:
                return 0;
        }
    }
    
    /**
     * Get theme value by index
     */
    public String getThemeByIndex(int index) {
        switch (index) {
            case 0:
                return THEME_SYSTEM;
            case 1:
                return THEME_LIGHT;
            case 2:
                return THEME_DARK;
            default:
                return THEME_SYSTEM;
        }
    }
}
