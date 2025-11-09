package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import data.model.ServiceExaminationForm;

@Dao
public interface ServiceExaminationFormDao {
    @Insert
    void insert(ServiceExaminationForm serviceExaminationForm);

    @Insert
    void insertAll(List<ServiceExaminationForm> serviceExaminationForms);

    @Query("SELECT * FROM serviceExaminationForms WHERE examinationId = :examinationId")
    List<ServiceExaminationForm> findByExaminationId(long examinationId);

    @Query("SELECT * FROM serviceExaminationForms WHERE appointmentId = :appointmentId")
    List<ServiceExaminationForm> findByAppointmentId(long appointmentId);

    @Query("DELETE FROM serviceExaminationForms WHERE examinationId = :examinationId")
    void deleteByExaminationId(long examinationId);
}
