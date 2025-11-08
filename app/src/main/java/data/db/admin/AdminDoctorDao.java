package data.db.admin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;import androidx.room.Query;

import java.util.List;

import data.dto.DoctorInfo;
import data.dto.MonthlyAppointmentStats;
import data.dto.MonthlyRevenueStats;
import data.dto.PatientDetailInfo;
import data.dto.PatientInfo;
import data.dto.ReviewInfo;

@Dao
public interface AdminDoctorDao {

    @Query("SELECT d.id AS doctorId, d.bio AS bio, u.id AS userId, u.fullName AS fullName, u.email AS email, u.phone AS phone, u.gender AS gender, d.specialties AS specialties FROM doctors d INNER JOIN users u ON d.userId = u.id")
    LiveData<List<DoctorInfo>> getAllDoctorInfo();

    @Query("SELECT d.id AS doctorId, d.bio AS bio, u.id AS userId, u.fullName AS fullName, u.email AS email, u.phone AS phone, u.gender AS gender, d.specialties AS specialties FROM doctors d INNER JOIN users u ON d.userId = u.id WHERE u.fullName LIKE '%' || :keyword || '%' OR u.email LIKE '%' || :keyword || '%'")
    LiveData<List<DoctorInfo>> searchDoctorInfo(String keyword);

    @Query("SELECT d.id AS doctorId, d.bio AS bio, u.id AS userId, u.fullName AS fullName, u.email AS email, u.phone AS phone, u.gender AS gender, d.specialties AS specialties FROM doctors d INNER JOIN users u ON d.userId = u.id WHERE d.id = :doctorId")
    LiveData<DoctorInfo> getDoctorInfoById(long doctorId);

    @Query("SELECT strftime('%m', datetime(examinationDate/1000, 'unixepoch')) as month, SUM(grandTotal) as totalRevenue FROM examinationForms WHERE doctorId = :doctorId AND strftime('%Y', datetime(examinationDate/1000, 'unixepoch')) = :year GROUP BY month")
    LiveData<List<MonthlyRevenueStats>> getMonthlyRevenue(long doctorId, String year);

    @Query("SELECT strftime('%m', datetime(checkInDate/1000, 'unixepoch')) as month, COUNT(id) as appointmentCount FROM appointments WHERE doctorId = :doctorId AND strftime('%Y', datetime(checkInDate/1000, 'unixepoch')) = :year AND checkInDate IS NOT NULL GROUP BY month")
    LiveData<List<MonthlyAppointmentStats>> getMonthlyAppointments(long doctorId, String year);

    @Query("DELETE FROM doctors WHERE id = :doctorId")
    void deleteDoctorById(long doctorId);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUserById(long userId); // <-- Phương thức mới để xóa User

    // Thêm các phương thức cập nhật
    @Query("UPDATE users SET fullName = :fullName, email = :email, phone = :phone, gender = :gender WHERE id = :userId")
    void updateUser(long userId, String fullName, String email, String phone, String gender);

    @Query("UPDATE doctors SET specialties = :specialties, bio = :bio WHERE id = :doctorId")
    void updateDoctor(long doctorId, List<String> specialties, String bio);

    @Query("SELECT " +
            // Phần thông tin cơ bản
            "p.id AS patientId, p.code as patientCode, u.id AS userId, u.fullName, u.email, u.phone, u.gender, " +
            // Phần thống kê
            "(SELECT SUM(ef.grandTotal) FROM examinationForms ef WHERE ef.patientId = p.id) as totalSpending, " +
            "(SELECT COUNT(apt.id) FROM appointments apt WHERE apt.patientId = p.id) as totalAppointments, " +
            "(SELECT MAX(apt.checkInDate) FROM appointments apt WHERE apt.patientId = p.id) as lastCheckInDate, " +

            // PHẦN LẦN KHÁM GẦN NHẤT (LẤY TẤT CẢ CÁC CỘT)
            "ef_last.id as last_exam_id, " +
            "ef_last.doctorId as last_exam_doctorId, " +
            "ef_last.patientId as last_exam_patientId, " +
            "ef_last.appointmentId as last_exam_appointmentId, " +
            "ef_last.grandTotal as last_exam_grandTotal, " +
            "ef_last.totalServiceAmount as last_exam_totalServiceAmount, " +
            "ef_last.totalPrescriptionAmount as last_exam_totalPrescriptionAmount, " +
            "ef_last.totalAppointmentAmount as last_exam_totalAppointmentAmount, " +
            "ef_last.totalService as last_exam_totalService, " +
            "ef_last.totalPrescription as last_exam_totalPrescription, " +
            "ef_last.height as last_exam_height, " +
            "ef_last.weight as last_exam_weight, " +
            "ef_last.bloodPressure as last_exam_bloodPressure, " +
            "ef_last.examinationCode as last_exam_examinationCode, " +
            "ef_last.temperature as last_exam_temperature, " +
            "ef_last.pulse as last_exam_pulse, " +
            "ef_last.medicalHistory as last_exam_medicalHistory, " +
            "ef_last.diagnosis as last_exam_diagnosis, " +
            "ef_last.examinationDate as last_exam_examinationDate " +

            "FROM patients p " +
            "INNER JOIN users u ON p.userId = u.id " +
            // Join với lần khám gần nhất
            "LEFT JOIN examinationForms ef_last ON ef_last.id = (SELECT id FROM examinationForms WHERE patientId = p.id ORDER BY examinationDate DESC LIMIT 1) " +
            "WHERE p.id = :patientId")
    LiveData<PatientDetailInfo> getPatientDetailInfoById(long patientId);

    // QUERY MỚI ĐỂ LẤY REVIEW CỦA BỆNH NHÂN
    @Query("SELECT r.rating, r.description, u.fullName as doctorName " +
            "FROM reviews r " +
            "INNER JOIN doctors d ON r.doctorId = d.id " +
            "INNER JOIN users u ON d.userId = u.id " +
            "WHERE r.patientId = :patientId ORDER BY r.id DESC")
    LiveData<List<ReviewInfo>> getReviewsByPatientId(long patientId);

    // CÁC QUERY MỚI CHO PATIENT
    @Query("SELECT p.id AS patientId, p.code as patientCode, u.id AS userId, u.fullName, u.email, u.phone, u.gender " +
            "FROM patients p INNER JOIN users u ON p.userId = u.id")
    LiveData<List<PatientInfo>> getAllPatientInfo();

    @Query("SELECT p.id AS patientId, p.code as patientCode, u.id AS userId, u.fullName, u.email, u.phone, u.gender " +
            "FROM patients p INNER JOIN users u ON p.userId = u.id WHERE u.fullName LIKE '%' || :keyword || '%' OR u.email LIKE '%' || :keyword || '%'")
    LiveData<List<PatientInfo>> searchPatientInfo(String keyword);
}
