package com.example.campusexpensemanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.data.UserDAO;
import com.example.campusexpensemanagement.models.User;
import com.example.campusexpensemanagement.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Initialize database and session manager
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Set up button click listeners
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to login activity
                finish();
            }
        });
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra nhập liệu
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter full information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu có ít nhất 6 ký tự
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Kiểm tra mật khẩu xác nhận
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra nếu tên người dùng đã tồn tại
        if (userDAO.checkUser(username)) {
            Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo người dùng mới
        User user = new User(username, email, password);
        long userId = userDAO.addUser(user);

        if (userId != -1) {
            // Đăng ký thành công
            Toast.makeText(this, "Register successfully", Toast.LENGTH_SHORT).show();

            // Lưu thông tin phiên đăng nhập
            sessionManager.createLoginSession((int) userId, username);

            // Chuyển đến MainActivity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Register failed. Please try again!", Toast.LENGTH_SHORT).show();
        }
    }

}