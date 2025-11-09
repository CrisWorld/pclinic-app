package ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.ChipGroup;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import data.db.admin.AdminDoctorDao;
import data.dto.DailyAppointmentStats;
import data.dto.DailyRevenueStats;
import data.dto.MonthlyAppointmentStats;
import data.dto.MonthlyRevenueStats;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminOverviewFragment extends Fragment {

    @Inject
    public AdminDoctorDao adminDao;

    // UI Components
    private TextView tvTotalPatients, tvTotalDoctors, tvTotalRevenue;
    private RecyclerView recyclerTopDoctors;
    private BarChart chartRevenue, chartAppointment;
    private ChipGroup chipGroupRevenue, chipGroupAppointment;

    private AdminDoctorRatingAdapter topDoctorsAdapter;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_overview, container, false);
        initViews(view);
        setupTopDoctorsRecycler();
        observeDashboardData();
        setupChipListeners();
        return view;
    }

    private void initViews(View view) {
        tvTotalPatients = view.findViewById(R.id.tvTotalPatients);
        tvTotalDoctors = view.findViewById(R.id.tvTotalDoctors);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        recyclerTopDoctors = view.findViewById(R.id.recyclerTopDoctors);
        chartRevenue = view.findViewById(R.id.chartRevenue);
        chartAppointment = view.findViewById(R.id.chartAppointment);
        chipGroupRevenue = view.findViewById(R.id.chipGroupRevenue);
        chipGroupAppointment = view.findViewById(R.id.chipGroupAppointment);
    }

    private void setupTopDoctorsRecycler() {
        topDoctorsAdapter = new AdminDoctorRatingAdapter();
        recyclerTopDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTopDoctors.setAdapter(topDoctorsAdapter);
    }

    private void observeDashboardData() {
        // Observe stats
        adminDao.getDashboardStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            tvTotalPatients.setText("Tổng số bệnh nhân: " + stats.totalPatients);
            tvTotalDoctors.setText("Tổng số bác sĩ: " + stats.totalDoctors);
            tvTotalRevenue.setText("Tổng doanh thu: " + currencyFormatter.format(stats.totalRevenue));
        });

        // Observe top doctors
        adminDao.getTopRatedDoctors().observe(getViewLifecycleOwner(), doctors -> {
            topDoctorsAdapter.setData(doctors);
        });

        // Load initial chart data for the year
        updateRevenueChart(true);
        updateAppointmentChart(true);
    }

    private void setupChipListeners() {
        chipGroupRevenue.setOnCheckedChangeListener((group, checkedId) -> {
            updateRevenueChart(checkedId == R.id.chipRevenueThisYear);
        });

        chipGroupAppointment.setOnCheckedChangeListener((group, checkedId) -> {
            updateAppointmentChart(checkedId == R.id.chipAppointmentThisYear);
        });
    }

    private void updateRevenueChart(boolean isYear) {
        Calendar cal = Calendar.getInstance();
        if (isYear) {
            String year = String.valueOf(cal.get(Calendar.YEAR));
            adminDao.getMonthlyRevenueStatsForYear(year).observe(getViewLifecycleOwner(), this::setupMonthlyRevenueChart);
        } else {
            String yearMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(cal.getTime());
            adminDao.getDailyRevenueStatsForMonth(yearMonth).observe(getViewLifecycleOwner(), this::setupDailyRevenueChart);
        }
    }
    private void updateAppointmentChart(boolean isYear) {
        Calendar cal = Calendar.getInstance();
        if (isYear) {
            String year = String.valueOf(cal.get(Calendar.YEAR));
            adminDao.getMonthlyAppointmentStatsForYear(year).observe(getViewLifecycleOwner(), this::setupMonthlyAppointmentChart);
        } else {
            String yearMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(cal.getTime());
            adminDao.getDailyAppointmentStatsForMonth(yearMonth).observe(getViewLifecycleOwner(), this::setupDailyAppointmentChart);
        }
    }


    // --- Chart Setup Methods ---

    private void setupMonthlyRevenueChart(List<MonthlyRevenueStats> stats) {
        if (stats == null) return;
        ArrayList<BarEntry> entries = new ArrayList<>();
        float[] monthlyData = new float[12];
        for (MonthlyRevenueStats stat : stats) {
            int monthIndex = Integer.parseInt(stat.month) - 1;
            if (monthIndex >= 0 && monthIndex < 12) {
                monthlyData[monthIndex] = (float) stat.totalRevenue;
            }
        }
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyData[i]));
        }
        String[] labels = new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
        setupBarChart(chartRevenue, entries, "Doanh thu (VND)", labels, ColorTemplate.MATERIAL_COLORS);
    }
    private void setupDailyRevenueChart(List<DailyRevenueStats> stats) {
        if (stats == null) return;
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        ArrayList<BarEntry> entries = new ArrayList<>();
        float[] dailyData = new float[daysInMonth];
        String[] labels = new String[daysInMonth];

        for (DailyRevenueStats stat : stats) {
            int dayIndex = Integer.parseInt(stat.day) - 1;
            if (dayIndex >= 0 && dayIndex < daysInMonth) {
                dailyData[dayIndex] = (float) stat.totalRevenue;
            }
        }
        for (int i = 0; i < daysInMonth; i++) {
            entries.add(new BarEntry(i, dailyData[i]));
            labels[i] = String.valueOf(i + 1);
        }
        setupBarChart(chartRevenue, entries, "Doanh thu (VND)", labels, ColorTemplate.MATERIAL_COLORS);
    }

    private void setupMonthlyAppointmentChart(List<MonthlyAppointmentStats> stats) {
        if (stats == null) return;
        ArrayList<BarEntry> entries = new ArrayList<>();
        float[] monthlyData = new float[12];
        for (MonthlyAppointmentStats stat : stats) {
            int monthIndex = Integer.parseInt(stat.month) - 1;
            if (monthIndex >= 0 && monthIndex < 12) {
                monthlyData[monthIndex] = stat.appointmentCount;
            }
        }
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyData[i]));
        }
        String[] labels = new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
        setupBarChart(chartAppointment, entries, "Số lượt khám", labels, ColorTemplate.PASTEL_COLORS);
    }

    private void setupDailyAppointmentChart(List<DailyAppointmentStats> stats) {
        if (stats == null) return;
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        ArrayList<BarEntry> entries = new ArrayList<>();
        float[] dailyData = new float[daysInMonth];
        String[] labels = new String[daysInMonth];

        for (DailyAppointmentStats stat : stats) {
            int dayIndex = Integer.parseInt(stat.day) - 1;
            if (dayIndex >= 0 && dayIndex < daysInMonth) {
                dailyData[dayIndex] = stat.appointmentCount;
            }
        }
        for (int i = 0; i < daysInMonth; i++) {
            entries.add(new BarEntry(i, dailyData[i]));
            labels[i] = String.valueOf(i + 1);
        }
        setupBarChart(chartAppointment, entries, "Số lượt khám", labels, ColorTemplate.PASTEL_COLORS);
    }

    private void setupBarChart(BarChart chart, ArrayList<BarEntry> entries, String label, String[] axisLabels, int[] colors) {
        if (entries.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        chart.getDescription().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(axisLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(axisLabels.length > 12 ? 10 : axisLabels.length);


        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
