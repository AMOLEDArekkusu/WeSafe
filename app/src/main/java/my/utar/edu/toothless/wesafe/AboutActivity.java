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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about_app_title);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
