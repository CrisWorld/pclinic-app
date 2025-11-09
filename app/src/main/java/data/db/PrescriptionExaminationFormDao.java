package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import data.model.PrescriptionExaminationForm;

@Dao
public interface PrescriptionExaminationFormDao {
    @Insert
    void insert(PrescriptionExaminationForm prescriptionExaminationForm);

    @Insert
    void insertAll(List<PrescriptionExaminationForm> prescriptionExaminationForms);

    @Query("SELECT * FROM prescription_examinationForms WHERE examinationId = :examinationId")
    List<PrescriptionExaminationForm> findByExaminationId(long examinationId);

    @Query("SELECT * FROM prescription_examinationForms WHERE appointmentId = :appointmentId")
    List<PrescriptionExaminationForm> findByAppointmentId(long appointmentId);

    @Query("DELETE FROM prescription_examinationForms WHERE examinationId = :examinationId")
    void deleteByExaminationId(long examinationId);
}

