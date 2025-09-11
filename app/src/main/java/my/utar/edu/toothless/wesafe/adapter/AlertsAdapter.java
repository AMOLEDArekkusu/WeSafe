package my.utar.edu.toothless.wesafe.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import my.utar.edu.toothless.wesafe.R;
import my.utar.edu.toothless.wesafe.model.Alert;
import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {
    private List<Alert> alerts;
    private Context context;

    public AlertsAdapter(Context context, List<Alert> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);
        holder.typeTextView.setText(alert.getType());
        holder.messageTextView.setText(alert.getMessage());
        holder.locationTextView.setText(alert.getLocation());
        
        // Format the time as "X time ago"
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
            alert.getTimestamp(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        );
        holder.timeTextView.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return alerts != null ? alerts.size() : 0;
    }

    public void updateAlerts(List<Alert> newAlerts) {
        this.alerts = newAlerts;
        notifyDataSetChanged();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView messageTextView;
        TextView locationTextView;
        TextView timeTextView;

        AlertViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.tv_alert_type);
            messageTextView = itemView.findViewById(R.id.tv_alert_message);
            locationTextView = itemView.findViewById(R.id.tv_alert_location);
            timeTextView = itemView.findViewById(R.id.tv_alert_time);
        }
    }
}
