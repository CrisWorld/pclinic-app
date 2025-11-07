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

import data.enums.Enum;
import data.model.Appointment;
import example.pclinic.com.R;

public class AppointmentHistoryAdapter extends RecyclerView.Adapter<AppointmentHistoryAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public void setOnAppointmentClickListener(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private TextView tvStatus;
        private TextView tvPatientName;
        private TextView tvPatientPhone;
        private TextView tvDescription;
        private TextView tvCheckinTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvPatientPhone = itemView.findViewById(R.id.tv_patient_phone);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCheckinTime = itemView.findViewById(R.id.tv_checkin_time);
        }

        void bind(Appointment appointment, OnAppointmentClickListener listener) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

            // Format time range
            String startTime = timeFormat.format(appointment.startDate);
            String endTime = timeFormat.format(appointment.endDate);
            tvTime.setText(startTime + " - " + endTime);

            // Set status
            String statusText = "";
            int statusColor = 0;

            if (appointment.status == Enum.AppointmentStatus.CONFIRMED) {
                statusText = "Đã xác nhận";
                statusColor = itemView.getContext().getColor(R.color.colorPrimary);
            } else if (appointment.status == Enum.AppointmentStatus.PENDING) {
                statusText = "Chờ xác nhận";
                statusColor = itemView.getContext().getColor(R.color.colorAccent);
            } else if (appointment.status == Enum.AppointmentStatus.DONE) {
                statusText = "Hoàn thành";
                statusColor = itemView.getContext().getColor(R.color.success);
            } else if (appointment.status == Enum.AppointmentStatus.ABSENT) {
                statusText = "Vắng mặt";
                statusColor = itemView.getContext().getColor(R.color.error);
            }

            tvStatus.setText(statusText);
            tvStatus.setBackgroundColor(statusColor);

            // Patient info (will be loaded separately)
            tvPatientName.setText("Bệnh nhân #" + appointment.patientId);
            tvPatientPhone.setText("ID: " + appointment.patientId);

            // Description
            if (appointment.description != null && !appointment.description.isEmpty()) {
                tvDescription.setText(appointment.description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setText("Khám bệnh");
                tvDescription.setVisibility(View.VISIBLE);
            }

            // Check-in time
            if (appointment.checkInDate != null) {
                tvCheckinTime.setText("✓ Đã check-in: " + timeFormat.format(appointment.checkInDate));
                tvCheckinTime.setVisibility(View.VISIBLE);
            } else {
                tvCheckinTime.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(appointment);
                }
            });
        }
    }
}
