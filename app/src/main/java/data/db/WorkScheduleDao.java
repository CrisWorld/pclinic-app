package data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import data.model.WorkSchedule;

@Dao
public interface WorkScheduleDao {

    @Insert
    long insert(WorkSchedule schedule);

    @Update
    void update(WorkSchedule schedule);

    @Delete
    void delete(WorkSchedule schedule);

    @Query("SELECT * FROM work_schedules WHERE doctorId = :doctorId AND date = :date ORDER BY startTime ASC")
    List<WorkSchedule> getSchedulesByDate(long doctorId, String date);

    @Query("SELECT * FROM work_schedules WHERE doctorId = :doctorId AND date >= :startDate ORDER BY date ASC, startTime ASC")
    List<WorkSchedule> getUpcomingSchedules(long doctorId, String startDate);

    @Query("DELETE FROM work_schedules WHERE doctorId = :doctorId AND date = :date")
    void deleteSchedulesByDate(long doctorId, String date);

    @Query("SELECT * FROM work_schedules WHERE id = :id LIMIT 1")
    WorkSchedule findById(long id);

    @Query("SELECT * FROM work_schedules ORDER BY date DESC, startTime DESC")
    List<WorkSchedule> getAll();
}
