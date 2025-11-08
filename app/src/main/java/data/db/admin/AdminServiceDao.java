package data.db.admin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import data.model.Service;

@Dao
public interface AdminServiceDao {

    @Insert
    void insert(Service service);

    @Update
    void update(Service service);

    @Delete
    void delete(Service service);

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    Service findById(long id);

    @Query("SELECT * FROM services ORDER BY name ASC")
    LiveData<List<Service>> getAll();

    @Query("SELECT * FROM services WHERE name LIKE '%' || :keyword || '%' OR code LIKE '%' || :keyword || '%' ORDER BY name ASC")
    LiveData<List<Service>> search(String keyword);
}
