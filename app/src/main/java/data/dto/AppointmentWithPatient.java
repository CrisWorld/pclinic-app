package data.dto;

import java.util.Date;
import data.enums.Enum;

public class AppointmentWithPatient {
    public long id;
    public long doctorId;
    public long patientId;
    public Date createdAt;
    public Date startDate;
    public Date endDate;
    public Date checkInDate;
    public String description;
    public Enum.AppointmentStatus status;

    // Patient info
    public String patientName;
    public String patientPhone;
    public String patientGender;
    public Long patientBirthDate;
    public String patientCode;
}

