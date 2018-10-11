package mdg.com.imagecolorizer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
        Handler handler= new Handler();
        Bitmap colouredBitmap;
        ImageView colourBlur,share,done;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_colorize);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button buSave = findViewById(R.id.save);
        slider = findViewById(R.id.mySlider);
        colourBlur = findViewById(R.id.colouredBlur);
        progressBar = findViewById(R.id.mprogressbar);
        progressBar.setVisibility(View.VISIBLE);
        share = findViewById(R.id.ivShare);
        done = findViewById(R.id.ivDone);

        setSliderParams();
        setSliderImages();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AfterColorizeActivity.this , MainActivity.class);
                startActivity(i);
                finish();

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String filePath = "/storage/emulated/0/Colorizer/Coloured Images/"+"col_" + filename + ".png";
                File f = new File(filePath);
                if (!f.exists()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AfterColorizeActivity.this);
                    builder.setMessage("First save the image to device.\nDo you want to save the image?");
                    builder.setTitle("Image Does Not Exist On Device");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressBar.setVisibility(View.VISIBLE);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final Bitmap myBitmap = getBitmapfromURL("http://ec2-18-222-228-140.us-east-2.compute.amazonaws.com/colored/col_"+ filename + ".png");
                                    storeColoredImage(myBitmap);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                            sharingIntent.setType("image/jpeg");
                                            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
                                            startActivity(Intent.createChooser(sharingIntent, "Share Image"));
                                        }
                                    });
                                }
                            }).start();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else{
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("image/jpeg");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
                    startActivity(Intent.createChooser(sharingIntent, "Share Image"));
                }
            }
        });

        buSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = "/storage/emulated/0/Colorizer/Coloured Images/"+"col_" + filename + ".png";
                File f = new File(filePath);
                if (!f.exists()){
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap myBitmap = getBitmapfromURL("http://ec2-18-222-228-140.us-east-2.compute.amazonaws.com/colored/col_"+ filename + ".png");
                            storeColoredImage(myBitmap);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(AfterColorizeActivity.this);
                                    builder.setMessage("Colorized Image is Saved to your device.\nFile Manager -> Colorizer -> Coloured Images -> col_"+filename+".png");
                                    builder.setTitle("Image Saved");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            });
                        }
                    }).start();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(AfterColorizeActivity.this);
                    builder.setMessage("File Manager -> Colorizer -> Coloured Images -> col_"+filename+".png");
                    builder.setTitle("Image Already Saved");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
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
        new Thread(new Runnable() {
            public void run() {
                final Bitmap myBitmap = getBitmapfromURL("http://ec2-18-222-228-140.us-east-2.compute.amazonaws.com/colored/col_"+ filename + ".png");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        colouredBitmap = myBitmap;
                        progressBar.setVisibility(View.GONE);
                        slider.setVisibility(View.VISIBLE);
                        Bitmap blurredBitmap = blur(myBitmap);
                        colourBlur.setImageBitmap(blurredBitmap);
                    }
                });

            }
        }).start();
    }

    private static final float BLUR_RADIUS = 12f;
    public Bitmap blur(Bitmap image) {
        if (null == image) return null;
        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);
        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

}
