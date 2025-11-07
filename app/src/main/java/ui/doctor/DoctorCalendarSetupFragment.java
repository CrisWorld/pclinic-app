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

        // Delete existing schedules for this date first
        workScheduleRepository.deleteSchedulesByDate(doctorId, selectedDate);

        // Save new schedules
        for (TimeSlot slot : selectedSlots) {
            WorkSchedule schedule = new WorkSchedule(
                    doctorId,
                    selectedDate,
                    slot.getStartTime(),
                    slot.getEndTime(),
                    SLOT_DURATION
            );
            workScheduleRepository.createSchedule(schedule);
        }

        Toasty.success(requireContext(), 
                "Đã lưu " + selectedSlots.size() + " khung giờ làm việc", 
                Toast.LENGTH_SHORT).show();
    }

    private void clearSchedules() {
        workScheduleRepository.deleteSchedulesByDate(doctorId, selectedDate);
        morningAdapter.setTimeSlots(generateTimeSlots("07:00", "11:00"));
        afternoonAdapter.setTimeSlots(generateTimeSlots("13:00", "17:00"));
        Toasty.info(requireContext(), "Đã xóa lịch làm việc", Toast.LENGTH_SHORT).show();
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
    }

    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}

