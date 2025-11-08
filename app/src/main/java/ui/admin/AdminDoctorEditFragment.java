package ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import data.db.admin.AdminDoctorDao;
import data.dto.DoctorInfo;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import android.widget.RadioGroup; // Thêm import này
import android.widget.RadioButton;

@AndroidEntryPoint
public class AdminDoctorEditFragment extends Fragment {

    private static final String ARG_DOCTOR_ID = "doctor_id";

    private TextInputEditText etFullName, etEmail, etPhone, etSpecialties, etBio;
    private Button btnUpdate;
    private ImageButton btnBack;
    private RadioGroup rgGender; // Thêm biến
    private RadioButton rbMale, rbFemale;

    @Inject
    public AdminDoctorDao adminDoctorDao;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private DoctorInfo currentDoctor;

    // Factory method để tạo instance và truyền doctorId
    public static AdminDoctorEditFragment newInstance(long doctorId) {
        AdminDoctorEditFragment fragment = new AdminDoctorEditFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCTOR_ID, doctorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_doctor_edit, container, false); // Dùng layout mới

        initViews(view);
        loadDoctorData();

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnUpdate.setOnClickListener(v -> updateDoctor());

        return view;
    }

    private void initViews(View view) {
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etSpecialties = view.findViewById(R.id.etSpecialties);
        etBio = view.findViewById(R.id.etBio);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnBack = view.findViewById(R.id.btnBack);
        rgGender = view.findViewById(R.id.rgGender);
        rbMale = view.findViewById(R.id.rbMale);
        rbFemale = view.findViewById(R.id.rbFemale);
        // Cập nhật tiêu đề
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Chỉnh Sửa Bác Sĩ");
    }

    private void loadDoctorData() {
        if (getArguments() == null) return;
        long doctorId = getArguments().getLong(ARG_DOCTOR_ID);

        adminDoctorDao.getDoctorInfoById(doctorId).observe(getViewLifecycleOwner(), doctorInfo -> {
            if (doctorInfo != null) {
                currentDoctor = doctorInfo;
                populateForm(doctorInfo);
            }
        });
    }

    private void populateForm(DoctorInfo doctor) {
        etFullName.setText(doctor.fullName);
        etEmail.setText(doctor.email);
        etPhone.setText(doctor.phone);
        etBio.setText(doctor.bio);

        if (doctor.gender != null) {
            if (doctor.gender.equalsIgnoreCase("male")) {
                rbMale.setChecked(true);
            } else if (doctor.gender.equalsIgnoreCase("female")) {
                rbFemale.setChecked(true);
            }
        }

        if (doctor.specialties != null && !doctor.specialties.isEmpty()) {
            etSpecialties.setText(TextUtils.join(", ", doctor.specialties));
        }
    }

    private void updateDoctor() {
        if (currentDoctor == null) {
            Toasty.error(requireContext(), "Không tìm thấy thông tin bác sĩ.").show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String specialtiesInput = etSpecialties.getText().toString().trim();
        List<String> specialtiesList = Arrays.asList(specialtiesInput.split("\\s*,\\s*"));

        String gender;
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rbMale) {
            gender = "male";
        } else if (selectedGenderId == R.id.rbFemale) {
            gender = "female";
        } else {
            gender = "male";
        }

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
            Toasty.warning(requireContext(), "Tên và Email không được để trống.").show();
            return;
        }

        dbExecutor.execute(() -> {
            adminDoctorDao.updateUser(currentDoctor.userId, fullName, email, phone, gender);
            adminDoctorDao.updateDoctor(currentDoctor.doctorId, specialtiesList, bio);

            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Cập nhật thông tin bác sĩ thành công!").show();
                getParentFragmentManager().popBackStack();
            });
        });
    }
}

