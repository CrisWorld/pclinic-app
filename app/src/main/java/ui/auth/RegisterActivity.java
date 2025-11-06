package ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.PatientDao;
import data.enums.Enum;
import data.model.Patient;
import data.model.User;
import data.repository.UserRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import util.ValidationUtils;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    @Inject
    public UserRepository userRepository;
    @Inject
    public PatientDao patientDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_register_activity);

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Ẩn title mặc định của ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        // Bật nút back (mũi tên)
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Quay lại màn hình gọi Intent
        });

        // 🔹 Ánh xạ View
        layoutName = findViewById(R.id.layoutName);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        findViewById(R.id.btnRegister).setOnClickListener(v -> validateAndRegister());

    }

    private void validateAndRegister() {
        clearErrors();

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean valid = true;

        if (!ValidationUtils.isValidFullName(name)) {
            layoutName.setError("Họ tên không được để trống");
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            layoutEmail.setError("Email không hợp lệ");
            valid = false;
        }
        if (!ValidationUtils.isStrongPassword(password)) {
            layoutPassword.setError("Mật khẩu >=8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt");
            valid = false;
        }
        if (!ValidationUtils.passwordsMatch(password, confirm)) {
            layoutConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            valid = false;
        }

        if (!valid) return;

        // 🔹 Tạo user
        User user = new User();
        user.fullName = name;
        user.email = email;
        user.password = password;
        user.role = Enum.UserRole.PATIENT;

        new Thread(() -> {
            User userFound = userRepository.findByEmail(email);
            if(userFound == null) {
                long userId = userRepository.register(user);
                patientDao.insert(new Patient(userId));

                runOnUiThread(() -> {
                            Intent intent = new Intent();
                            intent.putExtra("registered_email", email);
                            setResult(RESULT_OK, intent);
                            Toasty.success(this, "Đăng ký thành công!", Toast.LENGTH_SHORT, true).show();
                            finish();
                        }
                );
            } else {
                runOnUiThread(() ->
                            Toasty.error(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT, true).show()
                );
            }

        }).start();
    }

    private void clearErrors() {
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
    }
}
