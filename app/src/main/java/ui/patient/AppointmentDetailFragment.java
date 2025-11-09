package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.AppointmentDao;
import data.dto.AppointmentWithDoctor;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AppointmentDetailFragment extends Fragment {

    private static final String KEY_ID = "appointment_id";

    @Inject
    AppointmentDao appointmentDao;

    private TextView tvDoctor, tvDate, tvEndDate, tvStatus, tvCheckIn, tvDescription;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

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
        View v = inflater.inflate(R.layout.patient_fragment_appointment_detail, container, false);

        tvDoctor = v.findViewById(R.id.tvDoctor);
        tvDate = v.findViewById(R.id.tvDate);
        tvEndDate = v.findViewById(R.id.tvEndDate);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvCheckIn = v.findViewById(R.id.tvCheckIn);
        tvDescription = v.findViewById(R.id.tvDescription);

        long id = requireArguments().getLong(KEY_ID);
        loadDetail(id);

        return v;
    }

    private void loadDetail(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppointmentWithDoctor ap = appointmentDao.getDetail(id);

            requireActivity().runOnUiThread(() -> {
                if (ap == null) return;

                tvDoctor.setText("👨‍⚕️ Bác sĩ: " + ap.fullName);
                tvDate.setText("📅 Bắt đầu: " + sdf.format(ap.startDate));
                tvEndDate.setText("⏱ Kết thúc: " + sdf.format(ap.endDate));
                tvStatus.setText("✅ Trạng thái: " + ap.status.name());

                if (ap.checkInDate != null)
                    tvCheckIn.setText("⏳ Check-in: " + sdf.format(ap.checkInDate));
                else
                    tvCheckIn.setText("⏳ Check-in: Chưa check-in");

                tvDescription.setText("📝 Ghi chú: " + (ap.description == null ? "Không có" : ap.description));
            });
        });
    }
}

