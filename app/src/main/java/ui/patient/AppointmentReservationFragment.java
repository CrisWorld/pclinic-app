package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import example.pclinic.com.R;

public class AppointmentReservationFragment extends Fragment {

    public AppointmentReservationFragment() {
        // Constructor rỗng bắt buộc cho Fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_appointment_reservation, container, false);

        Spinner spinnerDoctor = view.findViewById(R.id.spinnerDoctor);
        EditText edtDate = view.findViewById(R.id.edtDate);
        EditText edtDescription = view.findViewById(R.id.edtDescription);
        Button btnBook = view.findViewById(R.id.btnBook);

        // Dữ liệu mẫu cho danh sách bác sĩ
        String[] doctors = {"BS. John Doe - Nội tổng quát", "BS. Jane Smith - Tai mũi họng", "BS. Lê Văn Tùng - Nhi khoa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, doctors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(adapter);

        btnBook.setOnClickListener(v -> {
            String doctor = spinnerDoctor.getSelectedItem().toString();
            String date = edtDate.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();

            if (date.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập ngày hẹn", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(requireContext(),
                    "Đã đặt lịch hẹn với " + doctor + " vào ngày " + date,
                    Toast.LENGTH_LONG).show();
        });

        return view;
    }
}
