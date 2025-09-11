package my.utar.edu.toothless.wesafe.model;

public class Alert {
    private String type;
    private String message;
    private String location;
    private long timestamp;

    public Alert(String type, String message, String location, long timestamp) {
        this.type = type;
        this.message = message;
        this.location = location;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
