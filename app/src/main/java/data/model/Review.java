package data.model;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.NO_ACTION;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reviews",
        foreignKeys = {
                @ForeignKey(
                        entity = Patient.class,
                        parentColumns = "id",
                        childColumns = "patientId",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Doctor.class,
                        parentColumns = "id",
                        childColumns = "doctorId",
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
public class Review {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int rating;
    public long appointmentId;
    public long doctorId;
    public long patientId;
    @Nullable
    public String description;
}
