package com.example.externalstorageactivity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;


import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Context context;
    Activity activity;
    Button button,btnsave;
    ProgressDialog mProgressDialog;
    ImageView mImageView;
    URL url;
    AsyncTask mMyTask;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        activity = MainActivity.this;
        button = findViewById(R.id.btnDownload);
        btnsave = findViewById(R.id.save);
        mImageView = findViewById(R.id.imageView);
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("AsyncTask");
        mProgressDialog.setMessage("Please wait, we are downloading your image file...");


        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 121);
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMyTask = new DownloadTask().execute(stringToURL());
            }
        });


        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GetImage();
            }
        });

    }
        public void GetImage() {

            Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

            startActivityForResult(intent,100);
        }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode==100 && resultCode==RESULT_OK)
            {
                Uri uri = data.getData();
                mImageView.setImageURI(uri);
            }
        }



    private class DownloadTask extends AsyncTask<URL,Void,Bitmap>{

        Bitmap finalBitmap;
        protected void onPreExecute(){
            mProgressDialog.show();
        }
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;
            try{
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                finalBitmap=BitmapFactory.decodeStream(bufferedInputStream);
                saveImageToGallery(finalBitmap," ");
                return finalBitmap;
            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }


        private void saveImageToGallery(Bitmap bitmap, String s){
            OutputStream fos;

            try{

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                    ContentResolver resolver = getContentResolver();
                    ContentValues contentValues =  new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + ".jpg");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "TestFolder");
                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                    fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Objects.requireNonNull(fos);

                }

            }catch(Exception e){

            }
        }

        protected void onPostExecute(Bitmap result){
            mProgressDialog.dismiss();
            if(result!=null){
                Toast.makeText(MainActivity.this, "Image Downloaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected URL stringToURL() {
        try {
            url = new URL("https://s7.vitaminshoppe.com/is/image/VitaminShoppe/VShoppe_BodyTechDCComics_Generic?wid=1220&hei=808&fmt=png-alpha&qlt=85,1&op_sharpen=0&resMode=sharp2&op_usm=1,1,6,0&iccEmbed=0&printRes=300");
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}