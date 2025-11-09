package ui.patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import data.dto.AppointmentWithDoctor;
import data.model.Appointment;
import example.pclinic.com.R;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(Appointment appointment);
    }

    private final List<AppointmentWithDoctor> list;
    private final OnItemClickListener listener;

    public AppointmentAdapter(List<AppointmentWithDoctor> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.patient_item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDoctor, txtDate, txtStatus;
        MaterialCardView card;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAppointment);
            txtDoctor = itemView.findViewById(R.id.txtDoctor);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }

        void bind(AppointmentWithDoctor ap, OnItemClickListener listener) {
            txtDoctor.setText("Bác sĩ: " + ap.fullName);
            txtDate.setText("Ngày: " + sdf.format(ap.startDate));
            txtStatus.setText("Trạng thái: " + ap.status.name());

            card.setOnClickListener(v -> {
                Appointment appointment = new Appointment();
                appointment.id = ap.id;
                appointment.doctorId = ap.doctorId;
                appointment.patientId = ap.patientId;
                appointment.startDate = ap.startDate;
                appointment.endDate = ap.endDate;
                appointment.status = ap.status;
                listener.onClick(appointment);
            });
        }

    }
}
