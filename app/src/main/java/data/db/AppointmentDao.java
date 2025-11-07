package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import data.enums.Enum;
import data.model.Appointment;

@Dao
public interface AppointmentDao {

    @Insert
    long insert(Appointment appointment);

    @Update
    void update(Appointment appointment);

    @Delete
    void delete(Appointment appointment);

    @Query("SELECT * FROM appointments ORDER BY id ASC")
    List<Appointment> getAll();

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    Appointment findById(long id);

    // Doctor-specific queries for Overview
    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND status = :status AND startDate >= :fromDate ORDER BY startDate ASC")
    List<Appointment> findByDoctorAndStatus(long doctorId, Enum.AppointmentStatus status, Date fromDate);

    @Query("SELECT COUNT(*) FROM appointments WHERE doctorId = :doctorId AND status = :status")
    int countByDoctorAndStatus(long doctorId, Enum.AppointmentStatus status);

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND startDate >= :fromDate AND startDate <= :toDate ORDER BY startDate ASC")
    List<Appointment> findUpcomingByDoctor(long doctorId, Date fromDate, Date toDate);

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND DATE(startDate/1000, 'unixepoch') = :date ORDER BY startDate ASC")
    List<Appointment> findByDoctorAndDate(long doctorId, String date);

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY startDate DESC")
    List<Appointment> findByPatient(long patientId);
}
