package ui.doctor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.enums.Enum;
import data.model.Appointment;
import data.model.Doctor;
import data.model.User;
import data.repository.AppointmentRepository;
import data.repository.DoctorRepository;
import data.repository.PatientRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import util.AuthUtils;

@AndroidEntryPoint
public class DoctorAppointmentFragment extends Fragment {

    @Inject
    AppointmentRepository appointmentRepository;
    
    @Inject
    PatientRepository patientRepository;
    
    @Inject
    DoctorRepository doctorRepository;

    private TextView tvCurrentDate, tvTotalCount, tvConfirmedCount, tvDoneCount;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvAppointments;
    private LinearLayout layoutEmpty;
    private TodayAppointmentAdapter adapter;

    private List<Appointment> allAppointments = new ArrayList<>();
    private List<User> allPatients = new ArrayList<>();
    private long doctorId = -1;
    private Enum.AppointmentStatus selectedStatus = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_appointment, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupFilters();
        loadDoctorId(); // This will call loadTodayAppointments() after getting doctorId
        
        return view;
    }

    private void initViews(View view) {
        tvCurrentDate = view.findViewById(R.id.tv_current_date);
        tvTotalCount = view.findViewById(R.id.tv_total_count);
        tvConfirmedCount = view.findViewById(R.id.tv_confirmed_count);
        tvDoneCount = view.findViewById(R.id.tv_done_count);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        rvAppointments = view.findViewById(R.id.rv_appointments);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        tvCurrentDate.setText(dateFormat.format(new Date()));
    }

    private void loadDoctorId() {
        long userId = AuthUtils.getUserId(requireContext());
        
        if (userId == -1) {
            Toasty.error(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Load doctor info from userId in background
        Executors.newSingleThreadExecutor().execute(() -> {
            Doctor doctor = doctorRepository.findByUserIdSync(userId);
            
            requireActivity().runOnUiThread(() -> {
                if (doctor != null) {
                    doctorId = doctor.id;
                    loadTodayAppointments();
                } else {
                    Toasty.error(requireContext(), "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupRecyclerView() {
        adapter = new TodayAppointmentAdapter(new TodayAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onViewDetail(Appointment appointment, User patient) {
                showAppointmentDetail(appointment, patient);
            }

            @Override
            public void onConfirm(Appointment appointment) {
                confirmAppointment(appointment);
            }

            @Override
            public void onComplete(Appointment appointment) {
                completeAppointment(appointment);
            }
        });

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedStatus = null;
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_all) {
                    selectedStatus = null;
                } else if (checkedId == R.id.chip_pending) {
                    selectedStatus = Enum.AppointmentStatus.PENDING;
                } else if (checkedId == R.id.chip_confirmed) {
                    selectedStatus = Enum.AppointmentStatus.CONFIRMED;
                } else if (checkedId == R.id.chip_done) {
                    selectedStatus = Enum.AppointmentStatus.DONE;
                }
            }
            filterAppointments();
        });
    }

    private void loadTodayAppointments() {
        if (doctorId == -1) {
            Toasty.error(requireContext(), "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Appointment> appointments = appointmentRepository.getAppointmentsByDoctorAndDateSync(doctorId, today);
            
            // Load patient info for each appointment
            List<User> patients = new ArrayList<>();
            if (appointments != null) {
                for (Appointment appointment : appointments) {
                    User patient = patientRepository.getUserByPatientIdSync(appointment.patientId);
                    patients.add(patient != null ? patient : new User());
                }
            }

            requireActivity().runOnUiThread(() -> {
                allAppointments = appointments != null ? appointments : new ArrayList<>();
                allPatients = patients;
                updateStats();
                filterAppointments();
            });
        });
    }

    private void filterAppointments() {
        List<Appointment> filtered = new ArrayList<>();
        List<User> filteredPatients = new ArrayList<>();

        for (int i = 0; i < allAppointments.size(); i++) {
            Appointment appointment = allAppointments.get(i);
            if (selectedStatus == null || appointment.status == selectedStatus) {
                filtered.add(appointment);
                filteredPatients.add(allPatients.get(i));
            }
        }

        adapter.setData(filtered, filteredPatients);
        
        // Show/hide empty state
        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        int total = allAppointments.size();
        int confirmed = 0;
        int done = 0;

        for (Appointment appointment : allAppointments) {
            if (appointment.status == Enum.AppointmentStatus.CONFIRMED) {
                confirmed++;
            } else if (appointment.status == Enum.AppointmentStatus.DONE) {
                done++;
            }
        }

        tvTotalCount.setText(String.valueOf(total));
        tvConfirmedCount.setText(String.valueOf(confirmed));
        tvDoneCount.setText(String.valueOf(done));
    }

    private void showAppointmentDetail(Appointment appointment, User patient) {
        AppointmentDetailBottomSheet bottomSheet = AppointmentDetailBottomSheet.newInstance(appointment);
        bottomSheet.show(getParentFragmentManager(), "appointment_detail");
    }

    private void confirmAppointment(Appointment appointment) {
        appointment.status = Enum.AppointmentStatus.CONFIRMED;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            appointmentRepository.updateSync(appointment);
            
            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Đã xác nhận lịch hẹn", Toast.LENGTH_SHORT).show();
                loadTodayAppointments();
            });
        });
    }

    private void completeAppointment(Appointment appointment) {
        appointment.status = Enum.AppointmentStatus.DONE;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            appointmentRepository.updateSync(appointment);
            
            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Đã hoàn thành lịch hẹn", Toast.LENGTH_SHORT).show();
                loadTodayAppointments();
            });
        });
    }
}
