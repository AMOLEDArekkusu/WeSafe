package my.utar.edu.toothless.wesafe;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat; // Example preference type

/**
 * Activity for app settings and preferences
 */
public class SettingsActivity extends AppCompatActivity {

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

            // Example: Adding a SwitchPreference programmatically
            SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(requireContext());
            notificationPreference.setKey("notifications_new_message");
            notificationPreference.setTitle("Enable new message notifications");
            notificationPreference.setSummary("Receive notifications when a new message arrives");
            notificationPreference.setDefaultValue(true); // Set a default value
            preferenceScreen.addPreference(notificationPreference);

            // Example: Adding another preference
            Preference aboutPreference = new Preference(requireContext());
            aboutPreference.setKey("about_app");
            aboutPreference.setTitle("About App");
            aboutPreference.setSummary("View application version and details");

            // You can set an OnPreferenceClickListener for this preference
            aboutPreference.setOnPreferenceClickListener(preference -> {
                // Handle click, e.g., show a dialog
                return true;
            });
            preferenceScreen.addPreference(aboutPreference);

            // Add more preferences as needed in a similar way
            // For example, EditTextPreference, ListPreference, etc.
        }
    }

    private void hideStatusBar() {
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