package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import data.model.ServiceExaminationForm;

@Dao
public interface ServiceExaminationFormDao {
    @Insert
    void insert(ServiceExaminationForm serviceExaminationForm);
}
