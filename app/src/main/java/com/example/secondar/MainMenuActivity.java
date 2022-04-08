package com.example.secondar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenuActivity extends AppCompatActivity {
    private User user;
    private FirebaseUser firebaseUser;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        firebaseUser = intent.getParcelableExtra("firebaseUser");

        btnLogout = findViewById(R.id.btn_mainmenu_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });

    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();

        finish();

        SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void goToDecorate(View view) {
        Intent intent = new Intent(getApplicationContext(), ARDesignActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("firebaseUser", firebaseUser);
        startActivity(intent);
    }

    public void goToOhtersCreations(View view) {
        Intent intent = new Intent(getApplicationContext(), OthersCreationsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("firebaseUser", firebaseUser);
        startActivity(intent);
    }

    public void goToCatalogue(View view) {
        Intent intent = new Intent(getApplicationContext(), CatalogueActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("firebaseUser", firebaseUser);
        startActivity(intent);
    }

}
