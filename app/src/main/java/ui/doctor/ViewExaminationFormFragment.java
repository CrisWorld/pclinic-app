package ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.ExaminationFormDao;
import data.db.PatientDao;
import data.db.PrescriptionExaminationFormDao;
import data.db.ServiceExaminationFormDao;
import data.db.UserDao;
import data.dto.AppointmentWithPatient;
import data.model.ExaminationForm;
import data.model.Patient;
import data.model.PrescriptionExaminationForm;
import data.model.ServiceExaminationForm;
import data.model.User;
import example.pclinic.com.R;

@AndroidEntryPoint
public class ViewExaminationFormFragment extends Fragment {

    @Inject
    ExaminationFormDao examinationFormDao;
    @Inject
    PatientDao patientDao;
    @Inject
    UserDao userDao;
    @Inject
    ServiceExaminationFormDao serviceExaminationFormDao;
    @Inject
    PrescriptionExaminationFormDao prescriptionExaminationFormDao;

    private long appointmentId;

    public static ViewExaminationFormFragment newInstance(AppointmentWithPatient appointment) {
        ViewExaminationFormFragment fragment = new ViewExaminationFormFragment();
        Bundle args = new Bundle();
        long id = appointment != null ? appointment.id : 0;
        args.putLong("appointmentId", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_view_examination_form, container, false);
        Bundle args = getArguments();
        if (args != null) {
            appointmentId = args.getLong("appointmentId");
        }
        setupBackButton(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && appointmentId > 0) {
            loadExaminationForm(getView());
        }
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadExaminationForm(View view) {
        if (appointmentId == 0) {
            displayErrorMessage(view, "Không có thông tin lịch hẹn.");
            return;
        }

        showLoadingState(view);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);
                if (form == null) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> displayErrorMessage(getView(), "Không tìm thấy phiếu khám cho lịch hẹn này."));
                    }
                    return;
                }

                Patient patient = patientDao.findById(form.patientId);
                User user = (patient != null) ? userDao.findById(patient.userId) : null;
                List<ServiceExaminationForm> services = serviceExaminationFormDao.findByExaminationId(form.id);
                List<PrescriptionExaminationForm> prescriptions = prescriptionExaminationFormDao.findByExaminationId(form.id);

                final User finalUser = user;
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (getView() != null) {
                            displayExaminationForm(getView(), form, finalUser, services, prescriptions);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> displayErrorMessage(getView(), "Lỗi khi tải phiếu khám: " + e.getMessage()));
                }
            }
        });
    }

    private void showLoadingState(View view) {
        if (view == null) return;
        TextView tvPatientInfo = view.findViewById(R.id.tvPatientInfo);
        if (tvPatientInfo != null) {
            tvPatientInfo.setText("Đang tải thông tin...");
        }
        TextView tvServicesPrescriptions = view.findViewById(R.id.tvServicesPrescriptions);
        if (tvServicesPrescriptions != null) {
            tvServicesPrescriptions.setText("Đang tải...");
        }
    }

    private void displayErrorMessage(View view, String message) {
        if (view == null) return;
        TextView tvPatientInfo = view.findViewById(R.id.tvPatientInfo);
        if (tvPatientInfo != null) {
            tvPatientInfo.setText(message);
        }
        TextView tvServicesPrescriptions = view.findViewById(R.id.tvServicesPrescriptions);
        if(tvServicesPrescriptions != null) {
            tvServicesPrescriptions.setText("Không có dữ liệu");
        }
    }

    private void displayExaminationForm(View view, ExaminationForm form, User user, List<ServiceExaminationForm> services, List<PrescriptionExaminationForm> prescriptions) {
        if (view == null) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Header and Patient Info
        ((TextView) view.findViewById(R.id.tvExaminationCode)).setText("Mã: " + (form.examinationCode != null ? form.examinationCode : "N/A"));
        TextView tvPatientInfo = view.findViewById(R.id.tvPatientInfo);
        if (user != null) {
            tvPatientInfo.setText("Họ và tên: " + user.fullName + "\nSĐT: " + (user.phone != null ? user.phone : "N/A"));
        } else {
            tvPatientInfo.setText("Không tìm thấy thông tin bệnh nhân.");
        }

        // Examination Details
        ((TextView) view.findViewById(R.id.tvExaminationDate)).setText(form.examinationDate != null ? dateFormat.format(form.examinationDate) : "N/A");
        ((TextView) view.findViewById(R.id.tvMedicalHistory)).setText("Tiền sử bệnh: " + (form.medicalHistory != null ? form.medicalHistory : "--"));
        ((TextView) view.findViewById(R.id.tvGeneralCondition)).setText("Tổng trạng: " + (form.generalCondition != null ? form.generalCondition : "--"));
        ((TextView) view.findViewById(R.id.tvHeight)).setText("Chiều cao: " + (form.height != null ? form.height : "--") + " cm");
        ((TextView) view.findViewById(R.id.tvWeight)).setText("Cân nặng: " + (form.weight != null ? form.weight : "--") + " kg");
        ((TextView) view.findViewById(R.id.tvPulse)).setText("Mạch: " + (form.pulse != null ? form.pulse : "--"));
        ((TextView) view.findViewById(R.id.tvTemperature)).setText("Nhiệt độ: " + (form.temperature != null ? form.temperature : "--") + "°C");
        ((TextView) view.findViewById(R.id.tvBloodPressure)).setText("Huyết áp: " + (form.bloodPressure != null ? form.bloodPressure : "--"));
        ((TextView) view.findViewById(R.id.tvDiagnosis)).setText(form.diagnosis != null && !form.diagnosis.isEmpty() ? form.diagnosis : "Chưa có chẩn đoán");

        // Services and Prescriptions Summary
        TextView tvServicesPrescriptions = view.findViewById(R.id.tvServicesPrescriptions);
        StringBuilder sb = new StringBuilder();

        if (services.isEmpty()) {
            sb.append("Không có dịch vụ nào được chỉ định.\n");
        } else {
            sb.append("Dịch vụ: ").append(services.size()).append(" dịch vụ\n");
            double serviceTotal = 0;
            for (ServiceExaminationForm service : services) {
                serviceTotal += service.price;
            }
            sb.append("Tổng tiền dịch vụ: ").append(currencyFormatter.format(serviceTotal)).append("\n");
        }

        sb.append("\n"); // Add a blank line for separation

        if(prescriptions.isEmpty()) {
            sb.append("Không có thuốc nào được kê.\n");
        } else {
            sb.append("Đơn thuốc: ").append(prescriptions.size()).append(" loại thuốc\n");
            double prescriptionTotal = 0;
            for (PrescriptionExaminationForm prescription : prescriptions) {
                prescriptionTotal += prescription.price;
            }
            sb.append("Tổng tiền đơn thuốc: ").append(currencyFormatter.format(prescriptionTotal));
        }

        tvServicesPrescriptions.setText(sb.toString());
    }
}
