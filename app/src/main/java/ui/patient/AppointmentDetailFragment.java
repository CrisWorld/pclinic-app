package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import example.pclinic.com.R;

public class AppointmentDetailFragment extends Fragment {

    private static final String KEY_ID = "appointment_id";

    public static AppointmentDetailFragment newInstance(long id) {
        Bundle b = new Bundle();
        b.putLong(KEY_ID, id);
        AppointmentDetailFragment f = new AppointmentDetailFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.patient_fragment_appointment_detail, container, false);
    }
}
