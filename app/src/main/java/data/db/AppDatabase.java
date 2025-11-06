package data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import data.converters.Converters;
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

@Database(entities = {
        User.class,
        Patient.class,
        Doctor.class,
        Admin.class,
        Appointment.class,
        ExaminationForm.class,
        Prescription.class,
        PrescriptionExaminationForm.class,
        Review.class,
        Service.class,
        ServiceExaminationForm.class
}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PatientDao patientDao();
    public abstract AdminDao adminDao();
    public abstract DoctorDao doctorDao();
    public abstract PrescriptionDao prescriptionDao();
    public abstract ServiceDao serviceDao();
}
