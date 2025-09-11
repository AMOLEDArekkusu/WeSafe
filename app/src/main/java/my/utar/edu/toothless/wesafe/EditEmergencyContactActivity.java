package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for adding or editing emergency contact information
 */
public class EditEmergencyContactActivity extends BaseActivity {

    private TextInputEditText etName, etPhone, etEmail;
    private Spinner spinnerType;
    private Switch switchPrimary, switchReceiveAlerts, switchReceiveLocationUpdates;
    private Button btnSave, btnDelete;

    private boolean isEditMode = false;
    private EmergencyContact contactToEdit;
    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_edit_emergency_contact);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupSpinners();
        checkEditMode();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        spinnerType = findViewById(R.id.spinner_type);
        switchPrimary = findViewById(R.id.switch_primary);
        switchReceiveAlerts = findViewById(R.id.switch_receive_alerts);
        switchReceiveLocationUpdates = findViewById(R.id.switch_receive_location_updates);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        
        // Debug logging
        android.util.Log.d("EditEmergencyContact", "btnDelete initialized: " + (btnDelete != null));
        if (btnDelete == null) {
            android.util.Log.e("EditEmergencyContact", "btnDelete is null! Check if btn_delete exists in layout");
        }
        
        // Test string resources
        try {
            String deleteTitle = getString(R.string.delete_contact_title);
            String deleteMessage = getString(R.string.delete_contact_message);
            String deleteText = getString(R.string.delete);
            String cancelText = getString(R.string.cancel);
            
            android.util.Log.d("EditEmergencyContact", "String resources loaded successfully:");
            android.util.Log.d("EditEmergencyContact", "Delete title: " + deleteTitle);
            android.util.Log.d("EditEmergencyContact", "Delete message: " + deleteMessage);
            android.util.Log.d("EditEmergencyContact", "Delete text: " + deleteText);
            android.util.Log.d("EditEmergencyContact", "Cancel text: " + cancelText);
        } catch (Exception e) {
            android.util.Log.e("EditEmergencyContact", "Error loading string resources", e);
        }
    }

    private void setupSpinners() {
        // Set up contact type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.contact_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void checkEditMode() {
        // Check if we're editing an existing contact
        if (getIntent().hasExtra("contact")) {
            isEditMode = true;
            contactToEdit = getIntent().getParcelableExtra("contact");
            editPosition = getIntent().getIntExtra("position", -1);

            // Pre-fill the form with existing data
            populateFormWithContactData();

            // Update UI for edit mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Emergency Contact");
            }
            
            // Show delete button only in edit mode
            if (btnDelete != null) {
                btnDelete.setVisibility(View.VISIBLE);
                android.util.Log.d("EditEmergencyContact", "Delete button set to visible in edit mode");
            } else {
                android.util.Log.e("EditEmergencyContact", "btnDelete is null when trying to set visibility");
            }
        } else {
            // Hide delete button in add mode
            if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
                android.util.Log.d("EditEmergencyContact", "Delete button set to gone in add mode");
            }
        }
    }

    private void populateFormWithContactData() {
        if (contactToEdit != null) {
            etName.setText(contactToEdit.getName() != null ? contactToEdit.getName() : "");
            etPhone.setText(contactToEdit.getPhone() != null ? contactToEdit.getPhone() : "");
            etEmail.setText(contactToEdit.getEmail() != null ? contactToEdit.getEmail() : "");

            // Set spinner selection with proper null checking
            try {
                @SuppressWarnings("unchecked")
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerType.getAdapter();
                if (adapter != null && contactToEdit.getType() != null) {
                    int position = adapter.getPosition(contactToEdit.getType());
                    if (position >= 0) {
                        spinnerType.setSelection(position);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Set to default position (0) if there's an error
                spinnerType.setSelection(0);
            }

            // Set switch states
            switchPrimary.setChecked(contactToEdit.isPrimary());
            switchReceiveAlerts.setChecked(contactToEdit.receivesAlerts());
            switchReceiveLocationUpdates.setChecked(contactToEdit.receivesLocationUpdates());
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveContact());
        
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                android.util.Log.d("EditEmergencyContact", "Delete button clicked");
                showDeleteConfirmationDialog();
            });
            android.util.Log.d("EditEmergencyContact", "Delete button click listener set");
        } else {
            android.util.Log.e("EditEmergencyContact", "btnDelete is null in setupClickListeners");
        }
    }

    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            // Don't set selected item as this is a sub-activity
            
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
    }

    private void saveContact() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Get values from form with null safety
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            
            // Safe spinner selection with null checking
            String type = "Other"; // Default value
            if (spinnerType.getSelectedItem() != null) {
                type = spinnerType.getSelectedItem().toString();
            }
            
            boolean isPrimary = switchPrimary.isChecked();
            boolean receivesAlerts = switchReceiveAlerts.isChecked();
            boolean receivesLocation = switchReceiveLocationUpdates.isChecked();

            // Create or update contact
            EmergencyContact contact = new EmergencyContact(name, phone, email, type,
                    isPrimary, receivesAlerts, receivesLocation);

            // Return the edited contact to the calling activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("contact", contact);

            // If editing, also send back the position
            if (isEditMode) {
                resultIntent.putExtra("position", editPosition);
                resultIntent.putExtra("isEdit", true);
            } else {
                resultIntent.putExtra("isEdit", false);
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message to user
            Toast.makeText(this, "Error saving contact. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Safe null checking for EditText
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etName.setError("Name is required");
            isValid = false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            isValid = false;
        }
        return isValid;
    }

    @Override
    protected void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                     | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                     | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                     | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar(); // Re-hide status bar when activity resumes
    }

    private boolean hasUnsavedChanges() {
        // Safe null checking for all EditText fields
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        
        // Safe spinner selection with null checking
        String type = "Other"; // Default value
        if (spinnerType.getSelectedItem() != null) {
            type = spinnerType.getSelectedItem().toString();
        }
        
        boolean isPrimary = switchPrimary.isChecked();
        boolean receivesAlerts = switchReceiveAlerts.isChecked();
        boolean receivesLocation = switchReceiveLocationUpdates.isChecked();

        if (isEditMode && contactToEdit != null) {
            // Check if any field has changed
            return !name.equals(contactToEdit.getName() != null ? contactToEdit.getName() : "") ||
                   !phone.equals(contactToEdit.getPhone() != null ? contactToEdit.getPhone() : "") ||
                   !email.equals(contactToEdit.getEmail() != null ? contactToEdit.getEmail() : "") ||
                   !type.equals(contactToEdit.getType() != null ? contactToEdit.getType() : "Other") ||
                   isPrimary != contactToEdit.isPrimary() ||
                   receivesAlerts != contactToEdit.receivesAlerts() ||
                   receivesLocation != contactToEdit.receivesLocationUpdates();
        } else {
            // For new contact, check if any field has been filled
            return !name.isEmpty() || !phone.isEmpty() || !email.isEmpty();
        }
    }

    private void showDiscardDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.unsaved_changes_title))
                .setMessage(getString(R.string.unsaved_changes_message))
                .setPositiveButton(getString(R.string.discard), (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton(getString(R.string.keep_editing), null)
                .show();
    }

    private void showDeleteConfirmationDialog() {
        android.util.Log.d("EditEmergencyContact", "showDeleteConfirmationDialog called");
        
        try {
            // Test if basic dialog works first
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete_contact_title));
            builder.setMessage(getString(R.string.delete_contact_message));
            builder.setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                android.util.Log.d("EditEmergencyContact", "Delete button clicked in dialog");
                deleteContact();
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
                android.util.Log.d("EditEmergencyContact", "Cancel button clicked in dialog");
                dialogInterface.dismiss();
            });
            
            AlertDialog dialog = builder.create();
            dialog.show();
            android.util.Log.d("EditEmergencyContact", "Dialog.show() called successfully");
        } catch (Exception e) {
            android.util.Log.e("EditEmergencyContact", "Error showing delete dialog", e);
            
            // Fallback - simple dialog
            try {
                new AlertDialog.Builder(this)
                    .setTitle("Delete Contact")
                    .setMessage("Are you sure you want to delete this contact?")
                    .setPositiveButton("Delete", (dialogInterface, which) -> deleteContact())
                    .setNegativeButton("Cancel", null)
                    .show();
            } catch (Exception fallbackException) {
                android.util.Log.e("EditEmergencyContact", "Even fallback dialog failed", fallbackException);
                Toast.makeText(this, "Error showing delete confirmation dialog", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteContact() {
        try {
            // Return result indicating contact should be deleted
            Intent resultIntent = new Intent();
            resultIntent.putExtra("position", editPosition);
            resultIntent.putExtra("isDelete", true);
            
            setResult(RESULT_OK, resultIntent);
            
            // Show success message
            Toast.makeText(this, getString(R.string.contact_deleted), Toast.LENGTH_SHORT).show();
            
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error deleting contact. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (hasUnsavedChanges()) {
                showDiscardDialog();
            } else {
                onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}