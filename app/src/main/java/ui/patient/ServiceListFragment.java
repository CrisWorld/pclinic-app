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
import data.db.ServiceDao;
import data.dto.ServiceDetailDto;
import data.model.ExaminationForm;
import example.pclinic.com.R;

@AndroidEntryPoint
public class ServiceListFragment extends Fragment {

    private static final String ARG_APPOINTMENT_ID = "appointmentId";

    @Inject
    ExaminationFormDao examinationFormDao;
    @Inject
    ServiceDao serviceDao;

    private RecyclerView recyclerView;
    private TextView tvTotalAmount;
    private TextView txtEmpty;
    private long appointmentId;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static ServiceListFragment newInstance(long appointmentId) {
        ServiceListFragment fragment = new ServiceListFragment();
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
        // Tái sử dụng layout chung
        View view = inflater.inflate(R.layout.patient_fragment_list_generic, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewGeneric);
        txtEmpty = view.findViewById(R.id.txtEmptyGeneric);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmountGeneric);
        TextView tvTitle = view.findViewById(R.id.tvTitleGeneric);

        tvTitle.setText("Dịch vụ chỉ định");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadServices();
        return view;
    }

    private void loadServices() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);
            if (form == null) {
                showEmpty();
                return;
            }

            double totalAmount = serviceDao.sumPriceByExaminationId(form.id);
            List<ServiceDetailDto> services = serviceDao.findByExaminationId(form.id);

            if (services == null || services.isEmpty()) {
                showEmpty();
            } else {
                requireActivity().runOnUiThread(() -> {
                    txtEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    ServiceAdapter adapter = new ServiceAdapter(services);
                    recyclerView.setAdapter(adapter);

                    // Hiển thị tổng tiền
                    tvTotalAmount.setText("Tổng tiền: " + currencyFormatter.format(totalAmount));
                    tvTotalAmount.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showEmpty() {
        requireActivity().runOnUiThread(() -> {
            txtEmpty.setText("Không có dịch vụ nào cho lịch hẹn này.");
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (tvTotalAmount != null) {
                tvTotalAmount.setVisibility(View.GONE);
            }
        });
    }
}
