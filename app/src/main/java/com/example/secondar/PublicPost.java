package com.example.secondar;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class PublicPost implements Serializable {
    public String username;
    public String time;
    public String imageURL;
    public int noHearts;
    public String title;
    public String description;

    public PublicPost(){}

    public PublicPost(String username, String time, String imageURL, int noHearts) {
        this.username = username;
        this.time = time;
        this.imageURL = imageURL;
        this.noHearts = noHearts;
    }

    protected PublicPost(Parcel in) {
        username = in.readString();
        time = in.readString();
        imageURL = in.readString();
        noHearts = in.readInt();
        title = in.readString();
        description = in.readString();
    }

    /*
    public static final Creator<PublicPost> CREATOR = new Creator<PublicPost>() {
        @Override
        public PublicPost createFromParcel(Parcel in) {
            return new PublicPost(in);
        }

        @Override
        public PublicPost[] newArray(int size) {
            return new PublicPost[size];
        }
    };
    */

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(time);
        dest.writeString(imageURL);
        dest.writeInt(noHearts);
        dest.writeString(title);
        dest.writeString(description);
    }
    */

    @Override
    public String toString() {
        return "PublicPost{" +
                "imageURL='" + imageURL + '\'' +
                '}';
    }

    public void addHeart(){
        this.noHearts ++;
    }
}
