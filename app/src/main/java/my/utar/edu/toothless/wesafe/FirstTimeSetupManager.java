package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * FirstTimeSetupManager handles first-time user experience
 * Tracks whether the user has completed initial setup
 */
public class FirstTimeSetupManager {
    private static final String PREFS_NAME = "first_time_prefs";
    private static final String KEY_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_BACKGROUND_LOCATION_SHOWN = "background_location_dialog_shown";
    private static final String KEY_SETUP_COMPLETED = "setup_completed";
    
    private static FirstTimeSetupManager instance;
    private SharedPreferences preferences;
    
    private FirstTimeSetupManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get singleton instance of FirstTimeSetupManager
     */
    public static synchronized FirstTimeSetupManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirstTimeSetupManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Check if this is the first time the app is launched
     */
    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    /**
     * Check if background location dialog has been shown
     */
    public boolean hasShownBackgroundLocationDialog() {
        return preferences.getBoolean(KEY_BACKGROUND_LOCATION_SHOWN, false);
    }
    
    /**
     * Mark that background location dialog has been shown
     */
    public void markBackgroundLocationDialogShown() {
        preferences.edit()
                .putBoolean(KEY_BACKGROUND_LOCATION_SHOWN, true)
                .apply();
    }
    
    /**
     * Mark that first launch setup has been completed
     */
    public void completeFirstLaunch() {
        preferences.edit()
                .putBoolean(KEY_FIRST_LAUNCH, false)
                .putBoolean(KEY_SETUP_COMPLETED, true)
                .apply();
    }
    
    /**
     * Check if initial setup has been completed
     */
    public boolean isSetupCompleted() {
        return preferences.getBoolean(KEY_SETUP_COMPLETED, false);
    }
    
    /**
     * Reset first time setup (for testing purposes)
     */
    public void resetFirstTimeSetup() {
        preferences.edit()
                .putBoolean(KEY_FIRST_LAUNCH, true)
                .putBoolean(KEY_BACKGROUND_LOCATION_SHOWN, false)
                .putBoolean(KEY_SETUP_COMPLETED, false)
                .apply();
    }
    
    /**
     * Check if we should show background location dialog
     * Only show on first launch and if not shown before
     */
    public boolean shouldShowBackgroundLocationDialog() {
        return isFirstLaunch() && !hasShownBackgroundLocationDialog();
    }
    
    /**
     * Get debug information about the current setup state
     */
    public String getDebugInfo() {
        return String.format("FirstLaunch: %b, DialogShown: %b, SetupCompleted: %b", 
                isFirstLaunch(), hasShownBackgroundLocationDialog(), isSetupCompleted());
    }
}
