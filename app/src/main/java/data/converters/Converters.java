package data.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) return null;
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String json) {
        if (json == null) return null;
        Type type = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        if (list == null) return null;
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<Integer> toIntegerList(String json) {
        if (json == null) return null;
        Type type = new TypeToken<List<Integer>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
