package com.kogo.itodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.kogo.itodo.databinding.ActivityRegisterBinding;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private ActivityRegisterBinding registerBinding;
    private ProgressDialog loader;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(registerBinding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // dark mode cancel
        dialog = new Dialog(RegisterActivity.this);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            finish();
            return;
        }

        loader = new ProgressDialog(this);

        registerBinding.buttonRegister.setOnClickListener(view -> {
            if (isNetworkAvailable(RegisterActivity.this)){
                loader.dismiss();
                registerUser();
            }
            else {
                loader.setMessage("No internet, check your internet connection!");
                loader.show();
                return;
            }
        });
        registerBinding.textViewLogin.setOnClickListener(view -> { switchToLogin(); });
        registerBinding.imageViewVisibleOnOff.setOnClickListener(view -> {
            if (registerBinding.editTextPassword.getInputType() == 144){    // 144 mean is that if we can see the password now
                registerBinding.editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  // close the password
                registerBinding.editTextPasswordVerify.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  // close the password
                registerBinding.imageViewVisibleOnOff.setImageResource(R.drawable.visible_off);
            }
            else {
                registerBinding.editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);      // show the password
                registerBinding.editTextPasswordVerify.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);      // show the password
                registerBinding.imageViewVisibleOnOff.setImageResource(R.drawable.visible);
            }
            registerBinding.editTextPassword.setSelection(registerBinding.editTextPassword.length());   // set cursor position end of the password
            registerBinding.editTextPasswordVerify.setSelection(registerBinding.editTextPasswordVerify.length());   // set cursor position end of the password
        });

        registerBinding.imageViewInfoPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog.setContentView(R.layout.password_valid_info);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button buttonOkPasswordInfo=dialog.findViewById(R.id.buttonOkPasswordInfo);

                buttonOkPasswordInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }

    private void registerUser(){
        String username = registerBinding.editTextUsername.getText().toString().trim();
        String email = registerBinding.editTextEmail.getText().toString().trim();
        String password = registerBinding.editTextPassword.getText().toString().trim();
        String passwordVerify = registerBinding.editTextPasswordVerify.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordVerify.isEmpty()){
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isValidEmail(email)){
            Toast.makeText(this, "Please check email!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(passwordVerify)){
            Toast.makeText(this, "Please write same password!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isValidPassword(password) || !isValidPassword(passwordVerify)){
            Toast.makeText(this, "Please define stronger password!", Toast.LENGTH_LONG).show();

            return;
        }
        else{

            loader.setMessage("Loading . . .");
            loader.setCanceledOnTouchOutside(false);
            loader.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                User user = new User(username, email ,password);
                                // save the database
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        showMainActivity();
                                        loader.dismiss();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                                loader.dismiss();
                            }
                        }
                    });
        }
    }

    private void showMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static boolean isValidEmail(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static boolean isValidPassword(String password){

        String passwordRegex =  "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        Pattern pat = Pattern.compile(passwordRegex);
        if (password == null)
            return false;
        return pat.matcher(password).matches();

    }

}