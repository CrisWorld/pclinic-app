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
import data.db.ExaminationFormDao; // 👈 Import
import data.db.PrescriptionDao;  // 👈 Import
import data.db.ServiceDao;       // 👈 Import
import java.text.NumberFormat; // 👈 Import
import data.model.ExaminationForm;   // 👈 Import
import example.pclinic.com.R;

@AndroidEntryPoint
public class AppointmentHistoryDetailFragment extends Fragment {

    private static final String ARG_APPOINTMENT_ID = "appointmentId";
    private long appointmentId;
    @Inject
    AppointmentDao appointmentDao;
    @Inject ExaminationFormDao examinationFormDao; // 👈 Inject
    @Inject PrescriptionDao prescriptionDao;   // 👈 Inject
    @Inject ServiceDao serviceDao;         // 👈 Inject

    private TextView tvDoctor, tvDate, tvEndDate, tvStatus, tvCheckIn, tvDescription, tvTotalCost;
    private Button btnViewPrescription, btnViewServices;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
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
        tvTotalCost = v.findViewById(R.id.tvTotalCost);
        btnViewPrescription = v.findViewById(R.id.btnViewPrescription);
        btnViewServices = v.findViewById(R.id.btnViewServices);

        // Lấy ID và tải dữ liệu
        if (getArguments() != null) {
            appointmentId = getArguments().getLong(ARG_APPOINTMENT_ID); // Lưu lại ID
            loadAppointmentDetails(appointmentId);
        }

        btnViewPrescription.setOnClickListener(view -> {
            // Mở fragment đơn thuốc
            PrescriptionListFragment fragment = PrescriptionListFragment.newInstance(appointmentId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnViewServices.setOnClickListener(view -> {
            // TODO: Mở fragment dịch vụ (tương tự như đơn thuốc)
            Toast.makeText(requireContext(), "Chức năng xem dịch vụ sẽ được cập nhật sau.", Toast.LENGTH_SHORT).show();
        });

        return v;
    }

    private void loadAppointmentDetails(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppointmentWithDoctor ap = appointmentDao.getDetail(id);

            // 👈 TÍNH TOÁN TỔNG CHI PHÍ
            ExaminationForm form = examinationFormDao.findByAppointmentId(id);
            double totalPrescriptionCost = 0;
            double totalServiceCost = 0;
            if (form != null) {
                totalPrescriptionCost = prescriptionDao.sumPriceByExaminationId(form.id);
                totalServiceCost = serviceDao.sumPriceByExaminationId(form.id);
            }
            final double totalCost = totalPrescriptionCost + totalServiceCost;

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

                // 👈 HIỂN THỊ TỔNG CHI PHÍ
                tvTotalCost.setText("💰 Tổng chi phí: " + currencyFormatter.format(totalCost));
            });
        });
    }
}
