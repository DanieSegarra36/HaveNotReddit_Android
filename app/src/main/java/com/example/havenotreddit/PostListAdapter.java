package com.example.havenotreddit;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class PostListAdapter extends ArrayAdapter<SavedPost> {

// class extending ArrayAdapter which will be holding a list of SavedPost objects

    public PostListAdapter(Context context, int resource, ArrayList<SavedPost> values) {
        // call super passing the custom row layout and SavedPost list
        super(context, resource, values);
    }

    // Override getView to populate the view correctly.
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_layout, parent, false);
        }

        TextView postTitle = (TextView) convertView.findViewById(R.id.Title);
        TextView postAuthor = (TextView) convertView.findViewById(R.id.Author);
        TextView postSubreddit = (TextView) convertView.findViewById(R.id.Subreddit);
        TextView postBody = (TextView) convertView.findViewById(R.id.Body);
        TextView postUpvotes = (TextView) convertView.findViewById(R.id.Upvotes);
        TextView postDownvotes = (TextView) convertView.findViewById(R.id.Downvotes);
        TextView postURL = (TextView) convertView.findViewById(R.id.URL);

        postTitle.setText(getItem(position).getTitle());
        postAuthor.setText("by: "+getItem(position).getAuthor());
        postSubreddit.setText("\ton: r/" + getItem(position).getSubreddit());
        postBody.setText("\n"+getItem(position).getBody()+"\n\n");
        postUpvotes.setText("Upvotes: " + getItem(position).getUpvotes());
        postDownvotes.setText("\tDownvotes: " + getItem(position).getDownvotes());
        postURL.setText("View on Reddit: " + getItem(position).getURL() + "\n\n\n");


        return convertView;
    }
}