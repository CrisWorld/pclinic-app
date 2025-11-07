package ui.doctor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import data.enums.Enum;
import data.model.Appointment;
import example.pclinic.com.R;

public class BookedAppointmentAdapter extends RecyclerView.Adapter<BookedAppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booked_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAppointmentTime;
        private TextView tvPatientName;
        private TextView tvServiceName;
        private TextView tvStatusBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvAppointmentTime = itemView.findViewById(R.id.tv_appointment_time);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
        }

        void bind(Appointment appointment) {
            // Format time from startDate
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = timeFormat.format(appointment.startDate);
            tvAppointmentTime.setText(time);

            // Display patient and service info
            tvPatientName.setText("Bệnh nhân #" + appointment.patientId);
            tvServiceName.setText(appointment.description != null ? appointment.description : "Khám bệnh");

            // Set status badge
            String statusText = "";
            int statusColor = Color.GRAY;

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

            tvStatusBadge.setText(statusText);
            tvStatusBadge.setBackgroundColor(statusColor);
        }
    }
}
