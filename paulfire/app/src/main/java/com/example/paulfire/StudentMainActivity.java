package com.example.paulfire;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.paulfire.Fragment.AccountFragment;
import com.example.paulfire.Fragment.CourseFragment;
import com.example.paulfire.Fragment.ScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentMainActivity extends AppCompatActivity {

    Toolbar toolbar;
    Button button_logout;
    FirebaseUser mUser;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        toolbar = findViewById(R.id.toolbar_StudentMain);
        button_logout = findViewById(R.id.button_logout);


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_schedule:
                    selectedFragment = new ScheduleFragment();
                    toolbar.setTitle("Schedule");
                    break;
                case R.id.nav_course:
                    selectedFragment = new CourseFragment();
                    toolbar.setTitle("Course");
                    break;
                case R.id.nav_account:
                    selectedFragment = new AccountFragment();
                    toolbar.setTitle("Account");
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
    };


}