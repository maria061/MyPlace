package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class OthersCreationsActivity extends AppCompatActivity {

    public class OtherCreationItem
    {
        public String date;
        public String imageUrl;
        public String title;
        public String description;
        public String username;
    }
    private RecyclerView mRecyclerView;

    private User user;
    private FirebaseUser firebaseUser;
    private OtherCreationListAdapter mAdapter;
    private DatabaseReference postReference;
    private ArrayList<OtherCreationItem> mCreationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_creations);
        mRecyclerView = findViewById(R.id.listView);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 1);
        mRecyclerView.setLayoutManager(manager);
        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        firebaseUser = intent.getParcelableExtra("firebaseUser");

        postReference = FirebaseDatabase.getInstance().getReference("PublicPosts");
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getInfo((Map<String,Object>) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getInfo(Map<String,Object> posts) {

        ArrayList<Long> phoneNumbers = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : posts.entrySet()){

            //Get user map
            Map post = (Map) entry.getValue();
            //Get phone field and append to list

            OtherCreationItem item = new OtherCreationItem();
            item.title = (String)post.get("title");
            item.description = (String)post.get("description");
            item.date = (String)post.get("time");
            item.username = (String)post.get("username");
            item.imageUrl = (String)post.get("imageURL");
            mCreationList.add(item);
            if(mCreationList.size() == 1)
            {
                mAdapter = new OtherCreationListAdapter(mCreationList);
                mRecyclerView.setAdapter(mAdapter);
            }
            else
            {
                mAdapter.notifyDataSetChanged();
            }
        }

        System.out.println(phoneNumbers.toString());
    }

    public class OtherCreationListAdapter extends RecyclerView.Adapter<OtherCreationListAdapter.ViewHolder> {
        final String TAG = getClass().getSimpleName();

        ArrayList<OtherCreationItem> itemArrayList;
        /**
         * The interface that receives onClick messages.
         */

        public OtherCreationListAdapter(ArrayList<OtherCreationItem> dataList) {
            this.itemArrayList = dataList;
        }

        /**
         * Cache of the children views for a forecast list item.
         */

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView txtDate;
            public TextView txtUsername;
            public TextView txtTitle;
            public TextView txtDescription;
            public ImageView imageView;

            public ViewHolder(View v) {
                super(v);
                txtDate = v.findViewById(R.id.txtDate);
                txtTitle = v.findViewById(R.id.txtTitle);
                txtDescription = v.findViewById(R.id.txtDescription);
                txtUsername = v.findViewById(R.id.txtUsername);
                imageView = v.findViewById(R.id.imageView);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int adapterPosition = getAdapterPosition();
//                mImageWrapper.setVisibility(View.VISIBLE);
//                Glide.with(OthersCreationsActivity.this).load(itemArrayList.get(adapterPosition).imageUrl).into(mPresentImageView);
//                mActiveImageURL = itemArrayList.get(adapterPosition).imageUrl;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.othercreation_item, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            OtherCreationItem item = itemArrayList.get(position);
            holder.txtDate.setText(item.date);
            holder.txtTitle.setText(item.title);
            holder.txtDescription.setText(item.description);
            holder.txtUsername.setText(item.username);
            Glide.with(OthersCreationsActivity.this).load(item.imageUrl).into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            if (null == itemArrayList) return 0;
            return itemArrayList.size();
        }
    }
}