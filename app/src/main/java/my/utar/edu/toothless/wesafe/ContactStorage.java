package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContactStorage {
    private static final String PREF_NAME = "WeSafeContacts";
    private static final String KEY_CONTACTS = "emergency_contacts";
    private final SharedPreferences preferences;
    private final Gson gson;

    public ContactStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveContacts(List<EmergencyContact> contacts) {
        String json = gson.toJson(contacts);
        preferences.edit().putString(KEY_CONTACTS, json).apply();
    }

    public List<EmergencyContact> loadContacts() {
        String json = preferences.getString(KEY_CONTACTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<EmergencyContact>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addContact(EmergencyContact contact) {
        List<EmergencyContact> contacts = loadContacts();
        contacts.add(contact);
        saveContacts(contacts);
    }

    public void updateContact(int position, EmergencyContact contact) {
        List<EmergencyContact> contacts = loadContacts();
        if (position >= 0 && position < contacts.size()) {
            contacts.set(position, contact);
            saveContacts(contacts);
        }
    }

    public void deleteContact(int position) {
        List<EmergencyContact> contacts = loadContacts();
        if (position >= 0 && position < contacts.size()) {
            contacts.remove(position);
            saveContacts(contacts);
        }
    }
}
