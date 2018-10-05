package mdg.com.imagecolorizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import mdg.com.slidermodule.BeforeAfterSlider;

public class AfterColorizeActivity extends AppCompatActivity{

        String filename;
        BeforeAfterSlider slider;
        ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_colorize);

        Button buSave = findViewById(R.id.save);
        slider = findViewById(R.id.mySlider);
        progressBar = findViewById(R.id.mprogressbar);
        progressBar.setVisibility(View.VISIBLE);

        setSliderParams();
        setSliderImages();

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

    private void setSliderParams(){
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        int width = size. x;
        int height = getIntent().getExtras().getInt("Height");
        ViewGroup.LayoutParams lp = slider.getLayoutParams();
        lp.height = height ;
        lp.width = width-60;
        slider.setLayoutParams(lp);
    }

    private void setSliderImages(){
        String blw_url = getIntent().getExtras().getString("blw_url");
        String col_url = getIntent().getExtras().getString("col_url");
        filename = getIntent().getExtras().getString("filename");

        Log.e("blw", blw_url);
        Log.e("col", col_url);

        slider.setBeforeImage(col_url).setAfterImage(blw_url);
        progressBar.setVisibility(View.GONE);
    }
}
