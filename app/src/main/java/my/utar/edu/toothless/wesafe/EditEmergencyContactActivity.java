package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for adding or editing emergency contact information
 */
public class EditEmergencyContactActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail;
    private Spinner spinnerType;
    private Switch switchPrimary, switchReceiveAlerts, switchReceiveLocationUpdates;
    private Button btnSave;

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
        }
    }

    private void populateFormWithContactData() {
        if (contactToEdit != null) {
            etName.setText(contactToEdit.getName());
            etPhone.setText(contactToEdit.getPhone());
            etEmail.setText(contactToEdit.getEmail());

            // Set spinner selection
            ArrayAdapter adapter = (ArrayAdapter) spinnerType.getAdapter();
            int position = adapter.getPosition(contactToEdit.getType());
            spinnerType.setSelection(position);

            // Set switch states
            switchPrimary.setChecked(contactToEdit.isPrimary());
            switchReceiveAlerts.setChecked(contactToEdit.receivesAlerts());
            switchReceiveLocationUpdates.setChecked(contactToEdit.receivesLocationUpdates());
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from form
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
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
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Name is required");
            isValid = false;
        }

        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Phone number is required");
            isValid = false;
        }
        return isValid;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}