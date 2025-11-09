package ui.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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

@AndroidEntryPoint
public class DoctorActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_main_activity);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Gán Toolbar làm ActionBar
        setSupportActionBar(toolbar);

        // Thêm toggle mở/đóng drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý chọn menu
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Mặc định hiển thị OverviewFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new DoctorOverviewFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_overview);
            toolbar.setTitle("Tổng quan");
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_overview) {
            replaceFragment(new DoctorOverviewFragment(), "Tổng quan");
        } else if (id == R.id.nav_calendar_setup) {
            replaceFragment(new DoctorCalendarSetupFragment(), "Cài đặt lịch");
        } else if (id == R.id.nav_appointment) {
            replaceFragment(new DoctorAppointmentFragment(), "Lịch khám");
        } else if (id == R.id.nav_history) {
            replaceFragment(new DoctorHistoryFragment(), "Lịch sử khám");
        } else if (id == R.id.nav_logout) {
            util.AuthUtils.clearAuth(getApplicationContext());
            Toasty.success(DoctorActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DoctorActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            return false;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(androidx.fragment.app.Fragment fragment, String title) {
        toolbar.setTitle(title);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
