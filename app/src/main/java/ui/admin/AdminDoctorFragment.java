package ui.admin;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.admin.AdminDoctorDao;
import data.dto.DoctorInfo;
import data.model.Doctor;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminDoctorFragment extends Fragment {
    private EditText etSearchDoctor;
    private Button btnCreateDoctor;
    private RecyclerView recyclerDoctorList;
    private AdminDoctorAdapter adapter;
    @Inject
    public AdminDoctorDao doctorDao;

    // Executor để chạy tác vụ DB trên background thread
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_fragment_doctor, container, false);

        // Ánh xạ view
        etSearchDoctor = view.findViewById(R.id.etSearchDoctor);
        btnCreateDoctor = view.findViewById(R.id.btnCreateDoctor);
        recyclerDoctorList = view.findViewById(R.id.recyclerDoctorList);

        recyclerDoctorList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter
        adapter = new AdminDoctorAdapter(null, new AdminDoctorAdapter.OnDoctorClickListener() {
            @Override
            public void onClick(DoctorInfo doctor) {
                // Not used, using onViewDetail for both clicks
            }

            @Override
            public void onViewDetail(DoctorInfo doctor) {
                Intent intent = new Intent(getActivity(), DoctorDetailActivity.class);
                intent.putExtra("doctor_id", doctor.doctorId);
                startActivity(intent);
            }

            @Override
            public void onEdit(DoctorInfo doctor) {
                openEditDoctorFragment(doctor.doctorId); // <-- Thêm mới
            }

            @Override
            public void onDelete(DoctorInfo doctor) {
                showDeleteConfirmationDialog(doctor);
            }
        });
        recyclerDoctorList.setAdapter(adapter);

        // Lấy dữ liệu và cập nhật RecyclerView
        loadDoctors();


        // Tìm kiếm
        etSearchDoctor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                // Luôn tìm kiếm từ nguồn dữ liệu mới nhất
                doctorDao.searchDoctorInfo(keyword).observe(getViewLifecycleOwner(), doctors -> {
                    adapter.updateData(doctors);
                });
            }
        });

        btnCreateDoctor.setOnClickListener(v -> openCreateDoctorFragment());

        return view;
    }

    private void loadDoctors() {
        doctorDao.getAllDoctorInfo().observe(getViewLifecycleOwner(), doctors -> {
            adapter.updateData(doctors);
        });
    }

    private void showDeleteConfirmationDialog(DoctorInfo doctor) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bác sĩ \"" + doctor.fullName + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteDoctor(doctor);
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteDoctor(DoctorInfo doctor) {
        // Chạy tác vụ xóa trên một background thread
        databaseExecutor.execute(() -> {
            doctorDao.deleteUserById(doctor.userId);
            // Sau khi xóa, hiển thị thông báo trên UI thread
            requireActivity().runOnUiThread(() -> {
                Toasty.success(requireContext(), "Đã xóa bác sĩ " + doctor.fullName).show();
                // Không cần gọi loadDoctors() lại vì LiveData sẽ tự động cập nhật
            });
        });
    }

    private void openCreateDoctorFragment() {
        Fragment createFragment = new AdminDoctorCreateFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        // Thay thế fragment hiện tại và thêm vào back stack để có thể quay lại
        transaction.replace(R.id.fragmentContainer, createFragment); // Giả sử container của bạn có id là fragment_container_view
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openEditDoctorFragment(long doctorId) {
        Fragment editFragment = AdminDoctorEditFragment.newInstance(doctorId);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, editFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
