package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying emergency contacts in a RecyclerView
 */
public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    private List<EmergencyContact> contactList;
    private Context context;

    public EmergencyContactAdapter(List<EmergencyContact> contactList, Context context) {
        this.contactList = contactList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());
        holder.tvType.setText(contact.getType());

        // Show primary indicator if this is the primary contact
        if (contact.isPrimary()) {
            holder.primaryIndicator.setVisibility(View.VISIBLE);
            holder.ivPrimary.setVisibility(View.VISIBLE);
        } else {
            holder.primaryIndicator.setVisibility(View.GONE);
            holder.ivPrimary.setVisibility(View.GONE);
        }

        // Set up button click listeners
        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhone()));
            context.startActivity(intent);
        });

        holder.btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + contact.getPhone()));
            context.startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (context instanceof EmergencyContactsActivity) {
                ((EmergencyContactsActivity) context).editContact(position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (context instanceof EmergencyContactsActivity) {
                ((EmergencyContactsActivity) context).deleteContact(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View primaryIndicator;
        TextView tvName, tvPhone, tvType;
        ImageView ivPrimary;
        ImageButton btnCall, btnMessage, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            primaryIndicator = itemView.findViewById(R.id.primary_indicator);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvType = itemView.findViewById(R.id.tv_type);
            ivPrimary = itemView.findViewById(R.id.iv_primary);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnMessage = itemView.findViewById(R.id.btn_message);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}