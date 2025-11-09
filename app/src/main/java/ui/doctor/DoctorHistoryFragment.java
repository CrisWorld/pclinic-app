package ui.doctor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.enums.Enum;
import data.model.Appointment;
import data.model.Doctor;
import data.repository.AppointmentRepository;
import data.repository.DoctorRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import util.AuthUtils;

@AndroidEntryPoint
public class DoctorHistoryFragment extends Fragment {

    @Inject
    AppointmentRepository appointmentRepository;
    
    @Inject
    DoctorRepository doctorRepository;

    private RecyclerView rvAppointments;
    private LinearLayout layoutEmpty;
    private ChipGroup chipGroupStatus;
    private Chip chipAll, chipPending, chipConfirmed, chipDone, chipAbsent;
    private Button btnSelectDate;

    private AppointmentHistoryAdapter adapter;
    private List<Appointment> allAppointments = new ArrayList<>();
    private Enum.AppointmentStatus selectedStatus = null;
    private String selectedDate = null;

    private long doctorId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_history, container, false);

        initViews(view);
        setupRecyclerView();
        setupFilters();
        loadDoctorId();

        return view;
    }

    private void loadDoctorId() {
        long userId = AuthUtils.getUserId(requireContext());
        
        if (userId == -1) {
            Toasty.error(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Executors.newSingleThreadExecutor().execute(() -> {
            Doctor doctor = doctorRepository.findByUserIdSync(userId);
            
            requireActivity().runOnUiThread(() -> {
                if (doctor != null) {
                    doctorId = doctor.id;
                    loadAppointments();
                } else {
                    Toasty.error(requireContext(), "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initViews(View view) {
        rvAppointments = view.findViewById(R.id.rv_appointments);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        chipAll = view.findViewById(R.id.chip_all);
        chipPending = view.findViewById(R.id.chip_pending);
        chipConfirmed = view.findViewById(R.id.chip_confirmed);
        chipDone = view.findViewById(R.id.chip_done);
        chipAbsent = view.findViewById(R.id.chip_absent);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
    }

    private void setupRecyclerView() {
        adapter = new AppointmentHistoryAdapter();
        adapter.setOnAppointmentClickListener(appointment -> {
            AppointmentDetailBottomSheet bottomSheet = 
                AppointmentDetailBottomSheet.newInstance(appointment);
            bottomSheet.show(getChildFragmentManager(), "AppointmentDetail");
        });

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        // Set default date to today
        Calendar today = Calendar.getInstance();
        selectedDate = formatDate(today);
        updateDateButtonText(today);

        // Status filter
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                selectedStatus = null;
            } else if (checkedId == R.id.chip_pending) {
                selectedStatus = Enum.AppointmentStatus.PENDING;
            } else if (checkedId == R.id.chip_confirmed) {
                selectedStatus = Enum.AppointmentStatus.CONFIRMED;
            } else if (checkedId == R.id.chip_done) {
                selectedStatus = Enum.AppointmentStatus.DONE;
            } else if (checkedId == R.id.chip_absent) {
                selectedStatus = Enum.AppointmentStatus.ABSENT;
            }
            filterAppointments();
        });

        // Date picker
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedDate = formatDate(selected);
                    updateDateButtonText(selected);
                    loadAppointments();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void updateDateButtonText(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateText = sdf.format(calendar.getTime());
        
        // Check if today
        Calendar today = Calendar.getInstance();
        if (formatDate(calendar).equals(formatDate(today))) {
            btnSelectDate.setText("Hôm nay");
        } else {
            btnSelectDate.setText(dateText);
        }
    }

    private void loadAppointments() {
        if (selectedDate == null) return;

        appointmentRepository.getAppointmentsByDoctorAndDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), appointments -> {
                    if (appointments != null) {
                        allAppointments = appointments;
                        filterAppointments();
                    } else {
                        allAppointments = new ArrayList<>();
                        showEmptyState();
                    }
                });
    }

    private void filterAppointments() {
        List<Appointment> filtered = new ArrayList<>();

        for (Appointment appointment : allAppointments) {
            if (selectedStatus == null || appointment.status == selectedStatus) {
                filtered.add(appointment);
            }
        }

        if (filtered.isEmpty()) {
            showEmptyState();
        } else {
            adapter.setAppointments(filtered);
            rvAppointments.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        rvAppointments.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
