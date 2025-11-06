package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import data.model.Patient;

@Dao
public interface PatientDao {

    @Insert
    long insert(Patient patient);

    @Update
    void update(Patient patient);

    @Delete
    void delete(Patient patient);

    @Query("SELECT * FROM patients ORDER BY id ASC")
    List<Patient> getAll();

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    Patient findById(int id);

    @Query("SELECT * FROM patients WHERE userId = :userId LIMIT 1")
    Patient findByUserId(int userId);

    @Query("SELECT * FROM patients WHERE code = :code LIMIT 1")
    Patient findByCode(String code);
}
