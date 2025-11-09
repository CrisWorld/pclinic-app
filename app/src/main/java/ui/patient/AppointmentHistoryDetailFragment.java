package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import example.pclinic.com.R;

public class AppointmentHistoryDetailFragment extends Fragment {

    private static final String ARG_APPOINTMENT_ID = "appointmentId";

    public static AppointmentHistoryDetailFragment newInstance(long appointmentId) {
        AppointmentHistoryDetailFragment fragment = new AppointmentHistoryDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_APPOINTMENT_ID, appointmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.patient_fragment_appointment_history_detail, container, false);
    }
}

