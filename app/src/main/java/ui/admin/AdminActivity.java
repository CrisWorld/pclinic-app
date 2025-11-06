package ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import es.dmoral.toasty.Toasty;
import example.pclinic.com.R;
import ui.auth.LoginActivity;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main_activity); // file layout bạn gửi

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new AdminOverviewFragment())
                    .commit();

            // 🔹 Đánh dấu menu “Tổng quan” là được chọn
            navigationView.setCheckedItem(R.id.nav_overview);
            toolbar.setTitle("Tổng quan");
        }

        // Gắn toggle mở/đóng menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_overview) {
            replaceFragment(new AdminOverviewFragment(), "Tổng quan");
        } else if (id == R.id.nav_doctor) {
            replaceFragment(new AdminDoctorFragment(), "Bác sĩ");
        } else if (id == R.id.nav_patient) {
            replaceFragment(new AdminPatientFragment(), "Bệnh nhân");
        } else if (id == R.id.nav_service) {
            replaceFragment(new AdminServiceFragment(), "Dịch vụ");
        } else if (id == R.id.nav_prescription) {
            replaceFragment(new AdminPrescriptionFragment(), "Thuốc");
        } else if (id == R.id.nav_logout) {
            util.AuthUtils.clearAuth(getApplicationContext());
            Toasty.success(AdminActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            return false;
        }

        drawerLayout.closeDrawers();
        return true;
    }

    private void replaceFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
        toolbar.setTitle(title);
    }
}
