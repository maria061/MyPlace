package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {


    private TextView loginTV;
    private TextView registerTV;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseUsers;
    private User user;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginTV = (TextView) findViewById(R.id.login_tv);
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        registerTV = (TextView) findViewById(R.id.register_tv);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
            }
        });

        //mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String email = pref.getString("username", "");
        String password = pref.getString("password", "");

        if(!email.equals("") && !password.equals(""))
        {
            progressBar = findViewById(R.id.progressBar_login);
            progressBar.setVisibility(View.VISIBLE);
            enableButtons(false);

            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(android.view.View.GONE);

                            if (task.isSuccessful()) {


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

                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                            enableButtons(true);
                                        }
                                    });
                                }

                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                enableButtons(true);
                            }
                        }
                    });
        }
    }
    private void enableButtons(boolean enabled)
    {
        loginTV.setEnabled(enabled);
        registerTV.setEnabled(enabled);
        if(enabled)
            progressBar.setVisibility(View.GONE);
        else
            progressBar.setVisibility(View.VISIBLE);
    }

   /* @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            finish();
            Intent intent = new Intent(this, MainMenuActivity.class);


        }
    }*/

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
