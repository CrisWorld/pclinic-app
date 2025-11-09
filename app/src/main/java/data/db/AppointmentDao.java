package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import data.dto.AppointmentWithDoctor;
import data.dto.AppointmentWithPatient;
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

    @Query("SELECT a.*, u.fullName AS fullName " +
            "FROM appointments a " +
            "JOIN doctors d ON a.doctorId = d.id " +
            "JOIN users u ON d.userId = u.id " +
            "WHERE a.patientId = :patientId AND a.startDate >= :fromDate " +
            "ORDER BY a.startDate ASC")
    List<AppointmentWithDoctor> findUpcomingByPatient(long patientId, Date fromDate);

    @Query("SELECT a.*, u.fullName AS fullName " +
            "FROM appointments a " +
            "JOIN doctors d ON a.doctorId = d.id " +
            "JOIN users u ON d.userId = u.id " +
            "WHERE a.id = :appointmentId LIMIT 1")
    AppointmentWithDoctor getDetail(long appointmentId);

    @Query("SELECT a.*, " +
            "u.fullName AS patientName, " +
            "u.phone AS patientPhone, " +
            "u.gender AS patientGender, " +
            "u.birthDate AS patientBirthDate, " +
            "p.code AS patientCode " +
            "FROM appointments a " +
            "JOIN patients p ON a.patientId = p.id " +
            "JOIN users u ON p.userId = u.id " +
            "WHERE a.doctorId = :doctorId AND DATE(a.startDate/1000, 'unixepoch') = :date " +
            "ORDER BY a.startDate ASC")
    List<AppointmentWithPatient> findByDoctorAndDateWithPatient(long doctorId, String date);

}
