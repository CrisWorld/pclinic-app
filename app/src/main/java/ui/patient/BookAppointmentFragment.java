package ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import data.db.DoctorDao;
import data.db.PatientDao;
import data.db.UserDao;
import data.enums.Enum;
import data.model.Appointment;
import data.model.Doctor;
import data.model.Patient;
import data.model.User;
import data.model.WorkSchedule;
import data.repository.AppointmentRepository;
import data.repository.WorkScheduleRepository;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import ui.doctor.TimeSlot;
import util.AuthUtils;

@AndroidEntryPoint
public class BookAppointmentFragment extends Fragment {

    private static final String ARG_DOCTOR_ID = "doctor_id";

    @Inject
    WorkScheduleRepository workScheduleRepository;

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    DoctorDao doctorDao;

    @Inject
    UserDao userDao;

    @Inject
    PatientDao patientDao;

    private long doctorId;
    private long patientId;
    private String selectedDate;

    private CalendarView calendarView;
    private RecyclerView rvTimeSlots;
    private TextView txtDoctorName;
    private TextView txtDoctorSpecialties;
    private TextView txtNoSlots;
    private TextInputEditText edtDescription;
    private MaterialButton btnBookAppointment;

    private PatientTimeSlotAdapter timeSlotAdapter;

    public BookAppointmentFragment() {
        // Bắt buộc constructor rỗng
    }

    public static BookAppointmentFragment newInstance(long doctorId) {
        BookAppointmentFragment fragment = new BookAppointmentFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCTOR_ID, doctorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doctorId = getArguments().getLong(ARG_DOCTOR_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_fragment_book_appointment, container, false);

        initViews(view);
        setupCalendar();
        setupTimeSlots();
        loadDoctorInfo();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        rvTimeSlots = view.findViewById(R.id.rv_time_slots);
        txtDoctorName = view.findViewById(R.id.txtDoctorName);
        txtDoctorSpecialties = view.findViewById(R.id.txtDoctorSpecialties);
        txtNoSlots = view.findViewById(R.id.txtNoSlots);
        edtDescription = view.findViewById(R.id.edtDescription);
        btnBookAppointment = view.findViewById(R.id.btnBookAppointment);
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
            loadAvailableTimeSlots();
        });

        // Load slots for initial date
        loadAvailableTimeSlots();
    }

    private void setupTimeSlots() {
        timeSlotAdapter = new PatientTimeSlotAdapter();
        rvTimeSlots.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvTimeSlots.setAdapter(timeSlotAdapter);
        
        timeSlotAdapter.setOnSlotSelectedListener(slot -> {
            // Slot selected, enable book button if not already enabled
            btnBookAppointment.setEnabled(true);
        });
    }

    private void loadDoctorInfo() {
        if (doctorId == -1) {
            txtDoctorName.setText("Không có thông tin bác sĩ");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Doctor doctor = doctorDao.findById((int) doctorId);
            if (doctor != null) {
                User user = userDao.findById(doctor.userId);
                requireActivity().runOnUiThread(() -> {
                    if (user != null) {
                        txtDoctorName.setText("BS. " + user.fullName);
                        if (doctor.specialties != null && !doctor.specialties.isEmpty()) {
                            txtDoctorSpecialties.setText("Chuyên khoa: " + String.join(", ", doctor.specialties));
                        } else {
                            txtDoctorSpecialties.setText("Chuyên khoa: Chưa cập nhật");
                        }
                    }
                });
            }
        });
    }

    private void loadAvailableTimeSlots() {
        if (doctorId == -1) {
            return;
        }

        // Use CountDownLatch to wait for both async operations
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);
        final List<WorkSchedule>[] schedulesRef = new List[]{new ArrayList<>()};
        final List<Appointment>[] appointmentsRef = new List[]{new ArrayList<>()};

        // Get work schedules for selected date
        workScheduleRepository.getSchedulesByDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), schedules -> {
                    if (schedules != null) {
                        schedulesRef[0] = schedules;
                    }
                    latch.countDown();
                });

        // Get appointments for selected date to check booked slots
        appointmentRepository.getAppointmentsByDoctorAndDate(doctorId, selectedDate)
                .observe(getViewLifecycleOwner(), appointments -> {
                    if (appointments != null) {
                        appointmentsRef[0] = appointments;
                    }
                    latch.countDown();
                });

        // Process after both complete
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                latch.await(); // Wait for both operations to complete
                
                List<WorkSchedule> schedules = schedulesRef[0];
                List<Appointment> appointments = appointmentsRef[0];

                requireActivity().runOnUiThread(() -> {
                    if (schedules == null || schedules.isEmpty()) {
                        txtNoSlots.setVisibility(View.VISIBLE);
                        rvTimeSlots.setVisibility(View.GONE);
                        timeSlotAdapter.setTimeSlots(new ArrayList<>());
                        return;
                    }

                    // Build set of booked time slots
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Set<String> bookedTimes = new HashSet<>();
                    for (Appointment appointment : appointments) {
                        bookedTimes.add(timeFormat.format(appointment.startDate));
                    }

                    // Convert WorkSchedule to TimeSlot and mark booked ones as disabled
                    List<TimeSlot> availableSlots = new ArrayList<>();
                    for (WorkSchedule schedule : schedules) {
                        TimeSlot slot = new TimeSlot(schedule.startTime, schedule.endTime);
                        if (bookedTimes.contains(schedule.startTime)) {
                            slot.setDisabled(true);
                        }
                        availableSlots.add(slot);
                    }

                    if (availableSlots.isEmpty()) {
                        txtNoSlots.setVisibility(View.VISIBLE);
                        rvTimeSlots.setVisibility(View.GONE);
                    } else {
                        txtNoSlots.setVisibility(View.GONE);
                        rvTimeSlots.setVisibility(View.VISIBLE);
                    }

                    timeSlotAdapter.setTimeSlots(availableSlots);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupListeners() {
        btnBookAppointment.setOnClickListener(v -> bookAppointment());
        
        // Load patientId when view is ready
        loadPatientId();
    }

    private void loadPatientId() {
        long userId = AuthUtils.getUserId(requireContext());
        if (userId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Patient patient = patientDao.findByUserId((int) userId);
                if (patient != null) {
                    patientId = patient.id;
                }
            });
        }
    }

    private void bookAppointment() {
        if (doctorId == -1) {
            Toasty.error(requireContext(), "Không có thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get patientId if not loaded yet
        if (patientId == 0) {
            long userId = AuthUtils.getUserId(requireContext());
            if (userId != -1) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Patient patient = patientDao.findByUserId((int) userId);
                    if (patient != null) {
                        patientId = patient.id;
                        requireActivity().runOnUiThread(() -> {
                            bookAppointmentInternal();
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            Toasty.error(requireContext(), "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                Toasty.error(requireContext(), "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        bookAppointmentInternal();
    }

    private void bookAppointmentInternal() {
        if (patientId == 0) {
            Toasty.error(requireContext(), "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlot selectedSlot = timeSlotAdapter.getSelectedSlot();
        if (selectedSlot == null) {
            Toasty.warning(requireContext(), "Vui lòng chọn khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSlot.isDisabled()) {
            Toasty.error(requireContext(), "Khung giờ này đã được đặt", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create appointment
        try {
            // Parse selected date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(dateFormat.parse(selectedDate));
            
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(timeFormat.parse(selectedSlot.getStartTime()));
            startCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            startCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
            
            Calendar endCalendar = (Calendar) startCalendar.clone();
            timeCalendar.setTime(timeFormat.parse(selectedSlot.getEndTime()));
            endCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            endCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

            Appointment appointment = new Appointment();
            appointment.doctorId = doctorId;
            appointment.patientId = patientId;
            appointment.createdAt = new java.util.Date();
            appointment.startDate = startCalendar.getTime();
            appointment.endDate = endCalendar.getTime();
            appointment.description = edtDescription.getText().toString().trim();
            appointment.status = Enum.AppointmentStatus.PENDING;

            // Save appointment
            Executors.newSingleThreadExecutor().execute(() -> {
                long appointmentId = appointmentRepository.create(appointment);
                requireActivity().runOnUiThread(() -> {
                    if (appointmentId > 0) {
                        Toasty.success(requireContext(), 
                                "Đặt lịch hẹn thành công!", 
                                Toast.LENGTH_SHORT).show();
                        
                        // Clear selection and reload slots
                        timeSlotAdapter.setTimeSlots(new ArrayList<>());
                        edtDescription.setText("");
                        loadAvailableTimeSlots();
                    } else {
                        Toasty.error(requireContext(), 
                                "Đặt lịch hẹn thất bại", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.error(requireContext(), "Lỗi khi đặt lịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
