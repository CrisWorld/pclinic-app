package data.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import data.db.AppointmentDao;
import data.enums.Enum;
import data.model.Appointment;

@Singleton
public class AppointmentRepository {

    private final AppointmentDao appointmentDao;

    @Inject
    public AppointmentRepository(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    public long create(Appointment appointment) {
        return appointmentDao.insert(appointment);
    }

    public void update(Appointment appointment) {
        Executors.newSingleThreadExecutor().execute(() -> {
            appointmentDao.update(appointment);
        });
    }

    public void delete(Appointment appointment) {
        Executors.newSingleThreadExecutor().execute(() -> {
            appointmentDao.delete(appointment);
        });
    }

    public MutableLiveData<List<Appointment>> getUpcomingByDoctor(long doctorId) {
        MutableLiveData<List<Appointment>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 30); // Next 30 days
            Date thirtyDaysLater = calendar.getTime();
            
            List<Appointment> appointments = appointmentDao.findUpcomingByDoctor(doctorId, now, thirtyDaysLater);
            result.postValue(appointments);
        });
        return result;
    }

    public MutableLiveData<Integer> getCompletedCount(long doctorId) {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = appointmentDao.countByDoctorAndStatus(doctorId, Enum.AppointmentStatus.DONE);
            result.postValue(count);
        });
        return result;
    }

    public MutableLiveData<Integer> getCancelledCount(long doctorId) {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = appointmentDao.countByDoctorAndStatus(doctorId, Enum.AppointmentStatus.ABSENT);
            result.postValue(count);
        });
        return result;
    }

    public MutableLiveData<List<Appointment>> getConfirmedUpcoming(long doctorId) {
        MutableLiveData<List<Appointment>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Date now = new Date();
            List<Appointment> appointments = appointmentDao.findByDoctorAndStatus(
                doctorId, 
                Enum.AppointmentStatus.CONFIRMED, 
                now
            );
            result.postValue(appointments);
        });
        return result;
    }

    public MutableLiveData<Appointment> findById(long id) {
        MutableLiveData<Appointment> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Appointment appointment = appointmentDao.findById(id);
            result.postValue(appointment);
        });
        return result;
    }

    public MutableLiveData<List<Appointment>> findByPatient(long patientId) {
        MutableLiveData<List<Appointment>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Appointment> appointments = appointmentDao.findByPatient(patientId);
            result.postValue(appointments);
        });
        return result;
    }

    public MutableLiveData<List<Appointment>> getAppointmentsByDoctorAndDate(long doctorId, String date) {
        MutableLiveData<List<Appointment>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Appointment> appointments = appointmentDao.findByDoctorAndDate(doctorId, date);
            result.postValue(appointments);
        });
        return result;
    }

    // Synchronous methods for direct use
    public List<Appointment> getAppointmentsByDoctorAndDateSync(long doctorId, String date) {
        return appointmentDao.findByDoctorAndDate(doctorId, date);
    }

    public void updateSync(Appointment appointment) {
        appointmentDao.update(appointment);
    }
}
