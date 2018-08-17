package com.example.mattb240.photogameapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mattb240 on 8/16/18.
 */

public class UploadImage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ProgressDialog dialog;
    String tag_json_obj = "feed_request";
    public static final int DEFAULT_TIMEOUT_MS = 500000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1f;
    SharedPreferences sharedPrefs;
    ImageView photoBtn;
    EditText caption;
    EditText description;
    EditText location;
    String userChoosenTask = "";
    int SELECT_FILE = 22;
    int REQUEST_CAMERA = 33;
    String pic = "";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    Button upload_btn;
    Spinner category;
    String cat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_photo);
        caption = (EditText)findViewById(R.id.caption);
        description = (EditText)findViewById(R.id.description);
        location = (EditText)findViewById(R.id.location);
        photoBtn = (ImageView)findViewById(R.id.photoUpload);
        upload_btn = (Button)findViewById(R.id.uploadBtn);
        category = (Spinner)findViewById(R.id.categorySpinner);
        dialog = new ProgressDialog(UploadImage.this);
        category.setOnItemSelectedListener(this);
        List<String> categoryItems = new ArrayList<String>();
        categoryItems.add("Select Photo category");
        categoryItems.add("People");
        categoryItems.add("Culture");
        categoryItems.add("City Life");
        categoryItems.add("Love");
        categoryItems.add("Sports");
        categoryItems.add("Family");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(UploadImage.this, android.R.layout.simple_spinner_item, categoryItems){
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.GRAY  );
                }
                return view;
            }
        };
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        category.setAdapter(categoryAdapter);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                if(position > 0){
                    cat = selectedItemText;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sharedPrefs = getSharedPreferences("photoGameApp", MODE_PRIVATE);
        final Date c = Calendar.getInstance().getTime();
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(caption.getText().toString().equals("") || description.getText().equals("") || pic.equals("")) {
                    Toast.makeText(UploadImage.this, "Please fill in all missing required", Toast.LENGTH_LONG).show();
                }else{
                    dialog.setMessage("Please wait...");
                    dialog.show();
                    String base_url = getString(R.string.baseUrl)+"/photos/upload";
                    String todayDate = df.format(c);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userId", String.valueOf(sharedPrefs.getInt("userId", 0)));
                    params.put("photo", pic);
                    params.put("caption", caption.getText().toString());
                    params.put("description", description.getText().toString());
                    params.put("location", location.getText().toString());
                    params.put("time_taken", todayDate);
                    params.put("category", cat);
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
                                            Toast.makeText(UploadImage.this, "Photo uploaded successfully", Toast.LENGTH_LONG).show();
                                            Intent feed = new Intent(UploadImage.this, Feed.class);
                                            startActivity(feed);
                                            finish();
                                        }else{
                                            String message = response.getString("error");
                                            Toast.makeText(UploadImage.this, message, Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Toast.makeText(UploadImage.this, "Something has totally gone wrong while resetting your password. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            if (error instanceof TimeoutError) {
                                Toast.makeText(UploadImage.this, "Timeout error occurred. Please try again later", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(UploadImage.this, "Something has totally gone wrong. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
                }
            }
        });


    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadImage.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utility.checkPermission(UploadImage.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void galleryIntent()  {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }


    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(UploadImage.this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        photoBtn.setImageBitmap(bm);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        pic = Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        photoBtn.setImageBitmap(thumbnail);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        pic = Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

}
