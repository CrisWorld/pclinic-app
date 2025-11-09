package ui.doctor;

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
import data.db.ExaminationFormDao;
import data.dto.AppointmentWithPatient;
import data.model.ExaminationForm;
import example.pclinic.com.R;

@AndroidEntryPoint
public class ViewExaminationFormFragment extends Fragment {

    @Inject
    ExaminationFormDao examinationFormDao;

    private long appointmentId;

    public static ViewExaminationFormFragment newInstance(AppointmentWithPatient appointment) {
        ViewExaminationFormFragment fragment = new ViewExaminationFormFragment();
        Bundle args = new Bundle();
        args.putLong("appointmentId", appointment.id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_view_examination_form, container, false);

        Bundle args = getArguments();
        if (args != null) {
            appointmentId = args.getLong("appointmentId");
        }

        loadExaminationForm(view);

        return view;
    }

    private void loadExaminationForm(View view) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);
            if (form != null) {
                requireActivity().runOnUiThread(() -> {
                    TextView tvInfo = view.findViewById(R.id.tvInfo);
                    if (tvInfo != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String info = "Xem phiếu khám\n\n" +
                                "Mã phiếu: " + form.examinationCode + "\n" +
                                "Ngày khám: " + (form.examinationDate != null ? dateFormat.format(form.examinationDate) : "N/A") + "\n" +
                                "Chuẩn đoán: " + (form.diagnosis != null ? form.diagnosis : "N/A");
                        tvInfo.setText(info);
                    }
                });
            } else {
                requireActivity().runOnUiThread(() -> {
                    TextView tvInfo = view.findViewById(R.id.tvInfo);
                    if (tvInfo != null) {
                        tvInfo.setText("Không tìm thấy phiếu khám");
                    }
                });
            }
        });
    }
}

