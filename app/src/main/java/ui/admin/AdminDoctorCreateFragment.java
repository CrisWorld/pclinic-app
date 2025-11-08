package ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.DoctorDao;
import data.db.UserDao;
import data.enums.Enum;
import data.model.Doctor;
import data.model.User;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminDoctorCreateFragment extends Fragment {

    private TextInputEditText etFullName, etEmail, etPassword, etSpecialties;
    private Button btnCreate;
    private ImageButton btnBack;

    @Inject
    public UserDao userDao;
    @Inject
    public DoctorDao doctorDao;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_doctor_create, container, false);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etSpecialties = view.findViewById(R.id.etSpecialties);
        btnCreate = view.findViewById(R.id.btnCreate);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnCreate.setOnClickListener(v -> createDoctor());

        return view;
    }

    private void createDoctor() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String specialties = etSpecialties.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toasty.warning(requireContext(), "Vui lòng điền đầy đủ thông tin bắt buộc.").show();
            return;
        }

        databaseExecutor.execute(() -> {
            // Kiểm tra xem email đã tồn tại chưa
            if (userDao.findByEmail(email) != null) {
                requireActivity().runOnUiThread(() ->
                        Toasty.error(requireContext(), "Email đã được sử dụng.").show()
                );
                return;
            }

            // Tạo User mới
            User newUser = new User();
            newUser.fullName = fullName;
            newUser.email = email;
            newUser.password = password; // Trong thực tế, cần mã hóa mật khẩu
            newUser.role = Enum.UserRole.DOCTOR;

            // Chèn User và lấy ID
            long newUserId = userDao.insert(newUser);

            // Tạo Doctor mới
            Doctor newDoctor = new Doctor(newUserId);
            if (!TextUtils.isEmpty(specialties)) {
                List<String> specialtiesList = Arrays.asList(specialties.split("\\s*,\\s*"));
                newDoctor.specialties = specialtiesList;
            }

            // Chèn Doctor
            doctorDao.insert(newDoctor);

            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Tạo tài khoản bác sĩ thành công!").show();
                getParentFragmentManager().popBackStack(); // Quay lại danh sách
            });
        });
    }
}
