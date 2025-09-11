package my.utar.edu.toothless.wesafe;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.widget.Toolbar;
import java.util.Locale;

/**
 * Activity for app settings and preferences
 */
public class SettingsActivity extends BaseActivity {

    private MaterialSwitch notificationSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("WeSafePrefs", Context.MODE_PRIVATE);

        setupToolbar();
        setupUI();
        setupBottomNavigation();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupUI() {
        // Profile card click listener
        MaterialCardView profileCard = findViewById(R.id.card_profile);
        profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Language card click listener
        MaterialCardView languageCard = findViewById(R.id.card_language);
        languageCard.setOnClickListener(v -> {
            showLanguageDialog();
        });

        // Theme card click listener
        MaterialCardView themeCard = findViewById(R.id.card_theme);
        themeCard.setOnClickListener(v -> {
            showThemeDialog();
        });

        // Notification switch
        notificationSwitch = findViewById(R.id.switch_notifications);
        boolean notificationsEnabled = prefs.getBoolean("notifications_new_message", true);
        notificationSwitch.setChecked(notificationsEnabled);
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notifications_new_message", isChecked);
            editor.apply();
        });

        // About card click listener
        MaterialCardView aboutCard = findViewById(R.id.card_about);
        aboutCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
        
        // Developer card click listener
        MaterialCardView developerCard = findViewById(R.id.card_developer);
        developerCard.setOnClickListener(v -> showDeveloperOptions());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_maps) {
                Intent intent = new Intent(this, MapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_contact) {
                Intent intent = new Intent(this, EmergencyContactsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_home) {
                // Navigate back to main home screen
                navigateBackToMain();
                return true;
            } else if (itemId == R.id.nav_report) {
                Intent intent = new Intent(this, IncidentReportActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Already in settings
                return true;
            }
            return false;
        });
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Bahasa Malaysia", "简体中文"};
        String[] languageCodes = {"en", "ms", "zh-CN"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_language_title));
        
        String currentLanguage = prefs.getString("app_language", "en");
        int checkedItem = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                checkedItem = i;
                break;
            }
        }
        
        builder.setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
            String selectedLanguage = languageCodes[which];
            updateLanguage(selectedLanguage);
            dialog.dismiss();
        });
        
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showThemeDialog() {
        String[] themeNames = getResources().getStringArray(R.array.theme_names);
        String[] themeValues = getResources().getStringArray(R.array.theme_values);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_theme));
        
        ThemeManager themeManager = ThemeManager.getInstance(this);
        String currentTheme = themeManager.getCurrentTheme();
        int checkedItem = 0;
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(currentTheme)) {
                checkedItem = i;
                break;
            }
        }
        
        builder.setSingleChoiceItems(themeNames, checkedItem, (dialog, which) -> {
            String selectedTheme = themeValues[which];
            themeManager.setTheme(selectedTheme);
            dialog.dismiss();
            
            // Recreate activity to apply new theme
            recreate();
        });
        
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }
    
    /**
     * Show developer options dialog
     */
    private void showDeveloperOptions() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.developer_title))
                .setItems(new String[]{getString(R.string.reset_first_time_setup)}, (dialog, which) -> {
                    if (which == 0) {
                        // Reset first-time setup
                        FirstTimeSetupManager setupManager = FirstTimeSetupManager.getInstance(this);
                        setupManager.resetFirstTimeSetup();
                        
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.reset_successful))
                                .setMessage(getString(R.string.reset_first_time_setup_message))
                                .setPositiveButton(getString(R.string.ok), null)
                                .show();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();
    }

    private void navigateBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void updateLanguage(String languageCode) {
        // Save the selected language
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language", languageCode);
        editor.apply();

        // Update current activity's language
        Locale locale;
        if (languageCode.equals("default")) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else if (languageCode.equals("zh-CN")) {
            // Handle Chinese Simplified properly
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = new Locale.Builder().setLanguage(languageCode).build();
        }
        
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        // Restart all activities to apply the new language
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                     | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                     | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar(); // Re-hide status bar when activity resumes
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToMain();
        return true;
    }

    @Override
    public void onBackPressed() {
        navigateBackToMain();
    }
}