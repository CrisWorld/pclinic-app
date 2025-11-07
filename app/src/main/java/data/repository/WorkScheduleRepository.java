package data.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import data.db.WorkScheduleDao;
import data.model.WorkSchedule;

@Singleton
public class WorkScheduleRepository {

    private final WorkScheduleDao workScheduleDao;

    @Inject
    public WorkScheduleRepository(WorkScheduleDao workScheduleDao) {
        this.workScheduleDao = workScheduleDao;
    }

    public MutableLiveData<List<WorkSchedule>> getSchedulesByDate(long doctorId, String date) {
        MutableLiveData<List<WorkSchedule>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<WorkSchedule> schedules = workScheduleDao.getSchedulesByDate(doctorId, date);
            result.postValue(schedules);
        });
        return result;
    }

    public MutableLiveData<List<WorkSchedule>> getUpcomingSchedules(long doctorId, String startDate) {
        MutableLiveData<List<WorkSchedule>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<WorkSchedule> schedules = workScheduleDao.getUpcomingSchedules(doctorId, startDate);
            result.postValue(schedules);
        });
        return result;
    }

    public void createSchedule(WorkSchedule schedule) {
        Executors.newSingleThreadExecutor().execute(() -> {
            workScheduleDao.insert(schedule);
        });
    }

    public void deleteSchedulesByDate(long doctorId, String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            workScheduleDao.deleteSchedulesByDate(doctorId, date);
        });
    }

    public void updateSchedule(WorkSchedule schedule) {
        Executors.newSingleThreadExecutor().execute(() -> {
            workScheduleDao.update(schedule);
        });
    }

    public void deleteSchedule(WorkSchedule schedule) {
        Executors.newSingleThreadExecutor().execute(() -> {
            workScheduleDao.delete(schedule);
        });
    }
}
