package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import data.model.Doctor;

@Dao
public interface DoctorDao {

    @Insert
    long insert(Doctor doctor);

    @Update
    void update(Doctor doctor);

    @Delete
    void delete(Doctor doctor);

    @Query("SELECT * FROM doctors ORDER BY id ASC")
    List<Doctor> getAll();

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    Doctor findById(int id);

    @Query("SELECT * FROM doctors WHERE userId = :userId LIMIT 1")
    Doctor findByUserId(int userId);
}
