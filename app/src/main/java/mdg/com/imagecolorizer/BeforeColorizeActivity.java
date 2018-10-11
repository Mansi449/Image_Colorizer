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
import android.widget.ProgressBar;
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

    ImageView blw_image,blur_back,back;
    private String filename;
    Bitmap bitmap;
    Uri uri;
    boolean isBig;
    int sliderHeight;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.before_colorize);

        blw_image = findViewById(R.id.blw_image);
        blur_back = findViewById(R.id.blur);
        progressBar = findViewById(R.id.beforeProgressBar);
        back = findViewById(R.id.ivBack);

        Intent intent = getIntent();
        uri = getIntent().getParcelableExtra("b/w_image");
        isBig = Objects.requireNonNull(intent.getExtras()).getBoolean("Big");
        sliderHeight = intent.getExtras().getInt("Height");
        setBlackWhiteImage(uri);

        Button buColorize = findViewById(R.id.colorize);
        buColorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BeforeColorizeActivity.this , MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    void setBlackWhiteImage(Uri uri){
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            float bitmapHeight = bitmap.getHeight();
            float bitmapWidth = bitmap.getWidth();
            float aspectRatio = bitmapHeight/bitmapWidth;

            Bitmap blurred = BlurBuilder.blur(this,bitmap);
            BitmapDrawable bd = new BitmapDrawable(getResources(),blurred);
            blur_back.setImageDrawable(bd);

            Display display = getWindowManager(). getDefaultDisplay();
            Point size = new Point();
            display. getSize(size);
            int width = size. x-60;
            ViewGroup.LayoutParams lp = blw_image.getLayoutParams();
            lp.height = (int) (width*aspectRatio);
            lp.width = width;
            blw_image.setLayoutParams(lp);
            blw_image.setScaleType(ImageView.ScaleType.FIT_XY);

            blw_image.setImageBitmap(bitmap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadImage(){

        String filePath;
        if (isBig){
            filePath = "/storage/emulated/0/Colorizer/upload.jpg";
        }
        else{
            filePath = "/storage/emulated/0/Colorizer/display.jpg";
        }

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

                progressBar.setVisibility(View.GONE);

                Intent i = new Intent(BeforeColorizeActivity.this, AfterColorizeActivity.class);
                i.putExtra("blw_url", black_white);
                i.putExtra("col_url", colored);
                i.putExtra("filename", filename);
                i.putExtra("Height", sliderHeight);
                startActivity(i);
                finish();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BeforeColorizeActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }


}
