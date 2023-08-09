package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.dao.UserFirebaseDAO;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountSetting extends AppCompatActivity {
    private FirebaseAuth mAuth;

    FirebaseUser currentUser;
    private static final String TAG = "EmailPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        EditText edtUsername, edtPhone, edtNewPassword, edtConfirmPassword, edtEmail;
        edtPhone = findViewById(R.id.edtPhone);
        edtUsername = findViewById(R.id.edtUserName);
        edtNewPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtComfirmPassword);
        edtEmail = findViewById(R.id.edtEmail);
        Button btnSave = findViewById(R.id.btnSave);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (bundle != null) {
            UserInfo userInfo = getUserInfo();
            edtUsername.setText(userInfo.getUsername().toString().trim());
            edtPhone.setText(userInfo.getPhone().toString().trim());
            edtEmail.setText(currentUser.getEmail().trim());
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserFirebaseDAO userFirebaseDAO = new UserFirebaseDAO(getApplicationContext());
                UserInfo userInfo = getUserInfo();
                String username = edtUsername.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String newPassword = edtNewPassword.getText().toString().trim();
                String confirmPassword = edtConfirmPassword.getText().toString().trim();
                if (phone.isEmpty() || email.isEmpty() || username.isEmpty())  {
                    Toast.makeText(AccountSetting.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
                else if (newPassword.isEmpty() && confirmPassword.isEmpty()){
                    userInfo.setUsername(username);
                    userInfo.setPhone(phone);
                    userFirebaseDAO.Update(userInfo);
                    currentUser.updateEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User email address updated.");
                                    }
                                }
                            });
                    Toast.makeText(AccountSetting.this, "Update thông tin thành công", Toast.LENGTH_SHORT).show();
                    Intent intentToMain = new Intent(AccountSetting.this, MainActivity.class);
                    startActivity(intentToMain);
                    finish();
                }
                else {
                    userInfo.setUsername(username);
                    userInfo.setPhone(phone);
                    userFirebaseDAO.Update(userInfo);
                    currentUser.updateEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User email address updated.");
                                    }
                                    else {
                                        Toast.makeText(AccountSetting.this, "Không thể đổi email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    currentUser.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User password updated.");
                                    }
                                    else {
                                        Toast.makeText(AccountSetting.this, "Không thể đổi mật khẩu", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    Toast.makeText(AccountSetting.this, "Update thông tin thành công", Toast.LENGTH_SHORT).show();
                    Intent intentToMain = new Intent(AccountSetting.this, MainActivity.class);
                    startActivity(intentToMain);
                    finish();
                }
            }
        });
    }
    private UserInfo getUserInfo() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        UserInfo userInfo = new UserInfo();
        if (bundle != null) {
            userInfo = (UserInfo) bundle.getSerializable("userInfo");
        }
        return userInfo;
    }
}