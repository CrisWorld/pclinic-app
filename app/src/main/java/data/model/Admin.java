package data.model;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "admins",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = CASCADE))
public class Admin {
    public Admin(long userId) {
        this.userId = userId;
    }
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;
}
