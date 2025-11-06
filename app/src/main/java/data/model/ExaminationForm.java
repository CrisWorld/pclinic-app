package data.model;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "examinationForms",
        foreignKeys = {
                @ForeignKey(
                        entity = Doctor.class,
                        parentColumns = "id",
                        childColumns = "doctorId",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Patient.class,
                        parentColumns = "id",
                        childColumns = "patientId",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Appointment.class,
                        parentColumns = "id",
                        childColumns = "appointmentId",
                        onDelete = CASCADE
                )
        }
)
public class ExaminationForm {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long doctorId;
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
