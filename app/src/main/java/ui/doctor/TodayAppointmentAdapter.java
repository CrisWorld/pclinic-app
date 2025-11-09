package ui.doctor;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import data.enums.Enum;
import data.model.Appointment;
import data.model.User;
import example.pclinic.com.R;

public class TodayAppointmentAdapter extends RecyclerView.Adapter<TodayAppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private List<User> patients = new ArrayList<>();
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onViewDetail(Appointment appointment, User patient);
        void onConfirm(Appointment appointment);
        void onComplete(Appointment appointment);
    }

    public TodayAppointmentAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Appointment> appointments, List<User> patients) {
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        this.patients = patients != null ? patients : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        User patient = patients.get(position);
        
        holder.bind(appointment, patient, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour, tvDuration, tvPatientName, tvStatus;
        TextView tvPatientPhone, tvPatientGender, tvDescription;
        TextView tvCheckinTime;
        LinearLayout layoutCheckin;
        MaterialButton btnViewDetail, btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tv_hour);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPatientPhone = itemView.findViewById(R.id.tv_patient_phone);
            tvPatientGender = itemView.findViewById(R.id.tv_patient_gender);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCheckinTime = itemView.findViewById(R.id.tv_checkin_time);
            layoutCheckin = itemView.findViewById(R.id.layout_checkin);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            btnAction = itemView.findViewById(R.id.btn_action);
        }

        void bind(Appointment appointment, User patient, OnAppointmentActionListener listener) {
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            // Time display
            tvHour.setText(hourFormat.format(appointment.startDate));
            
            // Calculate duration
            long durationMs = appointment.endDate.getTime() - appointment.startDate.getTime();
            long durationMinutes = durationMs / (1000 * 60);
            tvDuration.setText(durationMinutes + "p");

            // Patient info
            tvPatientName.setText(patient.fullName);
            tvPatientPhone.setText("📱 " + (patient.phone != null ? patient.phone : "Chưa có SĐT"));
            
            // Gender mapping
            String genderText = "Khác";
            if (patient.gender != null) {
                if (patient.gender.equalsIgnoreCase("MALE") || patient.gender.equalsIgnoreCase("Nam")) {
                    genderText = "Nam";
                } else if (patient.gender.equalsIgnoreCase("FEMALE") || patient.gender.equalsIgnoreCase("Nữ")) {
                    genderText = "Nữ";
                }
            }
            tvPatientGender.setText(genderText);

            // Description
            tvDescription.setText(appointment.description != null && !appointment.description.isEmpty() 
                    ? appointment.description 
                    : "Chưa có mô tả");

            // Status badge
            updateStatusBadge(appointment.status);

            // Check-in time
            if (appointment.checkInDate != null) {
                layoutCheckin.setVisibility(View.VISIBLE);
                tvCheckinTime.setText(hourFormat.format(appointment.checkInDate));
            } else {
                layoutCheckin.setVisibility(View.GONE);
            }

            // Action button based on status
            updateActionButton(appointment);

            // Click listeners
            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(appointment, patient);
                }
            });

            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    if (appointment.status == Enum.AppointmentStatus.PENDING) {
                        listener.onConfirm(appointment);
                    } else if (appointment.status == Enum.AppointmentStatus.CONFIRMED) {
                        listener.onComplete(appointment);
                    }
                }
            });
        }

        private void updateStatusBadge(Enum.AppointmentStatus status) {
            int backgroundRes;
            String statusText;

            switch (status) {
                case PENDING:
                    backgroundRes = R.drawable.bg_status_pending;
                    statusText = "Chờ xác nhận";
                    break;
                case CONFIRMED:
                    backgroundRes = R.drawable.bg_status_confirmed;
                    statusText = "Đã xác nhận";
                    break;
                case DONE:
                    backgroundRes = R.drawable.bg_status_done;
                    statusText = "Hoàn thành";
                    break;
                case ABSENT:
                    backgroundRes = R.drawable.bg_status_pending;
                    statusText = "Vắng mặt";
                    break;
                default:
                    backgroundRes = R.drawable.bg_status_pending;
                    statusText = "Không xác định";
            }

            tvStatus.setBackgroundResource(backgroundRes);
            tvStatus.setText(statusText);
        }

        private void updateActionButton(Appointment appointment) {
            switch (appointment.status) {
                case PENDING:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Xác nhận");
                    btnAction.setBackgroundTintList(ContextCompat.getColorStateList(
                            itemView.getContext(), R.color.colorPrimary));
                    break;
                case CONFIRMED:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Hoàn thành");
                    btnAction.setBackgroundTintList(ContextCompat.getColorStateList(
                            itemView.getContext(), R.color.success));
                    break;
                case DONE:
                case ABSENT:
                    btnAction.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
