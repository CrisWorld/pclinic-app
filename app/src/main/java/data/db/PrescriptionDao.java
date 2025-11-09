package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

import data.dto.PrescriptionDetailDto;
import data.model.Prescription;

@Dao
public interface PrescriptionDao {

    @Insert
    long insert(Prescription prescription);

    @Insert
    void insertAll(List<Prescription> prescriptions);

    @Update
    void update(Prescription prescription);

    @Delete
    void delete(Prescription prescription);

    @Query("SELECT * FROM prescriptions ORDER BY name ASC")
    List<Prescription> getAll();

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    Prescription findById(long id);

    @Query("SELECT COUNT(*) FROM prescriptions")
    int count();

    @Query("SELECT " +
            "p.name AS prescriptionName, " +
            "p.code AS prescriptionCode, " +
            "pef.price AS price " +
            "FROM prescription_examinationForms pef " +
            "JOIN prescriptions p ON pef.prescriptionId = p.id " +
            "WHERE pef.examinationId = :examinationId")
    List<PrescriptionDetailDto> findByExaminationId(long examinationId);

    @Query("SELECT SUM(price) FROM prescription_examinationForms WHERE examinationId = :examinationId")
    double sumPriceByExaminationId(long examinationId);
}
