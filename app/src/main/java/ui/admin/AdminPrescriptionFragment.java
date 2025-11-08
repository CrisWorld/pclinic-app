package ui.admin;import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.admin.AdminPrescriptionDao;
import data.model.Prescription;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminPrescriptionFragment extends Fragment {

    private EditText etSearchPrescription;
    private Button btnCreatePrescription;
    private RecyclerView recyclerPrescriptionList;
    private AdminPrescriptionAdapter adapter;

    @Inject
    public AdminPrescriptionDao prescriptionDao;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_prescription, container, false);

        initViews(view);
        setupRecyclerView();
        loadPrescriptions();
        setupSearch();

        // Cập nhật listener cho nút tạo
        btnCreatePrescription.setOnClickListener(v -> {
            openUpsertFragment(null); // Truyền null cho chế độ tạo mới
        });

        return view;
    }

    private void initViews(View view) {
        etSearchPrescription = view.findViewById(R.id.etSearchPrescription);
        btnCreatePrescription = view.findViewById(R.id.btnCreatePrescription);
        recyclerPrescriptionList = view.findViewById(R.id.recyclerPrescriptionList);
    }

    private void setupRecyclerView() {
        recyclerPrescriptionList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminPrescriptionAdapter(null, new AdminPrescriptionAdapter.OnPrescriptionClickListener() {
            @Override
            public void onEdit(Prescription prescription) {
                // Cập nhật listener cho nút sửa
                openUpsertFragment(prescription); // Truyền đối tượng prescription cho chế độ sửa
            }

            @Override
            public void onDelete(Prescription prescription) {
                showDeleteConfirmationDialog(prescription);
            }
        });
        recyclerPrescriptionList.setAdapter(adapter);
    }

    private void loadPrescriptions() {
        prescriptionDao.getAll().observe(getViewLifecycleOwner(), prescriptions -> {
            adapter.updateData(prescriptions);
        });
    }

    private void setupSearch() {
        etSearchPrescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                prescriptionDao.search(keyword).observe(getViewLifecycleOwner(), prescriptions -> {
                    adapter.updateData(prescriptions);
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showDeleteConfirmationDialog(Prescription prescription) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thuốc \"" + prescription.name + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deletePrescription(prescription))
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePrescription(Prescription prescription) {
        dbExecutor.execute(() -> {
            prescriptionDao.delete(prescription);
            requireActivity().runOnUiThread(() ->
                    Toasty.success(requireContext(), "Đã xóa thuốc " + prescription.name).show()
            );
        });
    }

    private void openUpsertFragment(Prescription prescription) {
        Fragment upsertFragment;
        if (prescription == null) {
            // Chế độ TẠO
            upsertFragment = AdminPrescriptionUpsertFragment.newInstance();
        } else {
            // Chế độ SỬA
            upsertFragment = AdminPrescriptionUpsertFragment.newInstance(prescription.id);
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        // Thay thế bằng ID container của bạn
        transaction.replace(R.id.fragmentContainer, upsertFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
