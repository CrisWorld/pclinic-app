package data.dto;

import java.util.Date;
import data.enums.Enum;

public class AppointmentWithDoctor {
    public long id;
    public long doctorId;
    public long patientId;
    public Date createdAt;
    public Date startDate;
    public Date endDate;
    public Date checkInDate;
    public String description;
    public Enum.AppointmentStatus status;

    public String fullName; // 👈 tên bác sĩ
}
