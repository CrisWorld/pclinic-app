package ui.patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import data.dto.PrescriptionDetailDto;
import example.pclinic.com.R;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {

    private final List<PrescriptionDetailDto> prescriptionList;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public PrescriptionAdapter(List<PrescriptionDetailDto> prescriptionList) {
        this.prescriptionList = prescriptionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(prescriptionList.get(position));
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrescriptionName);
            tvCode = itemView.findViewById(R.id.tvPrescriptionCode);
            tvPrice = itemView.findViewById(R.id.tvPrescriptionPrice);
        }

        void bind(PrescriptionDetailDto item) {
            tvName.setText(item.prescriptionName);
            tvCode.setText("Mã: " + item.prescriptionCode);
            tvPrice.setText("Giá: " + currencyFormatter.format(item.price));
        }
    }
}
