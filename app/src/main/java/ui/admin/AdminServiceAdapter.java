package ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import data.model.Service;
import example.pclinic.com.R;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ServiceViewHolder> {
    private List<Service> serviceList;
    private final OnServiceClickListener listener;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnServiceClickListener {
        void onEdit(Service service);
        void onDelete(Service service);
    }

    public AdminServiceAdapter(List<Service> list, OnServiceClickListener listener) {
        this.serviceList = list;
        this.listener = listener;
    }

    public void updateData(List<Service> newList) {
        this.serviceList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_service, parent, false);
        return new ServiceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.tvServiceName.setText(service.name);
        holder.tvServiceCode.setText("Mã: " + service.code);
        holder.tvServicePrice.setText(currencyFormatter.format(service.price));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(service));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(service));
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServiceCode, tvServicePrice;
        Button btnEdit, btnDelete;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceCode = itemView.findViewById(R.id.tvServiceCode);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
