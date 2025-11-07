package ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.model.Appointment;
import data.model.WorkSchedule;
import data.repository.AppointmentRepository;
import data.repository.WorkScheduleRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;

@AndroidEntryPoint
public class DoctorCalendarSetupFragment extends Fragment {

    @Inject
    WorkScheduleRepository workScheduleRepository;

    @Inject
    AppointmentRepository appointmentRepository;

    private CalendarView calendarView;
    private RecyclerView rvMorningSlots;
    private RecyclerView rvAfternoonSlots;
    private RecyclerView rvBookedAppointments;
    private TextView tvBookedCount;
    private TextView tvNoAppointments;
    private Button btnSave;
    private Button btnClear;

    private TimeSlotAdapter morningAdapter;
    private TimeSlotAdapter afternoonAdapter;
    private BookedAppointmentAdapter bookedAppointmentAdapter;

    private String selectedDate;
    private long doctorId = 1; // TODO: Get from logged in user
    private static final int SLOT_DURATION = 30; // 30 minutes per slot

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doctor_fragment_calendar_setup, container, false);

        initViews(view);
        setupCalendar();
        setupTimeSlots();
        setupBookedAppointments();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        rvMorningSlots = view.findViewById(R.id.rv_morning_slots);
        rvAfternoonSlots = view.findViewById(R.id.rv_afternoon_slots);
        rvBookedAppointments = view.findViewById(R.id.rv_booked_appointments);
        tvBookedCount = view.findViewById(R.id.tv_booked_count);
        tvNoAppointments = view.findViewById(R.id.tv_no_appointments);
        btnSave = view.findViewById(R.id.btn_save);
        btnClear = view.findViewById(R.id.btn_clear);
    }

    private void setupCalendar() {
        // Set minimum date to today
        calendarView.setMinDate(System.currentTimeMillis());

        // Set initial selected date to today
        Calendar calendar = Calendar.getInstance();
        selectedDate = formatDate(calendar);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            selectedDate = formatDate(selected);
            loadExistingSchedules();
            loadBookedAppointments();
        });

        loadExistingSchedules();
    }

    private void setupBookedAppointments() {
        bookedAppointmentAdapter = new BookedAppointmentAdapter();
        rvBookedAppointments.setAdapter(bookedAppointmentAdapter);
        loadBookedAppointments();
    }

    private void loadBookedAppointments() {
        appointmentRepository.getAppointmentsByDoctorAndDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), appointments -> {
                    if (appointments != null && !appointments.isEmpty()) {
                        bookedAppointmentAdapter.setAppointments(appointments);
                        tvBookedCount.setText(appointments.size() + " lịch hẹn");
                        tvNoAppointments.setVisibility(View.GONE);
                        rvBookedAppointments.setVisibility(View.VISIBLE);
                        
                        // Disable booked time slots
                        disableBookedSlots(appointments);
                    } else {
                        bookedAppointmentAdapter.setAppointments(new ArrayList<>());
                        tvBookedCount.setText("0 lịch hẹn");
                        tvNoAppointments.setVisibility(View.VISIBLE);
                        rvBookedAppointments.setVisibility(View.GONE);
                    }
                });
    }

    private void disableBookedSlots(List<Appointment> appointments) {
        // Mark time slots as disabled if they have appointments
        List<TimeSlot> morningSlots = morningAdapter.getTimeSlots();
        List<TimeSlot> afternoonSlots = afternoonAdapter.getTimeSlots();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (Appointment appointment : appointments) {
            // Extract time from startDate
            String appointmentTime = timeFormat.format(appointment.startDate);

            // Check and disable morning slots
            for (TimeSlot slot : morningSlots) {
                if (slot.getStartTime().equals(appointmentTime)) {
                    slot.setDisabled(true);
                    break;
                }
            }

            // Check and disable afternoon slots
            for (TimeSlot slot : afternoonSlots) {
                if (slot.getStartTime().equals(appointmentTime)) {
                    slot.setDisabled(true);
                    break;
                }
            }
        }

        morningAdapter.notifyDataSetChanged();
        afternoonAdapter.notifyDataSetChanged();
    }

    private void setupTimeSlots() {
        // Morning slots: 7:00 - 11:00
        morningAdapter = new TimeSlotAdapter();
        rvMorningSlots.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvMorningSlots.setAdapter(morningAdapter);
        morningAdapter.setTimeSlots(generateTimeSlots("07:00", "11:00"));

        // Afternoon slots: 13:00 - 17:00 (1 PM - 5 PM)
        afternoonAdapter = new TimeSlotAdapter();
        rvAfternoonSlots.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvAfternoonSlots.setAdapter(afternoonAdapter);
        afternoonAdapter.setTimeSlots(generateTimeSlots("13:00", "17:00"));
    }

    private List<TimeSlot> generateTimeSlots(String startTime, String endTime) {
        List<TimeSlot> slots = new ArrayList<>();
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar start = Calendar.getInstance();
            start.setTime(sdf.parse(startTime));
            
            Calendar end = Calendar.getInstance();
            end.setTime(sdf.parse(endTime));

            while (start.before(end)) {
                String slotStart = sdf.format(start.getTime());
                start.add(Calendar.MINUTE, SLOT_DURATION);
                String slotEnd = sdf.format(start.getTime());
                
                if (!start.after(end)) {
                    slots.add(new TimeSlot(slotStart, slotEnd));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return slots;
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveSchedules());
        btnClear.setOnClickListener(v -> clearSchedules());
    }

    private void saveSchedules() {
        List<TimeSlot> selectedSlots = new ArrayList<>();
        selectedSlots.addAll(morningAdapter.getSelectedSlots());
        selectedSlots.addAll(afternoonAdapter.getSelectedSlots());

        if (selectedSlots.isEmpty()) {
            Toasty.warning(requireContext(), "Vui lòng chọn ít nhất một khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all current slots for endTime lookup (before async operations)
        List<TimeSlot> allSlots = new ArrayList<>();
        allSlots.addAll(morningAdapter.getTimeSlots());
        allSlots.addAll(afternoonAdapter.getTimeSlots());

        // Use CountDownLatch to wait for both async operations
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);
        final List<Appointment>[] appointmentsRef = new List[]{new ArrayList<>()};
        final List<WorkSchedule>[] existingSchedulesRef = new List[]{new ArrayList<>()};

        // Get current appointments
        appointmentRepository.getAppointmentsByDoctorAndDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), appointments -> {
                    if (appointments != null) {
                        appointmentsRef[0] = appointments;
                    }
                    latch.countDown();
                });

        // Get current schedules
        workScheduleRepository.getSchedulesByDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), schedules -> {
                    if (schedules != null) {
                        existingSchedulesRef[0] = schedules;
                    }
                    latch.countDown();
                });

        // Process after both complete
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                latch.await(); // Wait for both operations to complete
                
                List<Appointment> appointments = appointmentsRef[0];
                List<WorkSchedule> existingSchedules = existingSchedulesRef[0];

                // Build set of appointment times for quick lookup
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                java.util.Set<String> appointmentTimes = new java.util.HashSet<>();
                for (Appointment appointment : appointments) {
                    appointmentTimes.add(timeFormat.format(appointment.startDate));
                }

                // Collect all slots to save:
                // 1. Selected slots (user's choice)
                // 2. Existing schedules that have appointments (must preserve)
                java.util.Set<String> slotsToSave = new java.util.HashSet<>();
                
                // Add all selected slots
                for (TimeSlot slot : selectedSlots) {
                    slotsToSave.add(slot.getStartTime());
                }
                
                // Add existing schedules that have appointments (preserve them)
                for (WorkSchedule schedule : existingSchedules) {
                    if (appointmentTimes.contains(schedule.startTime)) {
                        slotsToSave.add(schedule.startTime);
                    }
                }

                // Delete existing schedules for this date
                workScheduleRepository.deleteSchedulesByDate(doctorId, selectedDate);

                // Save all schedules (selected + preserved booked slots)
                for (String startTime : slotsToSave) {
                    // Find the corresponding TimeSlot to get endTime
                    TimeSlot matchingSlot = null;
                    for (TimeSlot slot : allSlots) {
                        if (slot.getStartTime().equals(startTime)) {
                            matchingSlot = slot;
                            break;
                        }
                    }
                    
                    // If not found in current slots, try to get from existing schedules
                    if (matchingSlot == null) {
                        for (WorkSchedule schedule : existingSchedules) {
                            if (schedule.startTime.equals(startTime)) {
                                matchingSlot = new TimeSlot(schedule.startTime, schedule.endTime);
                                break;
                            }
                        }
                    }
                    
                    // If still not found, calculate endTime from startTime + duration
                    if (matchingSlot == null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(sdf.parse(startTime));
                            cal.add(Calendar.MINUTE, SLOT_DURATION);
                            String endTime = sdf.format(cal.getTime());
                            matchingSlot = new TimeSlot(startTime, endTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue; // Skip this slot if we can't parse time
                        }
                    }
                    
                    if (matchingSlot != null) {
                        WorkSchedule schedule = new WorkSchedule(
                                doctorId,
                                selectedDate,
                                matchingSlot.getStartTime(),
                                matchingSlot.getEndTime(),
                                SLOT_DURATION
                        );
                        workScheduleRepository.createSchedule(schedule);
                    }
                }

                // Wait a bit for all inserts to complete
                Thread.sleep(200);

                // Reload on UI thread
                requireActivity().runOnUiThread(() -> {
                    Toasty.success(requireContext(), 
                            "Đã lưu " + selectedSlots.size() + " khung giờ làm việc", 
                            Toast.LENGTH_SHORT).show();
                    
                    // Reload schedules to update UI after saving
                    if (getView() != null) {
                        getView().postDelayed(() -> {
                            loadExistingSchedules();
                            loadBookedAppointments(); // Reload appointments to refresh disabled state
                        }, 300);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toasty.error(requireContext(), "Lỗi khi lưu lịch", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void clearSchedules() {
        workScheduleRepository.deleteSchedulesByDate(doctorId, selectedDate);
        
        // Reset UI immediately
        morningAdapter.setTimeSlots(generateTimeSlots("07:00", "11:00"));
        afternoonAdapter.setTimeSlots(generateTimeSlots("13:00", "17:00"));
        
        Toasty.info(requireContext(), "Đã xóa lịch làm việc", Toast.LENGTH_SHORT).show();
        
        // Reload to ensure UI is in sync with database
        if (getView() != null) {
            getView().postDelayed(() -> {
                loadExistingSchedules();
            }, 300); // 300ms delay to ensure DB operation completes
        }
    }

    private void loadExistingSchedules() {
        workScheduleRepository.getSchedulesByDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), schedules -> {
                    if (schedules != null && !schedules.isEmpty()) {
                        markSelectedSlots(schedules);
                    } else {
                        // Reset all selections
                        morningAdapter.setTimeSlots(generateTimeSlots("07:00", "11:00"));
                        afternoonAdapter.setTimeSlots(generateTimeSlots("13:00", "17:00"));
                    }
                });
    }

    private void markSelectedSlots(List<WorkSchedule> schedules) {
        List<TimeSlot> morningSlots = generateTimeSlots("07:00", "11:00");
        List<TimeSlot> afternoonSlots = generateTimeSlots("13:00", "17:00");

        for (WorkSchedule schedule : schedules) {
            String time = schedule.startTime + " - " + schedule.endTime;
            
            // Check morning slots
            for (TimeSlot slot : morningSlots) {
                if (slot.getDisplayTime().equals(time)) {
                    slot.setSelected(true);
                    break;
                }
            }
            
            // Check afternoon slots
            for (TimeSlot slot : afternoonSlots) {
                if (slot.getDisplayTime().equals(time)) {
                    slot.setSelected(true);
                    break;
                }
            }
        }

        morningAdapter.setTimeSlots(morningSlots);
        afternoonAdapter.setTimeSlots(afternoonSlots);
        
        // After marking selected slots, reload appointments to mark disabled slots
        // This ensures disabled state is preserved after reload
        loadBookedAppointments();
    }

    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}

