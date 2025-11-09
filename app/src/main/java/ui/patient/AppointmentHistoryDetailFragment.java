package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.AppointmentDao;
import data.dto.AppointmentWithDoctor;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AppointmentHistoryDetailFragment extends Fragment {

    private static final String ARG_APPOINTMENT_ID = "appointmentId";

    @Inject
    AppointmentDao appointmentDao;

    private TextView tvDoctor, tvDate, tvEndDate, tvStatus, tvCheckIn, tvDescription;
    private Button btnViewPrescription, btnViewServices;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static AppointmentHistoryDetailFragment newInstance(long appointmentId) {
        AppointmentHistoryDetailFragment fragment = new AppointmentHistoryDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_APPOINTMENT_ID, appointmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.patient_fragment_appointment_history_detail, container, false);

        // Ánh xạ view
        tvDoctor = v.findViewById(R.id.tvDoctor);
        tvDate = v.findViewById(R.id.tvDate);
        tvEndDate = v.findViewById(R.id.tvEndDate);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvCheckIn = v.findViewById(R.id.tvCheckIn);
        tvDescription = v.findViewById(R.id.tvDescription);
        btnViewPrescription = v.findViewById(R.id.btnViewPrescription);
        btnViewServices = v.findViewById(R.id.btnViewServices);

        // Lấy ID và tải dữ liệu
        if (getArguments() != null) {
            long id = getArguments().getLong(ARG_APPOINTMENT_ID);
            loadAppointmentDetails(id);
        }

        // Xử lý sự kiện click (hiện tại chỉ hiển thị Toast)
        btnViewPrescription.setOnClickListener(view -> {
            // TODO: Thay thế bằng việc mở fragment đơn thuốc
            Toast.makeText(requireContext(), "Chức năng xem đơn thuốc sẽ được cập nhật sau.", Toast.LENGTH_SHORT).show();
        });

        btnViewServices.setOnClickListener(view -> {
            // TODO: Thay thế bằng việc mở fragment dịch vụ
            Toast.makeText(requireContext(), "Chức năng xem dịch vụ sẽ được cập nhật sau.", Toast.LENGTH_SHORT).show();
        });


        return v;
    }

    private void loadAppointmentDetails(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppointmentWithDoctor ap = appointmentDao.getDetail(id);

            requireActivity().runOnUiThread(() -> {
                if (ap == null) return;

                tvDoctor.setText("👨‍⚕️ Bác sĩ: " + ap.fullName);
                tvDate.setText("📅 Bắt đầu: " + sdf.format(ap.startDate));
                tvEndDate.setText("⏱ Kết thúc: " + sdf.format(ap.endDate));
                tvStatus.setText("✅ Trạng thái: " + ap.status.name());

                if (ap.checkInDate != null)
                    tvCheckIn.setText("⏳ Check-in: " + sdf.format(ap.checkInDate));
                else
                    tvCheckIn.setText("⏳ Check-in: Chưa check-in");

                tvDescription.setText("📝 Ghi chú: " + (ap.description == null ? "Không có" : ap.description));
            });
        });
    }
}
