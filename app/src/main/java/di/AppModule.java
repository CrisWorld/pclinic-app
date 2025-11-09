package di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import data.db.AdminDao;
import data.db.AppDatabase;
import data.db.AppointmentDao;
import data.db.DoctorDao;
import data.db.ExaminationFormDao;
import data.db.PatientDao;
import data.db.PrescriptionDao;
import data.db.PrescriptionExaminationFormDao;
import data.db.ReviewDao;
import data.db.ServiceDao;
import data.db.ServiceExaminationFormDao;
import data.db.UserDao;
import data.db.WorkScheduleDao;
import data.db.admin.AdminDoctorDao;
import data.db.admin.AdminPrescriptionDao;
import data.db.admin.AdminServiceDao;
import data.enums.Enum;
import data.model.Admin;
import data.model.Appointment;
import data.model.Doctor;
import data.model.ExaminationForm;
import data.model.Patient;
import data.model.Prescription;
import data.model.PrescriptionExaminationForm;
import data.model.Review;
import data.model.Service;
import data.model.ServiceExaminationForm;
import data.model.User;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        AppDatabase db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "pclinic_database"
                )
                .fallbackToDestructiveMigration()
                .build();

        // Seed sau khi tạo DB
        Executors.newSingleThreadExecutor().execute(() -> seedDatabase(db));

        return db;
    }

    @Provides
    @Singleton
    public UserDao provideUserDao(AppDatabase db) {
        return db.userDao();
    }


    @Provides
    @Singleton
    public PatientDao providePatientDao(AppDatabase db) {
        return db.patientDao();
    }

    @Provides
    @Singleton
    public AdminDao provideAdminDao(AppDatabase db) {
        return db.adminDao();
    }

    @Provides
    @Singleton
    public DoctorDao provideDoctorDao(AppDatabase db) {
        return db.doctorDao();
    }

    @Provides
    @Singleton
    public PrescriptionDao providePrescriptionDao(AppDatabase db) {
        return db.prescriptionDao();
    }

    @Provides
    @Singleton
    public ServiceDao provideServiceDao(AppDatabase db) {
        return db.serviceDao();
    }

    @Provides
    @Singleton
    public AppointmentDao provideAppointmentDao(AppDatabase db) {
        return db.appointmentDao();
    }

    @Provides
    @Singleton
    public ReviewDao provideReviewDao(AppDatabase db) {
        return db.reviewDao();
    }

    @Provides
    @Singleton
    public WorkScheduleDao provideWorkScheduleDao(AppDatabase db) {
        return db.workScheduleDao();
    }

    @Provides
    @Singleton
    public AdminDoctorDao provideAdminDoctorDao(AppDatabase db) {
        return db.adminDoctorDao();
    }

    @Provides
    @Singleton
    public AdminPrescriptionDao provideAdminPrescriptionDao(AppDatabase db) {
        return db.adminPrescriptionDao();
    }

    @Provides
    @Singleton
    public AdminServiceDao provideAdminServiceDao(AppDatabase db) {
        return db.adminServiceDao();
    }

    @Provides
    @Singleton
    public ExaminationFormDao provideExaminationFormDao(AppDatabase db) {
        return db.examinationFormDao();
    }

    @Provides
    @Singleton
    public ServiceExaminationFormDao provideServiceExaminationFormDao(AppDatabase db) {
        return db.serviceExaminationFormDao();
    }

    @Provides
    @Singleton
    public PrescriptionExaminationFormDao providePrescriptionExaminationFormDao(AppDatabase db) {
        return db.prescriptionExaminationFormDao();
    }

    // custom
    public void seedDatabase(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // check
            User userFound = db.userDao().findByEmail("admin@pclinic.com");
            if(userFound != null) return;
                // Admin user
            User adminUser = new User();
            adminUser.fullName = "Admin";
            adminUser.email = "admin@pclinic.com";
            adminUser.password = "Admin@123";
            adminUser.role = data.enums.Enum.UserRole.ADMIN;
            long adminUserId = db.userDao().insert(adminUser);
            db.adminDao().insert(new Admin(adminUserId));

            // Doctor user
            User doctorUser = new User();
            doctorUser.fullName = "Dr. John Doe";
            doctorUser.email = "doctor@pclinic.com";
            doctorUser.password = "Doctor@123";
            doctorUser.role = data.enums.Enum.UserRole.DOCTOR;
            long doctorUserId = db.userDao().insert(doctorUser);

            Doctor doctor = new Doctor(doctorUserId);
            doctor.bio = "Chuyên khoa nội tổng quát";
            db.doctorDao().insert(doctor);

            // Patient users - Multiple patients for testing
            List<Long> patientUserIds = new ArrayList<>();
            List<Long> patientIds = new ArrayList<>();
            
            // Patient 1
            User patientUser1 = new User();
            patientUser1.fullName = "Nguyễn Văn A";
            patientUser1.email = "patient1@pclinic.com";
            patientUser1.password = "Patient@123";
            patientUser1.role = data.enums.Enum.UserRole.PATIENT;
            patientUser1.phone = "0785771092";
            patientUser1.gender = "MALE";
            Calendar birthDate1 = Calendar.getInstance();
            birthDate1.set(1995, Calendar.JANUARY, 15);
            patientUser1.birthDate = birthDate1.getTimeInMillis();
            patientUser1.address = "24 Nam Kỳ Khởi Nghĩa, Hòa Hải, TP Đà Nẵng";
            long patientUserId1 = db.userDao().insert(patientUser1);
            Patient patient1 = new Patient(patientUserId1);
            long patientId1 = db.patientDao().insert(patient1);
            patientUserIds.add(patientUserId1);
            patientIds.add(patientId1);
            
            // Patient 2
            User patientUser2 = new User();
            patientUser2.fullName = "Trần Thị B";
            patientUser2.email = "patient2@pclinic.com";
            patientUser2.password = "Patient@123";
            patientUser2.role = data.enums.Enum.UserRole.PATIENT;
            patientUser2.phone = "0912345678";
            patientUser2.gender = "FEMALE";
            Calendar birthDate2 = Calendar.getInstance();
            birthDate2.set(1990, Calendar.MARCH, 20);
            patientUser2.birthDate = birthDate2.getTimeInMillis();
            patientUser2.address = "123 Lê Duẩn, Hải Châu, TP Đà Nẵng";
            long patientUserId2 = db.userDao().insert(patientUser2);
            Patient patient2 = new Patient(patientUserId2);
            long patientId2 = db.patientDao().insert(patient2);
            patientUserIds.add(patientUserId2);
            patientIds.add(patientId2);
            
            // Patient 3
            User patientUser3 = new User();
            patientUser3.fullName = "Lê Văn C";
            patientUser3.email = "patient3@pclinic.com";
            patientUser3.password = "Patient@123";
            patientUser3.role = data.enums.Enum.UserRole.PATIENT;
            patientUser3.phone = "0987654321";
            patientUser3.gender = "MALE";
            Calendar birthDate3 = Calendar.getInstance();
            birthDate3.set(1988, Calendar.JULY, 10);
            patientUser3.birthDate = birthDate3.getTimeInMillis();
            patientUser3.address = "456 Nguyễn Văn Linh, Thanh Khê, TP Đà Nẵng";
            long patientUserId3 = db.userDao().insert(patientUser3);
            Patient patient3 = new Patient(patientUserId3);
            long patientId3 = db.patientDao().insert(patient3);
            patientUserIds.add(patientUserId3);
            patientIds.add(patientId3);
            
            // Patient 4
            User patientUser4 = new User();
            patientUser4.fullName = "Phạm Thị D";
            patientUser4.email = "patient4@pclinic.com";
            patientUser4.password = "Patient@123";
            patientUser4.role = data.enums.Enum.UserRole.PATIENT;
            patientUser4.phone = "0901234567";
            patientUser4.gender = "FEMALE";
            Calendar birthDate4 = Calendar.getInstance();
            birthDate4.set(1992, Calendar.NOVEMBER, 5);
            patientUser4.birthDate = birthDate4.getTimeInMillis();
            patientUser4.address = "789 Trần Phú, Sơn Trà, TP Đà Nẵng";
            long patientUserId4 = db.userDao().insert(patientUser4);
            Patient patient4 = new Patient(patientUserId4);
            long patientId4 = db.patientDao().insert(patient4);
            patientUserIds.add(patientUserId4);
            patientIds.add(patientId4);
            
            // Patient 5
            User patientUser5 = new User();
            patientUser5.fullName = "Hoàng Văn E";
            patientUser5.email = "patient5@pclinic.com";
            patientUser5.password = "Patient@123";
            patientUser5.role = data.enums.Enum.UserRole.PATIENT;
            patientUser5.phone = "0923456789";
            patientUser5.gender = "MALE";
            Calendar birthDate5 = Calendar.getInstance();
            birthDate5.set(1998, Calendar.APRIL, 25);
            patientUser5.birthDate = birthDate5.getTimeInMillis();
            patientUser5.address = "321 Hoàng Diệu, Liên Chiểu, TP Đà Nẵng";
            long patientUserId5 = db.userDao().insert(patientUser5);
            Patient patient5 = new Patient(patientUserId5);
            long patientId5 = db.patientDao().insert(patient5);
            patientUserIds.add(patientUserId5);
            patientIds.add(patientId5);

            List<Prescription> prescriptions = Arrays.asList(
                    createPrescription("Paracetamol 500mg", 12000, "PRC001"),
                    createPrescription("Amoxicillin 500mg", 25000, "AMX002"),
                    createPrescription("Vitamin C 1000mg", 15000, "VIT003"),
                    createPrescription("Ibuprofen 200mg", 18000, "IBU004"),
                    createPrescription("Cefuroxime 250mg", 35000, "CEF005"),
                    createPrescription("Loratadine 10mg", 20000, "LOR006"),
                    createPrescription("Omeprazole 20mg", 22000, "OME007")
            );
            db.prescriptionDao().insertAll(prescriptions);

            List<Service> services = new ArrayList<>();
            services.add(createService("Khám tổng quát", 150000, "SRV001"));
            services.add(createService("Xét nghiệm máu", 250000, "SRV002"));
            services.add(createService("Siêu âm bụng", 300000, "SRV003"));
            services.add(createService("Chụp X-quang", 200000, "SRV004"));
            services.add(createService("Khám tai mũi họng", 180000, "SRV005"));
            db.serviceDao().insertAll(services);

            // Seed Appointments for testing
            long doctorId = 1; // First doctor
            Calendar calendar = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            // ========== TODAY'S APPOINTMENTS ==========
            // Today - PENDING appointments (Đang chờ)
            calendar.setTime(today.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 30);
            Appointment todayPending1 = createAppointment(doctorId, patientIds.get(0), calendar.getTime(), 
                "Bệnh nhân đăng ký khám tổng quát nhằm kiểm tra toàn diện về sức khỏe, bao gồm các xét nghiệm và kiểm tra lâm sàng", 
                Enum.AppointmentStatus.PENDING);
            db.appointmentDao().insert(todayPending1);
            
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 30);
            Appointment todayPending2 = createAppointment(doctorId, patientIds.get(1), calendar.getTime(), 
                "Khám sức khỏe định kỳ, kiểm tra huyết áp và tim mạch", 
                Enum.AppointmentStatus.PENDING);
            db.appointmentDao().insert(todayPending2);
            
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 0);
            Appointment todayPending3 = createAppointment(doctorId, patientIds.get(2), calendar.getTime(), 
                "Tư vấn về chế độ dinh dưỡng và tập luyện", 
                Enum.AppointmentStatus.PENDING);
            db.appointmentDao().insert(todayPending3);
            
            // Today - CONFIRMED appointments (Đang chờ - đã xác nhận)
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            calendar.set(Calendar.MINUTE, 0);
            Appointment todayConfirmed1 = createAppointment(doctorId, patientIds.get(3), calendar.getTime(), 
                "Khám bệnh về đường hô hấp, ho khan kéo dài", 
                Enum.AppointmentStatus.CONFIRMED);
            db.appointmentDao().insert(todayConfirmed1);
            
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            calendar.set(Calendar.MINUTE, 30);
            Appointment todayConfirmed2 = createAppointment(doctorId, patientIds.get(4), calendar.getTime(), 
                "Tái khám sau điều trị, kiểm tra tiến triển bệnh", 
                Enum.AppointmentStatus.CONFIRMED);
            db.appointmentDao().insert(todayConfirmed2);
            
            // Today - DONE appointments (Đã khám xong)
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 30);
            Appointment todayDone1 = createAppointment(doctorId, patientIds.get(0), calendar.getTime(), 
                "Khám tổng quát đã hoàn thành, bệnh nhân khỏe mạnh", 
                Enum.AppointmentStatus.DONE);
            todayDone1.checkInDate = calendar.getTime();
            db.appointmentDao().insert(todayDone1);
            
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            calendar.set(Calendar.MINUTE, 0);
            Appointment todayDone2 = createAppointment(doctorId, patientIds.get(1), calendar.getTime(), 
                "Khám và kê đơn thuốc điều trị cảm cúm", 
                Enum.AppointmentStatus.DONE);
            todayDone2.checkInDate = calendar.getTime();
            db.appointmentDao().insert(todayDone2);
            
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            calendar.set(Calendar.MINUTE, 30);
            Appointment todayDone3 = createAppointment(doctorId, patientIds.get(2), calendar.getTime(), 
                "Khám da liễu, điều trị mụn trứng cá", 
                Enum.AppointmentStatus.DONE);
            todayDone3.checkInDate = calendar.getTime();
            db.appointmentDao().insert(todayDone3);
            
            // Today - ABSENT appointments (Vắng mặt)
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            calendar.set(Calendar.MINUTE, 0);
            Appointment todayAbsent1 = createAppointment(doctorId, patientIds.get(3), calendar.getTime(), 
                "Bệnh nhân không đến khám theo lịch hẹn", 
                Enum.AppointmentStatus.ABSENT);
            db.appointmentDao().insert(todayAbsent1);
            
            // ========== UPCOMING APPOINTMENTS (Future) ==========
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            Appointment apt1 = createAppointment(doctorId, patientIds.get(0), calendar.getTime(), "Khám định kỳ", Enum.AppointmentStatus.CONFIRMED);
            db.appointmentDao().insert(apt1);
            
            calendar.add(Calendar.DAY_OF_MONTH, 2);
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            Appointment apt2 = createAppointment(doctorId, patientIds.get(1), calendar.getTime(), "Tái khám", Enum.AppointmentStatus.CONFIRMED);
            db.appointmentDao().insert(apt2);
            
            calendar.add(Calendar.DAY_OF_MONTH, 2);
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            Appointment apt3 = createAppointment(doctorId, patientIds.get(2), calendar.getTime(), "Xét nghiệm máu", Enum.AppointmentStatus.CONFIRMED);
            db.appointmentDao().insert(apt3);
            
            // ========== HISTORY APPOINTMENTS (Past) ==========
            // Completed appointments for history
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            for (int i = 0; i < 5; i++) {
                long patientId = patientIds.get(i % patientIds.size());
                Appointment completed = createAppointment(doctorId, patientId, calendar.getTime(), 
                    "Khám hoàn thành - " + (i + 1), Enum.AppointmentStatus.DONE);
                completed.checkInDate = calendar.getTime();
                db.appointmentDao().insert(completed);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                calendar.set(Calendar.HOUR_OF_DAY, 9 + (i % 4));
            }
            
            // More history - 2 weeks ago
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -14);
            for (int i = 0; i < 10; i++) {
                long patientId = patientIds.get(i % patientIds.size());
                Appointment completed = createAppointment(doctorId, patientId, calendar.getTime(), 
                    "Khám hoàn thành - Lịch sử", Enum.AppointmentStatus.DONE);
                completed.checkInDate = calendar.getTime();
                db.appointmentDao().insert(completed);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                calendar.set(Calendar.HOUR_OF_DAY, 8 + (i % 5));
            }
            
            // History - PENDING appointments (past)
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -5);
            for (int i = 0; i < 3; i++) {
                long patientId = patientIds.get(i % patientIds.size());
                Appointment pending = createAppointment(doctorId, patientId, calendar.getTime(), 
                    "Lịch hẹn đã qua - PENDING", Enum.AppointmentStatus.PENDING);
                db.appointmentDao().insert(pending);
                calendar.add(Calendar.DAY_OF_MONTH, -2);
            }
            
            // History - ABSENT appointments
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -10);
            for (int i = 0; i < 5; i++) {
                long patientId = patientIds.get(i % patientIds.size());
                Appointment absent = createAppointment(doctorId, patientId, calendar.getTime(), 
                    "Bệnh nhân vắng mặt - Lịch sử", Enum.AppointmentStatus.ABSENT);
                db.appointmentDao().insert(absent);
                calendar.add(Calendar.DAY_OF_MONTH, -2);
            }
            
            // Seed Reviews for testing
            Review review1 = createReview(doctorId, patientIds.get(0), 1, 5, "Bác sĩ rất tận tình và chu đáo. Giải thích kỹ càng!");
            Review review2 = createReview(doctorId, patientIds.get(1), 2, 4, "Khám bệnh tốt, thời gian chờ hơi lâu");
            Review review3 = createReview(doctorId, patientIds.get(2), 3, 5, "Rất hài lòng với dịch vụ");
            Review review4 = createReview(doctorId, patientIds.get(3), 4, 5, "Bác sĩ chuyên nghiệp");
            Review review5 = createReview(doctorId, patientIds.get(4), 5, 4, "Tốt, sẽ quay lại");
            
            db.reviewDao().insert(review1);
            db.reviewDao().insert(review2);
            db.reviewDao().insert(review3);
            db.reviewDao().insert(review4);
            db.reviewDao().insert(review5);
        });
    }

    private Appointment createAppointment(long doctorId, long patientId, Date startDate, String description, Enum.AppointmentStatus status) {
        Appointment apt = new Appointment();
        apt.doctorId = doctorId;
        apt.patientId = patientId;
        apt.createdAt = new Date();
        apt.startDate = startDate;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 1);
        apt.endDate = calendar.getTime();

        apt.description = description;
        apt.status = status;
        return apt;
    }

    private Review createReview(long doctorId, long patientId, long appointmentId, int rating, String description) {
        Review review = new Review();
        review.doctorId = doctorId;
        review.patientId = patientId;
        review.appointmentId = appointmentId;
        review.rating = rating;
        review.description = description;
        return review;
    }

    private Prescription createPrescription(String name, double price, String code) {
        Prescription p = new Prescription();
        p.name = name;
        p.price = price;
        p.code = code;
        return p;
    }

    private Service createService(String name, double price, String code) {
        Service s = new Service();
        s.name = name;
        s.price = price;
        s.code = code;
        return s;
    }

}
