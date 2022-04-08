package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private User user;
    private EditText emailET;
    private EditText passwordET;
    private ProgressBar progressBar;
    private String email;
    private String password;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //if(mAuth.getCurrentUser() != null){
        //start mainChatActivity
        //}

        emailET = findViewById(R.id.login_email);
        passwordET = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progressBar_login);


    }

    public void login(View View) {
        email = emailET.getText().toString().trim();
        password = passwordET.getText().toString().trim();
        progressBar.setVisibility(android.view.View.VISIBLE);

        if(email != null && !email.isEmpty()) {
            if (password != null && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(android.view.View.GONE);

                                if (task.isSuccessful()) {
                                    finish();

                                    firebaseUser = mAuth.getCurrentUser();

                                    databaseUsers = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                                    if (databaseUsers != null) {
                                        databaseUsers.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                user = dataSnapshot.getValue(User.class);

                                                Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                                                intent.putExtra("user", user);
                                                intent.putExtra("firebaseUser", firebaseUser);
                                                startActivity(intent);

                                                //save account
                                                SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = pref.edit();
                                                editor.putString("username", email);
                                                editor.putString("password", password);
                                                editor.apply();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                } else {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

            } else {
                Toast.makeText(LoginActivity.this, R.string.emptyPassword, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(android.view.View.GONE);
            }
        }else{
            Toast.makeText(LoginActivity.this, R.string.emptyEmail, Toast.LENGTH_LONG).show();
            progressBar.setVisibility(android.view.View.GONE);
        }
    }
}
