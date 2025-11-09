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
    public PrescriptionExaminationFormDao providePrescriptionExaminationFormDao(AppDatabase db) {
        return db.prescriptionExaminationFormDao();
    }

    @Provides
    @Singleton
    public ServiceExaminationFormDao provideServiceExaminationFormDao(AppDatabase db) {
        return db.serviceExaminationFormDao();
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

            // Patient user
            User patientUser = new User();
            patientUser.fullName = "Nguyen Van A";
            patientUser.email = "patient@pclinic.com";
            patientUser.password = "Patient@123";
            patientUser.role = data.enums.Enum.UserRole.PATIENT;
            long patientUserId = db.userDao().insert(patientUser);
            db.patientDao().insert(new Patient(patientUserId));

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
            long patientId = 1; // First patient

            Calendar calendar = Calendar.getInstance();

            // Upcoming appointment 1 - Tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            Appointment apt1 = createAppointment(doctorId, patientId, calendar.getTime(), "Khám định kỳ", Enum.AppointmentStatus.CONFIRMED);

            // Upcoming appointment 2 - In 3 days
            calendar.add(Calendar.DAY_OF_MONTH, 2);
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            Appointment apt2 = createAppointment(doctorId, patientId, calendar.getTime(), "Tái khám", Enum.AppointmentStatus.CONFIRMED);

            // Upcoming appointment 3 - In 5 days
            calendar.add(Calendar.DAY_OF_MONTH, 2);
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            Appointment apt3 = createAppointment(doctorId, patientId, calendar.getTime(), "Xét nghiệm máu", Enum.AppointmentStatus.CONFIRMED);
            // === SEED DATA FOR HISTORY & PRESCRIPTION TEST ===
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -7); // 1 tuần trước
            Appointment completedAppointmentForTest = createAppointment(doctorId, patientId, calendar.getTime(), "Đau đầu, cảm cúm", Enum.AppointmentStatus.DONE);
            long completedAppointmentId = db.appointmentDao().insert(completedAppointmentForTest);

            // Tạo phiếu khám cho cuộc hẹn đã hoàn thành này
            ExaminationForm form = new ExaminationForm();
            form.appointmentId = completedAppointmentId;
            form.patientId = patientId;
            form.doctorId = doctorId;
            form.diagnosis = "Cảm cúm siêu vi";
            form.medicalHistory = "Bệnh nhân không có tiền sử bệnh lý đặc biệt.";
            form.examinationDate = calendar.getTime();
            form.grandTotal = 250000 + 12000 + 25000; // Tổng tiền tạm tính
            long examinationId = db.examinationFormDao().insert(form);

            // Thêm 1 dịch vụ vào phiếu khám
            ServiceExaminationForm sef = new ServiceExaminationForm();
            sef.examinationId = examinationId;
            sef.serviceId = 2; // Xét nghiệm máu
            sef.price = 250000;
            sef.appointmentId = completedAppointmentId;
            sef.patientId = patientId;
            sef.doctorId = doctorId;
            db.serviceExaminationFormDao().insert(sef);

            // Thêm 2 loại thuốc vào phiếu khám
            PrescriptionExaminationForm pef1 = new PrescriptionExaminationForm();
            pef1.examinationId = examinationId;
            pef1.prescriptionId = 1; // Paracetamol 500mg
            pef1.price = 12000;
            pef1.appointmentId = completedAppointmentId;
            pef1.patientId = patientId;
            pef1.doctorId = doctorId;
            db.prescriptionExaminationFormDao().insert(pef1);

            PrescriptionExaminationForm pef2 = new PrescriptionExaminationForm();
            pef2.examinationId = examinationId;
            pef2.prescriptionId = 2; // Amoxicillin 500mg
            pef2.price = 25000;
            pef2.appointmentId = completedAppointmentId;
            pef2.patientId = patientId;
            pef2.doctorId = doctorId;
            db.prescriptionExaminationFormDao().insert(pef2);
            // === END OF SEED DATA FOR TEST ===

            // Completed appointments for stats
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            for (int i = 0; i < 15; i++) {
                Appointment completed = createAppointment(doctorId, patientId, calendar.getTime(), "Khám hoàn thành", Enum.AppointmentStatus.DONE);
                db.appointmentDao().insert(completed);
                calendar.add(Calendar.DAY_OF_MONTH, -2);
            }

            // Cancelled appointments for stats
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -20);
            for (int i = 0; i < 3; i++) {
                Appointment cancelled = createAppointment(doctorId, patientId, calendar.getTime(), "Bệnh nhân hủy", Enum.AppointmentStatus.ABSENT);
                db.appointmentDao().insert(cancelled);
                calendar.add(Calendar.DAY_OF_MONTH, -3);
            }

            db.appointmentDao().insert(apt1);
            db.appointmentDao().insert(apt2);
            db.appointmentDao().insert(apt3);

            // Seed Reviews for testing
            Review review1 = createReview(doctorId, patientId, 1, 5, "Bác sĩ rất tận tình và chu đáo. Giải thích kỹ càng!");
            Review review2 = createReview(doctorId, patientId, 2, 4, "Khám bệnh tốt, thời gian chờ hơi lâu");
            Review review3 = createReview(doctorId, patientId, 3, 5, "Rất hài lòng với dịch vụ");
            Review review4 = createReview(doctorId, patientId, 4, 5, "Bác sĩ chuyên nghiệp");
            Review review5 = createReview(doctorId, patientId, 5, 4, "Tốt, sẽ quay lại");

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
