package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import data.db.DoctorDao;
import data.db.ExaminationFormDao;
import data.db.PrescriptionDao;
import data.db.ServiceDao;
import data.db.UserDao;
import data.dto.PrescriptionDetailDto;
import data.dto.ServiceDetailDto;
import data.model.Doctor;
import data.model.ExaminationForm;
import data.model.User;
import example.pclinic.com.R;

@AndroidEntryPoint
public class PatientViewExaminationFormFragment extends Fragment {

    @Inject ExaminationFormDao examinationFormDao;
    @Inject DoctorDao doctorDao;
    @Inject UserDao userDao;
    @Inject ServiceDao serviceDao;
    @Inject PrescriptionDao prescriptionDao;

    private long appointmentId;

    public static PatientViewExaminationFormFragment newInstance(long appointmentId) {
        PatientViewExaminationFormFragment fragment = new PatientViewExaminationFormFragment();
        Bundle args = new Bundle();
        args.putLong("appointmentId", appointmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_view_examination_form, container, false);
        if (getArguments() != null) {
            appointmentId = getArguments().getLong("appointmentId");
        }
        setupBackButton(view);
        loadExaminationData(view);
        return view;
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void loadExaminationData(View view) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);

            if (form == null) {
                requireActivity().runOnUiThread(() -> displayErrorMessage(view, "Chưa có phiếu khám cho lịch hẹn này."));
                return;
            }

            // --- 👇 SỬA LỖI TẠI ĐÂY ---
            // TÍNH TOÁN TẤT CẢ DỮ LIỆU Ở LUỒNG NỀN
            Doctor doctor = doctorDao.findById(form.doctorId);
            User doctorUser = (doctor != null) ? userDao.findById(doctor.userId) : null;
            List<ServiceDetailDto> services = serviceDao.findByExaminationId(form.id);
            List<PrescriptionDetailDto> prescriptions = prescriptionDao.findByExaminationId(form.id);

            // TÍNH TỔNG TIỀN Ở LUỒNG NỀN
            double totalServiceCost = serviceDao.sumPriceByExaminationId(form.id);
            double totalPrescriptionCost = prescriptionDao.sumPriceByExaminationId(form.id);
            double grandTotal = totalServiceCost + totalPrescriptionCost;

            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    // TRUYỀN TẤT CẢ DỮ LIỆU ĐÃ TÍNH TOÁN SẴN SANG HÀM DISPLAY
                    displayData(view, form, doctorUser, services, prescriptions, grandTotal);
                }
            });
        });
    }

    private void displayData(View view, ExaminationForm form, User doctorUser, List<ServiceDetailDto> services, List<PrescriptionDetailDto> prescriptions, double grandTotal) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Header
        ((TextView) view.findViewById(R.id.tvExaminationCode)).setText("Mã: " + (form.examinationCode != null ? form.examinationCode : "N/A"));

        // Doctor Info
        TextView tvDoctorInfo = view.findViewById(R.id.tvDoctorInfo);
        if (doctorUser != null) {
            String phone = (doctorUser.phone != null && !doctorUser.phone.isEmpty()) ? doctorUser.phone : "Chưa có";
            tvDoctorInfo.setText("Họ và tên: " + doctorUser.fullName + "\n" + "SĐT: " + phone);
        } else {
            tvDoctorInfo.setText("Không tìm thấy thông tin bác sĩ.");
        }

        // Examination Details
        ((TextView) view.findViewById(R.id.tvExaminationDate)).setText(dateFormat.format(form.examinationDate));
        ((TextView) view.findViewById(R.id.tvDiagnosis)).setText(form.diagnosis != null ? form.diagnosis : "Chưa có chẩn đoán.");

        // Services
        RecyclerView rvServices = view.findViewById(R.id.rvServices);
        TextView tvEmptyServices = view.findViewById(R.id.tvEmptyServices);
        ((TextView) view.findViewById(R.id.tvTotalService)).setText("(" + services.size() + ")");
        if (services.isEmpty()) {
            tvEmptyServices.setVisibility(View.VISIBLE);
            rvServices.setVisibility(View.GONE);
        } else {
            tvEmptyServices.setVisibility(View.GONE);
            rvServices.setVisibility(View.VISIBLE);
            rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
            rvServices.setAdapter(new ServiceAdapter(services));
        }

        // Prescriptions
        RecyclerView rvPrescriptions = view.findViewById(R.id.rvPrescriptions);
        TextView tvEmptyPrescriptions = view.findViewById(R.id.tvEmptyPrescriptions);
        ((TextView) view.findViewById(R.id.tvTotalPrescription)).setText("(" + prescriptions.size() + ")");
        if (prescriptions.isEmpty()) {
            tvEmptyPrescriptions.setVisibility(View.VISIBLE);
            rvPrescriptions.setVisibility(View.GONE);
        } else {
            tvEmptyPrescriptions.setVisibility(View.GONE);
            rvPrescriptions.setVisibility(View.VISIBLE);
            rvPrescriptions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvPrescriptions.setAdapter(new PrescriptionAdapter(prescriptions));
        }

        // Grand Total - KHÔNG CÒN TRUY VẤN DAO Ở ĐÂY
        ((TextView) view.findViewById(R.id.tvGrandTotal)).setText(currencyFormatter.format(grandTotal));
    }
    private void displayErrorMessage(View view, String message) {
        // You can implement a more sophisticated error view if needed
        ((TextView) view.findViewById(R.id.tvDoctorInfo)).setText(message);
    }
}
