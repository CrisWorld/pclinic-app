package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import data.model.ExaminationForm;

@Dao
public interface ExaminationFormDao {

    @Insert
    long insert(ExaminationForm form);

    @Query("SELECT * FROM examinationForms WHERE appointmentId = :appointmentId LIMIT 1")
    ExaminationForm findByAppointmentId(long appointmentId);
}
