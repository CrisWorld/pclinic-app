package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import data.model.Review;

@Dao
public interface ReviewDao {

    @Insert
    long insert(Review review);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);

    @Query("SELECT * FROM reviews ORDER BY id DESC")
    List<Review> getAll();

    @Query("SELECT * FROM reviews WHERE id = :id LIMIT 1")
    Review findById(long id);

    @Query("SELECT * FROM reviews WHERE doctorId = :doctorId ORDER BY id DESC LIMIT :limit")
    List<Review> findRecentByDoctor(long doctorId, int limit);

    @Query("SELECT AVG(rating) FROM reviews WHERE doctorId = :doctorId")
    Double getAverageRatingByDoctor(long doctorId);

    @Query("SELECT COUNT(*) FROM reviews WHERE doctorId = :doctorId")
    int countByDoctor(long doctorId);
}
