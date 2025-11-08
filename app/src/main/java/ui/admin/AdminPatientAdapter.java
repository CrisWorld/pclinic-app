package ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import data.dto.PatientInfo;
import example.pclinic.com.R;

public class AdminPatientAdapter extends RecyclerView.Adapter<AdminPatientAdapter.PatientViewHolder> {
    private List<PatientInfo> patientList;
    private final OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onViewDetail(PatientInfo patient);
    }

    public AdminPatientAdapter(List<PatientInfo> list, OnPatientClickListener listener) {
        this.patientList = list;
        this.listener = listener;
    }

    public void updateData(List<PatientInfo> newList) {
        this.patientList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_patient, parent, false);
        return new PatientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientInfo patient = patientList.get(position);
        holder.tvPatientName.setText(patient.fullName);
        holder.tvPatientEmail.setText(patient.email);
        holder.tvPatientCode.setText("Mã BN: " + patient.patientCode);

        // Bắt sự kiện click trên cả item và nút "Chi tiết"
        holder.itemView.setOnClickListener(v -> listener.onViewDetail(patient));
        holder.btnViewDetail.setOnClickListener(v -> listener.onViewDetail(patient));
    }

    @Override
    public int getItemCount() {
        return patientList != null ? patientList.size() : 0;
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvPatientEmail, tvPatientCode;
        Button btnViewDetail;

        PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientEmail = itemView.findViewById(R.id.tvPatientEmail);
            tvPatientCode = itemView.findViewById(R.id.tvPatientCode);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }
}
