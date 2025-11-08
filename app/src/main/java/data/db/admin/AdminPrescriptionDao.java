package data.db.admin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

import data.model.Prescription;

@Dao
public interface AdminPrescriptionDao {

    @Insert
    long insert(Prescription prescription);

    @Insert
    void insertAll(List<Prescription> prescriptions);

    @Update
    void update(Prescription prescription);

    @Delete
    void delete(Prescription prescription);

    // Sửa thành LiveData
    @Query("SELECT * FROM prescriptions ORDER BY name ASC")
    LiveData<List<Prescription>> getAll();

    // Thêm hàm tìm kiếm
    @Query("SELECT * FROM prescriptions WHERE name LIKE '%' || :keyword || '%' OR code LIKE '%' || :keyword || '%' ORDER BY name ASC")
    LiveData<List<Prescription>> search(String keyword);

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    Prescription findById(long id);

    @Query("SELECT COUNT(*) FROM prescriptions")
    int count();
}
