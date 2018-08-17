package com.example.mattb240.photogameapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FeedAdapter extends ArrayAdapter<JSONObject> {

    int vg;
    ArrayList<JSONObject> list;
    Context context;
    SharedPreferences sharedPrefs;
    public static final int DEFAULT_TIMEOUT_MS = 500000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1f;
    String tag_vote_request = "vote_request";
    Boolean votedUp;
    Boolean votedDown;


    public FeedAdapter(Context context, int vg, int id, ArrayList<JSONObject> list){
        super(context,vg, id, list);
        this.context=context;
        this.vg=vg;
        this.list=list;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(vg, parent, false);
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        TextView caption =(TextView)itemView.findViewById(R.id.caption);
        TextView category = (TextView)itemView.findViewById(R.id.category);
        ImageView photo = (ImageView)itemView.findViewById(R.id.photo);
        final ImageView likeBtn = (ImageView)itemView.findViewById(R.id.likebtn);
        final ImageView dislikeBtn = (ImageView)itemView.findViewById(R.id.dislikebtn);
        TextView views = (TextView)itemView.findViewById(R.id.no_of_views);
        TextView no_of_likes = (TextView)itemView.findViewById(R.id.no_of_likes);
        TextView no_of_dislikes = (TextView)itemView.findViewById(R.id.no_of_dislikes);
        try {
            caption.setText(list.get(position).getString("caption"));
            category.setText(list.get(position).getString("category"));
            String photoViews;
            if(list.get(position).getString("views") != "null"){
                 photoViews = list.get(position).getString("views");
            }else{
                 photoViews = "0";
            }
            final int photoId = list.get(position).getInt("photoId");
            final int userId = list.get(position).getInt("userId");
            views.setText(photoViews);
            final JSONObject userActivity = list.get(position).getJSONObject("user_photo_activity");

            final JSONObject act = list.get(position).getJSONObject("activity");
            no_of_likes.setText(String.valueOf(act.getInt("up_votes")));
            no_of_dislikes.setText(String.valueOf(act.getInt("down_votes")));

            if(userActivity.getInt("upVote") > 0){
                likeBtn.setImageResource(R.mipmap.like_active);
            }else if(userActivity.getInt("downVote") > 0) {
                dislikeBtn.setImageResource(R.mipmap.dislike_active);
            }
            Glide.with(context).load(context.getString(R.string.baseUrl)+"/uploads/"+list.get(position).getString("photo")).into(photo);

            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(userActivity.getInt("upVote") == 0 || userActivity.getInt("downVote") == 0){
                            String base_url = context.getString(R.string.baseUrl)+"/photos/vote";
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("photoId", String.valueOf(photoId));
                            params.put("userId", String.valueOf(userId));
                            params.put("vote_type", "up");
                            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                                    base_url, new JSONObject(params),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String code = response.getString("code");
                                                if(code.equals("100")) {
                                                    likeBtn.setImageResource(R.mipmap.like_active);
                                                }else{
                                                    String message = response.getString("error");
                                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                                }
                                            } catch (JSONException e) {
                                                Toast.makeText(context, "Something has totally gone wrong while resetting your password. Please try again.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("volley error", error.getMessage());
                                    if (error instanceof TimeoutError) {
                                        Toast.makeText(context, "Timeout error occurred. Please try again later", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "Something has totally gone wrong. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_vote_request);
                        }else{
                            Toast.makeText(context, "You already voted on this picture", Toast.LENGTH_LONG).show();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });

            dislikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(userActivity.getInt("upVote") == 0 || userActivity.getInt("downVote") == 0){
                            String base_url = context.getString(R.string.baseUrl)+"/photos/vote";
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("photoId", String.valueOf(photoId));
                            params.put("userId", String.valueOf(userId));
                            params.put("vote_type", "down");
                            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                                    base_url, new JSONObject(params),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String code = response.getString("code");
                                                if(code.equals("100")) {
                                                        dislikeBtn.setImageResource(R.mipmap.dislike_active);
                                                }else{
                                                    String message = response.getString("error");
                                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                                }
                                            } catch (JSONException e) {
                                                Toast.makeText(context, "Something has totally gone wrong while resetting your password. Please try again.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("volley error", error.getMessage());
                                    if (error instanceof TimeoutError) {
                                        Toast.makeText(context, "Timeout error occurred. Please try again later", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "Something has totally gone wrong. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_vote_request);
                        }else{
                            Toast.makeText(context, "You already voted on this picture", Toast.LENGTH_LONG).show();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }catch(JSONException e){
            e.printStackTrace();
        }
        return itemView;
    }

    public void vote(int photoId, String userId, final String vote_type){

    }

}