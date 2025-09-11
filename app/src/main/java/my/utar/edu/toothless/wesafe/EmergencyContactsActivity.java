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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;


public class EmergencyContactsActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private static final int EDIT_CONTACT_REQUEST_CODE = 2001;
    
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
        // Remove back arrow - users can use bottom navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        contactStorage = new ContactStorage(this);
        
        initializeViews();
        setupRecyclerView();
        loadContacts();
        setupClickListeners();
        
        // Initialize location services
        initializeLocationServices();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Show first-time setup dialog if needed
        showFirstTimeSetupDialog();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_contact); // Set contact as selected

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
                // Already in contacts, do nothing
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

    private void showFirstTimeSetupDialog() {
        // Check if it's the first time opening the contacts
        android.content.SharedPreferences prefs = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);
        
        if (isFirstTime) {
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.first_time_setup_title))
                .setMessage(getString(R.string.first_time_setup_message))
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    // Mark as not first time
                    prefs.edit().putBoolean("isFirstTime", false).apply();
                    // Check and request permissions after user acknowledges
                    checkAndRequestLocationPermissions();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    prefs.edit().putBoolean("isFirstTime", false).apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create()
                .show();
        } else {
            // Check and request permissions
            checkAndRequestLocationPermissions();
        }
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
            try {
                Intent intent = new Intent(EmergencyContactsActivity.this, EditEmergencyContactActivity.class);
                android.util.Log.d("EmergencyContacts", "Starting EditEmergencyContactActivity");
                startActivityForResult(intent, EDIT_CONTACT_REQUEST_CODE);
                android.util.Log.d("EmergencyContacts", "StartActivityForResult called");
            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.e("EmergencyContacts", "Error opening contact form", e);
                Toast.makeText(this, "Error opening contact form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to handle contact deletion
    public void deleteContact(int position) {
        try {
            if (position >= 0 && position < contactList.size()) {
                // Remove from storage first
                contactStorage.deleteContact(position);
                // Reload all contacts to ensure consistency
                loadContacts();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error deleting contact. Please try again.", Toast.LENGTH_SHORT).show();
            // Reload contacts in case of error
            loadContacts();
        }
    }

    // Method to handle contact editing
    public void editContact(int position) {
        try {
            if (position >= 0 && position < contactList.size()) {
                EmergencyContact contact = contactList.get(position);
                Intent intent = new Intent(this, EditEmergencyContactActivity.class);
                intent.putExtra("contact", contact);
                intent.putExtra("position", position);
                startActivityForResult(intent, EDIT_CONTACT_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening contact editor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        android.util.Log.d("EmergencyContacts", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        if (requestCode == EDIT_CONTACT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                boolean isDelete = data.getBooleanExtra("isDelete", false);
                
                if (isDelete) {
                    // Handle deleting contact
                    int position = data.getIntExtra("position", -1);
                    android.util.Log.d("EmergencyContacts", "Deleting contact at position: " + position);
                    if (position >= 0) {
                        contactStorage.deleteContact(position);
                        Toast.makeText(this, getString(R.string.contact_deleted), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    EmergencyContact contact = data.getParcelableExtra("contact");
                    boolean isEdit = data.getBooleanExtra("isEdit", false);
                    
                    android.util.Log.d("EmergencyContacts", "Contact received: " + (contact != null ? contact.getName() : "null") + ", isEdit: " + isEdit);
                    
                    if (contact != null) {
                        if (isEdit) {
                            // Handle editing existing contact
                            int position = data.getIntExtra("position", -1);
                            android.util.Log.d("EmergencyContacts", "Updating contact at position: " + position);
                            if (position >= 0) {
                                contactStorage.updateContact(position, contact);
                                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle adding new contact
                            android.util.Log.d("EmergencyContacts", "Adding new contact");
                            contactStorage.addContact(contact);
                            Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.e("EmergencyContacts", "Error processing contact", e);
                Toast.makeText(this, "Error processing contact", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Always reload contacts to reflect changes
        android.util.Log.d("EmergencyContacts", "Reloading contacts");
        loadContacts();
    }

    // Back navigation removed - users can use bottom navigation

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
                .setTitle(getString(R.string.location_permission_needed))
                .setMessage(getString(R.string.location_permission_rationale))
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            LOCATION_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
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
                        .setTitle(getString(R.string.background_location_needed))
                        .setMessage(getString(R.string.background_location_rationale))
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
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