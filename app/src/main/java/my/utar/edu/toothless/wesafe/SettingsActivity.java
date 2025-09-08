package my.utar.edu.toothless.wesafe;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.ListPreference;
import java.util.Locale;

/**
 * Activity for app settings and preferences
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load settings fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Create a PreferenceScreen to hold your preferences
            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            setPreferenceScreen(preferenceScreen);

            // Language settings
            ListPreference languagePreference = new ListPreference(requireContext());
            languagePreference.setKey("app_language");
            languagePreference.setTitle(getString(R.string.language));
            languagePreference.setSummary(getString(R.string.change_language));
            languagePreference.setEntries(R.array.language_entries);
            languagePreference.setEntryValues(R.array.language_values);
            
            // Set current language as default
            SharedPreferences prefs = requireContext().getSharedPreferences("WeSafePrefs", Context.MODE_PRIVATE);
            String currentLanguage = prefs.getString("app_language", "default");
            languagePreference.setDefaultValue(currentLanguage);
            languagePreference.setValue(currentLanguage);
            
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String languageCode = (String) newValue;
                updateLanguage(languageCode);
                return true;
            });
            preferenceScreen.addPreference(languagePreference);

            // Notifications settings
            SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(requireContext());
            notificationPreference.setKey("notifications_new_message");
            notificationPreference.setTitle(getString(R.string.enable_notifications));
            notificationPreference.setSummary(getString(R.string.enable_notifications_desc));
            notificationPreference.setDefaultValue(true);
            preferenceScreen.addPreference(notificationPreference);

            // Example: Adding another preference
            Preference aboutPreference = new Preference(requireContext());
            aboutPreference.setKey("about_app");
            aboutPreference.setTitle(getString(R.string.about_app_title));
            aboutPreference.setSummary(getString(R.string.about_summary));

            // Launch AboutActivity when clicked
            aboutPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), AboutActivity.class);
                startActivity(intent);
                return true;
            });
            preferenceScreen.addPreference(aboutPreference);
        }

        private void updateLanguage(String languageCode) {
            // Save the selected language
            SharedPreferences.Editor editor = requireContext().getSharedPreferences("WeSafePrefs", Context.MODE_PRIVATE).edit();
            editor.putString("app_language", languageCode);
            editor.apply();

            // Update current activity's language
            Locale locale;
            if (languageCode.equals("default")) {
                locale = Resources.getSystem().getConfiguration().getLocales().get(0);
            } else {
                locale = new Locale.Builder().setLanguage(languageCode).build();
            }
            
            Locale.setDefault(locale);
            Resources resources = requireContext().getResources();
            Configuration config = new Configuration(resources.getConfiguration());
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            
            // Restart all activities to apply the new language
            Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage(requireContext().getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
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
        onBackPressed();
        return true;
    }
}