package data.model;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

import data.enums.Enum;

@Entity(
        tableName = "appointments",
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
                )
        }
)
public class Appointment {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public long doctorId;
    @NonNull
    public long patientId;
    @NonNull
    public Date createdAt;
    @NonNull
    public Date startDate;
    @NonNull
    public Date endDate;
    @Nullable
    public Date checkInDate;

    @Nullable
    public String description;

    @NonNull
    public Enum.AppointmentStatus status;

}
