package data.enums;

public class Enum {
    public static enum gender {
        MALE,
        FEMALE,
        OTHER
    }

    public static enum UserRole {
        PATIENT,
        DOCTOR,
        ADMIN
    }

    public static enum AppointmentStatus {
        ABSENT,
        PENDING,
        CONFIRMED,
        DONE,
    }
}
