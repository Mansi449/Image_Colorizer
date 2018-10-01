package mdg.com.imagecolorizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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


public class BeforeColorizeActivity extends AppCompatActivity {

    ImageView blw_image;
    RelativeLayout background;
    private String filename;
    Bitmap bitmap;
    Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.before_colorize);

        blw_image = findViewById(R.id.blw_image);
        background = findViewById(R.id.background);

        uri = getIntent().getParcelableExtra("b/w_image");
        setBlackWhiteImage(uri);

        Button buColorize = findViewById(R.id.colorize);
        buColorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    void setBlackWhiteImage(Uri uri){
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            Bitmap blurred = BlurBuilder.blur(this,bitmap);
            BitmapDrawable bd = new BitmapDrawable(getResources(),blurred);
            background.setBackground(bd);

            Display display = getWindowManager(). getDefaultDisplay();
            Point size = new Point();
            display. getSize(size);
            int width = size. x;
            int height = size. y;
            ViewGroup.LayoutParams lp = blw_image.getLayoutParams();
            lp.height = height/2 ;
            lp.width = width-60;
            blw_image.setLayoutParams(lp);
            blw_image.setScaleType(ImageView.ScaleType.FIT_XY);

            blw_image.setImageBitmap(bitmap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

        String baseUrl="http://ec2-18-222-228-140.us-east-2.compute.amazonaws.com";
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
                String black_white="http://ec2-18-222-228-140.us-east-2.compute.amazonaws.com/original/"+ filename + ".jpg";
                String colored="http://ec2-18-222-228-140.us-east-2.compute.amazonaws.com/colored/col_"+ filename + ".png";
                Log.e("b/w", black_white);
                Log.e("col", colored);

                originalfile.delete();

                Intent i = new Intent(BeforeColorizeActivity.this, AfterColorizeActivity.class);
                i.putExtra("blw_url", black_white);
                i.putExtra("col_url", colored);
                i.putExtra("filename", filename);
                startActivity(i);

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BeforeColorizeActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
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
}
