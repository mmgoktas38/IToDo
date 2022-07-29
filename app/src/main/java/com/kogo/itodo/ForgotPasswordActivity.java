package com.kogo.itodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kogo.itodo.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding forgotPasswordBinding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forgotPasswordBinding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(forgotPasswordBinding.getRoot());

        auth = FirebaseAuth.getInstance();
        forgotPasswordBinding.buttonResetPassword.setOnClickListener(view -> {
            resetPassword();
        });
    }

    private void resetPassword() {
        String email = forgotPasswordBinding.editTextEmailForgotPassword.getText().toString().trim();

        if (email.isEmpty()) {
            forgotPasswordBinding.editTextEmailForgotPassword.setError("Email is required!");
            forgotPasswordBinding.editTextEmailForgotPassword.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            forgotPasswordBinding.editTextEmailForgotPassword.setError("Please provide valid email!");
            forgotPasswordBinding.editTextEmailForgotPassword.requestFocus();
            return;
        }

        forgotPasswordBinding.progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this,"Check your email (spam) to reset your password!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(ForgotPasswordActivity.this,"Try again! Something wrong happened! ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}