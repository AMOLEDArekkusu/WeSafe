package my.utar.edu.toothless.wesafe;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for reporting new incidents with optional media attachments
 */
public class IncidentReportActivity extends BaseActivity {

    private TextInputEditText etTitle, etDescription, etAddress;
    private Spinner spinnerType, spinnerSeverity;
    private MaterialButton btnAddPhoto, btnAddFromGallery, btnSubmitReport;
    private RecyclerView rvMediaPreview;

    private MediaPreviewAdapter mediaAdapter;
    private List<Uri> mediaUris = new ArrayList<>();
    private String currentPhotoPath;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private static final int PERMISSION_REQUEST_CODE = 123;
    private boolean cameraPermissionGranted = false;
    private boolean storagePermissionGranted = false;
    private FusedLocationProviderClient fusedLocationClient;
    private IncidentStorage incidentStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_incident_report);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupSpinners();
        setupMediaRecyclerView();
        setupClickListeners();
        checkAndRequestPermissions();
        
        // Initialize incident storage
        incidentStorage = new IncidentStorage(this);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();
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
        if (!cameraPermissionGranted) {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            checkAndRequestPermissions();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Create a file to save the image
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(
                this,
                "Error creating image file.",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                photoFile
            );
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                // Handle the case where no camera app is available
                Toast.makeText(
                    this,
                    "No camera app available.",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",        /* suffix */
            storageDir     /* directory */
        );

        // Save the file path for use with ACTION_VIEW intent
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void pickFromGallery() {
        if (!storagePermissionGranted) {
            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            checkAndRequestPermissions();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                REQUEST_IMAGE_PICK
            );
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle the case where no gallery app is available
            Toast.makeText(
                this,
                "Please install a File Manager or Gallery app.",
                Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Handle camera photo
                if (currentPhotoPath != null) {
                    File f = new File(currentPhotoPath);
                    Uri contentUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        f
                    );
                    mediaUris.add(contentUri);
                    mediaAdapter.notifyDataSetChanged();
                    updateMediaPreviewVisibility();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Handle gallery photo
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // Get persistent permission for the URI
                    final int takeFlags = data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        getContentResolver().takePersistableUriPermission(
                            selectedImageUri,
                            takeFlags
                        );
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Could not access the image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    mediaUris.add(selectedImageUri);
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
        
        // Increment incident count
        incidentStorage.incrementIncidentCount();

        // Show success message
        Toast.makeText(this, "Incident report submitted successfully", Toast.LENGTH_SHORT).show();

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

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();

        // Check location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Check camera and storage permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            } else {
                cameraPermissionGranted = true;
            }
            
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                storagePermissionGranted = true;
            }
        } else {
            // For Android 12 and below
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            } else {
                cameraPermissionGranted = true;
            }
            
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                storagePermissionGranted = true;
            }
        }

        // Request all needed permissions at once
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
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
        getCurrentLocation(); // Refresh location when activity resumes
    }

    private void getCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Get the address from the location
                        getAddressFromLocation(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void getAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, java.util.Locale.getDefault());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use the new geocoder API for Android 13 and above
                geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1,
                    addresses -> {
                        if (!addresses.isEmpty()) {
                            updateAddressField(addresses.get(0));
                        }
                    }
                );
            } else {
                // Use the old geocoder API for older versions
                List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
                );
                if (!addresses.isEmpty()) {
                    updateAddressField(addresses.get(0));
                }
            }
        } catch (IOException e) {
            // Fallback to coordinates if geocoding fails
            String coordinates = String.format(
                Locale.getDefault(),
                "%.6f, %.6f",
                location.getLatitude(),
                location.getLongitude()
            );
            etAddress.setText(coordinates);
        }
    }

    private void updateAddressField(Address address) {
        StringBuilder addressText = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            if (i > 0) addressText.append(", ");
            addressText.append(address.getAddressLine(i));
        }
        runOnUiThread(() -> etAddress.setText(addressText.toString()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    cameraPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                } else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                          permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                    storagePermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
        }
    }
}