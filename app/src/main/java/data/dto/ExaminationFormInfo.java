package data.dto;

import java.util.Date;

// DTO chứa đầy đủ thông tin của một phiếu khám
public class ExaminationFormInfo {
    public long id;public long doctorId;
    public long patientId;
    public long appointmentId;
    public double grandTotal;
    public double totalServiceAmount;
    public double totalPrescriptionAmount;
    public double totalAppointmentAmount;
    public int totalService;
    public int totalPrescription;
    public String height; // cm
    public String weight; // kg
    public String bloodPressure;
    public String examinationCode;
    public String temperature;
    public String pulse;
    public String medicalHistory;
    public String diagnosis;
    public Date examinationDate;
}
