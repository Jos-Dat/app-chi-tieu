package com.example.campusexpensemanagement.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.data.UserDAO;
import com.example.campusexpensemanagement.models.User;

public class UpdatePasswordActivity extends AppCompatActivity {

    private EditText edtNewPassword, edtConfirmNewPassword;
    private Button btnSaveChangePassword;
    private String username;
    private UserDAO userDAO;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Toast.makeText(this, "Error: Username not provided", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Initialize UI components
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnSaveChangePassword = findViewById(R.id.btnSaveChangePassword);

        // Initialize database
        userDAO = new UserDAO(this);

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating password...");
        progressDialog.setCancelable(false);

        // Set up button click listener
        btnSaveChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    private void updatePassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("New password is required");
            edtNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            edtConfirmNewPassword.setError("Please confirm your password");
            edtConfirmNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            edtConfirmNewPassword.setError("Passwords do not match");
            edtConfirmNewPassword.requestFocus();
            return;
        }

        // Password strength validation
        if (newPassword.length() < 6) {
            edtNewPassword.setError("Password must be at least 6 characters");
            edtNewPassword.requestFocus();
            return;
        }

        // Show loading dialog
        progressDialog.show();

        try {
            // Get user by username
            User user = userDAO.getUserByUsername(username);

            if (user != null) {
                // Update password
                user.setPassword(newPassword);
                boolean success = userDAO.updateUser(user);

                if (success) {
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Hide loading dialog
            progressDialog.dismiss();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}