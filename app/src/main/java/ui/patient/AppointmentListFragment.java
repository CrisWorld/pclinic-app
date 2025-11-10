package ui.patient;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.AppointmentDao;
import data.db.PatientDao;
import data.dto.AppointmentWithDoctor;
import data.model.Appointment;
import data.model.Patient;
import example.pclinic.com.R;
import util.AuthUtils;

@AndroidEntryPoint
public class AppointmentListFragment extends Fragment {

    @Inject AppointmentDao appointmentDao;
    @Inject PatientDao patientDao;

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private AppointmentAdapter adapter;
    private List<AppointmentWithDoctor> appointmentList = new ArrayList<>();

    public AppointmentListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_appointment_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        long userId = AuthUtils.getUserId(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            Patient patient = patientDao.findByUserId(userId);
            if (patient == null) return;

            List<AppointmentWithDoctor> list = appointmentDao.findUpcomingByPatient(patient.id, new Date());
            appointmentList.clear();
            appointmentList.addAll(list);

            requireActivity().runOnUiThread(() -> {
                if (appointmentList.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter = new AppointmentAdapter(appointmentList, this::openDetail);
                    recyclerView.setAdapter(adapter);
                }
            });
        });
    }

    private void openDetail(AppointmentWithDoctor appointment) {
        AppointmentDetailFragment fragment = AppointmentDetailFragment.newInstance(appointment.id);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
