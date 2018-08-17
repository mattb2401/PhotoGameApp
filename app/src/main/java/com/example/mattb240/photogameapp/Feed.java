package com.example.mattb240.photogameapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by mattb240 on 8/14/18.
 */

public class Feed extends AppCompatActivity {

    ListView listView;
    ProgressDialog dialog;
    String tag_json_obj = "feed_request";
    public static final int DEFAULT_TIMEOUT_MS = 500000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1f;
    SharedPreferences sharedPrefs;
    ImageView uploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed);
        dialog = new ProgressDialog(this);
        listView = (ListView)findViewById(R.id.listView);
        uploadBtn = (ImageView)findViewById(R.id.uploadBtn);
        sharedPrefs = getSharedPreferences("photoGameApp", MODE_PRIVATE);
        fetchFeed();
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadImg = new Intent(Feed.this, UploadImage.class);
                startActivity(uploadImg);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchFeed();
    }

    public void fetchFeed(){
        dialog.setMessage("Fetching feed");
        dialog.show();
        String base_url = getString(R.string.baseUrl)+"/photos/feed";
        Map<String, String> params = new HashMap<String, String>();
        int userId = sharedPrefs.getInt("userId", 0);
        Log.d("UserId", String.valueOf(userId));
        params.put("userId", String.valueOf(userId));
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                base_url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            String code = response.getString("code");
                            if (code.equals("100")) {
                                JSONArray photos = response.getJSONArray("photos");
                                ArrayList<JSONObject> listItems = new ArrayList<JSONObject>();
                                try {
                                    if (photos != null) {
                                        if(photos.length() > 0) {
                                            for (int i = 0; i < photos.length(); i++) {
                                                listItems.add(photos.getJSONObject(i));
                                            }
                                        }else {
                                            Toast.makeText(Feed.this, "Feed couldn't be loaded.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }catch (JSONException je){je.printStackTrace();}
                                FeedAdapter feedsAdapter = new FeedAdapter(Feed.this, R.layout.feed_item, R.id.caption, listItems);
                                listView.setAdapter(feedsAdapter);
                            } else {
                                Toast.makeText(Feed.this, "Feed couldn't be loaded", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.d("Json error", e.getMessage());
                            Toast.makeText(Feed.this, "Feed couldn't be loaded", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error response", error.toString());
                dialog.dismiss();
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(Feed.this, "Feed couldn't be loaded result timeout / no connection error", Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(Feed.this, "Feed couldn't be loaded result auth failure error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(Feed.this, "Feed couldn't be loaded result server error", Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(Feed.this, "Feed couldn't be loaded result network error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(Feed.this, "Feed couldn't be loaded result parse error", Toast.LENGTH_LONG).show();
                }
            }
        });
        jsonObjReq.setShouldCache(false);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }
}
