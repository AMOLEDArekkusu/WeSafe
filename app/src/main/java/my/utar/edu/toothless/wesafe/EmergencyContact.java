package my.utar.edu.toothless.wesafe;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * Data model for Emergency Contact
 */
public class EmergencyContact implements Parcelable {
    private String name;
    private String phone;
    private String email;
    private String type;
    private boolean isPrimary;
    private boolean receivesAlerts;
    private boolean receivesLocationUpdates;

    // Constructors
    public EmergencyContact(String name, String phone, String type, boolean isPrimary) {
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.isPrimary = isPrimary;
        this.receivesAlerts = true; // Default to true
        this.receivesLocationUpdates = false; // Default to false for privacy
    }

    public EmergencyContact(String name, String phone, String email, String type,
                            boolean isPrimary, boolean receivesAlerts, boolean receivesLocationUpdates) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.type = type;
        this.isPrimary = isPrimary;
        this.receivesAlerts = receivesAlerts;
        this.receivesLocationUpdates = receivesLocationUpdates;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public boolean receivesAlerts() { return receivesAlerts; }
    public void setReceivesAlerts(boolean receivesAlerts) { this.receivesAlerts = receivesAlerts; }

    public boolean receivesLocationUpdates() { return receivesLocationUpdates; }
    public void setReceivesLocationUpdates(boolean receivesLocationUpdates) {
        this.receivesLocationUpdates = receivesLocationUpdates;
    }

    // Parcelable implementation
    protected EmergencyContact(Parcel in) {
        name = in.readString();
        phone = in.readString();
        email = in.readString();
        type = in.readString();
        isPrimary = in.readByte() != 0;
        receivesAlerts = in.readByte() != 0;
        receivesLocationUpdates = in.readByte() != 0;
    }

    public static final Creator<EmergencyContact> CREATOR = new Creator<EmergencyContact>() {
        @Override
        public EmergencyContact createFromParcel(Parcel in) {
            return new EmergencyContact(in);
        }

        @Override
        public EmergencyContact[] newArray(int size) {
            return new EmergencyContact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(type);
        dest.writeByte((byte) (isPrimary ? 1 : 0));
        dest.writeByte((byte) (receivesAlerts ? 1 : 0));
        dest.writeByte((byte) (receivesLocationUpdates ? 1 : 0));
    }
}