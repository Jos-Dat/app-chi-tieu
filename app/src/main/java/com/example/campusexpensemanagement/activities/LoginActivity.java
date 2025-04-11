package com.example.campusexpensemanagement.activities;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.data.UserDAO;
import com.example.campusexpensemanagement.models.User;
import com.example.campusexpensemanagement.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private CheckBox cbRememberMe;
    private UserDAO userDAO;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);

        // Initialize database and session manager
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // Redirect to main activity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Check for remembered credentials
        if (sessionManager.isRememberMeEnabled()) {
            etUsername.setText(sessionManager.getRememberedUsername());
            etPassword.setText(sessionManager.getRememberedPassword());
            cbRememberMe.setChecked(true);
        }

        // Set up button click listeners
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to register activity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            }
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean rememberMe = cbRememberMe.isChecked();

        // Advanced input validation
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show loading dialog
        progressDialog.show();

        try {
            // Authenticate user
            // Note: In a production app, you would hash the password here
            // For now, we're directly comparing with the stored password
            User user = userDAO.authenticateUser(username, password);

            if (user != null) {
                // Handle remember me functionality
                if (rememberMe) {
                    sessionManager.setRememberMe(username, password);
                } else {
                    sessionManager.clearRememberMe();
                }

                // Save user session
                sessionManager.createLoginSession(user.getId(), user.getUsername());
                Log.d(TAG, "Login successful for user: " + username);

                // Redirect to main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Login failed for user: " + username);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Login error", e);
        } finally {
            // Hide loading dialog
            progressDialog.dismiss();
        }
    }
}