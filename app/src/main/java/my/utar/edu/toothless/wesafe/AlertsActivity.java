package my.utar.edu.toothless.wesafe;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

import my.utar.edu.toothless.wesafe.adapter.AlertsAdapter;
import my.utar.edu.toothless.wesafe.model.Alert;

public class AlertsActivity extends BaseActivity {
    private RecyclerView alertsRecyclerView;
    private LinearLayout noAlertsLayout;
    private AlertsAdapter alertsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        alertsRecyclerView = findViewById(R.id.rv_alerts);
        noAlertsLayout = findViewById(R.id.layout_no_alerts);

        // Set up RecyclerView
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertsAdapter = new AlertsAdapter(this, new ArrayList<>());
        alertsRecyclerView.setAdapter(alertsAdapter);

        // Load alerts (you would typically load this from your data source)
        loadAlerts();
    }

    /**
     * Show or hide the "no alerts" view
     * @param show true to show the no alerts view, false to show the alerts list
     */
    private void showNoAlerts(boolean show) {
        if (show) {
            alertsRecyclerView.setVisibility(View.GONE);
            noAlertsLayout.setVisibility(View.VISIBLE);
        } else {
            alertsRecyclerView.setVisibility(View.VISIBLE);
            noAlertsLayout.setVisibility(View.GONE);
        }
    }

    private void loadAlerts() {
        // This is where you would typically load your alerts from a data source
        // For now, we'll create some sample data
        List<Alert> alerts = new ArrayList<>();
        long now = System.currentTimeMillis();
        
        alerts.add(new Alert(
            "Emergency Alert",
            "There has been a reported incident in your area. Please stay vigilant.",
            "1.2 km away",
            now - 7200000 // 2 hours ago
        ));

        alerts.add(new Alert(
            "Weather Alert",
            "Heavy rain expected in your area. Take necessary precautions.",
            "Your location",
            now - 3600000 // 1 hour ago
        ));

        // Update the adapter with the new alerts
        alertsAdapter.updateAlerts(alerts);
        
        // Show/hide the no alerts view based on whether we have alerts
        showNoAlerts(alerts.isEmpty());
    }
}
