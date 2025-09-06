package my.utar.edu.toothless.wesafe;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Incident {
    private String title;
    private String description;
    private String location;
    private String type;
    private String severity;
    private List<Uri> mediaUris;
    private Date timestamp;
    private String reportedBy;

    public Incident(String title, String description, String location, String type,
                    String severity, List<Uri> mediaUris) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.severity = severity;
        this.mediaUris = mediaUris;
        this.timestamp = new Date();
        // In a real app, this would be the current user's name or ID
        this.reportedBy = "Current User";
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public List<Uri> getMediaUris() { return mediaUris; }
    public void setMediaUris(List<Uri> mediaUris) { this.mediaUris = mediaUris; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
}