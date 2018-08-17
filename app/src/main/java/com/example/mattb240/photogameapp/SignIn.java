package com.example.mattb240.photogameapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by mattb240 on 8/13/18.
 */

public class SignIn extends AppCompatActivity {

    EditText username;
    EditText password;
    Button signInBtn;
    String tag_json_obj = "sign_in_request";
    private ProgressDialog dialog;
    public static final int DEFAULT_TIMEOUT_MS = 500000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1f;
    SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sigin);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        dialog = new ProgressDialog(this);
        signInBtn = (Button)findViewById(R.id.signin_btn);
        sharedPrefs = getSharedPreferences("photoGameApp", MODE_PRIVATE);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setMessage("Please wait...");
                dialog.show();
                String base_url = getString(R.string.baseUrl)+"/users/authenticate";
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        base_url, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("resp", response.toString());
                                dialog.dismiss();
                                try {
                                    String code = response.getString("code");
                                    if(code.equals("100")) {
                                        JSONObject user = response.getJSONObject("user");
                                        SharedPreferences.Editor editor = sharedPrefs.edit();
                                        editor.putInt("userId", user.getInt("id"));
                                        editor.putBoolean("isLoggedIn", true);
                                        editor.apply();
                                        Intent feed = new Intent(SignIn.this, Feed.class);
                                        startActivity(feed);
                                        finish();
                                    }else{
                                        String message = response.getString("error");
                                        Toast.makeText(SignIn.this, message, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(SignIn.this, "Something has totally gone wrong while resetting your password. Please try again.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("volley error", error.getMessage());
                        dialog.dismiss();
                        if (error instanceof TimeoutError) {
                            Toast.makeText(SignIn.this, "Timeout error occurred. Please try again later", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignIn.this, "Something has totally gone wrong. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
            }
        });
    }

}
