package ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import data.db.AppointmentDao;
import data.db.DoctorDao;
import data.dto.AppointmentWithPatient;
import data.enums.Enum;
import example.pclinic.com.R;
import util.AuthUtils;

@AndroidEntryPoint
public class DoctorAppointmentFragment extends Fragment {

    @Inject
    AppointmentDao appointmentDao;

    @Inject
    DoctorDao doctorDao;

    private TextView txtTitle;
    private TextView txtDate;
    private ChipGroup chipGroupFilter;
    private Chip chipWaiting;
    private Chip chipCompleted;
    private Chip chipAbsent;
    private RecyclerView rvAppointments;
    private TextView txtEmpty;

    private TodayAppointmentAdapter adapter;

    private long doctorId;
    private List<AppointmentWithPatient> allAppointments = new ArrayList<>();
    private boolean showWaiting = true;
    private boolean showCompleted = true;
    private boolean showAbsent = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_appointment, container, false);

        initViews(view);
        setupRecyclerViews();
        setupFilters();
        loadDoctorId(); // This will call loadAppointments() after doctorId is loaded

        return view;
    }

    private void initViews(View view) {
        txtTitle = view.findViewById(R.id.txtTitle);
        txtDate = view.findViewById(R.id.txtDate);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipWaiting = view.findViewById(R.id.chipWaiting);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipAbsent = view.findViewById(R.id.chipAbsent);
        rvAppointments = view.findViewById(R.id.rvAppointments);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        // Set today's date
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtDate.setText("Ngày " + dateFormat.format(today.getTime()));
    }

    private void loadDoctorId() {
        long userId = AuthUtils.getUserId(requireContext());
        if (userId == -1) {
            // User not logged in
            return;
        }
        
        Executors.newSingleThreadExecutor().execute(() -> {
            data.model.Doctor doctor = doctorDao.findByUserId((int) userId);
            if (doctor != null) {
                doctorId = doctor.id;
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            loadAppointments();
                        }
                    });
                }
            }
        });
    }

    private void setupRecyclerViews() {
        adapter = new TodayAppointmentAdapter(new TodayAppointmentAdapter.OnActionClickListener() {
            @Override
            public void onCreateExaminationForm(AppointmentWithPatient appointment) {
                if (!isAdded() || getActivity() == null) return;
                // Navigate to create examination form
                CreateExaminationFormFragment fragment = CreateExaminationFormFragment.newInstance(appointment);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onViewExaminationForm(AppointmentWithPatient appointment) {
                if (!isAdded() || getActivity() == null) return;
                // Navigate to view examination form
                ViewExaminationFormFragment fragment = ViewExaminationFormFragment.newInstance(appointment);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        chipWaiting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showWaiting = isChecked;
            filterAndDisplayAppointments();
        });

        chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showCompleted = isChecked;
            filterAndDisplayAppointments();
        });

        chipAbsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showAbsent = isChecked;
            filterAndDisplayAppointments();
        });
    }

    private void loadAppointments() {
        if (doctorId == 0) return;

        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(today.getTime());

        Executors.newSingleThreadExecutor().execute(() -> {
            List<AppointmentWithPatient> appointments = appointmentDao.findByDoctorAndDateWithPatient(doctorId, dateStr);
            allAppointments.clear();
            if (appointments != null) {
                allAppointments.addAll(appointments);
            }

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        filterAndDisplayAppointments();
                    }
                });
            }
        });
    }

    private void filterAndDisplayAppointments() {
        List<AppointmentWithPatient> filteredList = new ArrayList<>();

        for (AppointmentWithPatient appointment : allAppointments) {
            Enum.AppointmentStatus status = appointment.status;

            if (status == Enum.AppointmentStatus.DONE) {
                if (showCompleted) {
                    filteredList.add(appointment);
                }
            } else if (status == Enum.AppointmentStatus.PENDING || status == Enum.AppointmentStatus.CONFIRMED) {
                if (showWaiting) {
                    filteredList.add(appointment);
                }
            } else if (status == Enum.AppointmentStatus.ABSENT) {
                if (showAbsent) {
                    filteredList.add(appointment);
                }
            }
        }

        // Update list
        if (filteredList.isEmpty()) {
            rvAppointments.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            rvAppointments.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);
            adapter.setAppointments(filteredList);
        }
    }
}
