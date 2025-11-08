package ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import data.db.admin.AdminDoctorDao;
import data.dto.PatientDetailInfo;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminPatientDetailActivity extends AppCompatActivity {

    // Thêm các biến cho UI mới
    private TextView tvPatientName, tvPatientCode, tvPatientEmail, tvPatientPhone, tvPatientGender;
    private TextView tvTotalSpending, tvTotalAppointments, tvLastCheckIn;
    private TextView tvLastExamDate, tvLastExamDiagnosis, tvLastExamTotal, tvNoLastExam, tvLastExamCode, tvLastExamHeightWeight, tvLastExamBloodPressure, tvLastExamPulse, tvLastExamMedicalHistory;
    private TextView tvNoReviews;
    private ImageButton btnBack;
    private RecyclerView recyclerPatientReviews;
    private PatientReviewAdapter reviewAdapter;
    private LinearLayout layoutLastExamDetails; // Biến cho layout group

    // Định dạng tiền tệ và ngày tháng
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Inject
    public AdminDoctorDao adminDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_patient_detail);

        long patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        // Lấy và hiển thị dữ liệu chi tiết
        adminDao.getPatientDetailInfoById(patientId).observe(this, this::updateUI);
        // Lấy và hiển thị danh sách reviews
        adminDao.getReviewsByPatientId(patientId).observe(this, reviews -> {
            reviewAdapter.updateData(reviews);
            tvNoReviews.setVisibility(reviews == null || reviews.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerPatientReviews.setVisibility(reviews != null && !reviews.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        // Thông tin cơ bản
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientCode = findViewById(R.id.tvPatientCode);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientPhone = findViewById(R.id.tvPatientPhone);
        tvPatientGender = findViewById(R.id.tvPatientGender);

        // Thống kê
        tvTotalSpending = findViewById(R.id.tvTotalSpending);
        tvTotalAppointments = findViewById(R.id.tvTotalAppointments);
        tvLastCheckIn = findViewById(R.id.tvLastCheckIn);

        // Lần khám gần nhất
        layoutLastExamDetails = findViewById(R.id.layoutLastExamDetails); // Ánh xạ layout group
        tvLastExamDate = findViewById(R.id.tvLastExamDate);
        tvLastExamCode = findViewById(R.id.tvLastExamCode);
        tvLastExamHeightWeight = findViewById(R.id.tvLastExamHeightWeight);
        tvLastExamBloodPressure = findViewById(R.id.tvLastExamBloodPressure);
        tvLastExamPulse = findViewById(R.id.tvLastExamPulse);
        tvLastExamMedicalHistory = findViewById(R.id.tvLastExamMedicalHistory);
        tvLastExamDiagnosis = findViewById(R.id.tvLastExamDiagnosis);
        tvLastExamTotal = findViewById(R.id.tvLastExamTotal);
        tvNoLastExam = findViewById(R.id.tvNoLastExam);

        // Reviews
        recyclerPatientReviews = findViewById(R.id.recyclerPatientReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
    }

    private void setupRecyclerView() {
        reviewAdapter = new PatientReviewAdapter();
        recyclerPatientReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerPatientReviews.setAdapter(reviewAdapter);
    }

    private void updateUI(PatientDetailInfo detail) {
        if (detail == null || detail.patientInfo == null) return;

        // Cập nhật thông tin cơ bản
        tvPatientName.setText(detail.patientInfo.fullName);
        tvPatientCode.setText("Mã bệnh nhân: " + detail.patientInfo.patientCode);
        tvPatientEmail.setText(detail.patientInfo.email);
        tvPatientPhone.setText("SĐT: " + (detail.patientInfo.phone != null ? detail.patientInfo.phone : "Chưa cập nhật"));
        tvPatientGender.setText("Giới tính: " + (detail.patientInfo.gender != null ? (detail.patientInfo.gender.equals("male") ? "Nam" : "Nữ") : "Chưa cập nhật"));

        // Cập nhật thẻ thống kê
        tvTotalSpending.setText("Tổng chi tiêu: " + currencyFormatter.format(detail.totalSpending));
        tvTotalAppointments.setText("Tổng số lịch hẹn: " + detail.totalAppointments);
        tvLastCheckIn.setText("Lần khám gần nhất: " + (detail.lastCheckInDate != null ? dateFormat.format(detail.lastCheckInDate) : "Chưa có"));

        // Cập nhật thẻ lần khám gần nhất
        if (detail.lastExamination != null && detail.lastExamination.examinationDate != null) {
            layoutLastExamDetails.setVisibility(View.VISIBLE);
            tvNoLastExam.setVisibility(View.GONE);

            tvLastExamDate.setText("Ngày khám: " + dateFormat.format(detail.lastExamination.examinationDate));
            tvLastExamCode.setText("Mã phiếu khám: " + detail.lastExamination.examinationCode);
            tvLastExamHeightWeight.setText(String.format(Locale.US, "Chiều cao / Cân nặng: %s cm / %s kg", detail.lastExamination.height, detail.lastExamination.weight));
            tvLastExamBloodPressure.setText("Huyết áp: " + detail.lastExamination.bloodPressure + " mmHg");
            tvLastExamPulse.setText("Mạch: " + detail.lastExamination.pulse + " bpm");
            tvLastExamMedicalHistory.setText("Tiền sử bệnh: " + detail.lastExamination.medicalHistory);
            tvLastExamDiagnosis.setText("Chẩn đoán: " + detail.lastExamination.diagnosis);
            tvLastExamTotal.setText("Chi phí: " + currencyFormatter.format(detail.lastExamination.grandTotal));

        } else {
            // Nếu không có dữ liệu khám, ẩn layout chi tiết và hiện thông báo
            layoutLastExamDetails.setVisibility(View.GONE);
            tvNoLastExam.setVisibility(View.VISIBLE);
        }
    }
}
