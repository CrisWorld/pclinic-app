package ui.doctor;

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

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.model.Appointment;
import data.model.Review;
import data.repository.AppointmentRepository;
import data.repository.ReviewRepository;
import example.pclinic.com.R;
import util.AuthUtils;

@AndroidEntryPoint
public class DoctorOverviewFragment extends Fragment {

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    ReviewRepository reviewRepository;

    private TextView tvTotalAppointments;
    private TextView tvCancelledAppointments;
    private TextView tvAverageRating;
    private TextView tvTotalReviews;
    private RecyclerView rvUpcomingAppointments;
    private RecyclerView rvRecentReviews;
    private AppointmentAdapter appointmentAdapter;
    private ReviewAdapter reviewAdapter;

    private long doctorId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_overview, container, false);

        initViews(view);
        loadDoctorId();
        setupRecyclerViews();
        loadData();

        return view;
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
        // Get doctor ID from AuthUtils - for now using placeholder
        // In real app, you'd get this from the logged in user
        long userId = AuthUtils.getUserId(requireContext());
        // TODO: Get actual doctorId from userId via DoctorRepository
        // For demo purposes, using userId as doctorId
        doctorId = 1; // Placeholder - should query from database
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
        // Load total completed appointments
        appointmentRepository.getCompletedCount(doctorId).observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvTotalAppointments.setText(String.valueOf(count));
            }
        });

        // Load cancelled appointments
        appointmentRepository.getCancelledCount(doctorId).observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvCancelledAppointments.setText(String.valueOf(count));
            }
        });

        // Load upcoming appointments
        appointmentRepository.getConfirmedUpcoming(doctorId).observe(getViewLifecycleOwner(), appointments -> {
            if (appointments != null) {
                List<Appointment> limitedList = appointments.size() > 5 
                    ? appointments.subList(0, 5) 
                    : appointments;
                appointmentAdapter.setAppointments(limitedList);
            }
        });

        // Load average rating
        reviewRepository.getAverageRating(doctorId).observe(getViewLifecycleOwner(), avgRating -> {
            if (avgRating != null) {
                tvAverageRating.setText(String.format("%.1f", avgRating));
            }
        });

        // Load total reviews
        reviewRepository.getTotalReviews(doctorId).observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvTotalReviews.setText(String.valueOf(count));
            }
        });

        // Load recent reviews
        reviewRepository.getRecentByDoctor(doctorId, 5).observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null) {
                reviewAdapter.setReviews(reviews);
            }
        });
    }
}

