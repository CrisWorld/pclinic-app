package data.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import data.db.PatientDao;
import data.db.UserDao;
import data.model.Patient;
import data.model.User;

@Singleton
public class PatientRepository {

    private final PatientDao patientDao;
    private final UserDao userDao;

    @Inject
    public PatientRepository(PatientDao patientDao, UserDao userDao) {
        this.patientDao = patientDao;
        this.userDao = userDao;
    }

    public MutableLiveData<Patient> findById(long patientId) {
        MutableLiveData<Patient> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Patient patient = patientDao.findById(patientId);
            result.postValue(patient);
        });
        return result;
    }

    public MutableLiveData<User> getUserByPatientId(long patientId) {
        MutableLiveData<User> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Patient patient = patientDao.findById(patientId);
            if (patient != null) {
                User user = userDao.findById(patient.userId);
                result.postValue(user);
            } else {
                result.postValue(null);
            }
        });
        return result;
    }

    // Synchronous method for direct use
    public User getUserByPatientIdSync(long patientId) {
        Patient patient = patientDao.findById(patientId);
        if (patient != null) {
            return userDao.findById(patient.userId);
        }
        return null;
    }
}
