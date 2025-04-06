package com.example.campusexpensemanagement.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.fragments.BudgetManagementFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.example.campusexpensemanagement.fragments.ExpenseManagementFragment;
import com.example.campusexpensemanagement.fragments.HomeFragment;
import com.example.campusexpensemanagement.fragments.ReportFragment;
import com.example.campusexpensemanagement.fragments.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // Hiển thị fragment Home khi khởi động
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_budget) {
                    selectedFragment = new BudgetManagementFragment();
                } else if (itemId == R.id.nav_expense_management) {
                    selectedFragment = new ExpenseManagementFragment();
                } else if (itemId == R.id.nav_report) {
                    selectedFragment = new ReportFragment();
                } else if (itemId == R.id.nav_user_profile) {
                    selectedFragment = new UserProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }


                return true;
            };

    }
