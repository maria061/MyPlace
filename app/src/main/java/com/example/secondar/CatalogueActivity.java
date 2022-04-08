package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CatalogueActivity extends AppCompatActivity {

    public class CatalogueItem
    {
        public String date;
        public String imageUrl;
    }

    private RecyclerView mRecyclerView;

    private User user;
    private FirebaseUser firebaseUser;
    private StorageReference storageRef;
    private DatabaseReference imageUrlStore;
    private String TAG = "CatalogueActivity";
    private ArrayList<CatalogueItem> mListCatalogue = new ArrayList<>();
    private CatalogueListAdapter mAdapter;
    private RelativeLayout mImageWrapper;
    private ImageView mPresentImageView;
    private int read_count;
    private String mActiveImageURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);
        Intent intent = getIntent();

        mRecyclerView = findViewById(R.id.gridView);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(manager);

        user = intent.getParcelableExtra("user");
        firebaseUser = intent.getParcelableExtra("firebaseUser");

        storageRef = FirebaseStorage.getInstance().getReference().child("images/" + firebaseUser.getUid());
//        imageUrlStore = FirebaseDatabase.getInstance().getReference("images").child(firebaseUser.getUid());

        mImageWrapper = findViewById(R.id.image_wrapper);
        mImageWrapper.setOnTouchListener((view, motionEvent) -> true);
        mPresentImageView = findViewById(R.id.image_present);

        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                final int count = listResult.getItems().size();
                read_count = 0;
                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                    Log.d(TAG, item.getDownloadUrl().toString());
                    item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            CatalogueItem catalogueItem = new CatalogueItem();

                            catalogueItem.imageUrl = uri.toString();
                            catalogueItem.date = catalogueItem.imageUrl.substring(catalogueItem.imageUrl.indexOf("MyPlace")+7,catalogueItem.imageUrl.indexOf(".png"));
                            read_count++;
                            mListCatalogue.add(catalogueItem);

                            if(read_count == 1)
                            {
                                mAdapter = new CatalogueListAdapter(mListCatalogue);
                                mRecyclerView.setAdapter(mAdapter);
                            }
                            else
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });

        Button btn = findViewById(R.id.btn_close);
        btn.setOnClickListener(view -> {
            mImageWrapper.setVisibility(View.GONE);
            mPresentImageView.setImageResource(android.R.color.transparent);
        });

        btn = findViewById(R.id.btn_post);
        btn.setOnClickListener(view -> {
            mImageWrapper.setVisibility(View.GONE);
            mPresentImageView.setImageResource(android.R.color.transparent);
            Intent _intent = new Intent(CatalogueActivity.this, ManageImagineActivity.class);
            _intent.putExtra("user", user);
            _intent.putExtra("imageURL", mActiveImageURL);
            startActivity(_intent);
        });

    }

    public class CatalogueListAdapter extends RecyclerView.Adapter<CatalogueListAdapter.ViewHolder> {
        final String TAG = getClass().getSimpleName();

        ArrayList<CatalogueItem> itemArrayList;
        /**
         * The interface that receives onClick messages.
         */

        public CatalogueListAdapter(ArrayList<CatalogueItem> dataList) {
            this.itemArrayList = dataList;
        }

        /**
         * Cache of the children views for a forecast list item.
         */

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView txtDate;
            public ImageView imageView;

            public ViewHolder(View v) {
                super(v);
                txtDate = v.findViewById(R.id.txtDate);
                imageView = v.findViewById(R.id.imageView);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int adapterPosition = getAdapterPosition();
                mImageWrapper.setVisibility(View.VISIBLE);
                Glide.with(CatalogueActivity.this).load(itemArrayList.get(adapterPosition).imageUrl).into(mPresentImageView);
                mActiveImageURL = itemArrayList.get(adapterPosition).imageUrl;
            }
        }

        @Override
        public CatalogueListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.catalogue_item, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.txtDate.setText(itemArrayList.get(position).date);
            Glide.with(CatalogueActivity.this).load(itemArrayList.get(position).imageUrl).into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            if (null == itemArrayList) return 0;
            return itemArrayList.size();
        }
    }
}