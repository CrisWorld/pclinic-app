package data.model;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "prescriptions", indices = {
        @Index(value = {"code"}, unique = true)
})
public class Prescription {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public double price;
    public String code;
}
