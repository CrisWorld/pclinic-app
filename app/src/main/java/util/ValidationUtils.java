package util;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidFullName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static boolean isStrongPassword(String password) {
        if (password == null) return false;
        // Ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(regex);
    }

    public static boolean passwordsMatch(String password, String confirm) {
        return password != null && password.equals(confirm);
    }
}
