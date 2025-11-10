package ui.doctor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.AppointmentDao;
import data.db.ExaminationFormDao;
import data.db.PatientDao;
import data.db.PrescriptionExaminationFormDao;
import data.db.ServiceExaminationFormDao;
import data.db.UserDao;
import data.enums.Enum; // 👈 QUAN TRỌNG: Phải import Enum
import data.model.Appointment;
import data.dto.AppointmentWithPatient;
import data.model.ExaminationForm;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class CreateExaminationFormFragment extends Fragment {

    public static CreateExaminationFormFragment newInstance(AppointmentWithPatient appointment) {
        CreateExaminationFormFragment fragment = new CreateExaminationFormFragment();
        Bundle args = new Bundle();
        // Serialize appointment data
        args.putLong("appointmentId", appointment.id);
        args.putLong("patientId", appointment.patientId);
        args.putLong("doctorId", appointment.doctorId);
        args.putString("patientName", appointment.patientName);
        args.putString("patientPhone", appointment.patientPhone);
        args.putString("patientGender", appointment.patientGender);
        args.putLong("patientBirthDate", appointment.patientBirthDate != null ? appointment.patientBirthDate : 0);
        args.putString("patientCode", appointment.patientCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ExaminationFormDao examinationFormDao;

    @Inject
    AppointmentDao appointmentDao;

    @Inject
    PatientDao patientDao;

    @Inject
    UserDao userDao;

    @Inject
    ServiceExaminationFormDao serviceExaminationFormDao;

    @Inject
    PrescriptionExaminationFormDao prescriptionExaminationFormDao;

    private long appointmentId;
    private long patientId;
    private long doctorId;
    private String patientName;
    private String patientPhone;
    private String patientGender;
    private long patientBirthDate;
    private String patientCode;
    private String patientAddress;

    // Views
    private TextView tvPatientName, tvPatientBirthDate, tvPatientAddress, tvPatientGender;
    private TextView tvPatientCode, tvPatientPhone, tvExaminationCode, tvExaminationDate;
    private TextInputEditText etMedicalHistory, etGeneralCondition, etHeight, etWeight;
    private TextInputEditText etPulse, etTemperature, etBloodPressure, etDiagnosis;
    private MaterialButton btnSave, btnCreateService, btnCreatePrescription, btnEditPatient;

    private long examinationFormId = 0; // 0 means new, >0 means existing

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_create_examination_form, container, false);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            appointmentId = args.getLong("appointmentId");
            patientId = args.getLong("patientId");
            doctorId = args.getLong("doctorId");
            patientName = args.getString("patientName");
            patientPhone = args.getString("patientPhone");
            patientGender = args.getString("patientGender");
            patientBirthDate = args.getLong("patientBirthDate");
            patientCode = args.getString("patientCode");
            android.util.Log.d("CreateExaminationForm", "onCreateView - appointmentId: " + appointmentId + ", patientId: " + patientId + ", doctorId: " + doctorId);
        } else {
            android.util.Log.e("CreateExaminationForm", "onCreateView - No arguments found!");
        }

        initViews(view);
        setupBackButton(view);
        loadPatientInfo();
        setupListeners();
        checkExistingExaminationForm();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload totals when returning from AddService/AddPrescription fragments
        if (examinationFormId > 0) {
            loadTotals();
        }
    }

    private void initViews(View view) {
        tvPatientName = view.findViewById(R.id.tvPatientName);
        tvPatientBirthDate = view.findViewById(R.id.tvPatientBirthDate);
        tvPatientAddress = view.findViewById(R.id.tvPatientAddress);
        tvPatientGender = view.findViewById(R.id.tvPatientGender);
        tvPatientCode = view.findViewById(R.id.tvPatientCode);
        tvPatientPhone = view.findViewById(R.id.tvPatientPhone);
        tvExaminationCode = view.findViewById(R.id.tvExaminationCode);
        tvExaminationDate = view.findViewById(R.id.tvExaminationDate);

        etMedicalHistory = view.findViewById(R.id.etMedicalHistory);
        etGeneralCondition = view.findViewById(R.id.etGeneralCondition);
        etHeight = view.findViewById(R.id.etHeight);
        etWeight = view.findViewById(R.id.etWeight);
        etPulse = view.findViewById(R.id.etPulse);
        etTemperature = view.findViewById(R.id.etTemperature);
        etBloodPressure = view.findViewById(R.id.etBloodPressure);
        etDiagnosis = view.findViewById(R.id.etDiagnosis);

        btnSave = view.findViewById(R.id.btnSave);
        btnCreateService = view.findViewById(R.id.btnCreateService);
        btnCreatePrescription = view.findViewById(R.id.btnCreatePrescription);
        btnEditPatient = view.findViewById(R.id.btnEditPatient);

        // Set examination date to today
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvExaminationDate.setText(dateFormat.format(new Date()));

        // Generate examination code
        String examinationCode = UUID.randomUUID().toString();
        tvExaminationCode.setText(examinationCode);
    }

    private void loadPatientInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            data.model.Patient patient = patientDao.findById(patientId);
            if (patient != null) {
                data.model.User user = userDao.findById(patient.userId);
                if (user != null) {
                    patientAddress = user.address;
                    requireActivity().runOnUiThread(() -> {
                        // Display patient info
                        tvPatientName.setText("Họ và Tên: " + (patientName != null ? patientName : user.fullName));
                        tvPatientCode.setText("Mã bệnh nhân: #" + (patientCode != null ? patientCode : patient.code));
                        tvPatientPhone.setText("SĐT: " + (patientPhone != null ? patientPhone : (user.phone != null ? user.phone : "N/A")));

                        // Gender
                        String genderText = "Nữ";
                        if (patientGender != null) {
                            String genderLower = patientGender.toLowerCase();
                            if (genderLower.equals("male") || genderLower.equals("nam")) {
                                genderText = "Nam";
                            } else if (genderLower.equals("female") || genderLower.equals("nữ")) {
                                genderText = "Nữ";
                            }
                        } else if (user.gender != null) {
                            String genderLower = user.gender.toLowerCase();
                            if (genderLower.equals("male") || genderLower.equals("nam")) {
                                genderText = "Nam";
                            } else if (genderLower.equals("female") || genderLower.equals("nữ")) {
                                genderText = "Nữ";
                            }
                        }
                        tvPatientGender.setText("Giới tính: " + genderText);

                        // Birth date and age
                        long birthDate = patientBirthDate > 0 ? patientBirthDate : (user.birthDate != null ? user.birthDate : 0);
                        if (birthDate > 0) {
                            Calendar birthCal = Calendar.getInstance();
                            birthCal.setTimeInMillis(birthDate);
                            Calendar today = Calendar.getInstance();
                            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
                            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                                age--;
                            }
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            tvPatientBirthDate.setText("Ngày sinh: " + dateFormat.format(birthCal.getTime()) + " - " + age + " tuổi");
                        } else {
                            tvPatientBirthDate.setText("Ngày sinh: N/A");
                        }

                        // Address
                        tvPatientAddress.setText("Địa chỉ: " + (patientAddress != null ? patientAddress : (user.address != null ? user.address : "N/A")));
                    });
                }
            }
        });
    }

    private void calculateAndUpdateTotals(ExaminationForm form) {
        long formId = form.id > 0 ? form.id : examinationFormId;
        if (formId == 0) {
            // Form not saved yet, set defaults
            form.totalService = 0;
            form.totalPrescription = 0;
            form.totalServiceAmount = 0;
            form.totalPrescriptionAmount = 0;
            form.totalAppointmentAmount = 0;
            form.grandTotal = 0;
            return;
        }

        // Calculate service totals
        List<data.model.ServiceExaminationForm> services = serviceExaminationFormDao.findByExaminationId(formId);
        form.totalService = services.size();
        form.totalServiceAmount = 0;
        for (data.model.ServiceExaminationForm service : services) {
            form.totalServiceAmount += service.price;
        }

        // Calculate prescription totals
        List<data.model.PrescriptionExaminationForm> prescriptions = prescriptionExaminationFormDao.findByExaminationId(formId);
        form.totalPrescription = prescriptions.size();
        form.totalPrescriptionAmount = 0;
        for (data.model.PrescriptionExaminationForm prescription : prescriptions) {
            form.totalPrescriptionAmount += prescription.price;
        }

        // Calculate appointment amount (can be set separately, default 0)
        form.totalAppointmentAmount = 0;

        // Calculate grand total
        form.grandTotal = form.totalServiceAmount + form.totalPrescriptionAmount + form.totalAppointmentAmount;
    }

    private void loadTotals() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm form = examinationFormDao.findById(examinationFormId);
            if (form != null) {
                calculateAndUpdateTotals(form);
                examinationFormDao.update(form);
            }
        });
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

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveExaminationForm());
        btnCreateService.setOnClickListener(v -> openAddServiceFragment());
        btnCreatePrescription.setOnClickListener(v -> openAddPrescriptionFragment());
        btnEditPatient.setOnClickListener(v -> {
            // TODO: Open edit patient dialog/fragment
            Toasty.info(requireContext(), "Tính năng chỉnh sửa bệnh nhân đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkExistingExaminationForm() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm existing = examinationFormDao.findByAppointmentId(appointmentId);
            if (existing != null) {
                examinationFormId = existing.id;
                requireActivity().runOnUiThread(() -> {
                    // Load existing data
                    etMedicalHistory.setText(existing.medicalHistory);
                    etGeneralCondition.setText(existing.generalCondition);
                    etHeight.setText(existing.height);
                    etWeight.setText(existing.weight);
                    etPulse.setText(existing.pulse);
                    etTemperature.setText(existing.temperature);
                    etBloodPressure.setText(existing.bloodPressure);
                    etDiagnosis.setText(existing.diagnosis);
                    tvExaminationCode.setText(existing.examinationCode);
                    if (existing.examinationDate != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tvExaminationDate.setText(dateFormat.format(existing.examinationDate));
                    }
                });
            }
        });
    }

    private void saveExaminationForm() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        if (TextUtils.isEmpty(diagnosis)) {
            Toasty.warning(requireContext(), "Vui lòng nhập chẩn đoán.", Toast.LENGTH_SHORT).show();
            etDiagnosis.requestFocus();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ExaminationForm form;
                if (examinationFormId > 0) {
                    form = examinationFormDao.findById(examinationFormId);
                } else {
                    form = new ExaminationForm();
                    form.appointmentId = appointmentId;
                    form.patientId = patientId;
                    form.doctorId = doctorId;
                    form.examinationCode = tvExaminationCode.getText().toString();
                    form.examinationDate = new Date();
                }

                // Update fields
                form.medicalHistory = etMedicalHistory.getText().toString();
                form.generalCondition = etGeneralCondition.getText().toString();
                form.diagnosis = diagnosis;
                form.height = etHeight.getText().toString();
                form.weight = etWeight.getText().toString();
                form.pulse = etPulse.getText().toString();
                form.temperature = etTemperature.getText().toString();
                form.bloodPressure = etBloodPressure.getText().toString();

                calculateAndUpdateTotals(form);

                if (examinationFormId > 0) {
                    examinationFormDao.update(form);
                } else {
                    examinationFormId = examinationFormDao.insert(form);
                }

                // 👇 LOGIC QUAN TRỌNG: CẬP NHẬT TRẠNG THÁI LỊCH HẸN
                Appointment appointment = appointmentDao.findById(appointmentId);
                if (appointment != null && appointment.status != Enum.AppointmentStatus.DONE) {
                    appointment.status = Enum.AppointmentStatus.DONE;
                    appointmentDao.update(appointment);
                }

                // Quay về màn hình trước trên luồng UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toasty.success(requireContext(), "Lưu phiếu khám thành công!", Toast.LENGTH_SHORT).show();
                        // Tự động quay về danh sách lịch hẹn
                        getParentFragmentManager().popBackStack();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toasty.error(requireContext(), "Lỗi khi lưu phiếu khám.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void openAddServiceFragment() {
        if (examinationFormId == 0) {
            // Save form first to get ID
            Executors.newSingleThreadExecutor().execute(() -> {
                // Save synchronously in this thread
                ExaminationForm form = new ExaminationForm();
                form.appointmentId = appointmentId;
                form.patientId = patientId;
                form.doctorId = doctorId;
                form.examinationCode = tvExaminationCode.getText().toString();
                form.examinationDate = new Date();
                form.medicalHistory = etMedicalHistory.getText().toString();
                form.generalCondition = etGeneralCondition.getText().toString();
                form.diagnosis = etDiagnosis.getText().toString();
                form.height = etHeight.getText().toString();
                form.weight = etWeight.getText().toString();
                form.pulse = etPulse.getText().toString();
                form.temperature = etTemperature.getText().toString();
                form.bloodPressure = etBloodPressure.getText().toString();
                calculateAndUpdateTotals(form);
                examinationFormId = examinationFormDao.insert(form);
                
                requireActivity().runOnUiThread(() -> {
                    AddServiceFragment fragment = AddServiceFragment.newInstance(appointmentId, examinationFormId);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            });
        } else {
            AddServiceFragment fragment = AddServiceFragment.newInstance(appointmentId, examinationFormId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openAddPrescriptionFragment() {
        if (examinationFormId == 0) {
            // Save form first to get ID
            Executors.newSingleThreadExecutor().execute(() -> {
                // Save synchronously in this thread
                ExaminationForm form = new ExaminationForm();
                form.appointmentId = appointmentId;
                form.patientId = patientId;
                form.doctorId = doctorId;
                form.examinationCode = tvExaminationCode.getText().toString();
                form.examinationDate = new Date();
                form.medicalHistory = etMedicalHistory.getText().toString();
                form.generalCondition = etGeneralCondition.getText().toString();
                form.diagnosis = etDiagnosis.getText().toString();
                form.height = etHeight.getText().toString();
                form.weight = etWeight.getText().toString();
                form.pulse = etPulse.getText().toString();
                form.temperature = etTemperature.getText().toString();
                form.bloodPressure = etBloodPressure.getText().toString();
                calculateAndUpdateTotals(form);
                examinationFormId = examinationFormDao.insert(form);
                
                requireActivity().runOnUiThread(() -> {
                    AddPrescriptionFragment fragment = AddPrescriptionFragment.newInstance(appointmentId, examinationFormId);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            });
        } else {
            AddPrescriptionFragment fragment = AddPrescriptionFragment.newInstance(appointmentId, examinationFormId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
