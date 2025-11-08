package vm.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import data.db.admin.AdminDoctorDao;
import data.dto.DoctorInfo;
import data.dto.MonthlyAppointmentStats;
import data.dto.MonthlyRevenueStats;

@HiltViewModel
public class DoctorDetailViewModel extends ViewModel {
    private final AdminDoctorDao doctorDao;
    private final String currentYear;

    @Inject
    public DoctorDetailViewModel(AdminDoctorDao doctorDao) {
        this.doctorDao = doctorDao;
        this.currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    public LiveData<DoctorInfo> getDoctorInfo(long doctorId) {
        return doctorDao.getDoctorInfoById(doctorId);
    }

    public LiveData<List<MonthlyRevenueStats>> getRevenueStats(long doctorId) {
        return doctorDao.getMonthlyRevenue(doctorId, currentYear);
    }

    public LiveData<List<MonthlyAppointmentStats>> getAppointmentStats(long doctorId) {
        return doctorDao.getMonthlyAppointments(doctorId, currentYear);
    }
}
