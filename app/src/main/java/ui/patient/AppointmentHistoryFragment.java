package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import example.pclinic.com.R;

public class AppointmentHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentHistoryAdapter adapter;

    public AppointmentHistoryFragment() {
        // Bắt buộc constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_appointment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Dữ liệu mẫu
        List<AppointmentHistoryItem> items = new ArrayList<>();
        items.add(new AppointmentHistoryItem("BS. John Doe", "12/10/2025", "Khám nội tổng quát", "Đã hoàn thành"));
        items.add(new AppointmentHistoryItem("BS. Jane Smith", "05/09/2025", "Khám tai mũi họng", "Đã hoàn thành"));
        items.add(new AppointmentHistoryItem("BS. Lê Văn Tùng", "30/08/2025", "Khám nhi khoa", "Đã hủy"));

        adapter = new AppointmentHistoryAdapter(items);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Lớp model cho từng item
    public static class AppointmentHistoryItem {
        public String doctorName;
        public String date;
        public String description;
        public String status;

        public AppointmentHistoryItem(String doctorName, String date, String description, String status) {
            this.doctorName = doctorName;
            this.date = date;
            this.description = description;
            this.status = status;
        }
    }

    // Adapter cho RecyclerView
    private static class AppointmentHistoryAdapter extends RecyclerView.Adapter<AppointmentHistoryAdapter.ViewHolder> {

        private final List<AppointmentHistoryItem> data;

        public AppointmentHistoryAdapter(List<AppointmentHistoryItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_item_appointment_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentHistoryItem item = data.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView txtDoctorName;
            private final android.widget.TextView txtDate;
            private final android.widget.TextView txtDescription;
            private final android.widget.TextView txtStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtDoctorName = itemView.findViewById(R.id.txtDoctorName);
                txtDate = itemView.findViewById(R.id.txtDate);
                txtDescription = itemView.findViewById(R.id.txtDescription);
                txtStatus = itemView.findViewById(R.id.txtStatus);

                itemView.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(),
                            "Xem chi tiết lịch hẹn với " + txtDoctorName.getText(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            public void bind(AppointmentHistoryItem item) {
                txtDoctorName.setText(item.doctorName);
                txtDate.setText("Ngày: " + item.date);
                txtDescription.setText(item.description);
                txtStatus.setText(item.status);

                // Màu theo trạng thái
                int color;
                if (item.status.equalsIgnoreCase("Đã hoàn thành"))
                    color = itemView.getResources().getColor(R.color.success);
                else if (item.status.equalsIgnoreCase("Đã hủy"))
                    color = itemView.getResources().getColor(android.R.color.holo_red_dark);
                else
                    color = itemView.getResources().getColor(android.R.color.darker_gray);

                txtStatus.setTextColor(color);
            }
        }
    }
}
