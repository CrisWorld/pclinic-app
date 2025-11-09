package ui.doctor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.AppointmentDao;
import data.db.ExaminationFormDao;
import data.db.PrescriptionDao;
import data.db.PrescriptionExaminationFormDao;
import data.model.Appointment;
import data.model.ExaminationForm;
import data.model.Prescription;
import data.model.PrescriptionExaminationForm;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AddPrescriptionFragment extends Fragment {

    @Inject
    PrescriptionDao prescriptionDao;

    @Inject
    PrescriptionExaminationFormDao prescriptionExaminationFormDao;

    @Inject
    AppointmentDao appointmentDao;

    @Inject
    ExaminationFormDao examinationFormDao;

    private long appointmentId;
    private long examinationFormId;
    private long patientId;
    private long doctorId;

    private TextInputEditText etSearch;
    private RecyclerView rvPrescriptions;
    private RecyclerView rvSelectedPrescriptions;
    private MaterialCardView cardSelectedPrescriptions;
    private TextView tvTotalAmount;
    private TextView tvEmpty;
    private MaterialButton btnSave;

    private PrescriptionSelectionAdapter adapter;
    private SelectedPrescriptionAdapter selectedAdapter;
    private List<Prescription> allPrescriptions = new ArrayList<>();
    private List<Prescription> filteredPrescriptions = new ArrayList<>();
    private List<PrescriptionExaminationForm> selectedPrescriptions = new ArrayList<>();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static AddPrescriptionFragment newInstance(long appointmentId, long examinationFormId) {
        AddPrescriptionFragment fragment = new AddPrescriptionFragment();
        Bundle args = new Bundle();
        args.putLong("appointmentId", appointmentId);
        args.putLong("examinationFormId", examinationFormId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_add_prescription, container, false);

        Bundle args = getArguments();
        if (args != null) {
            appointmentId = args.getLong("appointmentId");
            examinationFormId = args.getLong("examinationFormId");
        }

        initViews(view);
        setupRecyclerViews();
        loadAppointmentInfo();
        loadPrescriptions();
        loadExistingPrescriptions();
        setupSearch();
        setupSaveButton();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        rvPrescriptions = view.findViewById(R.id.rvPrescriptions);
        rvSelectedPrescriptions = view.findViewById(R.id.rvSelectedPrescriptions);
        cardSelectedPrescriptions = view.findViewById(R.id.cardSelectedPrescriptions);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupRecyclerViews() {
        // Available prescriptions
        rvPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PrescriptionSelectionAdapter(filteredPrescriptions, prescription -> {
            addPrescription(prescription);
        });
        rvPrescriptions.setAdapter(adapter);

        // Selected prescriptions
        rvSelectedPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectedAdapter = new SelectedPrescriptionAdapter(selectedPrescriptions, prescriptionDao, prescriptionExaminationForm -> {
            removePrescription(prescriptionExaminationForm);
        });
        rvSelectedPrescriptions.setAdapter(selectedAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPrescriptions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> savePrescriptions());
    }

    private void loadAppointmentInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Appointment appointment = appointmentDao.findById(appointmentId);
            if (appointment != null) {
                patientId = appointment.patientId;
                doctorId = appointment.doctorId;
            } else {
                // Try to get from examination form
                ExaminationForm form = examinationFormDao.findByAppointmentId(appointmentId);
                if (form != null) {
                    patientId = form.patientId;
                    doctorId = form.doctorId;
                }
            }
        });
    }

    private void loadPrescriptions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            allPrescriptions = prescriptionDao.getAll();
            filteredPrescriptions = new ArrayList<>(allPrescriptions);
            requireActivity().runOnUiThread(() -> {
                adapter.updateData(filteredPrescriptions);
                updateEmptyState();
            });
        });
    }

    private void loadExistingPrescriptions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<PrescriptionExaminationForm> existing = prescriptionExaminationFormDao.findByExaminationId(examinationFormId);
            requireActivity().runOnUiThread(() -> {
                selectedPrescriptions.clear();
                selectedPrescriptions.addAll(existing);
                selectedAdapter.notifyDataSetChanged();
                updateSelectedPrescriptionsUI();
                updateTotal();
            });
        });
    }

    private void filterPrescriptions(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredPrescriptions = new ArrayList<>(allPrescriptions);
        } else {
            filteredPrescriptions = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Prescription prescription : allPrescriptions) {
                if (prescription.name != null && prescription.name.toLowerCase().contains(lowerQuery) ||
                    prescription.code != null && prescription.code.toLowerCase().contains(lowerQuery)) {
                    filteredPrescriptions.add(prescription);
                }
            }
        }
        adapter.updateData(filteredPrescriptions);
        updateEmptyState();
    }

    private void addPrescription(Prescription prescription) {
        // Check if already added
        for (PrescriptionExaminationForm existing : selectedPrescriptions) {
            if (existing.prescriptionId == prescription.id) {
                Toasty.info(requireContext(), "Đơn thuốc đã được thêm", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        PrescriptionExaminationForm prescriptionForm = new PrescriptionExaminationForm();
        prescriptionForm.prescriptionId = prescription.id;
        prescriptionForm.appointmentId = appointmentId;
        prescriptionForm.examinationId = examinationFormId;
        prescriptionForm.patientId = patientId;
        prescriptionForm.doctorId = doctorId;
        prescriptionForm.price = prescription.price;

        selectedPrescriptions.add(prescriptionForm);
        selectedAdapter.notifyDataSetChanged();
        updateSelectedPrescriptionsUI();
        updateTotal();
        Toasty.success(requireContext(), "Đã thêm đơn thuốc", Toast.LENGTH_SHORT).show();
    }

    private void removePrescription(PrescriptionExaminationForm prescriptionForm) {
        selectedPrescriptions.remove(prescriptionForm);
        selectedAdapter.notifyDataSetChanged();
        updateSelectedPrescriptionsUI();
        updateTotal();
        Toasty.info(requireContext(), "Đã xóa đơn thuốc", Toast.LENGTH_SHORT).show();
    }

    private void updateSelectedPrescriptionsUI() {
        if (selectedPrescriptions.isEmpty()) {
            cardSelectedPrescriptions.setVisibility(View.GONE);
        } else {
            cardSelectedPrescriptions.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotal() {
        double total = 0;
        for (PrescriptionExaminationForm prescriptionForm : selectedPrescriptions) {
            total += prescriptionForm.price;
        }
        tvTotalAmount.setText(currencyFormatter.format(total));
    }

    private void updateEmptyState() {
        if (filteredPrescriptions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvPrescriptions.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvPrescriptions.setVisibility(View.VISIBLE);
        }
    }

    private void savePrescriptions() {
        if (selectedPrescriptions.isEmpty()) {
            Toasty.warning(requireContext(), "Vui lòng chọn ít nhất một đơn thuốc", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Delete existing prescriptions for this examination
            prescriptionExaminationFormDao.deleteByExaminationId(examinationFormId);
            // Insert new prescriptions
            prescriptionExaminationFormDao.insertAll(selectedPrescriptions);

            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Lưu đơn thuốc thành công", Toast.LENGTH_SHORT).show();
                // Go back
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        });
    }

    // Adapter for available prescriptions
    private static class PrescriptionSelectionAdapter extends RecyclerView.Adapter<PrescriptionSelectionAdapter.ViewHolder> {
        private List<Prescription> prescriptions;
        private final OnPrescriptionClickListener listener;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        interface OnPrescriptionClickListener {
            void onAdd(Prescription prescription);
        }

        PrescriptionSelectionAdapter(List<Prescription> prescriptions, OnPrescriptionClickListener listener) {
            this.prescriptions = prescriptions;
            this.listener = listener;
        }

        void updateData(List<Prescription> newList) {
            this.prescriptions = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_prescription_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Prescription prescription = prescriptions.get(position);
            holder.tvPrescriptionName.setText(prescription.name);
            holder.tvPrescriptionCode.setText("Mã: " + prescription.code);
            holder.tvPrescriptionPrice.setText(currencyFormatter.format(prescription.price));
            holder.btnAdd.setOnClickListener(v -> listener.onAdd(prescription));
            holder.btnRemove.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return prescriptions != null ? prescriptions.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPrescriptionName, tvPrescriptionCode, tvPrescriptionPrice;
            MaterialButton btnAdd, btnRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPrescriptionName = itemView.findViewById(R.id.tvPrescriptionName);
                tvPrescriptionCode = itemView.findViewById(R.id.tvPrescriptionCode);
                tvPrescriptionPrice = itemView.findViewById(R.id.tvPrescriptionPrice);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }

    // Adapter for selected prescriptions
    private static class SelectedPrescriptionAdapter extends RecyclerView.Adapter<SelectedPrescriptionAdapter.ViewHolder> {
        private List<PrescriptionExaminationForm> selectedPrescriptions;
        private final PrescriptionDao prescriptionDao;
        private final OnPrescriptionClickListener listener;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        interface OnPrescriptionClickListener {
            void onRemove(PrescriptionExaminationForm prescriptionForm);
        }

        SelectedPrescriptionAdapter(List<PrescriptionExaminationForm> selectedPrescriptions, PrescriptionDao prescriptionDao, OnPrescriptionClickListener listener) {
            this.selectedPrescriptions = selectedPrescriptions;
            this.prescriptionDao = prescriptionDao;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_prescription_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PrescriptionExaminationForm prescriptionForm = selectedPrescriptions.get(position);
            // Load prescription name
            Executors.newSingleThreadExecutor().execute(() -> {
                Prescription prescription = prescriptionDao.findById(prescriptionForm.prescriptionId);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                if (prescription != null) {
                    mainHandler.post(() -> {
                        holder.tvPrescriptionName.setText(prescription.name);
                        holder.tvPrescriptionCode.setText("Mã: " + prescription.code);
                        holder.tvPrescriptionPrice.setText(currencyFormatter.format(prescriptionForm.price));
                    });
                } else {
                    mainHandler.post(() -> {
                        holder.tvPrescriptionName.setText("Đơn thuốc #" + prescriptionForm.prescriptionId);
                        holder.tvPrescriptionCode.setText("Giá: " + currencyFormatter.format(prescriptionForm.price));
                    });
                }
            });
            holder.tvPrescriptionPrice.setVisibility(View.VISIBLE);
            holder.btnAdd.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> listener.onRemove(prescriptionForm));
        }

        @Override
        public int getItemCount() {
            return selectedPrescriptions != null ? selectedPrescriptions.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPrescriptionName, tvPrescriptionCode, tvPrescriptionPrice;
            MaterialButton btnAdd, btnRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPrescriptionName = itemView.findViewById(R.id.tvPrescriptionName);
                tvPrescriptionCode = itemView.findViewById(R.id.tvPrescriptionCode);
                tvPrescriptionPrice = itemView.findViewById(R.id.tvPrescriptionPrice);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}
