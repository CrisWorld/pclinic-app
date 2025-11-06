package di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
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
import data.db.DoctorDao;
import data.db.PatientDao;
import data.db.PrescriptionDao;
import data.db.ServiceDao;
import data.db.UserDao;
import data.model.Admin;
import data.model.Doctor;
import data.model.Patient;
import data.model.Prescription;
import data.model.Service;
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
        });
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
