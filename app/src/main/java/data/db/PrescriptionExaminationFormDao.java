package data.db;

import androidx.room.Dao;import androidx.room.Insert;
import data.model.PrescriptionExaminationForm;

@Dao
public interface PrescriptionExaminationFormDao {
    @Insert
    void insert(PrescriptionExaminationForm prescriptionExaminationForm);
}
