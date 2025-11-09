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
import data.model.PrescriptionExaminationForm;
import data.model.ServiceExaminationForm;
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
        args.putLong("appointmentId", appointment.id);
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
        loadExaminationForm(view);

        return view;
    }

    private void setupBackButton(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void loadExaminationForm(View view) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);
            if (form != null) {
                // Load patient info
                data.model.Patient patient = patientDao.findById(form.patientId);
                data.model.User user = null;
                if (patient != null) {
                    user = userDao.findById(patient.userId);
                }

                // Load services and prescriptions
                List<ServiceExaminationForm> services = serviceExaminationFormDao.findByExaminationId(form.id);
                List<PrescriptionExaminationForm> prescriptions = prescriptionExaminationFormDao.findByExaminationId(form.id);

                final data.model.User finalUser = user;
                final data.model.Patient finalPatient = patient;
                requireActivity().runOnUiThread(() -> {
                    displayExaminationForm(view, form, finalPatient, finalUser, services, prescriptions);
                });
            } else {
                requireActivity().runOnUiThread(() -> {
                    TextView tvInfo = view.findViewById(R.id.tvPatientInfo);
                    if (tvInfo != null) {
                        tvInfo.setText("Không tìm thấy phiếu khám");
                    }
                });
            }
        });
    }

    private void displayExaminationForm(View view, ExaminationForm form, data.model.Patient patient,
                                       data.model.User user, List<ServiceExaminationForm> services,
                                       List<PrescriptionExaminationForm> prescriptions) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Header - Examination Code
        TextView tvExaminationCode = view.findViewById(R.id.tvExaminationCode);
        if (tvExaminationCode != null) {
            tvExaminationCode.setText("Mã: " + (form.examinationCode != null ? form.examinationCode : "N/A"));
        }

        // Patient Info
        TextView tvPatientInfo = view.findViewById(R.id.tvPatientInfo);
        if (tvPatientInfo != null && user != null) {
            String patientInfo = "Họ và tên: " + (user.fullName != null ? user.fullName : "N/A") + "\n" +
                    "SĐT: " + (user.phone != null ? user.phone : "N/A") + "\n" +
                    "Địa chỉ: " + (user.address != null ? user.address : "N/A");
            if (patient != null) {
                patientInfo += "\nMã bệnh nhân: #" + (patient.code != null ? patient.code : "N/A");
            }
            tvPatientInfo.setText(patientInfo);
        }

        // Examination Date
        TextView tvExaminationDate = view.findViewById(R.id.tvExaminationDate);
        if (tvExaminationDate != null) {
            tvExaminationDate.setText(form.examinationDate != null ? dateFormat.format(form.examinationDate) : "N/A");
        }

        // Medical History
        TextView tvMedicalHistory = view.findViewById(R.id.tvMedicalHistory);
        if (tvMedicalHistory != null) {
            tvMedicalHistory.setText(form.medicalHistory != null && !form.medicalHistory.isEmpty() ? form.medicalHistory : "N/A");
        }

        // General Condition
        TextView tvGeneralCondition = view.findViewById(R.id.tvGeneralCondition);
        if (tvGeneralCondition != null) {
            tvGeneralCondition.setText(form.generalCondition != null && !form.generalCondition.isEmpty() ? form.generalCondition : "N/A");
        }

        // Vital Signs
        TextView tvHeight = view.findViewById(R.id.tvHeight);
        if (tvHeight != null) {
            tvHeight.setText(form.height != null && !form.height.isEmpty() ? form.height + " cm" : "-- cm");
        }

        TextView tvWeight = view.findViewById(R.id.tvWeight);
        if (tvWeight != null) {
            tvWeight.setText(form.weight != null && !form.weight.isEmpty() ? form.weight + " kg" : "-- kg");
        }

        TextView tvPulse = view.findViewById(R.id.tvPulse);
        if (tvPulse != null) {
            tvPulse.setText(form.pulse != null && !form.pulse.isEmpty() ? form.pulse : "--");
        }

        TextView tvTemperature = view.findViewById(R.id.tvTemperature);
        if (tvTemperature != null) {
            tvTemperature.setText(form.temperature != null && !form.temperature.isEmpty() ? form.temperature + "°C" : "--");
        }

        TextView tvBloodPressure = view.findViewById(R.id.tvBloodPressure);
        if (tvBloodPressure != null) {
            tvBloodPressure.setText(form.bloodPressure != null && !form.bloodPressure.isEmpty() ? form.bloodPressure : "--");
        }

        // Diagnosis
        TextView tvDiagnosis = view.findViewById(R.id.tvDiagnosis);
        if (tvDiagnosis != null) {
            tvDiagnosis.setText(form.diagnosis != null && !form.diagnosis.isEmpty() ? form.diagnosis : "Chưa có chẩn đoán");
        }

        // Services and Prescriptions
        TextView tvServicesPrescriptions = view.findViewById(R.id.tvServicesPrescriptions);
        if (tvServicesPrescriptions != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Dịch vụ: ").append(services.size()).append(" dịch vụ\n");
            double serviceTotal = 0;
            for (ServiceExaminationForm service : services) {
                serviceTotal += service.price;
            }
            sb.append("Tổng tiền dịch vụ: ").append(currencyFormatter.format(serviceTotal)).append("\n\n");

            sb.append("Đơn thuốc: ").append(prescriptions.size()).append(" đơn thuốc\n");
            double prescriptionTotal = 0;
            for (PrescriptionExaminationForm prescription : prescriptions) {
                prescriptionTotal += prescription.price;
            }
            sb.append("Tổng tiền đơn thuốc: ").append(currencyFormatter.format(prescriptionTotal)).append("\n\n");

            sb.append("Tổng cộng: ").append(currencyFormatter.format(form.grandTotal));
            tvServicesPrescriptions.setText(sb.toString());
        }
    }
}
