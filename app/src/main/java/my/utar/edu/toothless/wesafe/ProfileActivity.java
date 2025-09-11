package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileActivity extends BaseActivity {

    private TextInputEditText etFullName, etPhone, etEmail;
    private MaterialButton btnSave, btnAddContact;
    private RecyclerView rvEmergencyContacts;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("WeSafePrefs", MODE_PRIVATE);

        setupToolbar();
        setupUI();
        setupBottomNavigation();
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.profile_title));
        }
        
        toolbar.setNavigationOnClickListener(v -> navigateBackToSettings());
    }

    private void setupUI() {
        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        btnAddContact = findViewById(R.id.btn_add_contact);
        rvEmergencyContacts = findViewById(R.id.rv_emergency_contacts);

        // Save button click listener
        btnSave.setOnClickListener(v -> saveUserData());

        // Add emergency contact button
        btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmergencyContactsActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        // Don't set selected item as this is a sub-activity
        
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
                navigateBackToSettings();
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        // Load saved user data from SharedPreferences
        String fullName = prefs.getString("user_full_name", "");
        String phone = prefs.getString("user_phone", "");
        String email = prefs.getString("user_email", "");

        etFullName.setText(fullName);
        etPhone.setText(phone);
        etEmail.setText(email);
    }

    private void saveUserData() {
        // Save user data to SharedPreferences
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_full_name", fullName);
        editor.putString("user_phone", phone);
        editor.putString("user_email", email);
        editor.apply();

        // Show success message
        android.widget.Toast.makeText(this, getString(R.string.profile_saved_successfully), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void navigateBackToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void navigateBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
        hideStatusBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToSettings();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateBackToSettings();
    }
}
