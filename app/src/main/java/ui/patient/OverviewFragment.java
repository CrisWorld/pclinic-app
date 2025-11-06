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

public class OverviewFragment extends Fragment {

    public OverviewFragment() {
        // Bắt buộc cần constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout cho fragment
        View view = inflater.inflate(R.layout.patient_fragment_overview, container, false);

        // Tìm các view trong layout
        TextView txtWelcome = view.findViewById(R.id.txtWelcome);
        TextView txtAppointments = view.findViewById(R.id.txtAppointments);
        TextView txtMessages = view.findViewById(R.id.txtMessages);

        // Gán dữ liệu mẫu
        txtWelcome.setText("Chào mừng bạn đến với Phòng khám!");
        txtAppointments.setText("Lịch hẹn sắp tới: 2");
        txtMessages.setText("Tin nhắn mới: 1");

        return view;
    }
}
