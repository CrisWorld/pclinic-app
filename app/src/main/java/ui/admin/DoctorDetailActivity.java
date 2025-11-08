package ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import data.dto.DoctorInfo;
import data.dto.MonthlyAppointmentStats;
import data.dto.MonthlyRevenueStats;
import example.pclinic.com.R;
import vm.admin.DoctorDetailViewModel;

@AndroidEntryPoint
public class DoctorDetailActivity extends AppCompatActivity {

    private DoctorDetailViewModel viewModel;
    private TextView tvDoctorName, tvDoctorEmail, tvDoctorPhone, tvDoctorSpecialties, tvDoctorGender, tvDoctorBio;
    private BarChart revenueChart, appointmentChart;
    private ImageButton btnBack;
    private long doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_doctor_detail);

        doctorId = getIntent().getLongExtra("doctor_id", -1);
        if (doctorId == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(DoctorDetailViewModel.class);

        initViews();
        observeDoctorInfo();
        observeChartData();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorEmail = findViewById(R.id.tvDoctorEmail);
        tvDoctorPhone = findViewById(R.id.tvDoctorPhone);
        tvDoctorSpecialties = findViewById(R.id.tvDoctorSpecialties);
        tvDoctorGender = findViewById(R.id.tvDoctorGender);
        tvDoctorBio = findViewById(R.id.tvDoctorBio);
        revenueChart = findViewById(R.id.revenueChart);
        appointmentChart = findViewById(R.id.appointmentChart);
    }

    private void observeDoctorInfo() {
        viewModel.getDoctorInfo(doctorId).observe(this, this::updateDoctorInfoUI);
    }

    private void observeChartData() {
        viewModel.getRevenueStats(doctorId).observe(this, this::setupRevenueChart);
        viewModel.getAppointmentStats(doctorId).observe(this, this::setupAppointmentChart);
    }

    private void updateDoctorInfoUI(DoctorInfo doctor) {
        if (doctor == null) return;
        tvDoctorName.setText(doctor.fullName);
        tvDoctorEmail.setText(doctor.email);
        tvDoctorPhone.setText("SĐT: " + (doctor.phone != null ? doctor.phone : "N/A"));
        if (doctor.specialties != null && !doctor.specialties.isEmpty()) {
            tvDoctorSpecialties.setText("Chuyên khoa: " + TextUtils.join(", ", doctor.specialties));
        } else {
            tvDoctorSpecialties.setText("Chuyên khoa: N/A");
        }
        tvDoctorGender.setText("Giới tính: " + (doctor.gender != null ? (doctor.gender.equals("male") ? "Nam" : "Nữ") : "N/A"));
        tvDoctorBio.setText(doctor.bio != null ? doctor.bio : "");
    }

    private void setupRevenueChart(List<MonthlyRevenueStats> stats) {
        if (stats == null || stats.isEmpty()) {
            revenueChart.clear();
            revenueChart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] months = new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
        float[] monthlyData = new float[12];

        for (MonthlyRevenueStats stat : stats) {
            int monthIndex = Integer.parseInt(stat.month) - 1;
            if(monthIndex >= 0 && monthIndex < 12) {
                monthlyData[monthIndex] = (float) stat.totalRevenue;
            }
        }

        for(int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyData[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VND)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        setupBarChart(revenueChart, new BarData(dataSet), months);
    }

    private void setupAppointmentChart(List<MonthlyAppointmentStats> stats) {
        if (stats == null || stats.isEmpty()) {
            appointmentChart.clear();
            appointmentChart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] months = new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
        float[] monthlyData = new float[12];

        for (MonthlyAppointmentStats stat : stats) {
            int monthIndex = Integer.parseInt(stat.month) - 1;
            if(monthIndex >= 0 && monthIndex < 12) {
                monthlyData[monthIndex] = stat.appointmentCount;
            }
        }

        for(int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyData[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Số lượt khám");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextSize(10f);

        setupBarChart(appointmentChart, new BarData(dataSet), months);
    }

    private void setupBarChart(BarChart chart, BarData data, String[] labels) {
        chart.getDescription().setEnabled(false);
        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
