package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for reporting new incidents with optional media attachments
 */
public class IncidentReportActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etAddress;
    private Spinner spinnerType, spinnerSeverity;
    private MaterialButton btnAddPhoto, btnAddFromGallery, btnSubmitReport;
    private RecyclerView rvMediaPreview;

    private MediaPreviewAdapter mediaAdapter;
    private List<Uri> mediaUris = new ArrayList<>();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_report);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupSpinners();
        setupMediaRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etAddress = findViewById(R.id.et_address);
        spinnerType = findViewById(R.id.spinner_type);
        spinnerSeverity = findViewById(R.id.spinner_severity);
        btnAddPhoto = findViewById(R.id.btn_add_photo);
        btnAddFromGallery = findViewById(R.id.btn_add_from_gallery);
        btnSubmitReport = findViewById(R.id.btn_submit_report);
        rvMediaPreview = findViewById(R.id.rv_media_preview);
    }

    private void setupSpinners() {
        // Set up incident type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.incident_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Set up severity spinner
        ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.severity_levels,
                android.R.layout.simple_spinner_item
        );
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeverity.setAdapter(severityAdapter);
    }

    private void setupMediaRecyclerView() {
        mediaAdapter = new MediaPreviewAdapter(mediaUris, this);
        rvMediaPreview.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        rvMediaPreview.setAdapter(mediaAdapter);
    }

    private void setupClickListeners() {
        btnAddPhoto.setOnClickListener(v -> takePhoto());
        btnAddFromGallery.setOnClickListener(v -> pickFromGallery());
        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void takePhoto() {
        // Implementation for taking a photo would go here
        // This would typically use CameraX or the legacy camera API
        /*
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        */
    }

    private void pickFromGallery() {
        // Implementation for picking from gallery would go here, But I don't have Images. Please help.

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_PICK) {
                // Handle the returned image URI
                if (data != null && data.getData() != null) {
                    mediaUris.add(data.getData());
                    mediaAdapter.notifyDataSetChanged();
                    updateMediaPreviewVisibility();
                }
            }
        }
    }

    void updateMediaPreviewVisibility() {
        if (mediaUris.isEmpty()) {
            rvMediaPreview.setVisibility(View.GONE);
        } else {
            rvMediaPreview.setVisibility(View.VISIBLE);
        }
    }

    private void submitReport() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from form
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String severity = spinnerSeverity.getSelectedItem().toString();

        // Create incident object
        Incident incident = new Incident(title, description, address, type, severity, mediaUris);

        // Submit to server/database (implementation would go here)

        // Return to previous activity
        setResult(RESULT_OK);
        finish();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description is required");
            isValid = false;
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Location is required");
            isValid = false;
        }

        return isValid;
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