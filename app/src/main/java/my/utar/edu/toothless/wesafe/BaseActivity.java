package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("WeSafePrefs", MODE_PRIVATE);
        String languageCode = prefs.getString("app_language", "default");
        
        Context context = newBase;
        if (!languageCode.equals("default")) {
            Locale newLocale = new Locale.Builder().setLanguage(languageCode).build();
            Locale.setDefault(newLocale);
            
            Configuration config = new Configuration(newBase.getResources().getConfiguration());
            config.setLocale(newLocale);
            context = newBase.createConfigurationContext(config);
        }
        
        super.attachBaseContext(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCurrentLanguage();
    }
    
    protected void applyCurrentLanguage() {
        SharedPreferences prefs = getSharedPreferences("WeSafePrefs", MODE_PRIVATE);
        String languageCode = prefs.getString("app_language", "default");
        
        if (!languageCode.equals("default")) {
            Locale newLocale = new Locale.Builder().setLanguage(languageCode).build();
            Locale.setDefault(newLocale);
            
            Resources resources = getResources();
            Configuration config = new Configuration(resources.getConfiguration());
            config.setLocale(newLocale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences prefs = getSharedPreferences("WeSafePrefs", MODE_PRIVATE);
        String languageCode = prefs.getString("app_language", "default");
        
        if (!languageCode.equals("default")) {
            Locale newLocale = new Locale.Builder().setLanguage(languageCode).build();
            Locale.setDefault(newLocale);
            Configuration config = new Configuration(newConfig);
            config.setLocale(newLocale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }
    
    protected void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                     | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                     | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                     | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
