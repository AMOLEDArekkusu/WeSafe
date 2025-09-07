package my.utar.edu.toothless.wesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;


public class EmergencyContactsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;
    
    private RecyclerView rvContacts;
    private View emptyView;
    private FloatingActionButton fabAddContact;
    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> contactList = new ArrayList<>();
    private ContactStorage contactStorage;
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_emergency_contacts);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        contactStorage = new ContactStorage(this);
        initializeViews();
        setupRecyclerView();
        loadContacts();
        setupClickListeners();
        
        // Initialize location services
        initializeLocationServices();
        // Check and request permissions
        checkAndRequestLocationPermissions();
    }

    private void initializeViews() {
        rvContacts = findViewById(R.id.rv_contacts);
        emptyView = findViewById(R.id.empty_view);
        fabAddContact = findViewById(R.id.fab_add_contact);
    }

    private void setupRecyclerView() {
        adapter = new EmergencyContactAdapter(contactList, this);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);
    }

    private void loadContacts() {
        contactList.clear();
        contactList.addAll(contactStorage.loadContacts());
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (contactList.isEmpty()) {
            rvContacts.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvContacts.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        fabAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(EmergencyContactsActivity.this, EditEmergencyContactActivity.class);
            startActivityForResult(intent, 1); // Use the same request code (1)
        });
    }

    // Method to handle contact deletion
    public void deleteContact(int position) {
        contactList.remove(position);
        contactStorage.deleteContact(position);
        adapter.notifyItemRemoved(position);
        updateEmptyView();
    }

    // Method to handle contact editing
    public void editContact(int position) {
        EmergencyContact contact = contactList.get(position);
        Intent intent = new Intent(this, EditEmergencyContactActivity.class);
        intent.putExtra("contact", contact);
        intent.putExtra("position", position);
        startActivityForResult(intent, 1); // Use the same request code (1)
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Load contacts from storage whether the edit was successful or canceled
        if (requestCode == 1) {
            // Refresh contacts from storage
            loadContacts();
        }
        
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            boolean isEdit = data.getBooleanExtra("isEdit", false);

            if (isEdit) {
                // Handle editing an existing contact
                int position = data.getIntExtra("position", -1);
                EmergencyContact updatedContact = data.getParcelableExtra("contact");

                if (position != -1 && updatedContact != null) {
                    contactList.set(position, updatedContact);
                    contactStorage.updateContact(position, updatedContact);
                    adapter.notifyItemChanged(position);
                }
            } else {
                // Handle adding a new contact
                EmergencyContact newContact = data.getParcelableExtra("contact");
                if (newContact != null) {
                    contactList.add(newContact);
                    contactStorage.addContact(newContact);
                    adapter.notifyItemInserted(contactList.size() - 1);
                    updateEmptyView();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)        // 10 seconds
                .setFastestInterval(5000); // 5 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    lastKnownLocation = locationResult.getLastLocation();
                    updateLocationForContacts(lastKnownLocation);
                }
            }
        };
    }

    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user
                showLocationPermissionRationale();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Permission has already been granted
            startLocationUpdates();
            // Check for background location permission for Android 10 and above
            checkBackgroundLocationPermission();
        }
    }

    private void showLocationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission to ensure your safety and provide accurate location information to your emergency contacts.")
                .setPositiveButton("OK", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            LOCATION_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    showLocationRequiredSnackbar();
                })
                .create()
                .show();
    }

    private void checkBackgroundLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Background Location Access Needed")
                        .setMessage("This app needs background location access to notify your emergency contacts of your location even when the app is closed.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateLocationForContacts(Location location) {
        if (location != null) {
            // Here you would update your contacts with the new location
            // For example, send location updates to your backend server
            // or update local storage
            String locationInfo = String.format("Lat: %f, Lng: %f",
                    location.getLatitude(), location.getLongitude());
            // You might want to implement your own logic to handle the location update
        }
    }

    private void showLocationRequiredSnackbar() {
        Snackbar.make(findViewById(android.R.id.content),
                "Location permission is required for emergency features",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings", v -> {
                    // Open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startLocationUpdates();
                checkBackgroundLocationPermission();
            } else {
                // Permission denied
                showLocationRequiredSnackbar();
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Background location permission granted
                Toast.makeText(this, "Background location access granted",
                        Toast.LENGTH_SHORT).show();
            }
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
}