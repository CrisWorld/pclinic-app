package data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "work_schedules",
        foreignKeys = @ForeignKey(entity = Doctor.class,
                parentColumns = "id",
                childColumns = "doctorId",
                onDelete = CASCADE))
public class WorkSchedule {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long doctorId;
    public String date; // Format: yyyy-MM-dd
    public String startTime; // Format: HH:mm
    public String endTime; // Format: HH:mm
    public int slotDuration; // Duration in minutes (e.g., 30)
    public boolean isAvailable;

    public WorkSchedule(long doctorId, String date, String startTime, String endTime, int slotDuration) {
        this.doctorId = doctorId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDuration = slotDuration;
        this.isAvailable = true;
    }
}
