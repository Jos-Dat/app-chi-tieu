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

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText edtAccount, edtEmail;
    private Button btnConfirmAccount, btnCancel;
    private UserDAO userDAO;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Initialize UI components
        edtAccount = findViewById(R.id.edtAccount);
        edtEmail = findViewById(R.id.edtEmail);
        btnConfirmAccount = findViewById(R.id.btnConfirmAccount);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize database
        userDAO = new UserDAO(this);

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying account information...");
        progressDialog.setCancelable(false);

        // Set up button click listeners
        btnConfirmAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccountInformation();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void verifyAccountInformation() {
        String username = edtAccount.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(username)) {
            edtAccount.setError("Username is required");
            edtAccount.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }

        // Show loading dialog
        progressDialog.show();

        try {
            // Get user by username
            User user = userDAO.getUserByUsername(username);

            if (user != null && user.getEmail().equals(email)) {
                // Success: Username and email match
                Intent intent = new Intent(ForgetPasswordActivity.this, UpdatePasswordActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else {
                // Failure: Username doesn't exist or email doesn't match
                Toast.makeText(this, "Invalid username or email!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Hide loading dialog
            progressDialog.dismiss();
        }
    }
}