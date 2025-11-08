package ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.PrescriptionDao;
import data.model.Prescription;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminPrescriptionUpsertFragment extends Fragment {

    private static final String ARG_PRESCRIPTION_ID = "prescription_id";

    private TextInputEditText etName, etCode, etPrice;
    private TextView tvTitle;
    private Button btnSave;
    private ImageButton btnBack;

    @Inject
    public PrescriptionDao prescriptionDao;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Prescription currentPrescription;
    private boolean isEditMode = false;

    // Factory method cho chế độ TẠO
    public static AdminPrescriptionUpsertFragment newInstance() {
        return new AdminPrescriptionUpsertFragment();
    }

    // Factory method cho chế độ SỬA
    public static AdminPrescriptionUpsertFragment newInstance(long prescriptionId) {
        AdminPrescriptionUpsertFragment fragment = new AdminPrescriptionUpsertFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PRESCRIPTION_ID, prescriptionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_PRESCRIPTION_ID)) {
            isEditMode = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_prescription_upsert, container, false);
        initViews(view);
        setupUI();
        return view;
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        etName = view.findViewById(R.id.etName);
        etCode = view.findViewById(R.id.etCode);
        etPrice = view.findViewById(R.id.etPrice);
        btnSave = view.findViewById(R.id.btnSave);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupUI() {
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> savePrescription());

        if (isEditMode) {
            tvTitle.setText("Chỉnh Sửa Thuốc");
            btnSave.setText("Cập nhật");
            loadExistingData();
        } else {
            tvTitle.setText("Thêm Thuốc Mới");
            btnSave.setText("Lưu");
        }
    }

    private void loadExistingData() {
        long prescriptionId = getArguments().getLong(ARG_PRESCRIPTION_ID);
        dbExecutor.execute(() -> {
            currentPrescription = prescriptionDao.findById(prescriptionId);
            if (currentPrescription != null) {
                requireActivity().runOnUiThread(this::populateForm);
            }
        });
    }

    private void populateForm() {
        etName.setText(currentPrescription.name);
        etCode.setText(currentPrescription.code);
        etPrice.setText(String.valueOf(currentPrescription.price));
    }

    private void savePrescription() {
        String name = etName.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(code) || TextUtils.isEmpty(priceStr)) {
            Toasty.warning(requireContext(), "Vui lòng điền đầy đủ thông tin.").show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toasty.error(requireContext(), "Giá không hợp lệ.").show();
            return;
        }

        dbExecutor.execute(() -> {
            if (isEditMode) {
                // Cập nhật thuốc hiện có
                currentPrescription.name = name;
                currentPrescription.code = code;
                currentPrescription.price = price;
                prescriptionDao.update(currentPrescription);
                requireActivity().runOnUiThread(() -> {
                    Toasty.success(requireContext(), "Cập nhật thuốc thành công!").show();
                    getParentFragmentManager().popBackStack();
                });
            } else {
                // Tạo thuốc mới
                Prescription newPrescription = new Prescription();
                newPrescription.name = name;
                newPrescription.code = code;
                newPrescription.price = price;
                prescriptionDao.insert(newPrescription);
                requireActivity().runOnUiThread(() -> {
                    Toasty.success(requireContext(), "Thêm thuốc mới thành công!").show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}
