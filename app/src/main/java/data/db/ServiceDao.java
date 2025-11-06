package data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import data.model.Service;

@Dao
public interface ServiceDao {

    // 🔹 Thêm một service
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Service service);

    // 🔹 Thêm nhiều service (dùng khi seed dữ liệu)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Service> services);

    // 🔹 Lấy tất cả service
    @Query("SELECT * FROM services")
    List<Service> getAll();

    // 🔹 Tìm service theo ID
    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    Service findById(long id);

    // 🔹 Tìm service theo code
    @Query("SELECT * FROM services WHERE code = :code LIMIT 1")
    Service findByCode(String code);

    // 🔹 Đếm số lượng service (dùng để kiểm tra seed)
    @Query("SELECT COUNT(*) FROM services")
    int count();

    // 🔹 Xóa tất cả
    @Query("DELETE FROM services")
    void deleteAll();
}
