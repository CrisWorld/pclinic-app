package data.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "services", indices = {
        @Index(value = {"code"}, unique = true)
})
public class Service {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public double price;
    public String code;
}
