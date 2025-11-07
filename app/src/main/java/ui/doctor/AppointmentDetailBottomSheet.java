package ui.doctor;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

import data.enums.Enum;
import data.model.Appointment;
import data.model.User;
import data.repository.PatientRepository;
import example.pclinic.com.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppointmentDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_APPOINTMENT_ID = "appointment_id";

    @Inject
    PatientRepository patientRepository;

    private Appointment appointment;
    private TextView tvDetailId;
    private TextView tvDetailTime;
    private TextView tvDetailDate;
    private TextView tvDetailStatus;
    private TextView tvDetailPatientName;
    private TextView tvDetailPatientPhone;
    private TextView tvDetailPatientGender;
    private TextView tvDetailPatientAddress;
    private TextView tvDetailDescription;
    private TextView tvDetailCheckinTime;
    private LinearLayout layoutCheckinInfo;
    private Button btnClose;

    public static AppointmentDetailBottomSheet newInstance(Appointment appointment) {
        AppointmentDetailBottomSheet fragment = new AppointmentDetailBottomSheet();
        fragment.appointment = appointment;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_appointment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadAppointmentData();
        loadPatientData();

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void initViews(View view) {
        tvDetailId = view.findViewById(R.id.tv_detail_id);
        tvDetailTime = view.findViewById(R.id.tv_detail_time);
        tvDetailDate = view.findViewById(R.id.tv_detail_date);
        tvDetailStatus = view.findViewById(R.id.tv_detail_status);
        tvDetailPatientName = view.findViewById(R.id.tv_detail_patient_name);
        tvDetailPatientPhone = view.findViewById(R.id.tv_detail_patient_phone);
        tvDetailPatientGender = view.findViewById(R.id.tv_detail_patient_gender);
        tvDetailPatientAddress = view.findViewById(R.id.tv_detail_patient_address);
        tvDetailDescription = view.findViewById(R.id.tv_detail_description);
        tvDetailCheckinTime = view.findViewById(R.id.tv_detail_checkin_time);
        layoutCheckinInfo = view.findViewById(R.id.layout_checkin_info);
        btnClose = view.findViewById(R.id.btn_close);
    }

    private void loadAppointmentData() {
        if (appointment == null) return;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

        // ID
        tvDetailId.setText("#" + appointment.id);

        // Time
        String startTime = timeFormat.format(appointment.startDate);
        String endTime = timeFormat.format(appointment.endDate);
        tvDetailTime.setText(startTime + " - " + endTime);

        // Date
        tvDetailDate.setText(dateFormat.format(appointment.startDate));

        // Status
        String statusText = "";
        int statusColor = 0;

        if (appointment.status == Enum.AppointmentStatus.CONFIRMED) {
            statusText = "Đã xác nhận";
            statusColor = requireContext().getColor(R.color.colorPrimary);
        } else if (appointment.status == Enum.AppointmentStatus.PENDING) {
            statusText = "Chờ xác nhận";
            statusColor = requireContext().getColor(R.color.colorAccent);
        } else if (appointment.status == Enum.AppointmentStatus.DONE) {
            statusText = "Hoàn thành";
            statusColor = requireContext().getColor(R.color.success);
        } else if (appointment.status == Enum.AppointmentStatus.ABSENT) {
            statusText = "Vắng mặt";
            statusColor = requireContext().getColor(R.color.error);
        }

        tvDetailStatus.setText(statusText);
        tvDetailStatus.setBackgroundColor(statusColor);

        // Description
        if (appointment.description != null && !appointment.description.isEmpty()) {
            tvDetailDescription.setText(appointment.description);
        } else {
            tvDetailDescription.setText("Không có mô tả");
        }

        // Check-in info
        if (appointment.checkInDate != null) {
            tvDetailCheckinTime.setText(dateTimeFormat.format(appointment.checkInDate));
            layoutCheckinInfo.setVisibility(View.VISIBLE);
        } else {
            layoutCheckinInfo.setVisibility(View.GONE);
        }
    }

    private void loadPatientData() {
        if (appointment == null) return;

        patientRepository.getUserByPatientId(appointment.patientId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvDetailPatientName.setText(user.fullName);
                tvDetailPatientPhone.setText(user.phone != null ? user.phone : "Chưa cập nhật");
                
                // Gender
                String genderText = "Không xác định";
                if (user.gender != null) {
                    if (user.gender.equalsIgnoreCase("MALE") || user.gender.equalsIgnoreCase("Nam")) {
                        genderText = "Nam";
                    } else if (user.gender.equalsIgnoreCase("FEMALE") || user.gender.equalsIgnoreCase("Nữ")) {
                        genderText = "Nữ";
                    } else {
                        genderText = user.gender;
                    }
                }
                tvDetailPatientGender.setText(genderText);
                
                // Address
                if (user.address != null && !user.address.isEmpty()) {
                    tvDetailPatientAddress.setText(user.address);
                } else {
                    tvDetailPatientAddress.setText("Chưa cập nhật");
                }
            } else {
                tvDetailPatientName.setText("Bệnh nhân #" + appointment.patientId);
                tvDetailPatientPhone.setText("Không có thông tin");
                tvDetailPatientGender.setText("Không xác định");
                tvDetailPatientAddress.setText("Chưa cập nhật");
            }
        });
    }
}
