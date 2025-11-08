package data.model;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.NO_ACTION;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "prescription_examinationForms",
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
                ),
                @ForeignKey(
                        entity = ExaminationForm.class,
                        parentColumns = "id",
                        childColumns = "examinationId",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Prescription.class,
                        parentColumns = "id",
                        childColumns = "prescriptionId",
                        onDelete = CASCADE
                )
        }
)
public class PrescriptionExaminationForm {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public double price;
    public long prescriptionId;
    public long appointmentId;
    public long examinationId;
    public long patientId;
    public long doctorId;
}
