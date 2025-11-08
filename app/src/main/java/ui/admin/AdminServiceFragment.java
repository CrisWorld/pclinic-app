package ui.admin;

import android.content.DialogInterface;
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
import data.db.admin.AdminServiceDao;
import data.model.Service;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminServiceFragment extends Fragment {

    private EditText etSearchService;
    private Button btnCreateService;
    private RecyclerView recyclerServiceList;
    private AdminServiceAdapter adapter;

    @Inject
    public AdminServiceDao serviceDao;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_service, container, false);

        initViews(view);
        setupRecyclerView();
        loadServices();
        setupSearch();

        btnCreateService.setOnClickListener(v -> openUpsertFragment(null));

        return view;
    }

    private void initViews(View view) {
        etSearchService = view.findViewById(R.id.etSearchService);
        btnCreateService = view.findViewById(R.id.btnCreateService);
        recyclerServiceList = view.findViewById(R.id.recyclerServiceList);
    }

    private void setupRecyclerView() {
        recyclerServiceList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminServiceAdapter(null, new AdminServiceAdapter.OnServiceClickListener() {
            @Override
            public void onEdit(Service service) {
                openUpsertFragment(service);
            }

            @Override
            public void onDelete(Service service) {
                showDeleteConfirmationDialog(service);
            }
        });
        recyclerServiceList.setAdapter(adapter);
    }

    private void loadServices() {
        serviceDao.getAll().observe(getViewLifecycleOwner(), services -> {
            adapter.updateData(services);
        });
    }

    private void setupSearch() {
        etSearchService.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                serviceDao.search(s.toString()).observe(getViewLifecycleOwner(), services -> {
                    adapter.updateData(services);
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showDeleteConfirmationDialog(Service service) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa dịch vụ \"" + service.name + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteService(service))
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteService(Service service) {
        dbExecutor.execute(() -> {
            serviceDao.delete(service);
            requireActivity().runOnUiThread(() ->
                    Toasty.success(requireContext(), "Đã xóa dịch vụ " + service.name).show()
            );
        });
    }

    private void openUpsertFragment(Service service) {
        // Tương tự, bạn sẽ tạo AdminServiceUpsertFragment và layout của nó
        // Fragment upsertFragment;
        // if (service == null) {
        //     upsertFragment = AdminServiceUpsertFragment.newInstance();
        // } else {
        //     upsertFragment = AdminServiceUpsertFragment.newInstance(service.id);
        // }
        //
        // FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        // transaction.replace(R.id.fragmentContainer, upsertFragment);
        // transaction.addToBackStack(null);
        // transaction.commit();
        Toasty.info(getContext(), "Mở màn hình Tạo/Sửa Dịch vụ").show();
    }
}
