package ui.admin;

import android.view.LayoutInflater;import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import data.model.Prescription;
import example.pclinic.com.R;

public class AdminPrescriptionAdapter extends RecyclerView.Adapter<AdminPrescriptionAdapter.PrescriptionViewHolder> {
    private List<Prescription> prescriptionList;
    private final OnPrescriptionClickListener listener;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnPrescriptionClickListener {
        void onEdit(Prescription prescription);
        void onDelete(Prescription prescription);
    }

    public AdminPrescriptionAdapter(List<Prescription> list, OnPrescriptionClickListener listener) {
        this.prescriptionList = list;
        this.listener = listener;
    }

    public void updateData(List<Prescription> newList) {
        this.prescriptionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_prescription, parent, false);
        return new PrescriptionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        Prescription prescription = prescriptionList.get(position);
        holder.tvPrescriptionName.setText(prescription.name);
        holder.tvPrescriptionCode.setText("Mã: " + prescription.code);
        holder.tvPrescriptionPrice.setText(currencyFormatter.format(prescription.price));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(prescription));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(prescription));
    }

    @Override
    public int getItemCount() {
        return prescriptionList != null ? prescriptionList.size() : 0;
    }

    static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrescriptionName, tvPrescriptionCode, tvPrescriptionPrice;
        Button btnEdit, btnDelete;

        PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrescriptionName = itemView.findViewById(R.id.tvPrescriptionName);
            tvPrescriptionCode = itemView.findViewById(R.id.tvPrescriptionCode);
            tvPrescriptionPrice = itemView.findViewById(R.id.tvPrescriptionPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
