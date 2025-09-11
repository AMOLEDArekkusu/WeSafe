package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_about);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.about_app_title);
            // Remove back arrow - users can use bottom navigation
        }

        // Set app version
        TextView versionText = findViewById(R.id.app_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = getString(R.string.version) + " " + pInfo.versionName;
            versionText.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText(R.string.version);
        }

        // Make GitHub link clickable
        TextView contactInfo = findViewById(R.id.contact_info);
        SpannableString spannableString = new SpannableString(getString(R.string.contact_info));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://github.com/AMOLEDArekkusu/WeSafe"));
                startActivity(intent);
            }
        };

        // Find the position of the URL in the string
        String text = getString(R.string.contact_info);
        int start = text.indexOf("https://");
        if (start != -1) {
            spannableString.setSpan(clickableSpan, start, text.length(), 
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            contactInfo.setText(spannableString);
            contactInfo.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            contactInfo.setText(text);
        }

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_settings); // Set settings as selected since About is accessed from Settings

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_maps) {
                startActivity(new Intent(this, MapActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_contact) {
                startActivity(new Intent(this, EmergencyContactsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(this, IncidentReportActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    // Back navigation removed - users can use bottom navigation
}
