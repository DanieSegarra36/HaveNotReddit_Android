package com.example.havenotreddit;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.example.havenotreddit.SavedPost;
import java.lang.reflect.Array;
import java.util.ArrayList;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ArrayList<SavedPost> mySavedPosts = new ArrayList<>();
    private PostListAdapter adapter;
    private ListView postView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new PostListAdapter(this , R.layout.row_layout, mySavedPosts);
        postView = (ListView) findViewById(R.id.postView);
        postView.setAdapter(adapter);
    }

    private static final String TAG = "MainActivity";

    // Client app ID to authenticate API usage
    private static final String CLIENT_ID = "BeZEouoSZmOJBQ";

    // Due to this being an installed app that cannot securely store an API secret
    // We therefore forward/append the response to our access token request to the end of this REDIRECT_URI
    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";

    // Random string that developer uses
    // This string is used to verify the access token response
    // The REDIRECT_URI should return the same string
    private static final String STATE = "RANDOM_STRING";

    // URL to get user to LOG INTO REDDIT
    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact" +
                    "?client_id=" + CLIENT_ID +
                    "&response_type=code" +
                    "&state=" + STATE +
                    "&redirect_uri=" + REDIRECT_URI +
                    "&duration=temporary" +
                    "&scope=history identity save mysubreddits";

    // URL to get user token and act on behalf of them
    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token/";

    // Unique identifier to send with access token request (again because this app cannot safely store an API secret)
    private static final String DEVICE_ID = UUID.randomUUID().toString();

    // URL to make all OAUTH2 API calls (calls that require user authentication)
    private static final String OAUTH_URL =
            "https://oauth.reddit.com";

    // Function to initiate sign in process (send user to Reddit sign in)
    public void startSignIn(View view) {
        String url = String.format(AUTH_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();

            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e(TAG, "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(STATE)) {
                    String code = uri.getQueryParameter("code");
                    getAccessToken(code);
                }
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("posts", mySavedPosts);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mySavedPosts = savedInstanceState.getParcelableArrayList("posts");

//        if (mySavedPosts.size() > 0) {
//            Button button = (Button) findViewById(R.id.signin);
//            button.setVisibility(View.GONE);
//        }
    }

    private void getAccessToken(String code) {
        OkHttpClient client = new OkHttpClient();

        // Due to this being an installed app that cannot securely store an API secret
        // We authenticate the app/client with a public CLIENT_ID and a null/empty PASSWORD
        String authString = CLIENT_ID + ":" + "";

        // Encode string to ensure characters aren't misrepresented (spaces and other special characters)
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        // Build HTTP request
        Request request = new Request.Builder()
                .addHeader("User-Agent", "android:com.example.havenotreddit:v1.0 (by /u/atribecalleddaniel)")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "&grant_type=authorization_code" +
                                "&device_id="+ DEVICE_ID +
                                "&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI +
                                "&client_id=" + CLIENT_ID +
                                "&state=" + STATE +
                                "&redirect_uri=" + REDIRECT_URI +
                                "&duration=temporary" +
                                "&scope=history identity save"))
                .build();

        // Make HTTP request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");

                    Log.d(TAG, "Access Token = " + accessToken);

                    getMe(accessToken);
                } catch (JSONException e) {
                    Log.d("ERROR: ", "Authentication failed. No token received.");
                    e.printStackTrace();
                }
            }
        });
    }

    private void getMe(String token){
        final String send = token;

        Log.d("getMe() token: ", token);

        OkHttpClient client = new OkHttpClient();

        // Build HTTP request
        Request request = new Request.Builder()
                .addHeader("User-Agent", "android:com.example.havenotreddit:v1.0 (by /u/atribecalleddaniel)")
                .addHeader("Authorization", "Bearer " + token)
                .url("https://oauth.reddit.com/api/v1/me")
                .build();

        // Make HTTP request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    Log.d("JSON RESPONSE: ", json);
                    String name = data.optString("name");
                    Log.d("Name:", name);
                    getSaved(send, name, 0, "");
//                    }
                } catch (JSONException e) {
                    Log.d("ERROR: ", "Could not get saved posts.");
                    e.printStackTrace();
                }
            }
        });
    }

    private void getSaved(final String token, final String name, final int pos, final String lastID){

        // Max is 100 at a time
        String URL = OAUTH_URL + "/user/" + name + "/saved" + "?limit=100";

        // The query itself is a listing divided into pages
        // The 'after' param is use for pagination and tells the request where to resume fetching from
        // Only apply 'after' if we have passed first batch/set of posts since there is no where to resume from on it fetch
        URL = pos != 0 ? URL + "&after=" + lastID : URL;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("User-Agent", "android:com.example.havenotreddit:v1.0 (by /u/atribecalleddaniel)")
                .addHeader("Authorization", "Bearer " + token)
                .url(URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json).getJSONObject("data");;

                    // Array of posts from JSON
                    JSONArray savedSubreddits = data.getJSONArray("children");

                    for (int i = 0; i < savedSubreddits.length(); i++) {
                        try {
                            // Individual post from JSON
                            JSONObject newPost = savedSubreddits.getJSONObject(i).getJSONObject("data");

                            // POST DATA
                            final String title = newPost.has("title") ? newPost.getString("title") : "No Title";
                            final String author = newPost.has("author") ? newPost.getString("author") : "No author";
                            final String subreddit = newPost.has("subreddit") ? newPost.getString("subreddit") : "No subreddit";
                            final int upvotes = newPost.has("ups") ? Integer.parseInt(newPost.getString("ups")) : 0;
                            final int downvotes = newPost.has("downs") ? Integer.parseInt(newPost.getString("downs")) : 0;
                            final String url = newPost.has("url") ? newPost.getString("url") : "No URL";
                            final String body = newPost.has("selftext") ? newPost.getString("selftext") : "No post body";
                            final String uID = newPost.has("name") ? newPost.getString("name") : "No post ID";

                            // Update ListView on main UI thread
                            // Disable sign in button on main UI thread
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mySavedPosts.add(new SavedPost(title, author, subreddit, upvotes, downvotes, url, body, uID));
                                    adapter.notifyDataSetChanged();
                                    Button button = (Button) findViewById(R.id.signin);
                                    button.setVisibility(View.GONE);
                                }
                            });

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    try {
                        // Run at most 10 times to get max of 100 'saved' posts (max that reddit stores for you)
                        // Use last ID for pagination of saved posts (i.e. fetch posts saved before post with 'lastID', the last post we fetched)
                        if (pos != 900 && savedSubreddits.length() > 0) {
                            getSaved(token, name, pos + 100, savedSubreddits.getJSONObject(savedSubreddits.length()-1).getJSONObject("data").getString("name"));
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}