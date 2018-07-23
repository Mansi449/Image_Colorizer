package mdg.com.imagecolorizer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.developer__.BeforeAfterSlider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
    BeforeAfterSlider slider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider = findViewById(R.id.mySlider);
        Button choose_img_from_gallery = findViewById(R.id.choose_img_from_gallery);
        Button take_a_new_image = findViewById(R.id.take_a_new_image);
        Button button_colorize = findViewById(R.id.buColorize);
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

//        button_colorize.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                uploadImage();
//            }
//        });
        button_colorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ViewGroup parent = (ViewGroup) selected_image.getParent();
//                int index = parent.indexOfChild(selected_image);
//                Log.e("index", Integer.toString(index));
//                parent.removeView(selected_image);
                selected_image.setVisibility(View.GONE);
                slider.setBeforeImage("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR1rujusZvgl7fmmewITPFoJfRhy_55dSr2Qd5-l02m2e-tGCREsg").setAfterImage("http://www.status77.in/wp-content/uploads/2015/08/Funny-whatsapp-dp-images-600x450.jpg");
                slider.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                selected_image.setImageBitmap(bitmap);
                Log.e("image ", "set");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
           // assert data != null;
            Bitmap photo = (Bitmap) data.getExtras().get("data");
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
        try {
            String filePath;
            if(Build.VERSION.SDK_INT>=26){
                final String[] split = uri.getPath().split(":");//split the path.
                filePath = "storage/emulated/0/"+split[0];
            }else{
                filePath=PathUtil.getPath(this,uri);
            }

            File originalfile=new File(filePath);
            RequestBody filepart=RequestBody.create(
                    MediaType.parse(getContentResolver().getType(uri)),
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
                        filename=response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String beforeUrl="http://ec2-52-71-24-249.compute-1.amazonaws.com/original/"+ filename + ".jpg";
                    String afterUrl="http://ec2-52-71-24-249.compute-1.amazonaws.com/colored/col_"+ filename + ".png";
                    
                    //No need for this now!
                    //Toast.makeText(MainActivity.this,DownloadUrl,Toast.LENGTH_SHORT).show();
                    //Picasso.get().load(DownloadUrl).into(selected_image);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MainActivity.this,"no"+t.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
