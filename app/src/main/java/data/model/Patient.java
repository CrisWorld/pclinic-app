package data.model;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "patients",
        foreignKeys = {
        @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = CASCADE)
        }
)
public class Patient {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String code;

    @NonNull
    public long userId;

    // Constructor tự động sinh code
    public Patient(long userId) {
        this.userId = userId;
        this.code = generateCode();
    }

    private String generateCode() {
        return "P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
