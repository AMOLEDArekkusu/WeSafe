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
        if (contactList.isEmpty()) {
            contactList.add(new EmergencyContact("John Doe", "+1234567890", "Family", true));
            contactList.add(new EmergencyContact("Jane Smith", "+0987654321", "Friend", false));
            adapter.notifyDataSetChanged();
        }
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
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            boolean isEdit = data.getBooleanExtra("isEdit", false);

            if (isEdit) {
                // Handle editing an existing contact
                int position = data.getIntExtra("position", -1);
                EmergencyContact updatedContact = data.getParcelableExtra("contact");

                if (position != -1 && updatedContact != null) {
                    contactList.set(position, updatedContact);
                    adapter.notifyItemChanged(position);
                }
            } else {
                // Handle adding a new contact (if you implement add functionality)
                EmergencyContact newContact = data.getParcelableExtra("contact");
                if (newContact != null) {
                    contactList.add(newContact);
                    adapter.notifyItemInserted(contactList.size() - 1);
                    updateEmptyView();
                }
            }
        }
    }
}