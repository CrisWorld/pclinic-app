package ui.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import data.model.Appointment;
import example.pclinic.com.R;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.tvDate.setText(dateFormat.format(appointment.startDate));
        holder.tvStatus.setText(appointment.status.name());
        holder.tvDescription.setText(appointment.description != null ? appointment.description : "Không có mô tả");
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvStatus;
        TextView tvDescription;

        ViewHolder(View view) {
            super(view);
            tvDate = view.findViewById(R.id.tv_appointment_date);
            tvStatus = view.findViewById(R.id.tv_appointment_status);
            tvDescription = view.findViewById(R.id.tv_appointment_description);
        }
    }
}
