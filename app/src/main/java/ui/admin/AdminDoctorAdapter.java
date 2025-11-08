package ui.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import data.dto.DoctorInfo;
import example.pclinic.com.R;

public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.DoctorViewHolder> {
    private List<DoctorInfo> doctorList;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onClick(DoctorInfo doctor);
        void onViewDetail(DoctorInfo doctor);
        void onEdit(DoctorInfo doctor); // <-- Thêm mới
        void onDelete(DoctorInfo doctor);
    }

    public AdminDoctorAdapter(List<DoctorInfo> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }

    public void updateData(List<DoctorInfo> newList) {
        doctorList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_doctor, parent, false);
        return new DoctorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        DoctorInfo doctor = doctorList.get(position);

        holder.tvDoctorName.setText(doctor.fullName);
        holder.tvDoctorEmail.setText(doctor.email);
        holder.tvDoctorPhone.setText("SĐT: " + (doctor.phone != null ? doctor.phone : "Không có"));
        if (doctor.specialties != null && !doctor.specialties.isEmpty()) {
            holder.tvDoctorSpecialties.setText("Chuyên khoa: " + TextUtils.join(", ", doctor.specialties));
        } else {
            holder.tvDoctorSpecialties.setText("Chuyên khoa: N/A");
        }
        holder.itemView.setOnClickListener(v -> listener.onViewDetail(doctor));
        holder.btnEditDoctor.setOnClickListener(v -> listener.onEdit(doctor)); // <-- Thêm mới
        holder.btnViewDetail.setOnClickListener(v -> listener.onViewDetail(doctor));
        holder.btnDeleteDoctor.setOnClickListener(v -> listener.onDelete(doctor));
    }

    @Override
    public int getItemCount() {
        return doctorList != null ? doctorList.size() : 0;
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDoctorEmail, tvDoctorPhone, tvDoctorSpecialties;
        Button btnViewDetail, btnDeleteDoctor, btnEditDoctor;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorEmail = itemView.findViewById(R.id.tvDoctorEmail);
            tvDoctorPhone = itemView.findViewById(R.id.tvDoctorPhone);
            tvDoctorSpecialties = itemView.findViewById(R.id.tvDoctorSpecialties);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnEditDoctor = itemView.findViewById(R.id.btnEditDoctor);
            btnDeleteDoctor = itemView.findViewById(R.id.btnDeleteDoctor);
        }
    }
}