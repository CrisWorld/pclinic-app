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

import data.dto.ServiceDetailDto;
import example.pclinic.com.R;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private final List<ServiceDetailDto> serviceList;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ServiceAdapter(List<ServiceDetailDto> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tái sử dụng layout của đơn thuốc vì cấu trúc giống hệt
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(serviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrescriptionName); // ID giống nhau
            tvCode = itemView.findViewById(R.id.tvPrescriptionCode);
            tvPrice = itemView.findViewById(R.id.tvPrescriptionPrice);
        }

        void bind(ServiceDetailDto item) {
            tvName.setText(item.serviceName);
            tvCode.setText("Mã: " + item.serviceCode);
            tvPrice.setText("Giá: " + currencyFormatter.format(item.price));
        }
    }
}
