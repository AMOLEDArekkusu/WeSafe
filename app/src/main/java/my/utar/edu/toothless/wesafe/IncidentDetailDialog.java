package my.utar.edu.toothless.wesafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class IncidentDetailDialog extends BottomSheetDialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_TYPE = "type";
    private static final String ARG_SEVERITY = "severity";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_REPORTED_BY = "reported_by";

    private TextView tvIncidentTitle, tvIncidentType, tvIncidentSeverity,
            tvIncidentDescription, tvIncidentLocation, tvIncidentTimestamp, tvReportedBy;

    public static IncidentDetailDialog newInstance(String title, String type, String severity,
                                                   String description, String location,
                                                   String timestamp, String reportedBy) {
        IncidentDetailDialog fragment = new IncidentDetailDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_SEVERITY, severity);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_LOCATION, location);
        args.putString(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_REPORTED_BY, reportedBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Use the correct layout file name
        return inflater.inflate(R.layout.dialog_incident_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all text views with correct IDs from dialog_incident_details.xml
        tvIncidentTitle = view.findViewById(R.id.tv_incident_title);
        tvIncidentType = view.findViewById(R.id.tv_incident_type);
        tvIncidentSeverity = view.findViewById(R.id.tv_incident_severity);
        tvIncidentDescription = view.findViewById(R.id.tv_incident_description);
        tvIncidentLocation = view.findViewById(R.id.tv_incident_location);
        tvIncidentTimestamp = view.findViewById(R.id.tv_incident_timestamp);
        tvReportedBy = view.findViewById(R.id.tv_reported_by);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            String type = getArguments().getString(ARG_TYPE);
            String severity = getArguments().getString(ARG_SEVERITY);
            String description = getArguments().getString(ARG_DESCRIPTION);
            String location = getArguments().getString(ARG_LOCATION);
            String timestamp = getArguments().getString(ARG_TIMESTAMP);
            String reportedBy = getArguments().getString(ARG_REPORTED_BY);

            // Set all the text views
            tvIncidentTitle.setText(title);
            tvIncidentType.setText(type != null ? type : "Not specified");
            tvIncidentSeverity.setText(severity != null ? severity : "Not specified");
            tvIncidentDescription.setText(description != null ? description : "No description provided");
            tvIncidentLocation.setText(location != null ? location : "Location not specified");
            tvIncidentTimestamp.setText(timestamp != null ? timestamp : "Time not specified");
            tvReportedBy.setText(reportedBy != null ? reportedBy : "Unknown");
        }
    }

    // Helper method to show incident details from MapActivity
    public void showIncidentDetails(String title, String snippet) {
        // Parse the snippet to extract details if needed, or use default values
        // For now, using placeholder values
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TYPE, "Incident"); // Default type
        args.putString(ARG_SEVERITY, "Medium"); // Default severity
        args.putString(ARG_DESCRIPTION, snippet);
        args.putString(ARG_LOCATION, "Current Location"); // Would use actual location in real app
        args.putString(ARG_TIMESTAMP, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date()));
        args.putString(ARG_REPORTED_BY, "Anonymous User");
        setArguments(args);
    }
}