package ui.doctor;

import android.os.Bundle;
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

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.DoctorDao;
import data.model.Appointment;
import data.model.Doctor;
import data.model.Review;
import data.repository.AppointmentRepository;
import data.repository.ReviewRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import util.AuthUtils;

@AndroidEntryPoint
public class DoctorOverviewFragment extends Fragment {

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    ReviewRepository reviewRepository;
    
    @Inject
    DoctorDao doctorDao;

    private TextView tvTotalAppointments;
    private TextView tvCancelledAppointments;
    private TextView tvAverageRating;
    private TextView tvTotalReviews;
    private RecyclerView rvUpcomingAppointments;
    private RecyclerView rvRecentReviews;
    private AppointmentAdapter appointmentAdapter;
    private ReviewAdapter reviewAdapter;

    private long doctorId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.doctor_fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerViews();
        loadDoctorId(); // This will call loadData() after getting doctorId
    }

    private void initViews(View view) {
        tvTotalAppointments = view.findViewById(R.id.tv_total_appointments);
        tvCancelledAppointments = view.findViewById(R.id.tv_cancelled_appointments);
        tvAverageRating = view.findViewById(R.id.tv_average_rating);
        tvTotalReviews = view.findViewById(R.id.tv_total_reviews);
        rvUpcomingAppointments = view.findViewById(R.id.rv_upcoming_appointments);
        rvRecentReviews = view.findViewById(R.id.rv_recent_reviews);
    }

    private void loadDoctorId() {
        long userId = AuthUtils.getUserId(requireContext());
        
        if (userId == -1) {
            Toasty.error(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Executors.newSingleThreadExecutor().execute(() -> {
            Doctor doctor = doctorDao.findByUserId((int) userId);
            
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    if (doctor != null) {
                        doctorId = doctor.id;
                        loadData();
                    } else {
                        if (getContext() != null) {
                            Toasty.error(getContext(), "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void setupRecyclerViews() {
        // Upcoming appointments
        appointmentAdapter = new AppointmentAdapter();
        rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcomingAppointments.setAdapter(appointmentAdapter);

        // Recent reviews
        reviewAdapter = new ReviewAdapter();
        rvRecentReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentReviews.setAdapter(reviewAdapter);
    }

    private void loadData() {
        if (!isAdded() || getView() == null || doctorId == -1) {
            return;
        }

        // Load total completed appointments
        appointmentRepository.getCompletedCount(doctorId).observe(getViewLifecycleOwner(), count -> {
            if (isAdded() && tvTotalAppointments != null && count != null) {
                tvTotalAppointments.setText(String.valueOf(count));
            }
        });

        // Load cancelled appointments
        appointmentRepository.getCancelledCount(doctorId).observe(getViewLifecycleOwner(), count -> {
            if (isAdded() && tvCancelledAppointments != null && count != null) {
                tvCancelledAppointments.setText(String.valueOf(count));
            }
        });

        // Load upcoming appointments
        appointmentRepository.getConfirmedUpcoming(doctorId).observe(getViewLifecycleOwner(), appointments -> {
            if (isAdded() && appointmentAdapter != null && appointments != null) {
                List<Appointment> limitedList = appointments.size() > 5 
                    ? appointments.subList(0, 5) 
                    : appointments;
                appointmentAdapter.setAppointments(limitedList);
            }
        });

        // Load average rating
        reviewRepository.getAverageRating(doctorId).observe(getViewLifecycleOwner(), avgRating -> {
            if (isAdded() && tvAverageRating != null && avgRating != null) {
                tvAverageRating.setText(String.format("%.1f", avgRating));
            }
        });

        // Load total reviews
        reviewRepository.getTotalReviews(doctorId).observe(getViewLifecycleOwner(), count -> {
            if (isAdded() && tvTotalReviews != null && count != null) {
                tvTotalReviews.setText(String.valueOf(count));
            }
        });

        // Load recent reviews
        reviewRepository.getRecentByDoctor(doctorId, 5).observe(getViewLifecycleOwner(), reviews -> {
            if (isAdded() && reviewAdapter != null && reviews != null) {
                reviewAdapter.setReviews(reviews);
            }
        });
    }
}

