package mdg.com.imagecolorizer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;
    Intent mintent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Uri uri;
    private String filename;
    ImageView selected_image;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button choose_img_from_gallery = findViewById(R.id.choose_img_from_gallery);
        Button take_a_new_image = findViewById(R.id.take_a_new_image);
        Button button_colorize = findViewById(R.id.buColorize);
        Button  buSave = findViewById(R.id.buSave);
        selected_image = findViewById(R.id.selected_image);

        selected_image.setScaleType(ImageView.ScaleType.FIT_XY);

        choose_img_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });
        take_a_new_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    requestThePermission();
                } else {
                    // Permission has already been granted
                    startActivity(mintent);
                    startActivityForResult(mintent, CAMERA_REQUEST);
                }
            }
        });

        button_colorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        buSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {

                        Bitmap myBitmap = getBitmapfromURL("http://ec2-52-71-24-249.compute-1.amazonaws.com/colored/col_"+ filename + ".png");
                        storeColoredImage(myBitmap);

                    }
                }).start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                selected_image.setImageBitmap(bitmap);
                selected_image.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
           // assert data != null;
            assert data != null;
            Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            selected_image.setImageBitmap(photo);
            Log.e("image ", "captured");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startActivity(mintent);
                    startActivityForResult(mintent, CAMERA_REQUEST);
                } else {
                    // permission denied
                    Toast.makeText(getApplicationContext(), "Please grant camera permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void requestThePermission(){
        int MY_PERMISSIONS_REQUEST_CAMERA = 2;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);
    }

    public void uploadImage(){
        storeUploadImage(bitmap);

        String filePath = "/storage/emulated/0/Colorizer/upload.jpg";

        final File originalfile=new File(filePath);
        RequestBody filepart=RequestBody.create(
                MediaType.parse(Objects.requireNonNull(getContentResolver().getType(uri))),
                originalfile
        );

        Log.e("file name", originalfile.getName());
        MultipartBody.Part file=MultipartBody.Part.createFormData("photo",originalfile.getName(), filepart);

        String baseUrl="http://ec2-52-71-24-249.compute-1.amazonaws.com/";
        Retrofit retrofit= new Retrofit.Builder().baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create()).build();

        ApiInterface apiInterface=retrofit.create(ApiInterface.class);

        Call<ResponseBody> call= apiInterface.uploadImage(file);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    assert response.body() != null;
                    filename= Objects.requireNonNull(response.body()).string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String black_white="http://ec2-52-71-24-249.compute-1.amazonaws.com/original/"+ filename + ".jpg";
                String colored="http://ec2-52-71-24-249.compute-1.amazonaws.com/colored/col_"+ filename + ".png";

                //Bitmap myBitmap = getBitmapfromURL(colored);
                //storeColoredImage(myBitmap);

                originalfile.delete();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void storeUploadImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        Bitmap newImage = Bitmap.createScaledBitmap(image, 512, 512, false);
        if (pictureFile == null) {
            Log.d("Error",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            newImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputMediaFile(){

        File mediaStorageDir = new File("/storage/emulated/0/Colorizer/");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                mediaStorageDir.mkdirs();
            }
        }

        File mediaFile;
        String mImageName="upload.jpg" ;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public Bitmap getBitmapfromURL(String src) {

        Bitmap myBitmap;
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void storeColoredImage(Bitmap image) {
        File pictureFile = getOutputDirectory();
        if (pictureFile == null) {
            Log.d("Error",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputDirectory(){

        File mediaStorageDir = new File("/storage/emulated/0/Colorizer/Coloured Images/");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                mediaStorageDir.mkdirs();
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "col_" + filename + ".png");
        return mediaFile;
    }

}
