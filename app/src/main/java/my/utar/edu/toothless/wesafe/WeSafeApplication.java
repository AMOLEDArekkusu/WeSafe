package my.utar.edu.toothless.wesafe;

import android.app.Application;

/**
 * WeSafe Application class
 * Initializes app-wide settings including theme
 */
public class WeSafeApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize and apply the saved theme
        ThemeManager.getInstance(this).applyCurrentTheme();
    }
}
