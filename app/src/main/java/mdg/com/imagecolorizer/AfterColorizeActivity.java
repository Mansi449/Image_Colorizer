package mdg.com.imagecolorizer;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import mdg.com.slidermodule.BeforeAfterSlider;

public class AfterColorizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_colorize);

        Button buSave = findViewById(R.id.save);
        BeforeAfterSlider slider = findViewById(R.id.mySlider);

        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        int width = size. x;
        int height = size. y;
        ViewGroup.LayoutParams lp = slider.getLayoutParams();
        lp.height = height/2 ;
        lp.width = width-60;
        slider.setLayoutParams(lp);

        slider.setBeforeImage("https://images.theconversation.com/files/205966/original/file-20180212-58348-7huv6f.jpeg?ixlib=rb-1.1.0&q=45&auto=format&w=926&fit=clip").setAfterImage("https://cdn.pixabay.com/photo/2015/10/09/00/55/lotus-978659__340.jpg");
    }
}
