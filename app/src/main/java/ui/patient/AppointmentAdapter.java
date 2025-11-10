package ui.patient;

import android.graphics.Color;
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
import data.enums.Enum;
import data.model.Appointment;
import example.pclinic.com.R;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(AppointmentWithDoctor appointment);
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
        MaterialCardView card;
        TextView tvTimeSlot, tvStatusBadge, tvDoctorName, tvAppointmentDate, tvDescription;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAppointment);
            tvTimeSlot = itemView.findViewById(R.id.tv_time_slot);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvAppointmentDate = itemView.findViewById(R.id.tv_appointment_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        void bind(AppointmentWithDoctor ap, OnItemClickListener listener) {
            // Time slot
            String timeSlot = timeFormat.format(ap.startDate) + " - " + timeFormat.format(ap.endDate);
            tvTimeSlot.setText(timeSlot);

            // Doctor info
            tvDoctorName.setText("Bác sĩ: " + ap.fullName);
            tvAppointmentDate.setText("Ngày: " + dateFormat.format(ap.startDate));

            // Description
            if (ap.description != null && !ap.description.isEmpty()) {
                tvDescription.setText("Mô tả: " + ap.description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Status
            updateStatusBadge(ap.status);

            // Click listener
            card.setOnClickListener(v -> {
                if (listener != null) {
                    // 👇 KHÔNG CẦN TẠO OBJECT MỚI, TRUYỀN TRỰC TIẾP `ap`
                    listener.onClick(ap);
                }
            });
        }

        private void updateStatusBadge(Enum.AppointmentStatus status) {
            switch (status) {
                case CONFIRMED:
                    tvStatusBadge.setText("Đã xác nhận");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_badge_waiting); // Màu vàng
                    break;
                case DONE:
                    tvStatusBadge.setText("Hoàn thành");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_badge_completed); // Màu xanh
                    break;
                case ABSENT:
                    tvStatusBadge.setText("Vắng mặt");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_badge_absent); // Màu đỏ
                    break;
                case PENDING:
                default:
                    tvStatusBadge.setText("Chờ xác nhận");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_badge_pending); // Màu xám hoặc màu khác
                    break;
            }
        }
    }
}
