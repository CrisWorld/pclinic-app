package data.dto;

import androidx.room.Embedded;
import java.util.Date;

// DTO này chứa tất cả thông tin chi tiết của bệnh nhân
public class PatientDetailInfo {
    @Embedded
    public PatientInfo patientInfo; // Thông tin cơ bản

    // Thông tin thống kê
    public double totalSpending; // Tổng tiền đã chi
    public int totalAppointments; // Tổng số lịch hẹn
    public Date lastCheckInDate; // Ngày khám gần nhất

    @Embedded(prefix = "last_exam_")
    public ExaminationFormInfo lastExamination;
}
