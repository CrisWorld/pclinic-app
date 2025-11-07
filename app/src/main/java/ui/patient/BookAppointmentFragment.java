package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import example.pclinic.com.R;

public class BookAppointmentFragment extends Fragment {

    private static final String ARG_DOCTOR_ID = "doctor_id";

    private long doctorId;

    public BookAppointmentFragment() {
        // Bắt buộc constructor rỗng
    }

    public static BookAppointmentFragment newInstance(long doctorId) {
        BookAppointmentFragment fragment = new BookAppointmentFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCTOR_ID, doctorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doctorId = getArguments().getLong(ARG_DOCTOR_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_book_appointment, container, false);

        TextView txtDoctorInfo = view.findViewById(R.id.txtDoctorInfo);
        if (doctorId != -1) {
            txtDoctorInfo.setText("Bác sĩ ID: " + doctorId);
        } else {
            txtDoctorInfo.setText("Không có thông tin bác sĩ");
        }

        // Logic để trống, bạn sẽ cập nhật sau

        return view;
    }
}

