package util;

import android.content.Context;
import android.content.SharedPreferences;

import data.enums.Enum;

public class AuthUtils {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_ROLE = "role";


    // Lưu userId vào SharedPreferences
    public static void saveUserId(Context context, long userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    // Lấy userId (mặc định -1 nếu chưa có)
    public static long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public static boolean getRememberMe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public static void saveRememberMe(Context context, boolean remember){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
    }

    public static Enum.UserRole getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String roleName = prefs.getString(KEY_ROLE, null);

        if (roleName == null || roleName.isEmpty()) {
            return null; // không có role
        }

        try {
            return Enum.UserRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null; // chuỗi không khớp enum nào thì trả về null
        }
    }


    public static void saveRole(Context context, Enum.UserRole role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE, role.toString()).apply();
    }

    // Xóa userId (khi đăng xuất)
    public static void clearAuth(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
