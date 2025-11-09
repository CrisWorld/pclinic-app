package ui.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import data.dto.AppointmentWithPatient;
import data.enums.Enum;
import example.pclinic.com.R;

public class TodayAppointmentAdapter extends RecyclerView.Adapter<TodayAppointmentAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onCreateExaminationForm(AppointmentWithPatient appointment);
        void onViewExaminationForm(AppointmentWithPatient appointment);
    }

    private List<AppointmentWithPatient> appointments = new ArrayList<>();
    private final OnActionClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TodayAppointmentAdapter(OnActionClickListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<AppointmentWithPatient> appointments) {
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_today, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentWithPatient appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimeSlot;
        TextView tvPatientName;
        TextView tvPatientPhone;
        TextView tvPatientGender;
        TextView tvPatientAge;
        TextView tvDescription;
        TextView tvStatusBadge;
        MaterialButton btnAction;
        MaterialButton btnComplete;
        MaterialButton btnSetAbsent;

        ViewHolder(View view) {
            super(view);
            tvTimeSlot = view.findViewById(R.id.tv_time_slot);
            tvPatientName = view.findViewById(R.id.tv_patient_name);
            tvPatientPhone = view.findViewById(R.id.tv_patient_phone);
            tvPatientGender = view.findViewById(R.id.tv_patient_gender);
            tvPatientAge = view.findViewById(R.id.tv_patient_age);
            tvDescription = view.findViewById(R.id.tv_description);
            tvStatusBadge = view.findViewById(R.id.tv_status_badge);
            btnAction = view.findViewById(R.id.btn_create_examination);
            btnComplete = view.findViewById(R.id.btn_complete);
            btnSetAbsent = view.findViewById(R.id.btn_set_absent);
        }

        void bind(AppointmentWithPatient appointment, OnActionClickListener listener) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Time slot
            String timeSlot = timeFormat.format(appointment.startDate) + " - " + timeFormat.format(appointment.endDate);
            tvTimeSlot.setText(timeSlot);

            // Patient info
            tvPatientName.setText(appointment.patientName != null ? appointment.patientName : "N/A");
            tvPatientPhone.setText(appointment.patientPhone != null ? appointment.patientPhone : "N/A");

            // Gender
            if (appointment.patientGender != null) {
                String genderLower = appointment.patientGender.toLowerCase();
                String genderText;
                if (genderLower.equals("male") || genderLower.equals("nam")) {
                    genderText = "Nam";
                } else if (genderLower.equals("female") || genderLower.equals("nữ")) {
                    genderText = "Nữ";
                } else {
                    genderText = "Khác";
                }
                tvPatientGender.setText(genderText);
            } else {
                tvPatientGender.setText("N/A");
            }

            // Age
            if (appointment.patientBirthDate != null) {
                java.util.Calendar birthDate = java.util.Calendar.getInstance();
                birthDate.setTimeInMillis(appointment.patientBirthDate);
                java.util.Calendar today = java.util.Calendar.getInstance();
                int age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR);
                if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                tvPatientAge.setText(age + " tuổi");
            } else {
                tvPatientAge.setText("N/A");
            }

            // Description
            if (appointment.description != null && !appointment.description.isEmpty()) {
                tvDescription.setText("Mô tả: " + appointment.description);
            } else {
                tvDescription.setText("Mô tả: Không có mô tả");
            }

            // Status and action button
            Enum.AppointmentStatus status = appointment.status;
            if (status == Enum.AppointmentStatus.DONE) {
                // Completed - show "Xem phiếu khám"
                tvStatusBadge.setText("Khám xong");
                tvStatusBadge.setBackgroundResource(R.drawable.status_badge_completed);
                tvStatusBadge.setTextColor(0xFFFFFFFF); // White text
                btnAction.setText("Xem phiếu khám");
                btnAction.setIconResource(R.drawable.ic_add_circle);
                btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.colorPrimary));
                btnAction.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.white));
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewExaminationForm(appointment);
                    }
                });
                btnAction.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.GONE);
                btnSetAbsent.setVisibility(View.GONE);
            } else if (status == Enum.AppointmentStatus.PENDING || status == Enum.AppointmentStatus.CONFIRMED) {
                // Waiting - show "Tạo phiếu khám"
                tvStatusBadge.setText("Đang chờ");
                tvStatusBadge.setBackgroundResource(R.drawable.status_badge_waiting);
                tvStatusBadge.setTextColor(0xFFFFFFFF); // White text
                btnAction.setText("Tạo phiếu khám");
                btnAction.setIconResource(R.drawable.ic_add_circle);
                btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.colorPrimary));
                btnAction.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.white));
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCreateExaminationForm(appointment);
                    }
                });
                btnAction.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.GONE);
                btnSetAbsent.setVisibility(View.GONE);
            } else if (status == Enum.AppointmentStatus.ABSENT) {
                // Absent
                tvStatusBadge.setText("Vắng mặt");
                tvStatusBadge.setBackgroundResource(R.drawable.status_badge_absent);
                tvStatusBadge.setTextColor(0xFFFFFFFF); // White text
                btnAction.setText("Tạo phiếu khám");
                btnAction.setIconResource(R.drawable.ic_add_circle);
                btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.colorPrimary));
                btnAction.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.white));
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCreateExaminationForm(appointment);
                    }
                });
                btnAction.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.GONE);
                btnSetAbsent.setVisibility(View.GONE);
            }
        }
    }
}
