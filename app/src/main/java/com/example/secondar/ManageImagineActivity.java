package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ManageImagineActivity extends AppCompatActivity {
    private User user;
    private String username;
    private String imageURL;
    private EditText etTitle;
    private EditText etDescription;
    private DatabaseReference postReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_imagine);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        username = user.getUsername();

        imageURL = intent.getStringExtra("imageURL");

        postReference = FirebaseDatabase.getInstance().getReference("PublicPosts");

        etTitle = findViewById(R.id.ET_image_title);
        etDescription = findViewById(R.id.ET_image_description);
    }

    public void publishImage(View view) {
        //create post
        final String title = etTitle.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String time = currentDate + " " + currentTime;

        PublicPost post = new PublicPost(username, time, imageURL, 0);

        if ( title != null){
            post.setTitle(title);
        }

        if ( description != null){
            post.setDescription(description);
        }

        //publish the post in the database
        try {
            postReference.push().setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManageImagineActivity.this, R.string.success_image_published, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ManageImagineActivity.this, R.string.failed_image_publish, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        catch (Exception ex)
        {
            Log.d("Exception", ex.toString());
        }
    }

    public void deleteImage(View view) {

    }
}
