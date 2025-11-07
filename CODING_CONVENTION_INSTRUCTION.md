# PClinic App - Coding Convention & Architecture Instruction

## 🏗️ Existing Core Models & Context

### Current Entities (Database Tables)
```java
// Core User System
User.java                    // Base user entity with auth info
Patient.java                 // Patient profile extending User
Doctor.java                  // Doctor profile extending User  
Admin.java                   // Admin profile extending User

// Healthcare Domain
Appointment.java             // Patient-Doctor appointments
ExaminationForm.java         // Medical examination records
Prescription.java            // Medicine prescriptions
Review.java                  // Patient reviews for doctors
Service.java                 // Medical services offered

// Junction/Relationship Tables
PrescriptionExaminationForm.java    // Prescription-ExamForm relationship
ServiceExaminationForm.java         // Service-ExamForm relationship
```

### Current Enums (Business Rules)
```java
// data/enums/Enum.java
public enum UserRole {
    PATIENT,     // Can book appointments, view history
    DOCTOR,      // Can manage appointments, create prescriptions
    ADMIN        // Can manage users, services, system config
}

public enum gender {
    MALE,
    FEMALE,
    OTHER
}

public enum AppointmentStatus {
    ABSENT,      // Patient didn't show up
    PENDING,     // Waiting for confirmation
    CONFIRMED,   // Appointment confirmed
    DONE         // Appointment completed
}
```

### Current Repositories
```java
UserRepository.java          // User authentication & management
// Need to add: PatientRepository, DoctorRepository, AppointmentRepository, etc.
```

### Current UI Structure
```java
// Authentication Flow
ui/auth/LoginActivity.java
ui/auth/RegisterActivity.java

// Role-based Dashboards
ui/admin/AdminActivity.java
ui/doctor/DoctorActivity.java           // Main doctor dashboard
ui/doctor/DoctorOverviewFragment.java   // Doctor overview
ui/doctor/DoctorAppointmentFragment.java // Appointment management
ui/doctor/DoctorCalendarSetupFragment.java // Schedule setup
ui/doctor/DoctorHistoryFragment.java    // Medical history

ui/patient/PatientActivity.java         // Main patient dashboard
// Missing: Patient fragments for booking, history, profile
```

## 📁 Project Structure Overview

```
app/src/main/java/
├── com/example/pclinic/     # Application entry point
├── data/                    # Data layer
│   ├── converters/         # Room type converters
│   ├── db/                 # Database DAOs & Database class
│   ├── enums/              # Application enums
│   ├── model/              # Entity models (Room entities)
│   └── repository/         # Repository pattern implementation
├── di/                     # Dependency injection modules
├── ui/                     # UI layer (Activities & Fragments)
│   ├── admin/              # Admin role UI components
│   ├── auth/               # Authentication UI (Login/Register)
│   ├── doctor/             # Doctor role UI components
│   └── patient/            # Patient role UI components
└── util/                   # Utility classes
```

## 🏗️ Architecture Pattern

**Clean Architecture + MVVM + Repository Pattern**
- **Data Layer**: Models, DAOs, Database, Repositories
- **Domain Layer**: Use cases (implicit through repositories)
- **Presentation Layer**: Activities, Fragments, ViewModels (planned)

## 🔧 Technology Stack

- **Database**: Room (SQLite)
- **Dependency Injection**: Dagger Hilt
- **UI Framework**: Native Android Views
- **Authentication**: SharedPreferences
- **Toast Library**: Toasty
- **Threading**: Executors for background tasks

## 📝 Naming Conventions

### 1. Package Naming
```java
// Correct format - lowercase, no dots
package data.model;
package ui.auth;
package data.db;
```

### 2. Class Naming
```java
// Entities: Noun (singular)
public class Doctor
public class Patient
public class User

// DAOs: EntityName + Dao
public interface DoctorDao
public interface PatientDao

// Activities: Purpose + Activity
public class LoginActivity
public class DoctorActivity

// Fragments: Purpose + Fragment
public class DoctorOverviewFragment
public class DoctorAppointmentFragment

// Repositories: EntityName + Repository
public class UserRepository

// Utils: Purpose + Utils
public class AuthUtils
public class ValidationUtils
```

### 3. Variable Naming
```java
// UI components - prefix convention
private TextInputLayout layoutEmail, layoutPassword;
private TextInputEditText etEmail, etPassword;  // et = EditText
private Button btnLogin;                        // btn = Button
private TextView tvGoRegister;                  // tv = TextView
```

## 🗄️ Database Layer (Room)

### Entity Model Pattern
```java
@Entity(tableName = "table_name",
        foreignKeys = @ForeignKey(entity = ParentEntity.class,
                parentColumns = "id",
                childColumns = "parentId",
                onDelete = CASCADE))
public class EntityName {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    // Public fields for Room simplicity
    public String field1;
    public long parentId;
    
    // Constructor for required fields only
    public EntityName(long parentId) {
        this.parentId = parentId;
    }
}
```

### DAO Pattern
```java
@Dao
public interface EntityDao {
    @Insert
    long insert(Entity entity);

    @Update
    void update(Entity entity);

    @Delete
    void delete(Entity entity);

    @Query("SELECT * FROM table_name ORDER BY id ASC")
    List<Entity> getAll();

    @Query("SELECT * FROM table_name WHERE id = :id LIMIT 1")
    Entity findById(long id);
    
    // Specific business queries
    @Query("SELECT * FROM table_name WHERE field = :value")
    List<Entity> findByField(String value);
}
```

### Database Configuration
```java
@Database(entities = {
    // List all entities here
    User.class,
    Doctor.class,
    Patient.class
}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    // Abstract methods for each DAO
    public abstract UserDao userDao();
    public abstract DoctorDao doctorDao();
}
```

## 🏪 Repository Pattern

### Repository Implementation
```java
@Singleton
public class EntityRepository {
    
    private final EntityDao entityDao;

    @Inject
    public EntityRepository(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    // Synchronous operations for simple cases
    public Entity findByField(String field) {
        return entityDao.findByField(field);
    }

    // Asynchronous operations with LiveData for complex cases
    public MutableLiveData<Entity> findById(long id) {
        MutableLiveData<Entity> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Entity entity = entityDao.findById(id);
            result.postValue(entity);
        });
        return result;
    }

    // Business logic methods
    public long create(Entity entity) {
        return entityDao.insert(entity);
    }

    public void update(Entity entity) {
        Executors.newSingleThreadExecutor().execute(() -> {
            entityDao.update(entity);
        });
    }
}
```

## 🎨 UI Layer

### Activity Pattern
```java
@AndroidEntryPoint  // For Hilt injection
public class FeatureActivity extends AppCompatActivity {
    
    // Injected dependencies
    @Inject
    SomeRepository repository;
    
    // UI components with clear naming
    private TextInputLayout layoutField;
    private TextInputEditText etField;
    private Button btnAction;
    private TextView tvInfo;

    // Activity launchers (if needed)
    private final ActivityResultLauncher<Intent> someLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
            result -> {
                // Handle result
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        // Initialize all UI components
        layoutField = findViewById(R.id.layout_field);
        etField = findViewById(R.id.et_field);
        btnAction = findViewById(R.id.btn_action);
    }

    private void setupListeners() {
        // Setup click listeners and other event handlers
        btnAction.setOnClickListener(v -> handleAction());
    }

    private void loadData() {
        // Load initial data
    }

    private void handleAction() {
        // Handle user actions
        // Validate input
        // Call repository methods
        // Show feedback to user
    }
}
```

### Fragment Pattern
```java
public class FeatureFragment extends Fragment {
    
    // UI components
    private View view;
    private RecyclerView recyclerView;
    private SomeAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_feature, container, false);
        initViews();
        setupRecyclerView();
        return view;
    }

    private void initViews() {
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupRecyclerView() {
        adapter = new SomeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}
```

## 🔧 Dependency Injection (Hilt)

### App Module Pattern
```java
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "database_name"
        )
        .fallbackToDestructiveMigration()
        .build();
    }

    @Provides
    public EntityDao provideEntityDao(AppDatabase database) {
        return database.entityDao();
    }
}
```

## 🛠️ Utility Classes

### Utility Class Pattern
```java
public class FeatureUtils {
    
    // Constants
    private static final String PREF_NAME = "feature_prefs";
    private static final String KEY_SOME_VALUE = "some_value";

    // Static utility methods
    public static void saveSomeValue(Context context, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SOME_VALUE, value).apply();
    }

    public static String getSomeValue(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SOME_VALUE, null);
    }
}
```

## 📋 Enums Pattern

```java
public class Enum {
    public enum SomeStatus {
        STATUS_ONE,
        STATUS_TWO,
        STATUS_THREE
    }

    public enum UserRole {
        PATIENT,
        DOCTOR,
        ADMIN
    }
}
```

## 🎯 Feature Development Strategy & Conflict Prevention

### Before Starting Any New Feature

1. **Check Current State** 
   ```bash
   # Pull latest changes
   git pull origin main
   
   # Check what files others are working on
   git log --oneline --since="1 week ago"
   ```

2. **Create Feature Branch**
   ```bash
   # Use clear naming: feature/role-functionality
   git checkout -b feature/patient-appointment-booking
   git checkout -b feature/doctor-prescription-management
   git checkout -b bugfix/login-validation
   ```

3. **Plan File Changes (Avoid Conflicts)**
   - **Check if files exist**: Use existing entities/DAOs if possible
   - **Coordinate on shared files**: 
     - `AppDatabase.java` (add entities gradually)
     - `AppModule.java` (coordinate DAO additions)
     - `Enum.java` (discuss new enums with team)
   - **Create role-specific files**: Keep UI separated by role

### File Creation Rules (Prevent File Waste)

#### ✅ DO Create New Files For:
```java
// New business entities
data/model/MedicalRecord.java        // New domain concept
data/model/Notification.java         // New feature

// Role-specific UI
ui/patient/PatientBookingFragment.java    // Patient-only feature
ui/doctor/DoctorPatientListFragment.java  // Doctor-only feature

// Feature-specific repositories
data/repository/AppointmentRepository.java // New domain repository
data/repository/NotificationRepository.java
```

#### ❌ DON'T Create Duplicate Files For:
```java
// Use existing entities if possible
// DON'T create: PatientAppointment.java if Appointment.java exists
// DON'T create: DoctorUser.java if Doctor.java exists

// Use existing DAOs with new methods
// DON'T create: PatientAppointmentDao.java 
// ADD methods to: AppointmentDao.java

// Use existing utils
// DON'T create: PatientValidationUtils.java
// ADD methods to: ValidationUtils.java
```

## 🔄 Feature Development Workflow

### Step 1: Analyze Existing Code
```java
// Check what entities/enums you need
// Example: For Patient Appointment Booking
Required entities: Patient, Doctor, Appointment, Service
Required enums: AppointmentStatus
Required repositories: UserRepository (extend), AppointmentRepository (create)
```

### Step 2: Extend vs Create New
```java
// EXTEND existing entities (add fields if needed)
@Entity(tableName = "appointments")
public class Appointment {
    // Existing fields...
    
    // ADD new fields for your feature
    public String additionalNotes;  // Add if needed for your feature
    public long serviceId;          // Add if connecting to services
}

// CREATE new entities only for new domain concepts
@Entity(tableName = "medical_records")  // Completely new concept
public class MedicalRecord {
    // New entity for new business need
}
```

### Step 3: Repository Strategy
```java
// EXTEND existing repositories
@Singleton
public class UserRepository {
    // Existing methods...
    
    // ADD new methods for your feature
    public List<Doctor> findDoctorsBySpecialty(String specialty) {
        // Implementation
    }
}

// CREATE new repositories for new entities
@Singleton  
public class AppointmentRepository {
    // New repository for appointment management
}
```

## 🚀 Code Style Guidelines

### 1. Method Organization
```java
// Order methods as follows:
// 1. Constructor
// 2. Lifecycle methods (onCreate, onStart, etc.)
// 3. Public methods
// 4. Private helper methods
// 5. Inner classes
```

### 2. Error Handling
```java
// Use Toasty for user feedback
Toasty.success(context, "Success message", Toast.LENGTH_SHORT).show();
Toasty.error(context, "Error message", Toast.LENGTH_SHORT).show();
Toasty.info(context, "Info message", Toast.LENGTH_SHORT).show();
```

### 3. Threading
```java
// Use Executors for background tasks
Executors.newSingleThreadExecutor().execute(() -> {
    // Background operation
    Entity result = repository.someOperation();
    
    // Update UI on main thread
    runOnUiThread(() -> {
        // Update UI with result
    });
});
```

### 4. Validation
```java
// Create validation methods in Utils classes
public static boolean isValidEmail(String email) {
    return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
}
```

## 📱 Resource Naming

### Layout Files
- Activities: `activity_feature_name.xml`
- Fragments: `fragment_feature_name.xml`
- Items: `item_feature_name.xml`

### ID Naming
- Layouts: `layout_purpose`
- EditText: `et_purpose`
- Button: `btn_purpose`
- TextView: `tv_purpose`
- RecyclerView: `rv_purpose`

### Smart File Organization

#### Naming Convention for Feature Files
```java
// Use descriptive, non-conflicting names
// Pattern: [Role][Domain][Action][Type]

// Good Examples:
PatientAppointmentBookingFragment.java    // Clear scope
DoctorPrescriptionCreateFragment.java     // Specific action
AdminUserManagementFragment.java          // Admin-specific

// Avoid Generic Names (causes conflicts):
BookingFragment.java          // Too generic
ManageFragment.java           // Unclear purpose
ListFragment.java             // What list?
```

#### Layout File Strategy
```xml
<!-- Match Java file names -->
fragment_patient_appointment_booking.xml
fragment_doctor_prescription_create.xml
activity_admin_user_management.xml

<!-- Item layouts -->
item_appointment_list.xml
item_doctor_profile.xml
item_patient_history.xml
```

## � Quick Start Guide for New Features

### Example: Adding "Patient Appointment Booking"

#### Step 1: Analysis
```java
// What I need:
Entities: Patient ✓ (exists), Doctor ✓ (exists), Appointment ✓ (exists), Service ✓ (exists)
Enums: AppointmentStatus ✓ (exists) 
Repositories: Need AppointmentRepository (create new)
UI: PatientAppointmentBookingFragment (create new)
```

#### Step 2: Implementation Order
```java
// 1. Create AppointmentRepository (new file)
@Singleton
public class AppointmentRepository {
    @Inject
    public AppointmentRepository(AppointmentDao appointmentDao) {...}
}

// 2. Add to AppModule.java (coordinate with team)
@Provides
public AppointmentDao provideAppointmentDao(AppDatabase db) { return db.appointmentDao(); }

// 3. Create UI (new file) 
public class PatientAppointmentBookingFragment extends Fragment {
    @Inject AppointmentRepository appointmentRepository;
    @Inject UserRepository userRepository; // Reuse existing
}
```

#### Step 3: Team Coordination
```java
// Files that need team coordination:
- AppDatabase.java (if adding new entities)
- AppModule.java (when adding new providers)
- Enum.java (when adding new enums)

// Files you can work on independently:
- New Fragment/Activity files
- New Repository files  
- New layout files
- Feature-specific Utils
```

## �🔐 Security & Best Practices

1. **Authentication**
   - Use SharedPreferences for session management
   - Store user roles and IDs securely
   - Implement proper logout functionality

2. **Database**
   - Use Room for type safety
   - Implement proper foreign key relationships
   - Use TypeConverters for complex data types

3. **UI**
   - Implement proper input validation
   - Show loading states for async operations
   - Handle network/database errors gracefully

4. **Team Collaboration**
   - Always work on feature branches
   - Coordinate on shared files (AppDatabase, AppModule, Enum)
   - Use descriptive file names to avoid conflicts
   - Reuse existing code when possible
   - Document new patterns for team

This instruction provides a complete guide for maintaining consistency, avoiding conflicts, and implementing new features efficiently in the PClinic app.