package com.kogo.itodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kogo.itodo.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityLoginBinding loginBinding;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            finish();
            return;
        }

        loader = new ProgressDialog(this);
        loginBinding.buttonLogin.setOnClickListener(view -> {
            if (isNetworkAvailable(LoginActivity.this)){
                loader.dismiss();
                authenticateUser();
            }
            else {
                loader.setMessage("No internet, check your internet connection!");
                loader.show();
                return;
            }
        });
        loginBinding.textViewRegister.setOnClickListener(view -> { switchToRegister(); });
        loginBinding.imageViewVisibleOnOff.setOnClickListener(view -> {
            if (loginBinding.editTextPassword.getInputType() == 144){    // 144 mean is that if we can see the password now
                loginBinding.editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  // close the password
                loginBinding.imageViewVisibleOnOff.setImageResource(R.drawable.visible_off);
            }
            else {
                loginBinding.editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);      // show the password
                loginBinding.imageViewVisibleOnOff.setImageResource(R.drawable.visible);
            }
            loginBinding.editTextPassword.setSelection(loginBinding.editTextPassword.length());   // set cursor position end of the password
        });

    }

    private void authenticateUser(){
        String email = loginBinding.editTextEmail.getText().toString();
        String password = loginBinding.editTextPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(LoginActivity.this, "Please fill all fields.", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            loader.setMessage("Loading . . .");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showMainActivity();
                                loader.dismiss();
                            } else {
                                Toast.makeText(LoginActivity.this, "Not found any user", Toast.LENGTH_LONG).show();
                                loader.dismiss();
                            }
                        }
                    });
        }

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    private void switchToRegister(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}