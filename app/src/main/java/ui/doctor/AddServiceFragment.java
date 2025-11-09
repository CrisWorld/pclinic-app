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
import data.db.ServiceDao;
import data.db.ServiceExaminationFormDao;
import data.model.Appointment;
import data.model.ExaminationForm;
import data.model.Service;
import data.model.ServiceExaminationForm;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AddServiceFragment extends Fragment {

    @Inject
    ServiceDao serviceDao;

    @Inject
    ServiceExaminationFormDao serviceExaminationFormDao;

    @Inject
    AppointmentDao appointmentDao;

    @Inject
    ExaminationFormDao examinationFormDao;

    private long appointmentId;
    private long examinationFormId;
    private long patientId;
    private long doctorId;

    private TextInputEditText etSearch;
    private RecyclerView rvServices;
    private RecyclerView rvSelectedServices;
    private MaterialCardView cardSelectedServices;
    private TextView tvTotalAmount;
    private TextView tvEmpty;
    private MaterialButton btnSave;

    private ServiceSelectionAdapter adapter;
    private SelectedServiceAdapter selectedAdapter;
    private List<Service> allServices = new ArrayList<>();
    private List<Service> filteredServices = new ArrayList<>();
    private List<ServiceExaminationForm> selectedServices = new ArrayList<>();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static AddServiceFragment newInstance(long appointmentId, long examinationFormId) {
        AddServiceFragment fragment = new AddServiceFragment();
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
        View view = inflater.inflate(R.layout.doctor_fragment_add_service, container, false);

        Bundle args = getArguments();
        if (args != null) {
            appointmentId = args.getLong("appointmentId");
            examinationFormId = args.getLong("examinationFormId");
        }

        initViews(view);
        setupRecyclerViews();
        loadAppointmentInfo();
        loadServices();
        loadExistingServices();
        setupSearch();
        setupSaveButton();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        rvServices = view.findViewById(R.id.rvServices);
        rvSelectedServices = view.findViewById(R.id.rvSelectedServices);
        cardSelectedServices = view.findViewById(R.id.cardSelectedServices);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupRecyclerViews() {
        // Available services
        rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ServiceSelectionAdapter(filteredServices, service -> {
            addService(service);
        });
        rvServices.setAdapter(adapter);

        // Selected services
        rvSelectedServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectedAdapter = new SelectedServiceAdapter(selectedServices, serviceDao, serviceExaminationForm -> {
            removeService(serviceExaminationForm);
        });
        rvSelectedServices.setAdapter(selectedAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServices(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveServices());
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

    private void loadServices() {
        Executors.newSingleThreadExecutor().execute(() -> {
            allServices = serviceDao.getAll();
            filteredServices = new ArrayList<>(allServices);
            requireActivity().runOnUiThread(() -> {
                adapter.updateData(filteredServices);
                updateEmptyState();
            });
        });
    }

    private void loadExistingServices() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ServiceExaminationForm> existing = serviceExaminationFormDao.findByExaminationId(examinationFormId);
            requireActivity().runOnUiThread(() -> {
                selectedServices.clear();
                selectedServices.addAll(existing);
                selectedAdapter.notifyDataSetChanged();
                updateSelectedServicesUI();
                updateTotal();
            });
        });
    }

    private void filterServices(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredServices = new ArrayList<>(allServices);
        } else {
            filteredServices = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Service service : allServices) {
                if (service.name != null && service.name.toLowerCase().contains(lowerQuery) ||
                    service.code != null && service.code.toLowerCase().contains(lowerQuery)) {
                    filteredServices.add(service);
                }
            }
        }
        adapter.updateData(filteredServices);
        updateEmptyState();
    }

    private void addService(Service service) {
        // Check if already added
        for (ServiceExaminationForm existing : selectedServices) {
            if (existing.serviceId == service.id) {
                Toasty.info(requireContext(), "Dịch vụ đã được thêm", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ServiceExaminationForm serviceForm = new ServiceExaminationForm();
        serviceForm.serviceId = service.id;
        serviceForm.appointmentId = appointmentId;
        serviceForm.examinationId = examinationFormId;
        serviceForm.patientId = patientId;
        serviceForm.doctorId = doctorId;
        serviceForm.price = service.price;

        selectedServices.add(serviceForm);
        selectedAdapter.notifyDataSetChanged();
        updateSelectedServicesUI();
        updateTotal();
        Toasty.success(requireContext(), "Đã thêm dịch vụ", Toast.LENGTH_SHORT).show();
    }

    private void removeService(ServiceExaminationForm serviceForm) {
        selectedServices.remove(serviceForm);
        selectedAdapter.notifyDataSetChanged();
        updateSelectedServicesUI();
        updateTotal();
        Toasty.info(requireContext(), "Đã xóa dịch vụ", Toast.LENGTH_SHORT).show();
    }

    private void updateSelectedServicesUI() {
        if (selectedServices.isEmpty()) {
            cardSelectedServices.setVisibility(View.GONE);
        } else {
            cardSelectedServices.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotal() {
        double total = 0;
        for (ServiceExaminationForm serviceForm : selectedServices) {
            total += serviceForm.price;
        }
        tvTotalAmount.setText(currencyFormatter.format(total));
    }

    private void updateEmptyState() {
        if (filteredServices.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvServices.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvServices.setVisibility(View.VISIBLE);
        }
    }

    private void saveServices() {
        if (selectedServices.isEmpty()) {
            Toasty.warning(requireContext(), "Vui lòng chọn ít nhất một dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Delete existing services for this examination
            serviceExaminationFormDao.deleteByExaminationId(examinationFormId);
            // Insert new services
            serviceExaminationFormDao.insertAll(selectedServices);

            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Lưu dịch vụ thành công", Toast.LENGTH_SHORT).show();
                // Go back
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        });
    }

    // Adapter for available services
    private static class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ViewHolder> {
        private List<Service> services;
        private final OnServiceClickListener listener;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        interface OnServiceClickListener {
            void onAdd(Service service);
        }

        ServiceSelectionAdapter(List<Service> services, OnServiceClickListener listener) {
            this.services = services;
            this.listener = listener;
        }

        void updateData(List<Service> newList) {
            this.services = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Service service = services.get(position);
            holder.tvServiceName.setText(service.name);
            holder.tvServiceCode.setText("Mã: " + service.code);
            holder.tvServicePrice.setText(currencyFormatter.format(service.price));
            holder.btnAdd.setOnClickListener(v -> listener.onAdd(service));
            holder.btnRemove.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return services != null ? services.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvServiceName, tvServiceCode, tvServicePrice;
            MaterialButton btnAdd, btnRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvServiceName = itemView.findViewById(R.id.tvServiceName);
                tvServiceCode = itemView.findViewById(R.id.tvServiceCode);
                tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }

    // Adapter for selected services
    private static class SelectedServiceAdapter extends RecyclerView.Adapter<SelectedServiceAdapter.ViewHolder> {
        private List<ServiceExaminationForm> selectedServices;
        private final ServiceDao serviceDao;
        private final OnServiceClickListener listener;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        interface OnServiceClickListener {
            void onRemove(ServiceExaminationForm serviceForm);
        }

        SelectedServiceAdapter(List<ServiceExaminationForm> selectedServices, ServiceDao serviceDao, OnServiceClickListener listener) {
            this.selectedServices = selectedServices;
            this.serviceDao = serviceDao;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ServiceExaminationForm serviceForm = selectedServices.get(position);
            // Load service name
            Executors.newSingleThreadExecutor().execute(() -> {
                Service service = serviceDao.findById(serviceForm.serviceId);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                if (service != null) {
                    mainHandler.post(() -> {
                        holder.tvServiceName.setText(service.name);
                        holder.tvServiceCode.setText("Mã: " + service.code);
                        holder.tvServicePrice.setText(currencyFormatter.format(serviceForm.price));
                    });
                } else {
                    mainHandler.post(() -> {
                        holder.tvServiceName.setText("Dịch vụ #" + serviceForm.serviceId);
                        holder.tvServiceCode.setText("Giá: " + currencyFormatter.format(serviceForm.price));
                    });
                }
            });
            holder.tvServicePrice.setVisibility(View.VISIBLE);
            holder.btnAdd.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> listener.onRemove(serviceForm));
        }

        @Override
        public int getItemCount() {
            return selectedServices != null ? selectedServices.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvServiceName, tvServiceCode, tvServicePrice;
            MaterialButton btnAdd, btnRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvServiceName = itemView.findViewById(R.id.tvServiceName);
                tvServiceCode = itemView.findViewById(R.id.tvServiceCode);
                tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}
