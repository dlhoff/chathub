package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.sfsu.csc780.chathub.R;

/**
 * PickLocationActivity
 *  This class is used by the place graphic on picture feature. It displays the picture
 *  that the user has chosen and asks the user to pick the location to put the graphic
 */
public class PickLocationActivity extends AppCompatActivity {
    private static final String LOG_TAG = PickLocationActivity.class.getSimpleName();
    private ImageView mPonPImage;
    float touchX, touchY, imageX, imageY, scale, scaledX, scaledY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);
        mPonPImage = (ImageView) findViewById(R.id.pOnPImage);
        mPonPImage.setImageBitmap(MainActivity.mPOnPBitmap);
        imageX = MainActivity.mPOnPBitmap.getWidth();
        imageY = MainActivity.mPOnPBitmap.getHeight();

        mPonPImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touchX = event.getX();
                touchY = event.getY();
                scaledX = v.getWidth();
                scale = scaledX / imageX;
                scaledY = imageY * scale;
                // Touch must be inside picture to return. Picture takes the whole width
                if (touchY < scaledY) {
                    returnLocation();
                }
                return true;
            }
        });
    }

    // Scale and return the touched location
    void returnLocation() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("TX", touchX / scale);
        returnIntent.putExtra("TY", touchY / scale);
        returnIntent.putExtra("VX", scaledX);
        returnIntent.putExtra("VY", scaledY);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
