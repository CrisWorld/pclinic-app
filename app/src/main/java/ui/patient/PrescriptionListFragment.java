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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import data.db.ExaminationFormDao;
import data.db.PrescriptionDao;
import data.dto.PrescriptionDetailDto;
import data.model.ExaminationForm;
import example.pclinic.com.R;

@AndroidEntryPoint
public class PrescriptionListFragment extends Fragment {

    private static final String ARG_APPOINTMENT_ID = "appointmentId";

    @Inject ExaminationFormDao examinationFormDao;
    @Inject PrescriptionDao prescriptionDao;

    private RecyclerView recyclerView;
    private TextView tvTotalAmount;
    private TextView txtEmpty;
    private long appointmentId;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static PrescriptionListFragment newInstance(long appointmentId) {
        PrescriptionListFragment fragment = new PrescriptionListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_APPOINTMENT_ID, appointmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appointmentId = getArguments().getLong(ARG_APPOINTMENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_list_generic, container, false); // Dùng layout chung
        recyclerView = view.findViewById(R.id.recyclerViewGeneric);
        txtEmpty = view.findViewById(R.id.txtEmptyGeneric);
        TextView tvTitle = view.findViewById(R.id.tvTitleGeneric);
        // 👇 KHẮC PHỤC LỖI 3: Ánh xạ view cho tvTotalAmount
        tvTotalAmount = view.findViewById(R.id.tvTotalAmountGeneric);
        tvTitle.setText("Đơn thuốc chỉ định");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadPrescriptions();
        return view;
    }

    private void loadPrescriptions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);
            if (form == null) {
                showEmpty();
                return;
            }

            // 👇 KHẮC PHỤC LỖI 2: Lấy tổng tiền và lưu vào biến totalAmount
            double totalAmount = prescriptionDao.sumPriceByExaminationId(form.id);
            List<PrescriptionDetailDto> prescriptions = prescriptionDao.findByExaminationId(form.id);
            if (prescriptions == null || prescriptions.isEmpty()) {
                showEmpty();
            } else {
                requireActivity().runOnUiThread(() -> {
                    txtEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    PrescriptionAdapter adapter = new PrescriptionAdapter(prescriptions);
                    recyclerView.setAdapter(adapter);
                    // 👈 Hiển thị tổng tiền
                    tvTotalAmount.setText("Tổng tiền: " + currencyFormatter.format(totalAmount));
                    tvTotalAmount.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showEmpty() {
        requireActivity().runOnUiThread(() -> {
            txtEmpty.setText("Không có đơn thuốc nào cho lịch hẹn này.");
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvTotalAmount.setVisibility(View.GONE);
        });
    }
}
