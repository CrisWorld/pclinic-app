package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import data.model.Admin;

@Dao
public interface AdminDao {

    @Insert
    long insert(Admin admin);

    @Update
    void update(Admin admin);

    @Delete
    void delete(Admin admin);

    @Query("SELECT * FROM admins ORDER BY id ASC")
    List<Admin> getAll();

    @Query("SELECT * FROM admins WHERE id = :id LIMIT 1")
    Admin findById(int id);

    @Query("SELECT * FROM admins WHERE userId = :userId LIMIT 1")
    Admin findByUserId(int userId);
}
