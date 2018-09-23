package mdg.com.imagecolorizer;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;


public class BeforeColorizeActivity extends AppCompatActivity {

    ImageView blw_image;
    RelativeLayout background;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.before_colorize);

        blw_image = findViewById(R.id.blw_image);
        background = findViewById(R.id.background);

        Uri uri = getIntent().getParcelableExtra("b/w_image");
        setBlackWhiteImage(uri);
    }

    void setBlackWhiteImage(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

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
}
