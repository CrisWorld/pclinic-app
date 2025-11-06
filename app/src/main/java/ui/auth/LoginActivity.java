package ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.model.User;
import data.repository.UserRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import ui.admin.AdminActivity;
import ui.doctor.DoctorActivity;
import ui.patient.PatientActivity;
import util.AuthUtils;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    UserRepository userRepository;
    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    // Định nghĩa launcher
    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String registeredEmail = result.getData().getStringExtra("registered_email");
                    if (registeredEmail != null) {
                        etEmail.setText(registeredEmail); // tự động điền email đã đăng ký
                        Toasty.success(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_login_activity);

        // Kiểm tra nếu user đã đăng nhập và chọn Remember Me
        long savedUserId = AuthUtils.getUserId(getApplicationContext());
        boolean rememberMe = AuthUtils.getRememberMe(getApplicationContext());
        if (savedUserId != -1 && rememberMe) {
            userRepository.findById(savedUserId).observe(this,user -> {
                if(user == null) return;
                handleLogin(user);
            });
            return;
        }

        // 🔹 Ánh xạ view
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        // 🔹 Sự kiện login
        btnLogin.setOnClickListener(v -> {
            if (!validateInputs()) return;

            CheckBox chkRememberMe = findViewById(R.id.chkRememberMe);

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            this.userRepository.login(email, password).observe(this, user -> {
                if (user != null) {
                    // Lưu trạng thái remember me
                    AuthUtils.saveRememberMe(getApplicationContext(), chkRememberMe.isChecked());
                    AuthUtils.saveUserId(getApplicationContext(), user.id);
                    AuthUtils.saveRole(getApplicationContext(), user.role);
                    handleLogin(user);
                } else {
                    Toasty.error(this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT, true).show();
                }
            });
        });

        // 🔹 Sự kiện chuyển sang RegisterActivity
        tvGoRegister.setOnClickListener(v -> {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    registerLauncher.launch(intent);
        });

    }

    public void handleLogin(User user){
        Toasty.info(this, "Xin chào " + user.fullName + "!", Toast.LENGTH_SHORT, true).show();

        switch (user.role) {
            case ADMIN:
                // Chuyển sang màn hình quản trị
                startActivity(new Intent(this, AdminActivity.class));
                break;

            case DOCTOR:
                // Chuyển sang màn hình bác sĩ
                startActivity(new Intent(this, DoctorActivity.class));
                break;

            case PATIENT:
                // Chuyển sang màn hình bệnh nhân
                startActivity(new Intent(this, PatientActivity.class));
                break;

            default:
                Toasty.error(this, "Role không hợp lệ!", Toast.LENGTH_SHORT).show();
                break;
        }
        finish();
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isValid = true;

        layoutEmail.setError(null);
        layoutPassword.setError(null);

        if (email.isEmpty()) {
            layoutEmail.setError("Vui lòng nhập email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Email không hợp lệ");
            isValid = false;
        }

        if (password.isEmpty()) {
            layoutPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 6) {
            layoutPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }

        return isValid;
    }
}
