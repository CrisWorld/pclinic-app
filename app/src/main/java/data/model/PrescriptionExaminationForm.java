package data.model;

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
                        onDelete = NO_ACTION
                ),
                @ForeignKey(
                        entity = Patient.class,
                        parentColumns = "id",
                        childColumns = "patientId",
                        onDelete = NO_ACTION
                ),
                @ForeignKey(
                        entity = Appointment.class,
                        parentColumns = "id",
                        childColumns = "appointmentId",
                        onDelete = NO_ACTION
                ),
                @ForeignKey(
                        entity = ExaminationForm.class,
                        parentColumns = "id",
                        childColumns = "examinationId",
                        onDelete = NO_ACTION
                ),
                @ForeignKey(
                        entity = Prescription.class,
                        parentColumns = "id",
                        childColumns = "prescriptionId",
                        onDelete = NO_ACTION
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
