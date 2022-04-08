package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPass;
    private EditText editTextRepeatPass;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editTextUsername = findViewById(R.id.ET_reg_username);
        editTextEmail = findViewById(R.id.ET_reg_email);
        editTextPass = findViewById(R.id.ET_reg_password);
        editTextRepeatPass = findViewById(R.id.ET_reg_passwordRepeat);
        progressDialog = new ProgressDialog(this);
    }

    private boolean regexValidation(String regexPattern, String textToCheck) {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(textToCheck);

        return matcher.matches();
    }

    private boolean usernameAlreadyExists (String username){
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("NameUidCorrespondences").child(username);
        final boolean[] dataExists = new boolean[1];
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    dataExists[0] = true;
                }else {
                    dataExists[0] = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return dataExists[0];
    }

    //this function returns:
    //1 - valid username
    //2 - this username contains invalid characters (valid characters: letters, numbers and _)
    //3 - this username is too short (less than 4 characters)
    //4 - this username is too long  (more than 20 characters)
    //5 - this username already exists
    private int usernameValidation(String username) {
        //verify if the username already exists
        if (!regexValidation("\\w+", username))//username contains non-word characters
            return 2;
        else if (username.length() < 4)
            return 3;
        else if (username.length() > 20)
            return 4;
        else if (usernameAlreadyExists(username))
            return 5;
        else return 1;

    }

    //this function returns:
    //1 - valid password
    //2 - this password doesn't have a valid format (lowercase and uppercase letters, numbers and at least one special character)
    //3 - this password is too short (less than 6 characters)
    //4 - this password is too long (more than 30 characters)
    private int passwordValidation(String password) {

        Pattern lowerLetters = Pattern.compile("[a-z]");
        Pattern upperLetters = Pattern.compile("[A-Z]");
        Pattern digits = Pattern.compile("[0-9]");
        Pattern specials = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

        Matcher hasLowerLetter = lowerLetters.matcher(password);
        Matcher hasUpperLetter = upperLetters.matcher(password);
        Matcher hasDigit = digits.matcher(password);
        Matcher hasSpecial = specials.matcher(password);

        if (!(hasLowerLetter.find() && hasUpperLetter.find() && hasDigit.find() && hasSpecial.find()))
            return 2;
        else if (password.length() < 6)
            return 3;
        else if (password.length() > 30)
            return 4;
        else
            return 1;
    }

    public void register(View view) {
        final String username = editTextUsername.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String pass = editTextPass.getText().toString().trim();
        String repeatPass = editTextRepeatPass.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            //username is empty
            Toast.makeText(this, R.string.empty_username, Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(this, R.string.empty_email, Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            //password is empty
            Toast.makeText(this, R.string.empty_password, Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(repeatPass)) {
            //repeat password is empty
            Toast.makeText(this, R.string.empty_rep_password, Toast.LENGTH_LONG).show();
            return;
        }

        switch (usernameValidation(username)) {
            case 1:
                break;
            case 2:
                Toast.makeText(this, R.string.username_format, Toast.LENGTH_LONG).show();
                return;
            case 3:
                Toast.makeText(this, R.string.username_2short, Toast.LENGTH_LONG).show();
                return;
            case 4:
                Toast.makeText(this, R.string.username_2long, Toast.LENGTH_LONG).show();
                return;
            case 5:
                Toast.makeText(this, R.string.username_alrd_exists, Toast.LENGTH_LONG).show();
                return;
        }

        if (!regexValidation("[\\w+\\.]+@[a-zA-Z0-9-]+\\.[a-zA-Z]+", email)) {
            //email doesn't have an email format
            Toast.makeText(this, R.string.inv_email_format, Toast.LENGTH_LONG).show();
            return;
        }

        switch (passwordValidation(pass)) {
            case 1:
                break;
            case 2:
                Toast.makeText(this, R.string.inv_password_format, Toast.LENGTH_LONG)
                        .show();
                return;
            case 3:
                Toast.makeText(this, R.string.password_2short, Toast.LENGTH_LONG).show();
                return;
            case 4:
                Toast.makeText(this, R.string.password_2long, Toast.LENGTH_LONG).show();
                return;
        }

        if (!repeatPass.equals(pass)) {
            Toast.makeText(this, R.string.pass_dont_match, Toast.LENGTH_LONG).show();
            return;
        }


        progressDialog.setMessage(getString(R.string.creating_acc));
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //store the additional fields in firebase database
                            final User user = new User(username);
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                finish();

                                                FirebaseDatabase.getInstance().getReference("NameUidCorrespondences")
                                                        .child(username)
                                                        .setValue(uid)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    finish();
                                                                    Toast.makeText(RegisterActivity.this, R.string.registration_success, Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                                                                    intent.putExtra("user", user);
                                                                    intent.putExtra("firebaseUser", mAuth.getCurrentUser());
                                                                    startActivity(intent);

                                                                    //save account
                                                                    SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = pref.edit();
                                                                    editor.putString("username", email);
                                                                    editor.putString("password", pass);
                                                                    editor.apply();
                                                                } else {
                                                                    Toast.makeText(RegisterActivity.this, R.string.registration_failure, Toast.LENGTH_LONG).show();
                                                                }
                                                            }

                                                        });

                                            } else {
                                                Toast.makeText(RegisterActivity.this, R.string.registration_failure, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                        } else
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }
                });

    }

}
