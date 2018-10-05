package mdg.com.imagecolorizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;
    Intent mintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Uri uri;
    ImageView selected_image;
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    View arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.landing_page);
        TextView choose_img_from_gallery = findViewById(R.id.choose_img_from_gallery);
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        arrow = findViewById(R.id.vector);

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

    private void requestThePermission () {
        int MY_PERMISSIONS_REQUEST_CAMERA = 2;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);
    }
}
