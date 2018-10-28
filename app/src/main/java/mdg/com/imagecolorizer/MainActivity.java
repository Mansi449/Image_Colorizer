package mdg.com.imagecolorizer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.BottomSheetBehavior;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener  {

    private int PICK_IMAGE_REQUEST = 1;
    private PermissionUtil permissionUtil;
    Uri uri;
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    View arrow;
    private static final int REQUEST_STORAGE = 225;
    private static final int TXT_STORAGE = 2;
    private int displayBitmapSize;
    private boolean isBig=false;
    int uploadHeight;
    ImageView s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12;
    int permissionFlag=0;
    View sampleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final TextView choose_img_from_gallery = findViewById(R.id.choose_img_from_gallery);
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        arrow = findViewById(R.id.vector);
        permissionUtil = new PermissionUtil(this);

        s1 = findViewById(R.id.s1);
        s2 = findViewById(R.id.s2);
        s3 = findViewById(R.id.s3);
        s4 = findViewById(R.id.s4);
        s5 = findViewById(R.id.s5);
        s6 = findViewById(R.id.s6);
        s7 = findViewById(R.id.s7);
        s8 = findViewById(R.id.s8);
        s9 = findViewById(R.id.s9);
        s10 = findViewById(R.id.s10);
        s11 = findViewById(R.id.s11);
        s12 = findViewById(R.id.s12);

        loadSampleImages();

        s1.setOnClickListener(this);
        s2.setOnClickListener(this);
        s3.setOnClickListener(this);
        s4.setOnClickListener(this);
        s5.setOnClickListener(this);
        s6.setOnClickListener(this);
        s7.setOnClickListener(this);
        s8.setOnClickListener(this);
        s9.setOnClickListener(this);
        s10.setOnClickListener(this);
        s11.setOnClickListener(this);
        s12.setOnClickListener(this);

        choose_img_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                permissionFlag=1;

                if (CheckPermission(TXT_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        showPermissionExplanation(TXT_STORAGE);
                    }
                    else if (!permissionUtil.checkPermissionPreference("storage")){
                        requestPermission(TXT_STORAGE);
                        permissionUtil.updatePermissionPreference("storage");
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Please Allow Storage Permission in your App Setting.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(),null);
                        intent.setData(uri);
                        getApplicationContext().startActivity(intent);
                    }
                }else {
                    chooseImagefromGallery();
                }

            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        // btnBottomSheet.setText("Close Sheet");
                        arrow.setBackground(getDrawable(R.drawable.ic_expand_more_black_24dp));
                        break;
                    }

                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        arrow.setBackground(getDrawable(R.drawable.ic_expand_less_black_24dp));
                        break;
                    }
                    case BottomSheetBehavior.STATE_DRAGGING:
                        arrow.setBackground(getDrawable(R.drawable.ic_expand_more_black_24dp));
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        arrow.setBackground(getDrawable(R.drawable.ic_expand_less_black_24dp));
                        break;
                }
            }


            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    private void chooseImagefromGallery(){
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                storeImage(bitmap);

                Intent i = new Intent(MainActivity.this, BeforeColorizeActivity.class);
                i.putExtra("b/w_image", uri);
                i.putExtra("Big",isBig);
                i.putExtra("Height",uploadHeight);
                startActivity(i);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int CheckPermission(int permission){

        int status = PackageManager.PERMISSION_DENIED;

        switch (permission){
            case TXT_STORAGE:
                status = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
        }

        return status;
    }

    private void requestPermission(int permission){

        switch (permission){
            case TXT_STORAGE:
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                break;
        }
    }

    private void showPermissionExplanation(final int permission){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(permission == TXT_STORAGE){
            builder.setMessage("Please Allow");
            builder.setTitle("Storage Permission Needed");
        }

        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (permission == TXT_STORAGE)
                    requestPermission(TXT_STORAGE);
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (permissionFlag == 1){
                chooseImagefromGallery();
            }else if (permissionFlag == 2){
                sampleImageChoose(sampleImageView);
            }
        }
    }

    @Override
    public void onClick(View v) {

        permissionFlag = 2;
        sampleImageView = v;

        if (CheckPermission(TXT_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                showPermissionExplanation(TXT_STORAGE);
            }
            else if (!permissionUtil.checkPermissionPreference("storage")){
                requestPermission(TXT_STORAGE);
                permissionUtil.updatePermissionPreference("storage");
            }
            else{
                Toast.makeText(getApplicationContext(), "Please Allow Storage Permission in your App Setting.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(),null);
                intent.setData(uri);
                getApplicationContext().startActivity(intent);
            }
        }else {
            sampleImageChoose(v);
        }
    }

    private void sampleImageChoose(View v){
        Bitmap bitmap = null;
        if (v==s1){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image1);
        }else if (v==s2){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image2);
        }else if (v==s3){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image3);
        }else if (v==s4){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image4);
        }else if (v==s5){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image5);
        }else if (v==s6){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image6);
        }else if (v==s7){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image7);
        }else if (v==s8){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image8);
        }else if (v==s9){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image9);
        }else if (v==s10){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image10);
        }else if (v==s11){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image11);
        }else if (v==s12){
            bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.image12);
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null);
        Uri uri = Uri.parse(path);
        storeImage(bitmap);

        Intent i = new Intent(MainActivity.this, BeforeColorizeActivity.class);
        i.putExtra("b/w_image", uri);
        i.putExtra("Big",isBig);
        i.putExtra("Height",uploadHeight);
        startActivity(i);
        finish();
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        Bitmap newImage = getResizedBitmap(image);
        uploadHeight = newImage.getHeight();
        if (pictureFile == null) {
            Log.d("Error",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            newImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Error accessing file: " + e.getMessage());
        }

        if( displayBitmapSize > 300 ) {
            File pictureDir = new File("/storage/emulated/0/Colorizer/");
            isBig=true;

            String mImageName="upload.jpg" ;
            File mediaFile = new File(pictureDir.getPath() + File.separator + mImageName);
            Bitmap newBitmap = Bitmap.createScaledBitmap(newImage, newImage.getWidth()/2, newImage.getHeight()/2, false);
            try {
                FileOutputStream fos = new FileOutputStream(mediaFile);
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Error", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Error", "Error accessing file: " + e.getMessage());
            }

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
        String mImageName="display.jpg" ;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public Bitmap getResizedBitmap(Bitmap bitmap){

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float aspectRatio = originalWidth / (float) originalHeight;
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        int newWidth = size. x-60;
        int newHeight = Math.round(newWidth / aspectRatio);

        if(newHeight>0) {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            displayBitmapSize=bitmap.getAllocationByteCount()/1024;
            Log.e("Size", String.valueOf(displayBitmapSize));
            return newBitmap;
        }
        else{
            return bitmap;
        }
    }

    private void loadSampleImages(){
        Picasso.get().load(R.drawable.image1).fit().centerCrop().error(R.drawable.image1).into(s1);
        Picasso.get().load(R.drawable.image2).fit().centerCrop().error(R.drawable.image2).into(s2);
        Picasso.get().load(R.drawable.image3).fit().centerCrop().into(s3);
        Picasso.get().load(R.drawable.image4).fit().centerCrop().into(s4);
        Picasso.get().load(R.drawable.image5).fit().centerCrop().into(s5);
        Picasso.get().load(R.drawable.image6).fit().centerCrop().error(R.drawable.image6).into(s6);
        Picasso.get().load(R.drawable.image7).fit().centerCrop().error(R.drawable.image7).into(s7);
        Picasso.get().load(R.drawable.image8).fit().centerCrop().error(R.drawable.image8).into(s8);
        Picasso.get().load(R.drawable.image9).fit().centerCrop().error(R.drawable.image9).into(s9);
        Picasso.get().load(R.drawable.image10).fit().centerCrop().error(R.drawable.image7).into(s10);
        Picasso.get().load(R.drawable.image11).fit().centerCrop().error(R.drawable.image8).into(s11);
        Picasso.get().load(R.drawable.image12).fit().centerCrop().error(R.drawable.image9).into(s12);
    }

}
