package com.studyos.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.studyos.app.ui.HomeFragment;
import com.studyos.app.ui.MoreFragment;
import com.studyos.app.ui.ProjectsFragment;
import com.studyos.app.ui.StudyFragment;
import com.studyos.app.ui.TasksFragment;
import com.studyos.app.ui.Ui;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.nav);

        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        getWindow().setStatusBarColor(Ui.col(this, R.color.so_bg));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(!night);

        nav.setOnItemSelectedListener(item -> {
            showRoot(item.getItemId());
            return true;
        });
        if (savedInstanceState == null) nav.setSelectedItemId(R.id.nav_home);

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void showRoot(int itemId) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment f;
        if (itemId == R.id.nav_tasks) f = new TasksFragment();
        else if (itemId == R.id.nav_study) f = new StudyFragment();
        else if (itemId == R.id.nav_projects) f = new ProjectsFragment();
        else if (itemId == R.id.nav_more) f = new MoreFragment();
        else f = new HomeFragment();
        fm.beginTransaction().setReorderingAllowed(true).replace(R.id.container, f).commit();
    }

    /** Opens a secondary screen on top of the current tab (back button returns). */
    public void open(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void selectTab(int itemId) {
        nav.setSelectedItemId(itemId);
    }
}
