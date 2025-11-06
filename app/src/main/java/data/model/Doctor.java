package data.model;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "doctors",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = CASCADE))
public class Doctor {
    public Doctor(long userId) {
        this.userId = userId;
    }
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String bio;
    public long userId;
    public List<String> specialties;
    public List<Integer> workingDays;
}
