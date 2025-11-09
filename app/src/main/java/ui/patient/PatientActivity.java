package ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import dagger.hilt.android.AndroidEntryPoint;
import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import ui.auth.LoginActivity;
import util.AuthUtils;

@AndroidEntryPoint
public class PatientActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_main_activity); // 🔹 tên XML bạn gửi ở trên

        // Ánh xạ view
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Thiết lập toolbar làm ActionBar
        setSupportActionBar(toolbar);

        // Thiết lập toggle để mở/đóng drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        // 🔹 Default fragment là "Tổng quan"
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new DoctorListFragment())
                    .commit();

            // 🔹 Đánh dấu menu "Tổng quan" là được chọn
            navigationView.setCheckedItem(R.id.nav_overview);
            toolbar.setTitle("Tổng quan");
        }
        // Xử lý chọn menu trong sidebar
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_overview) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new DoctorListFragment())
                            .commit();
                    toolbar.setTitle("Tổng quan");
                }
                else if (id == R.id.nav_upcoming_appointments) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new AppointmentListFragment())
                            .commit();
                    toolbar.setTitle("Lịch hẹn sắp tới");
                } else if (id == R.id.nav_history) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new AppointmentHistoryFragment())
                            .commit();
                } else if (id == R.id.nav_logout) {
                    // 🔹 Xóa thông tin đăng nhập
                    util.AuthUtils.clearAuth(getApplicationContext());

                    // 🔹 Thông báo
                    Toasty.success(PatientActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

                    // 🔹 Quay về LoginActivity
                    Intent intent = new Intent(PatientActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // 🔹 Kết thúc activity hiện tại
                    finish();
                } else {
                    return false;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Xử lý khi người dùng nhấn back hoặc vuốt back
            }
        });
    }

}
