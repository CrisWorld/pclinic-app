package data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import data.enums.Enum;

@Entity(tableName = "users",
        indices = {
                @Index(value = {"email"}, unique = true)
        })
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String fullName;

    @NonNull
    public String email;

    @NonNull
    public String password;

    @NonNull
    public Enum.UserRole role;

    @Nullable
    public Long birthDate;

    @Nullable
    public String address;

    @Nullable
    public String phone;

    @Nullable
    public String gender;
}
