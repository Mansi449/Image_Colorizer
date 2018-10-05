package mdg.com.imagecolorizer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener  {

    private int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;
    Intent mintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Uri uri;
    ImageView selected_image;
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    View arrow;
    ImageView s1;
    ImageView s2;
    ImageView s3;
    ImageView s4;
    ImageView s5;
    ImageView s6;
    ImageView s7;
    ImageView s8;
    ImageView s9;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.landing_page);
        TextView choose_img_from_gallery = findViewById(R.id.choose_img_from_gallery);
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        arrow = findViewById(R.id.vector);

        s1 = findViewById(R.id.s1);
        s2 = findViewById(R.id.s2);
        s3 = findViewById(R.id.s3);
        s4 = findViewById(R.id.s4);
        s5 = findViewById(R.id.s5);
        s6 = findViewById(R.id.s6);
        s7 = findViewById(R.id.s7);
        s8 = findViewById(R.id.s8);
        s9 = findViewById(R.id.s9);

        s1.setOnClickListener(this);
        s2.setOnClickListener(this);
        s3.setOnClickListener(this);
        s4.setOnClickListener(this);
        s5.setOnClickListener(this);
        s6.setOnClickListener(this);
        s7.setOnClickListener(this);
        s8.setOnClickListener(this);
        s9.setOnClickListener(this);

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

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            Intent i = new Intent(MainActivity.this, BeforeColorizeActivity.class);
            i.putExtra("b/w_image", uri);
            startActivity(i);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                            @NonNull String permissions[], @NonNull int[] grantResults){
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

    @Override
    public void onClick(View v) {

        /*
        * TODO: line163 (bitmap.compress) gives null pointer exception i.e. 'bitmap' is null...find a way to get bitmap of an image from image view.
        *
        * */
        v.buildDrawingCache(true);
        Bitmap bitmap = v.getDrawingCache();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null);
        Uri uri = Uri.parse(path);

        Intent i = new Intent(MainActivity.this, BeforeColorizeActivity.class);
        i.putExtra("b/w_image", uri);
        startActivity(i);
    }

}
