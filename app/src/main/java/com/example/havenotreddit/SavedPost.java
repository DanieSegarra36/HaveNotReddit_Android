package com.example.havenotreddit;

import android.os.Parcel;
import android.os.Parcelable;

public class SavedPost implements Parcelable {
    private String title;
    private String author;
    private String subreddit;
    private int upvotes;
    private int downvotes;
    private String url;
    private String body;
    private String uID;

    public SavedPost (){
        this.title = " default Constructor title";
        this.author = " default Constructor author";
        this.subreddit = " default Constructor subreddit";
        this.upvotes = 0;
        this.downvotes = 0;
        this.url = " default Constructor url";
        this.body = " default Constructor body";
        this.uID = " default Constructor uID";
    }

    public SavedPost (String title, String author, String subreddit, int upvotes,
                      int downvotes, String url, String body, String uID ){
        this.title = title;
        this.author = author;
        this.subreddit = subreddit;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.url = url;
        this.body = body;
        this.uID = uID;

//        Log.d("class", getTitle()+" "+getAuthor()+" "+getBody());
    }

    public int getDownvotes() {
        return downvotes;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getURL() {
        return url;
    }

    public String getuID() {
        return uID;
    }

    private SavedPost(Parcel in) {
        title = in.readString();
        author = in.readString();
        subreddit = in.readString();
        upvotes = in.readInt();
        downvotes = in.readInt();
        url = in.readString();
        body = in.readString();
        uID = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return title + ": " + author + ": " + body;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(author);
        out.writeString(subreddit);
        out.writeInt(upvotes);
        out.writeInt(downvotes);
        out.writeString(url);
        out.writeString(body);
        out.writeString(uID);
    }

    public static final Parcelable.Creator<SavedPost> CREATOR = new Parcelable.Creator<SavedPost>() {
        public SavedPost createFromParcel(Parcel in) {
            return new SavedPost(in);
        }

        public SavedPost[] newArray(int size) {
            return new SavedPost[size];
        }
    };
}
