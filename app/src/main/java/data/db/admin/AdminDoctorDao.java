package data.db.admin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;import androidx.room.Query;

import java.util.List;

import data.dto.DoctorInfo;
import data.dto.MonthlyAppointmentStats;
import data.dto.MonthlyRevenueStats;

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
}
