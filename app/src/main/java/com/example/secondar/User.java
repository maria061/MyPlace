package com.example.secondar;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class User implements Parcelable {
    private String username;
    private int noFollowers; //number of ppl that follow this user
    private List<String> subscriptions; // UIDs of the users followed by this user

    public User(){

    }

    public User(String username) {
        this.username = username;
    }

    protected User(Parcel in) {
        username = in.readString();
        noFollowers = in.readInt();
        subscriptions = in.createStringArrayList();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNoFollowers() {
        return noFollowers;
    }

    public void setNoFollowers(int noFollowers) {
        this.noFollowers = noFollowers;
    }

    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", noFollowers=" + noFollowers +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeInt(noFollowers);
        dest.writeStringList(subscriptions);
    }

    //used when the user starts following another user
    public void addSubscription(String newUID){
        this.subscriptions.add(newUID);
    }

    //used when another user starts following this user
    public void addNewFollower(){
        this.noFollowers++;
    }
}
