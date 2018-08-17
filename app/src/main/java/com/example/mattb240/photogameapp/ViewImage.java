package com.example.mattb240.photogameapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mattb240 on 8/17/18.
 */

public class ViewImage extends AppCompatActivity {

    SharedPreferences sharedPrefs;
    String tag_json_obj = "view_image_request";
    public static final int DEFAULT_TIMEOUT_MS = 500000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1f;
    ImageView backbtn;
    ImageView ph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);
        String photoId = getIntent().getStringExtra("photoId");
        String photo = getIntent().getStringExtra("photo");
        ph = (ImageView)findViewById(R.id.photo);
        sharedPrefs = getSharedPreferences("photoGameApp", MODE_PRIVATE);
        addView(photoId);
        Glide.with(ViewImage.this).load(ViewImage.this.getString(R.string.baseUrl)+"/uploads/"+photo).into(ph);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent feed = new Intent(ViewImage.this, Feed.class);
                startActivity(feed);
            }
        });
    }

    public void addView(String photoId){
        String base_url = getString(R.string.baseUrl)+"/photos/addView";
        Map<String, String> params = new HashMap<String, String>();
        params.put("photoId", photoId);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                base_url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String code = response.getString("code");
                            if (code.equals("100")) {

                            } else {
                                Toast.makeText(ViewImage.this, "View couldn't be added", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ViewImage.this, "View couldn't be added", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(ViewImage.this, "View couldn't be added result timeout / no connection error", Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(ViewImage.this, "View couldn't be added result auth failure error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(ViewImage.this, "View couldn't be added result server error", Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(ViewImage.this, "View couldn't be added result network error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(ViewImage.this, "View couldn't be added result parse error", Toast.LENGTH_LONG).show();
                }
            }
        });
        jsonObjReq.setShouldCache(false);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }
}
