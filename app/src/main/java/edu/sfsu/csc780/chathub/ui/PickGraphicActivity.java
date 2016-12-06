package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import edu.sfsu.csc780.chathub.R;

/**
 * PickGraphicActivity
 *  This class is used by the place graphic on picture feature. It is used to provide a
 *  dialog modal to the user which contains all the graphics for the user to choose from
 *  to place on the chosen picture
 */
public class PickGraphicActivity extends AppCompatActivity {
    private static final String TAG = PickGraphicActivity.class.getSimpleName();
    private static final int G_SPACE_X = 2;
    private static final int G_SPACE_Y = 2;
    LinearLayout mMainLayout;
    TextView mTitleTV;
    // File name base part for access from resources drawable
    String mBaseName = "graphic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create dialog modal with the graphics the user can select from
        mMainLayout = new LinearLayout(this);
        mMainLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams paramsMML = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mMainLayout.setLayoutParams(paramsMML);

        // Create title
        mTitleTV = new TextView(this);
        mTitleTV.setText(R.string.select_graphic_text);
        mTitleTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        mTitleTV.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsTTV = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsTTV.bottomMargin = 8;
        paramsTTV.gravity = Gravity.CENTER;
        mMainLayout.addView(mTitleTV, paramsTTV);

        LinearLayout.LayoutParams paramsRLL = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsRLL.gravity = Gravity.CENTER;
        // Generates 6 X 8 matrix of graphics
        for (int yIdx = 0; yIdx < 8; yIdx++) {
            LinearLayout rowLL = new LinearLayout(this);
            rowLL.setOrientation(LinearLayout.HORIZONTAL);
            for(int xIdx = 0; xIdx < 6; xIdx++) {
                if ((yIdx > 4) && (xIdx > 4)) {
                    continue;
                }
                ImageView cellTV = new ImageView(this);
                cellTV.setPadding(G_SPACE_X, G_SPACE_Y, G_SPACE_X, G_SPACE_X);
                String name = mBaseName + yIdx + xIdx;
                // Log.d(TAG, "graphic file name: " + name);
                final int resourceId = getResources().getIdentifier(name, "drawable", getApplication().getPackageName());
                cellTV.setImageDrawable(getResources().getDrawable(resourceId));
                cellTV.setOnClickListener(new View.OnClickListener() {
                    int sValueI = resourceId;
                    @Override
                    public void onClick(View v) {
                        returnCode(sValueI);
                    }
                });
                rowLL.addView(cellTV, paramsRLL);
            }
            mMainLayout.addView(rowLL, paramsRLL);
        }
        // Use a ScrollView to allow more to fit on the screen
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scroll.addView(mMainLayout);
        setContentView(scroll);
    }

    void returnCode(int sReturn) {
        Intent returnIntent = new Intent();
        // Return graphic number of one chosen by user
        returnIntent.putExtra("result",sReturn);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
