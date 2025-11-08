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
import data.db.admin.AdminServiceDao;
import data.model.Service;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminServiceUpsertFragment extends Fragment {

    private static final String ARG_SERVICE_ID = "service_id";

    private TextInputEditText etName, etCode, etPrice;
    private TextView tvTitle;
    private Button btnSave;
    private ImageButton btnBack;

    @Inject
    public AdminServiceDao serviceDao;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Service currentService;
    private boolean isEditMode = false;

    // Factory method cho chế độ TẠO
    public static AdminServiceUpsertFragment newInstance() {
        return new AdminServiceUpsertFragment();
    }

    // Factory method cho chế độ SỬA
    public static AdminServiceUpsertFragment newInstance(long serviceId) {
        AdminServiceUpsertFragment fragment = new AdminServiceUpsertFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SERVICE_ID, serviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_SERVICE_ID)) {
            isEditMode = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_service_upsert, container, false);
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
        btnSave.setOnClickListener(v -> saveService());

        if (isEditMode) {
            tvTitle.setText("Chỉnh Sửa Dịch Vụ");
            btnSave.setText("Cập nhật");
            loadExistingData();
        } else {
            tvTitle.setText("Thêm Dịch Vụ Mới");
            btnSave.setText("Lưu");
        }
    }

    private void loadExistingData() {
        long serviceId = getArguments().getLong(ARG_SERVICE_ID);
        dbExecutor.execute(() -> {
            currentService = serviceDao.findById(serviceId);
            if (currentService != null) {
                requireActivity().runOnUiThread(this::populateForm);
            }
        });
    }

    private void populateForm() {
        etName.setText(currentService.name);
        etCode.setText(currentService.code);
        etPrice.setText(String.valueOf(currentService.price));
    }

    private void saveService() {
        String name = etName.getText().toString().trim();
        String code = etCode.getText().toString().trim().toUpperCase(); // Chuẩn hóa về chữ hoa
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
            // KIỂM TRA MÃ DUY NHẤT
            Service serviceWithSameCode = serviceDao.findByCode(code);
            boolean isCodeExisted = serviceWithSameCode != null;

            // Nếu đang ở chế độ sửa, mã chỉ được coi là đã tồn tại nếu nó thuộc về một dịch vụ KHÁC
            if (isEditMode && isCodeExisted && serviceWithSameCode.id == currentService.id) {
                isCodeExisted = false;
            }

            if (isCodeExisted) {
                requireActivity().runOnUiThread(() ->
                        Toasty.error(requireContext(), "Mã dịch vụ '" + code + "' đã tồn tại.").show()
                );
                return; // Dừng việc lưu
            }

            // TIẾP TỤC LƯU NẾU MÃ HỢP LỆ
            if (isEditMode) {
                currentService.name = name;
                currentService.code = code;
                currentService.price = price;
                serviceDao.update(currentService);
                requireActivity().runOnUiThread(() -> {
                    Toasty.success(requireContext(), "Cập nhật dịch vụ thành công!").show();
                    getParentFragmentManager().popBackStack();
                });
            } else {
                Service newService = new Service();
                newService.name = name;
                newService.code = code;
                newService.price = price;
                serviceDao.insert(newService);
                requireActivity().runOnUiThread(() -> {
                    Toasty.success(requireContext(), "Thêm dịch vụ mới thành công!").show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}
