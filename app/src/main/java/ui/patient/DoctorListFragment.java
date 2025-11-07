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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.DoctorDao;
import data.db.UserDao;
import data.model.Doctor;
import data.model.User;
import example.pclinic.com.R;

@AndroidEntryPoint
public class DoctorListFragment extends Fragment {

    @Inject
    DoctorDao doctorDao;

    @Inject
    UserDao userDao;

    private RecyclerView recyclerViewDoctors;
    private TextView txtEmpty;
    private DoctorListAdapter adapter;
    private List<DoctorListItem> doctorListItems = new ArrayList<>();

    public DoctorListFragment() {
        // Bắt buộc constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_doctor_list, container, false);

        recyclerViewDoctors = view.findViewById(R.id.recyclerViewDoctors);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerViewDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadDoctors();

        return view;
    }

    private void loadDoctors() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Doctor> doctors = doctorDao.getAll();
            doctorListItems.clear();

            for (Doctor doctor : doctors) {
                User user = userDao.findById(doctor.userId);
                if (user != null) {
                    DoctorListItem item = new DoctorListItem();
                    item.doctorId = doctor.id;
                    item.doctorName = user.fullName;
                    item.bio = doctor.bio != null ? doctor.bio : "";
                    item.specialties = doctor.specialties != null && !doctor.specialties.isEmpty()
                            ? String.join(", ", doctor.specialties)
                            : "Chưa có thông tin";
                    doctorListItems.add(item);
                }
            }

            requireActivity().runOnUiThread(() -> {
                if (doctorListItems.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    recyclerViewDoctors.setVisibility(View.GONE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                    recyclerViewDoctors.setVisibility(View.VISIBLE);
                    adapter = new DoctorListAdapter(doctorListItems, this::onBookAppointmentClick);
                    recyclerViewDoctors.setAdapter(adapter);
                }
            });
        });
    }

    private void onBookAppointmentClick(long doctorId) {
        // Navigate sang BookAppointmentFragment với doctorId
        BookAppointmentFragment fragment = BookAppointmentFragment.newInstance(doctorId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Model class cho item
    public static class DoctorListItem {
        public long doctorId;
        public String doctorName;
        public String specialties;
        public String bio;
    }

    // Adapter cho RecyclerView
    private static class DoctorListAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<DoctorListAdapter.ViewHolder> {

        private final List<DoctorListItem> data;
        private final OnBookAppointmentClickListener listener;

        public interface OnBookAppointmentClickListener {
            void onBookAppointmentClick(long doctorId);
        }

        public DoctorListAdapter(List<DoctorListItem> data, OnBookAppointmentClickListener listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_item_doctor, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DoctorListItem item = data.get(position);
            holder.bind(item, listener);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final TextView txtDoctorName;
            private final TextView txtSpecialties;
            private final TextView txtBio;
            private final com.google.android.material.button.MaterialButton btnBookAppointment;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtDoctorName = itemView.findViewById(R.id.txtDoctorName);
                txtSpecialties = itemView.findViewById(R.id.txtSpecialties);
                txtBio = itemView.findViewById(R.id.txtBio);
                btnBookAppointment = itemView.findViewById(R.id.btnBookAppointment);
            }

            public void bind(DoctorListItem item, OnBookAppointmentClickListener listener) {
                txtDoctorName.setText("BS. " + item.doctorName);
                txtSpecialties.setText("Chuyên khoa: " + item.specialties);
                txtBio.setText(item.bio.isEmpty() ? "Chưa có mô tả" : item.bio);

                btnBookAppointment.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onBookAppointmentClick(item.doctorId);
                    }
                });
            }
        }
    }
}

