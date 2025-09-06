package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

////This part is made with AI, I don't know what it does, but it fixes a lot of problems.
////Any attempts at understanding and updating this page results in me auto-fenestrating myself
///
public class EmergencyContactsActivity extends AppCompatActivity {

    private RecyclerView rvContacts;
    private View emptyView;
    private FloatingActionButton fabAddContact;
    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupRecyclerView();
        loadContacts();
        setupClickListeners();
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
        // In a real app, this would load from database or API
        // For demonstration, adding some sample contacts
        contactList.add(new EmergencyContact("John Doe", "+1234567890", "Family", true));
        contactList.add(new EmergencyContact("Jane Smith", "+0987654321", "Friend", false));

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
            startActivity(intent);
        });
    }

    // Method to handle contact deletion
    public void deleteContact(int position) {
        contactList.remove(position);
        adapter.notifyItemRemoved(position);
        updateEmptyView();
    }

    // Method to handle contact editing
    public void editContact(int position) {
        EmergencyContact contact = contactList.get(position);
        Intent intent = new Intent(this, EditEmergencyContactActivity.class);
        intent.putExtra("contact", contact); // Assuming EmergencyContact is Parcelable
        intent.putExtra("position", position);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh the contact list
            loadContacts();
        }
    }
}