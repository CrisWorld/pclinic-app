package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import data.model.ExaminationForm;

@Dao
public interface ExaminationFormDao {

    @Insert
    long insert(ExaminationForm examinationForm);

    @Update
    void update(ExaminationForm examinationForm);

    @Query("SELECT * FROM examinationForms WHERE id = :id LIMIT 1")
    ExaminationForm findById(long id);

    @Query("SELECT * FROM examinationForms WHERE appointmentId = :appointmentId LIMIT 1")
    ExaminationForm findByAppointmentId(long appointmentId);

    @Query("SELECT * FROM examinationForms WHERE patientId = :patientId ORDER BY examinationDate DESC")
    List<ExaminationForm> findByPatientId(long patientId);

    @Query("SELECT * FROM examinationForms WHERE doctorId = :doctorId ORDER BY examinationDate DESC")
    List<ExaminationForm> findByDoctorId(long doctorId);
}
