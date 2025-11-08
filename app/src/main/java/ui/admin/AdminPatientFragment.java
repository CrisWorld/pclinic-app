package ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import data.db.admin.AdminDoctorDao;
import data.dto.PatientInfo;
import example.pclinic.com.R;

@AndroidEntryPoint
public class AdminPatientFragment extends Fragment {

    private EditText etSearchPatient;
    private RecyclerView recyclerPatientList;
    private AdminPatientAdapter adapter;

    @Inject
    public AdminDoctorDao adminDao; // Dùng chung DAO

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_patient, container, false);

        initViews(view);
        setupRecyclerView();
        loadPatients();
        setupSearch();

        return view;
    }

    private void initViews(View view) {
        etSearchPatient = view.findViewById(R.id.etSearchPatient);
        recyclerPatientList = view.findViewById(R.id.recyclerPatientList);
    }

    private void setupRecyclerView() {
        recyclerPatientList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Cải tiến: Tách listener ra để code rõ ràng và nhất quán hơn
        adapter = new AdminPatientAdapter(null, new AdminPatientAdapter.OnPatientClickListener() {
            @Override
            public void onViewDetail(PatientInfo patient) {
                // Mở màn hình chi tiết khi click
                Intent intent = new Intent(getActivity(), AdminPatientDetailActivity.class);
                intent.putExtra("patient_id", patient.patientId);
                startActivity(intent);
            }
        });

        recyclerPatientList.setAdapter(adapter);
    }

    private void loadPatients() {
        // Quan sát và cập nhật danh sách bệnh nhân từ LiveData
        adminDao.getAllPatientInfo().observe(getViewLifecycleOwner(), patients -> {
            if (patients != null) {
                adapter.updateData(patients);
            }
        });
    }

    private void setupSearch() {
        etSearchPatient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi người dùng nhập, thực hiện tìm kiếm và cập nhật UI
                String keyword = s.toString();
                adminDao.searchPatientInfo(keyword).observe(getViewLifecycleOwner(), patients -> {
                    if (patients != null) {
                        adapter.updateData(patients);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
