package com.example.havenotreddit;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

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
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        FirebaseApp.initializeApp(this);
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference myRef = database.getReference("alarms");
//
//        // listens for new realtime database events and updates ui
//        ValueEventListener alarmListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                dataList.clear();
//                adapter.notifyDataSetChanged();
//                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
//                    Log.d("snap_id: ", singleSnapshot.getKey());
//                    String snapId = singleSnapshot.getKey();
//                    cryptoAlarm oneAlarm = singleSnapshot.getValue(cryptoAlarm.class);
//                    oneAlarm.setId(snapId);
//                    dataList.add(oneAlarm);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w("fetch alarm", "loadPost:onCancelled",databaseError.toException());
//            }
//        };
//
//        myRef.addListenerForSingleValueEvent(alarmListener);
//        myRef.addValueEventListener(alarmListener);
    }

    private static final String TAG = "MainActivity";

    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact?client_id=BeZEouoSZmOJBQ&response_type=code&state=RANDOM_STRING&redirect_uri=http://www.example.com/my_redirect&duration=temporary&scope=history identity save mysubreddits";
//            "https://www.reddit.com/api/v1/authorize.compact?client_id=BeZEouoSZmOJBQ&response_type=code&state=RANDOM_STRING&redirect_uri=http://www.example.com/my_redirect&duration=temporary&scope=history";

    private static final String CLIENT_ID = "BeZEouoSZmOJBQ";

    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";

    private static final String STATE = "RANDOM_STRING";

    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token/";

    private static final String OAUTH_URL =
            "https://oauth.reddit.com";

    private static final String DEVICE_ID = UUID.randomUUID().toString();

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
                Log.d("MY_FUCKING_MESSAGE","A REALLY LONG STRING\nTHAT WILL MAKE IT OBVIOUS\nTHAT I WANT TO SEE\n THIS MESSAGE.\nGOT A TOKEN");
                Log.d("STATE MSG","before getting state");
                String state = uri.getQueryParameter("state");
                Log.d("Are we stuck here", state);
                Log.d("Are we stuck here", STATE);
                if(state.equals(STATE)) {
                    String code = uri.getQueryParameter("code");
                    Log.d("STATE MSG","States MATCH");
                    Log.d("Passing 'code'",code);
                    getAccessToken(code);
                }else{
                    Log.d("STATE MSG","States do not match");
                }
            }
        }
    }

    private void getAccessToken(String code) {
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":" + "";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);
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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "this our fam ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    Log.d(TAG, "Access Token = " + accessToken);
                    Log.d(TAG, "Refresh Token = " + refreshToken);
                    getMe(accessToken);
                } catch (JSONException e) {
                    Log.d("we fucked up", "didn't get them tokens");
                    e.printStackTrace();
                }
            }
        });
    }

    private void getMe(String token){
        final String send = token;
        Log.d("getMe(): ", "Entered getSaved()");
        Log.d("getMe() token: ", token);
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":" + "";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);
        Request request = new Request.Builder()
                .addHeader("User-Agent", "android:com.example.havenotreddit:v1.0 (by /u/atribecalleddaniel)")
                .addHeader("Authorization", "Bearer " + token)
//                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url("https://oauth.reddit.com/api/v1/me")
//                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
//                        "&grant_type=https://oauth.reddit.com/grants/installed_client" +
//                                "&device_id="+DEVICE_ID+
//                                "&redirect_uri=" + REDIRECT_URI +
//                                "&client_id=" + CLIENT_ID +
//                                "&state=" + STATE +
//                                "&redirect_uri=" + REDIRECT_URI))
                .build();
        Log.d("getMe(): ", "built request");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "this our fam ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("getMe(): ", "onResponse()");
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    Log.d("JSON RESPONSE: ", json);
                    String name = data.optString("name");
                    Log.d("Name:", name);
                    getSaved(send, name);
//                    }
                } catch (JSONException e) {
                    Log.d("we fucked up", "could not get posts");
//                    e.printStackTrace();
                }
            }
        });
        Log.d("getMe(): ", "Leaving getSaved()?");
    }

    private void getSaved(String token, String name){

        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":" + "";
        Request request = new Request.Builder()
                .addHeader("User-Agent", "android:com.example.havenotreddit:v1.0 (by /u/atribecalleddaniel)")
                .addHeader("Authorization", "Bearer " + token)
                .url("https://oauth.reddit.com/user/"+name+"/saved")
                .build();
        Log.d("getSaved(): ", "built request");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "this our fam ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("getSaved(): ", "onResponse()");
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    Log.d("JSON RESPONSE: ", json);

//                    data = data.getJSONObject("data");
//                    JSONArray savedSubreddits = data.getJSONArray("children");
//                    for (int i = 0; i < savedSubreddits.length(); i++) {
//                        Log.d("Loop:", "another item in savedSubreddits");
//                        JSONObject topic = savedSubreddits.getJSONObject(i).getJSONObject("data");
//
//                        String author = topic.getString("author");
//                        String imageUrl = topic.getString("thumbnail");
//                        String postTime = topic.getString("created_utc");
//                        String rScore = topic.getString("score");
//                        String title = topic.getString("title");
//
//                        topicdata.add(new ListData(title, author, imageUrl, postTime, rScore));
//                        Log.v(DEBUG_TAG, topicdata.toString());
//                    }
                } catch (JSONException e) {
                    Log.d("we fucked up", "could not get posts");
                }
            }
        });
        Log.d("getSaved(): ", "Leaving getSaved()?");
    }
}
