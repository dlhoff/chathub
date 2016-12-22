/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
// import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        MessageUtil.MessageLoadListener {

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int MSG_LENGTH_LIMIT = 64;
    private static final double MAX_LINEAR_DIMENSION = 500.0;
    public static final String ANONYMOUS = "anonymous";
    private static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_PREFERENCES = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;
    public static final int REQUEST_RECORD_AUDIO = 4;
    public static final int REQUEST_ADD_EMOJI = 5;
    public static final int REQUEST_P_ON_P = 6;
    public static final int REQUEST_GET_LOCATION = 7;
    public static final int REQUEST_GET_GRAPHIC = 8;
    public static final int REQUEST_GET_CHAT_NAME = 9;
    public static final String EMOJI_FIRST_CHAR = "\uD83D";
    private static final String EMOJI_ICON_CHAR = "\ud83d\ude04";
    public static final int EMOJI_UNICODE_BASE = 0xDE00;
    private static final float MAX_GESTURE_SCALE = 1.0f;
    private static final float GYRO_TO_ACC_RATIO = 2f;
    private static final float MINIMUM_GYRO = 8f;
    private static final float MINIMUM_ACC = 16f;
    private static final int EMOJI_TO_MESSAGE = 1;
    private static final int EMOJI_TO_GESTURE = 2;
    public static final int EM_COUNT = 25;

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPrefEditor;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseRecyclerAdapter<ChatMessage, MessageUtil.MessageViewHolder>
            mFirebaseAdapter;
    private ImageButton mImageButton;
    private ImageButton mPhotoButton;
    private int mSavedTheme;
    private int mSavedLang;
    private int mCapturedTextLength;
    private ImageButton mLocationButton;
    private ImageButton mOverflowButton;
    private boolean mGestureEnable;
    private TextView mEmojiTextView;
    private TextView mChatGroupTextView;
    private CharSequence[] mEmChars;
    public static Bitmap mPOnPBitmap;
    private int mPOnPBitmapW, mPOnPBitmapH;
    private int mChosenGraphicID;
    private AlertDialog mAlertDialog;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer, mSensorGyro;
    private SensorEventListener mSensorListener;
    private boolean mCapture1, mCapture2;
    private float mXASum1, mYASum1, mZASum1;
    private float mXASum2, mYASum2, mZASum2;
    private float mXASum2M, mYASum2M, mZASum2M;
    private float mXGSum1, mYGSum1, mZGSum1;
    private float mXGSum2, mYGSum2, mZGSum2;
    private float mXGSum2M, mYGSum2M, mZGSum2M;
    private int mACnt1, mACnt2, mGCnt1, mGCnt2;
    private int mGestureIndex;
    private long mGesTimeStamp;
    private float [] mGesStrength;
    private String [] mGesString;
    private String [] mGesMsg;
    private String mPriorChatGroup;

    private View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView photoView =  (ImageView) v.findViewById(R.id.messageImageView);
            // Only show the larger view in dialog if there's a image for the message
            if (photoView.getVisibility() == View.VISIBLE) {
                Bitmap bitmap = ((GlideBitmapDrawable) photoView.getDrawable()).getBitmap();
                showPhotoDialog( ImageDialogFragment.newInstance(bitmap));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DesignUtils.applyColorfulTheme(this);
        MessageUtil.initLangArray();
        MessageUtil.sTransLang = DesignUtils.getLangTrans(this);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefEditor = mSharedPreferences.edit();
        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        //Initialize Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mUser.getDisplayName();
            if (mUser.getPhotoUrl() != null) {
                mPhotoUrl = mUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseAdapter = MessageUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mMessageRecyclerView,
                mImageClickListener);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MSG_LENGTH_LIMIT)});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Track text length. Gestures only register after user has >= 1 character in message field
                // to minimize undesired gesturing
                mCapturedTextLength = charSequence.toString().trim().length();
                if (mCapturedTextLength > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
                // updateSensors() turns off Acc & gyro when gestures not enabled to save power
                // This method called is repeated everytime a possibility in change of gesture
                // enable state exists
                updateSensors(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send messages on click.
                mMessageRecyclerView.scrollToPosition(0);
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl);
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
                mCapturedTextLength = 0;
                updateSensors(false);
            }
        });

        mImageButton = (ImageButton) findViewById(R.id.shareImageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(REQUEST_PICK_IMAGE);
            }
        });

        mPhotoButton = (ImageButton) findViewById(R.id.cameraButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePhotoIntent();
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMap();
            }
        });

        // Beginning of DH additions
        // Setup emoji button
        mEmojiTextView = (TextView) findViewById(R.id.emojiTextView);
        mEmojiTextView.setText(EMOJI_ICON_CHAR);
        mEmojiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEmoji(EMOJI_TO_MESSAGE);
            }
        });

        // Builds array of emoji characters for dialog
        // A consecutive set of emoji characters is used
        mEmChars = new CharSequence[EM_COUNT];
        for (int xIdx = 0; xIdx < EM_COUNT; xIdx++) {
            final int yIdx = 0;
            final int unicodeValue = EMOJI_UNICODE_BASE + yIdx*8 + xIdx;
            final String sValue = EMOJI_FIRST_CHAR + Character.toString((char)unicodeValue);
            mEmChars[xIdx] = sValue;
        }

        // Holds chat group name. Not visible when default chat group
        mChatGroupTextView = (TextView) findViewById(R.id.chatGroupText);
        mChatGroupTextView.setVisibility(View.GONE);

        // Overflow button of 3 vertical dots used to display 4 other options
        mOverflowButton = (ImageButton) findViewById(R.id.bottomOverflow);
        mOverflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomOverflow();
            }
        });

        // Read shared preferences for state of gesture enable or disable
        mGestureEnable = mSharedPreferences.getBoolean("GesEnable", false);

        // Setup Acc & Gyro sensors for gestures
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // flags used during capture of gesture movement
        mCapture1 = mCapture2 = false;
        mGesTimeStamp = 0;
        updateSensors(false);

        // Holds strength of gesture user made
        mGesStrength = new float[6];
        // Holds emoji character user has chosen
        mGesString = new String[6];
        // Read gesture strength and user chosen emoji from shared perferences, if they exist
        for (int i=0; i<6; i++) {
            mGesStrength[i] = mSharedPreferences.getFloat("GesStrength" + i, 0f);
            mGesString[i] = mSharedPreferences.getString("GesString" + i, "");
        }

        // String array of titles for gesture types
        mGesMsg = new String[] {"Lat X", "Lat Y", "Lat Z", "Rot X", "Rot Y", "Rot Z"};

        // Acc & Gyro sensor listener code
        // mCapture1 is used for calibration before user makes gesture
        // This movement will be subtracted from user gesture input
        // This is mostly to remove gravity from Acc.
        // mCapture2 is when we are detecting the user's gesture movement
        // The results from the mGesture1 cycle will be subtracted
        mSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;

                // The code below is the for the Acc. The Gyro code is very similar. The comments on
                // Acc code apply to the Gyro code as well
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    float [] e = new float[3];
                    for (int i = 0; i < 3; i++) {
                        e[i] = Math.abs(event.values[i]);
                    }
                    // Keep sum and count for mCapture1 cycle. These values are captured before
                    // user makes gesture action and will be subtracted from user gesture
                    if (mCapture1) {
                        mXASum1 += e[0];
                        mYASum1 += e[1];
                        mZASum1 += e[2];
                        mACnt1++;
                    } else if (mCapture2) {
                        // mCapture2 measures the user gesture movement
                        // The sensor value that has the highest average is registered with the emoji
                        mXASum2 += e[0];
                        mYASum2 += e[1];
                        mZASum2 += e[2];
                        mACnt2++;
                        // Max values are kept to be matched with the user movement to detect the gesture
                        if (e[0] > mXASum2M)
                            mXASum2M = e[0];
                        if (e[1] > mYASum2M)
                            mYASum2M = e[1];
                        if (e[2] > mZASum2M)
                            mZASum2M = e[2];
                    } else if (System.currentTimeMillis() > (mGesTimeStamp + 1000)   ) {
                        // Gestures are spaced by 1 second - to prevent double gesture
                        // This block of code is detecting the user doing the gesture to insert the emoji
                        // Check sensor signal and compare to strength of captured gesture. Scale factor
                        // allows user gesture to be a little weaker than input gesture
                        int gesIdx = -1;
                        // Check each of the 3 directions (X, Y, Z). The strongest that passes the threshold is used
                        for (int i = 0; i < 3; i++) {
                            // To detect gesture, 1 sec must have passed, there must be a gesture recorded
                            // (mGesStrength[] != 0), and the movement must be as strong as a scaled version of
                            // the gesture maximum
                            if ((System.currentTimeMillis() > (mGesTimeStamp + 1000)) &&
                                    (mGesStrength[i] != 0) && (e[i] > mGesStrength[i] * MAX_GESTURE_SCALE)) {
                                // Keep track of the strongest one
                                if ((gesIdx == -1) || (e[i] > e[gesIdx])) {
                                    gesIdx = i;
                                }
                            }
                        }
                        if (gesIdx > -1) {
                            // If a gesture movement met the criteria, add the emoji
                            // Use timestamp per suggestion from cjk
                            mGesTimeStamp = System.currentTimeMillis();
                            // clearGesClosed1();
                            Log.d(TAG, "AGesture detection: " + gesIdx + " : " + mGesString[gesIdx]);
                            mMessageEditText.getText().append(mGesString[gesIdx]);
                        }
                    }
                }

                // The code for the Gyro follows the same approach as the Acc.
                // Please see above comments
                if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    float [] e = new float[3];
                    for (int i = 0; i < 3; i++) {
                        e[i] = Math.abs(event.values[i]);
                    }

                    if (mCapture1) {
                        mXGSum1 += e[0];
                        mYGSum1 += e[1];
                        mZGSum1 += e[2];
                        mGCnt1++;
                    } else if (mCapture2) {
                        mXGSum2 += e[0];
                        mYGSum2 += e[1];
                        mZGSum2 += e[2];
                        mGCnt2++;
                        if (e[0] > mXGSum2M)
                            mXGSum2M = e[0];
                        if (e[1] > mYGSum2M)
                            mYGSum2M = e[1];
                        if (e[2] > mZGSum2M)
                            mZGSum2M = e[2];
                    } else if (System.currentTimeMillis() > (mGesTimeStamp + 1000)) {
                        int gesIdx = -1;
                        for (int i = 0; i < 3; i++) {
                            if ((System.currentTimeMillis() > (mGesTimeStamp + 1000)) &&
                                    (mGesStrength[i + 3] != 0) && (e[i] > mGesStrength[i + 3] * MAX_GESTURE_SCALE)) {
                                if ((gesIdx == -1) || (e[i] > e[gesIdx])) {
                                    gesIdx = i;
                                }
                            }
                        }
                        if (gesIdx > -1) {
                            mGesTimeStamp = System.currentTimeMillis();
                            // clearGesClosed1();
                            Log.d(TAG, "AGesture detection: " + (gesIdx+3) + " : " + mGesString[gesIdx + 3]);
                            mMessageEditText.getText().append(mGesString[gesIdx + 3]);

                        }
                    }
                }
            }
        };
        reloadChat();
        MessageUtil.loadLangArray();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
        // Implementation to support turning off sensors during Pause
        mSensorManager.unregisterListener(mSensorListener, mSensorAccelerometer);
        mSensorManager.unregisterListener(mSensorListener, mSensorGyro);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationUtils.startLocationUpdates(this);
        // Implementation to turn on sensors on Resume if appropriate
        updateSensors(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean isGranted = (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if (isGranted && requestCode == LocationUtils.REQUEST_CODE) {
            LocationUtils.startLocationUpdates(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.preferences_menu:
                mSavedTheme = DesignUtils.getPreferredTheme(this);
                mSavedLang = MessageUtil.sTransLang;
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivityForResult(i, REQUEST_PREFERENCES);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadComplete() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    // This is the 3 vertical dot bottom overflow menu button
    // It brings up a dialog for 4 additional features
    private void bottomOverflow() {
        // Make a dialog of the 4 other options
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Uses xml layout
        dialog.setContentView(R.layout.overflow_button);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Register listeners for the 4 buttons
        ImageButton audioButton = (ImageButton) dialog.findViewById(R.id.shareAudioButton);
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                recordAudio();
            }
        });

        ImageButton ponPButton = (ImageButton) dialog.findViewById(R.id.pOnPButton);
        ponPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pickImage(REQUEST_P_ON_P);
            }
        });

        ImageButton chatGroupButton = (ImageButton) dialog.findViewById(R.id.chatGroupButton);
        chatGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                setChatGroup();
            }
        });

        ImageButton gestureButton = (ImageButton) dialog.findViewById(R.id.gestureButton);
        gestureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                gestureDialog();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private void dispatchTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure the implicit intent can be handled
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    /* Code replaced by timestamp - lighter weight
    private void clearGesClosed1() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clearGesClosed2();
            }
        }, 1000);
    }

    private void clearGesClosed2() {
        mGesClosed = false;
    }
    */

    // This method from the exercises was enhanced to be used by two features, not just one
    // The request code of the calling routine is passed and that is used for the completion process
    private void pickImage(int request) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");
        startActivityForResult(intent, request);
    }

    // Handles the audio record activity. An implicit intent is used to record the audio
    private void recordAudio() {
        Intent iRA = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        iRA.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        PackageManager packageManager = getPackageManager();
        // Verifies that the implicit activity can be handled. If not, tell user by toast
        if (iRA.resolveActivity(packageManager) != null) {
            startActivityForResult(iRA, REQUEST_RECORD_AUDIO);
        } else {
            // If device does not support audio recording, notify user
            Toast.makeText(this, getResources().getString(R.string.unable_record),
                    Toast.LENGTH_LONG).show();
        }
    }

    // Create dialog so that user can select emoji
    private void addEmoji(final int actionType) {
        // Build a 5 x 5 grid of emoji characters. The user clicks on the desired one.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView title = new TextView(this);
        title.setText(R.string.emoji_select);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        builder.setCustomTitle(title);
        // Create 5 x 5 grid of 25 emojis from which the user can select
        GridView gridView = new GridView(this);
        gridView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, mEmChars));
        gridView.setNumColumns(5);

        // This method is used for both the direct adding emoji to text and the emoji assignment for
        // user gesture. Take the proper action based upon actionType code
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // This method is used for both adding an emoji to the message as well as selecting the
                // emoji to link to the gesture movement
                if (actionType == EMOJI_TO_MESSAGE) {
                    // Add emoji to input text. Update line length
                    mMessageEditText.getText().append(mEmChars[position]);
                    // Update message field for gesture sensor detection
                    mCapturedTextLength = mMessageEditText.getText().length();
                    updateSensors(false);
                    clearAlert();
                } else if (actionType == EMOJI_TO_GESTURE) {
                    // Store emoji for gesture movement
                    mGesString[mGestureIndex] = mEmChars[position].toString();
                    mSharedPrefEditor.putString("GesString" + mGestureIndex, mGesString[mGestureIndex]);
                    mSharedPrefEditor.commit();
                    clearAlert();
                }

            }
        });
        builder.setView(gridView);
        mAlertDialog = builder.show();
        mAlertDialog.setCancelable(true);
        mAlertDialog.setCanceledOnTouchOutside(true);
    }

    private void clearAlert() {
        mAlertDialog.cancel();
    }

    // Creates the user gesture dialog
    private void gestureDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // xml based gesture dialog
        dialog.setContentView(R.layout.gesture_dialog);
        dialog.setCancelable(true);
        // Toggle button to enable / disable gestures
        final ToggleButton gestureButton = (ToggleButton) dialog.findViewById(R.id.rd_t1);
        gestureButton.setChecked(mGestureEnable);

        // Button to view existing gesture emoji assignment
        Button rd1 = (Button) dialog.findViewById(R.id.rd_1);
        rd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When leaving this dialog, except for canceling, the toggle button state must
                // be copied to the mGestureEnable, the sensors must be updated and the shared
                // preferecnes must be updated
                mGestureEnable = gestureButton.isChecked();
                mSharedPrefEditor.putBoolean("GesEnable", mGestureEnable);
                mSharedPrefEditor.commit();
                updateSensors(false);
                dialog.dismiss();
                viewGestures();
            }
        });

        // Button to record gesture. Must also update change in gesture enable toggle button
        Button rd2 = (Button) dialog.findViewById(R.id.rd_2);
        rd2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGestureEnable = gestureButton.isChecked();
                mSharedPrefEditor.putBoolean("GesEnable", mGestureEnable);
                mSharedPrefEditor.commit();
                updateSensors(false);
                dialog.dismiss();
                recordGesture();
            }
        });

        // Button end dialog. Must also update change in gesture enable toggle button
        Button rd3 = (Button) dialog.findViewById(R.id.rd_3);
        rd3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGestureEnable = gestureButton.isChecked();
                mSharedPrefEditor.putBoolean("GesEnable", mGestureEnable);
                mSharedPrefEditor.commit();
                updateSensors(false);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // This method creates a dialog to view the existing gesture emoji assignments, and give the
    // user the opportunity to delete gestures
    private void viewGestures() {
        // Only display Gyro gestures if device has them
        Boolean hasGyro = mSensorManager
                .registerListener(mSensorListener, mSensorGyro, SensorManager.SENSOR_DELAY_NORMAL);
        updateSensors(false);
        // Create dialog layout to view gestures
        View fView;
        LinearLayout gLL = new LinearLayout(this);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        gLL.setLayoutParams(params);
        gLL.setOrientation(LinearLayout.VERTICAL);

        // Build the 3 column title row
        TextView title = new TextView(this);
        title.setText(R.string.view_gesture_emojis);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        LayoutParams paramsTV = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        paramsTV.gravity = Gravity.CENTER_HORIZONTAL;
        title.setLayoutParams(paramsTV);
        title.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
        gLL.addView(title);

        LayoutParams paramsF = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        paramsF.width = 0;
        paramsF.height = 0;
        paramsF.weight = 1;
        LayoutParams paramsGLine = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        paramsGLine.setMargins(12, 8, 8, 8);
        LinearLayout title2LL = new LinearLayout(this);
        title2LL.setLayoutParams(paramsGLine);
        title2LL.setOrientation(LinearLayout.HORIZONTAL);

        TextView title2L = new TextView(this);
        title2L.setText(R.string.gesture_header_type);
        title2L.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        LayoutParams paramsTV2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // paramsTV2.gravity = Gravity.LEFT;
        title2L.setLayoutParams(paramsTV2);
        title2L.setGravity(Gravity.LEFT);
        title2LL.addView(title2L);

        fView = new View(this);
        fView.setLayoutParams(paramsF);
        title2LL.addView(fView);

        TextView title2C = new TextView(this);
        title2C.setText(R.string.gesture_header_emoji);
        title2C.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        title2C.setLayoutParams(paramsTV2);
        title2C.setGravity(Gravity.CENTER);
        title2LL.addView(title2C);

        fView = new View(this);
        fView.setLayoutParams(paramsF);
        title2LL.addView(fView);

        TextView title2R = new TextView(this);
        title2R.setText(R.string.gesture_header_delete);
        title2R.setTextColor(Color.RED);
        title2R.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        title2R.setLayoutParams(paramsTV2);
        title2R.setGravity(Gravity.RIGHT);
        title2LL.addView(title2R);

        gLL.addView(title2LL);

        LinearLayout gLine;
        TextView tv1, tv2;
        String gesText;
        final CheckBox [] cb = new CheckBox[6];
        int lineCount = 3;
        if (hasGyro) {
            lineCount = 6;
        }
        // One line for each gesture possible. Acc & Gyro each provide 3
        final int lineCountF = lineCount;
        for (int i = 0; i < lineCount; i++) {
            gLine = new LinearLayout(this);
            gLine.setLayoutParams(paramsGLine);
            gLine.setOrientation(LinearLayout.HORIZONTAL);
            tv1 = new TextView(this);
            // This is the name of the gesture movement, Lat X, Lat Y, ..., Rot Z
            tv1.setText(mGesMsg[i]);
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

            LayoutParams paramsLeft = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsLeft.gravity = Gravity.LEFT;
            paramsLeft.setMargins(6, 6, 6, 6);
            tv1.setLayoutParams(paramsLeft);
            tv1.setGravity(Gravity.LEFT);
            gLine.addView(tv1);
            fView = new View(this);
            fView.setLayoutParams(paramsF);
            gLine.addView(fView);

            // A check box allows the user to delete the gesture movement
            cb[i] = new CheckBox(this);
            cb[i].setHighlightColor(Color.RED);

            // If no gesture, put <none>. Otherwise put gesture emoji character
            if (mGesStrength[i] == 0) {
                gesText = "<" + getResources().getString(R.string.none) + ">";
                cb[i].setEnabled(false);
            } else {
                gesText = mGesString[i];
            }
            tv2 = new TextView(this);
            tv2.setText(gesText);
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
            tv2.setLayoutParams(paramsTV);
            tv2.setGravity(Gravity.CENTER);
            gLine.addView(tv2);

            fView = new View(this);
            fView.setLayoutParams(paramsF);
            gLine.addView(fView);

            cb[i].setLayoutParams(paramsTV2);
            cb[i].setGravity(Gravity.RIGHT);
            gLine.addView(cb[i]);

            gLL.addView(gLine);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gLL);

        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete any gestures that user selected to delete
                        for (int i = 0; i < lineCountF; i++) {
                            if (cb[i].isChecked()) {
                                mGesStrength[i] = 0;
                                mSharedPrefEditor.putFloat("GesStrength" + i, 0);
                            }
                        }
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Record user gesture and link to emoji
    private void recordGesture() {
        // Capture existing sensor values (mostly for gravity) to subtract later
        // This is the mCapture1 phrase
        // This occurs right before the user is given the dialog below
        mXASum1 = mYASum1 = mZASum1 = mACnt1 = 0;
        mXGSum1 = mYGSum1 = mZGSum1 = mGCnt1 = 0;
        mCapture1 = true;
        // The true below will enable the sensors
        updateSensors(true);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.gesture_capture_title);
        alertDialog.setMessage(R.string.gesture_capture_message);

        alertDialog.setPositiveButton(R.string.gesture_capture,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Capture user gesture movement for 1 second
                        mCapture1 = false;
                        mXASum2 = mYASum2 = mZASum2 = mACnt2 = 0;
                        mXASum2M = mYASum2M = mZASum2M = 0;
                        mXGSum2 = mYGSum2 = mZGSum2 = mGCnt2 = 0;
                        mXGSum2M = mYGSum2M = mZGSum2M = 0;
                        mCapture2 = true;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gestureProcess2();
                            }
                        }, 1000);
                        dialog.cancel();
                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCapture1 = false;
                        updateSensors(false);
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    // After 1 second of user gesture movement, process result
    private void gestureProcess2() {
        mCapture2 = false;
        // Stop the capture
        updateSensors(false);

        float maxAvg = mXASum2/mACnt2 - mXASum1/mACnt1;
        float maxValue = mXASum2M - mXASum1/mACnt1;
        mGestureIndex = 0;

        // Determine which of the 6 sensor values has the largest average
        // This is taken as the dominate movement to consider as gesture
        if ((mYASum2/mACnt2 - mYASum1/mACnt1) > maxAvg) {
            maxAvg = mYASum2/mACnt2 - mYASum1/mACnt1;
            maxValue = mYASum2M - mYASum1/mACnt1;
            mGestureIndex = 1;
        }
        if ((mZASum2/mACnt2 - mZASum1/mACnt1) > maxAvg) {
            maxAvg = mZASum2/mACnt2 - mZASum1/mACnt1;
            maxValue = mZASum2M - mZASum1/mACnt1;
            mGestureIndex = 2;
        }
        if ( GYRO_TO_ACC_RATIO * (mXGSum2/mGCnt2 - mXGSum1/mGCnt1) > maxAvg) {
            maxAvg = mXGSum2/mGCnt2 - mXGSum1/mGCnt1;
            maxValue = GYRO_TO_ACC_RATIO * (mXGSum2M - mXGSum1/mGCnt1);
            mGestureIndex = 3;
        }
        if (GYRO_TO_ACC_RATIO * (mYGSum2/mGCnt2 - mYGSum1/mGCnt1) > maxAvg) {
            maxAvg = mYGSum2/mGCnt2 - mYGSum1/mGCnt1;
            maxValue = GYRO_TO_ACC_RATIO * (mYGSum2M - mYGSum1/mGCnt1);
            mGestureIndex = 4;
        }
        if (GYRO_TO_ACC_RATIO * (mZGSum2/mGCnt2 - mZGSum1/mGCnt1) > maxAvg) {
            maxAvg = mZGSum2/mGCnt2 - mZGSum1/mGCnt1;
            maxValue = GYRO_TO_ACC_RATIO * (mZGSum2M - mZGSum1/mGCnt1);
            mGestureIndex = 5;
        }

        // A minimum gesture strength is required to reduce false gesturing by casual movements
        if (mGestureIndex < 3) {
            // Log.d(TAG, "ACC gesture values (msrd/min): " + maxValue + " " + MINIMUM_ACC);
            maxValue = Math.max(maxValue, MINIMUM_ACC);
        } else {
            // Log.d(TAG, "Gyro gesture values (msrd/min): " + maxValue + " " + MINIMUM_GYRO);
            maxValue = Math.max(maxValue, MINIMUM_GYRO);
        }

        // Store gesture strength in appropriate location & update shared preferences
        mGesStrength[mGestureIndex] = maxValue;
        mSharedPrefEditor.putFloat("GesStrength" + mGestureIndex, maxValue);
        mSharedPrefEditor.commit();
        Toast.makeText(getApplicationContext(), mGesMsg[mGestureIndex] + " "
                + getResources().getString(R.string.gesture_captured_report), Toast.LENGTH_LONG).show();
        // Log.d(TAG, "" + mGesMsg[mGestureIndex] + ": " + maxValue + " / " + maxAvg);
        // Have user select emoji to link to gesture
        addEmoji(EMOJI_TO_GESTURE);
    }

    // Used to turn on/off Acc & Gyro as needed. this is the save power. The sensors are only on
    // when a gesture could be added to text input
    private void updateSensors(boolean force) {
        if ((mGestureEnable && (mCapturedTextLength > 0)) || force) {
            boolean hasSensor = mSensorManager.registerListener(mSensorListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            /* Used for debug
            if (hasSensor) {
                Log.d(TAG, "Has Accelerometer");
            } else {
                Log.d(TAG, "Has not Accelerometer");
            }
            */
            hasSensor = mSensorManager.registerListener(mSensorListener, mSensorGyro, SensorManager.SENSOR_DELAY_NORMAL);
            /* Used for debug
            if (hasSensor) {
                Log.d(TAG, "Has Gyroscope");
            } else {
                Log.d(TAG, "Has not Gyroscope");
            }
            */
        } else {
            mSensorManager.unregisterListener(mSensorListener, mSensorAccelerometer);
            mSensorManager.unregisterListener(mSensorListener, mSensorGyro);
        }
    }

    // Obtain user graphic selection from activity designed for a better UI
    private void setChatGroup() {
        Intent intent = new Intent(this, PickChatActivity.class);
        startActivityForResult(intent, REQUEST_GET_CHAT_NAME);
    }

    private void processChatGroup(String chatName) {
        mPriorChatGroup = MessageUtil.mMessagesChild;
        if (chatName.length() == 0) {
            // Empty line means to switch back to the default chat group
            MessageUtil.mMessagesChild = MessageUtil.MESSAGES_CHILD;
            mChatGroupTextView.setVisibility(View.GONE);
            Log.d(TAG, "=== chat group blank: " + MessageUtil.mMessagesChild);
        } else {
            // Otherwise redirect Firebase to the tag of the chat group
            MessageUtil.mMessagesChild = chatName;
            // Display chat group name
            String tStr = getResources().getString(
                    R.string.chat_group_text) + " " + MessageUtil.mMessagesChild;
            mChatGroupTextView.setText(tStr);
            mChatGroupTextView.setVisibility(View.VISIBLE);
            // Log.d(TAG, "chat group entered: " + MessageUtil.mMessagesChild);
        }
        if (!mPriorChatGroup.equals(MessageUtil.mMessagesChild)) {
            // Only change the adapter if chat group actually changed.
            // Before doing this, recreating the adapter when it was the same chat group
            // was causing the app to crash
            // Log.d(TAG, "chat adapter changed");
            mFirebaseAdapter = MessageUtil.getFirebaseAdapter(MainActivity.this,
                    MainActivity.this,
                    mLinearLayoutManager,
                    mMessageRecyclerView,
                    mImageClickListener);
            mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        }
    }


    /**
     * This version of setChatGroup has been replaced by one that calls an activity,
     * PickChatActivity that provides a better user interface.
     *

    // Be sure to put the following in the app build.gradle
    // compile 'com.android.support:recyclerview-v7:23.2.0'
    // Get user selection for chat group
    private void setChatGroup() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        String chatMsg = MessageUtil.mMessagesChild;
        // The "default" chat group is called "<none>"
        if (chatMsg.equals(MessageUtil.MESSAGES_CHILD)) {
            chatMsg = "<" + getResources().getString(R.string.default_chat) + ">";
        }
        alertDialog.setTitle(chatMsg);
        alertDialog.setMessage(R.string.chat_dialog);

        final EditText input = new EditText(MainActivity.this);
        input.setId(R.id.chatDialogEditText);
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_group_work_black_24dp);

        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String priorChatGroup = MessageUtil.mMessagesChild;
                        String inpStr = input.getText().toString().trim().replaceAll("[^a-zA-Z0-9]", "");
                        // If any non-alphanumeric chars entered, notify user, cannot change group
                        if (inpStr.length() != input.getText().toString().length()) {
                            chatToast();
                        } else {
                            // If no entry, then set to default
                            if (inpStr.length() == 0) {
                                MessageUtil.mMessagesChild = MessageUtil.MESSAGES_CHILD;
                                mChatGroupTextView.setVisibility(View.GONE);
                                Log.d(TAG, "chat group blank: " + MessageUtil.mMessagesChild);
                            } else {
                                // Otherwise redirect Firebase to the tag of the chat group
                                MessageUtil.mMessagesChild = input.getText().toString().trim();
                                String tStr = getResources().getString(
                                        R.string.chat_group_text) + " " + MessageUtil.mMessagesChild;
                                mChatGroupTextView.setText(tStr);
                                mChatGroupTextView.setVisibility(View.VISIBLE);
                                Log.d(TAG, "chat group entered: " + MessageUtil.mMessagesChild);
                            }
                            if (!priorChatGroup.equals(MessageUtil.mMessagesChild)) {
                                Log.d(TAG, "chat adapter changed");
                                mFirebaseAdapter = MessageUtil.getFirebaseAdapter(MainActivity.this,
                                        MainActivity.this,
                                        mLinearLayoutManager,
                                        mMessageRecyclerView,
                                        mImageClickListener);
                                mMessageRecyclerView.setAdapter(mFirebaseAdapter);
                            }
                        }
                        dialog.cancel();
                    }
                });

        alertDialog.setNegativeButton(getResources().getString(R.string.None),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String priorChatGroup = MessageUtil.mMessagesChild;
                        // Sets back to the default chat group
                        MessageUtil.mMessagesChild = MessageUtil.MESSAGES_CHILD;
                        Log.d(TAG, "chat group canceled: " + MessageUtil.mMessagesChild);
                        mChatGroupTextView.setVisibility(View.GONE);
                        if (!priorChatGroup.equals(MessageUtil.mMessagesChild)) {
                            Log.d(TAG, "chat adapter changed");
                            mFirebaseAdapter = MessageUtil.getFirebaseAdapter(MainActivity.this,
                                    MainActivity.this,
                                    mLinearLayoutManager,
                                    mMessageRecyclerView,
                                    mImageClickListener);
                            mMessageRecyclerView.setAdapter(mFirebaseAdapter);
                        }
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void chatToast() {
        Toast.makeText(this, "Only Alphanumeric characters allowed", Toast.LENGTH_SHORT).show();
    }

     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.d(TAG, "onActivityResult: request=" + requestCode + ", result=" + resultCode);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // Process selected image here
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // Resize if too big for messaging
                Bitmap bitmap = getBitmapForUri(uri);
                Bitmap resizedBitmap = scaleImage(bitmap);
                if (bitmap != resizedBitmap) {
                    uri = savePhotoImage(resizedBitmap);
                }
                createImageMessage(uri);
            } else {
                Log.e(TAG, "Cannot get image for uploading");
            }
        } else if (requestCode == REQUEST_P_ON_P && resultCode == Activity.RESULT_OK) {
            // Process selected image here
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                mPOnPBitmap = getBitmapForUri(uri);
                mPOnPBitmapW = mPOnPBitmap.getWidth();
                mPOnPBitmapH = mPOnPBitmap.getHeight();
                getGraphic();
            } else {
                Log.e(TAG, "Cannot get image for uploading");
            }
        } else if (requestCode == REQUEST_GET_GRAPHIC && resultCode == Activity.RESULT_OK) {
            // This is the integer code of the chosen graphic.
            mChosenGraphicID = data.getIntExtra("result", 0);
            // Next, have the user specify location on picture to put image.
            getImageLocation();
        } else if (requestCode == REQUEST_GET_LOCATION && resultCode == Activity.RESULT_OK) {
            // This obtains user touch location on picture. This is where the graphic will go
            float tx = data.getFloatExtra("TX", -1f);
            float ty = data.getFloatExtra("TY", -1f);
            float vx = data.getFloatExtra("VX", -1f);
            float vy = data.getFloatExtra("VY", -1f);
            float relX = tx / mPOnPBitmapW;
            float relY = ty / mPOnPBitmapH;
            // The images are scaled for maximum width + height <= MAX_LINEAR_DIMENSION
            // The size of the graphic images was chosen to be about 10% of MAX_LINEAR_DIMENSION
            Bitmap gBitmap = BitmapFactory.decodeResource(getResources(),mChosenGraphicID);
            int gW = gBitmap.getWidth();
            int gH = gBitmap.getHeight();

            // Resize if too big for messaging
            Bitmap resizedBitmap = scaleImage(mPOnPBitmap);
            int rBW = resizedBitmap.getWidth();
            int rBH = resizedBitmap.getHeight();
            int posX = (int) (relX * rBW) - gW/2;
            int posY = (int) (relY * rBH) - gH/2;
            posX = Math.max(0, Math.min(posX, rBW - gW));
            posY = Math.max(0, Math.min(posY, rBH - gH));
            // Add the graphic in the selected location on the picture
            Canvas canvas = new Canvas(resizedBitmap);
            canvas.drawBitmap(gBitmap, posX, posY, null);
            Uri uri = savePhotoImage(resizedBitmap);
            createImageMessage(uri);
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Log.d(TAG, "imageBitmap size:" + imageBitmap.getByteCount());
                createImageMessage(savePhotoImage(imageBitmap));
            } else {
                Log.e(TAG, "Cannot get photo URI after taking photo");
            }
        } else if (requestCode == REQUEST_PREFERENCES) {
            Boolean flag = false;
            if (DesignUtils.getLangTrans(this) != mSavedLang) {
                MessageUtil.sTransLang = DesignUtils.getLangTrans(this);
                flag = true;
            }
            if (DesignUtils.getPreferredTheme(this) != mSavedTheme) {
                DesignUtils.applyColorfulTheme(this);
                flag = true;
            }
            if (flag) {
                this.recreate();
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO) {
            if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                Uri savedUri = data.getData();
                Log.i(TAG, "Audio recording worked. Uri: " + savedUri.toString() + " " + savedUri.getPath());
                // If audio recorded ok then make audio message like image message was made
                createAudioMessage(savedUri);
            } else {
                Log.i(TAG, "Audio recording failed. resultCode: " + resultCode);
                if (data == null) {
                    Log.i(TAG, "Audio Recording returned data object is null");
                }
            }
        } else if (requestCode == REQUEST_ADD_EMOJI) {
            if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                // Get user entered emoji back from activity
                // Add emoji to text input
                mMessageEditText.getText().append(data.getStringExtra("result"));
                mCapturedTextLength =  mMessageEditText.getText().length();
                updateSensors(false);
            } else {
                Log.i(TAG, "Pick Emoji failed. resultCode: " + resultCode);
                if (data == null) {
                    Log.i(TAG, "Pick Emoji returned data object is null");
                }
            }
        } else if (requestCode == REQUEST_GET_CHAT_NAME) {
            // if resultCode is not Activity.RESULT_OK, then the change chat group was canceled.
            reloadChat();
            if (resultCode == Activity.RESULT_OK) {
                processChatGroup(data.getStringExtra("chatName"));
            }
        }
    }

    private void reloadChat() {
        Log.d(TAG, "=== Inside reloadChat m/M: " + MessageUtil.mMessagesChild + " " + MessageUtil.MESSAGES_CHILD);
        mChatGroupTextView = (TextView) findViewById(R.id.chatGroupText);
        if (MessageUtil.mMessagesChild == MessageUtil.MESSAGES_CHILD) {
            Log.d(TAG, "=== Inside reloadChat match");
            mChatGroupTextView.setVisibility(View.GONE);
        } else {
            String tStr = getResources().getString(
                    R.string.chat_group_text) + " " + MessageUtil.mMessagesChild;
            mChatGroupTextView.setText(tStr);
            mChatGroupTextView.setVisibility(View.VISIBLE);
            Log.d(TAG, "=== Inside reloadChat no match");
        }
    }

    // Obtain user graphic selection from activity designed for that
    private void getGraphic() {
        Intent intent = new Intent(this, PickGraphicActivity.class);
        startActivityForResult(intent, REQUEST_GET_GRAPHIC);
    }

    // Have user tap on location to put image
    private void getImageLocation() {
        Intent intent = new Intent(this, PickLocationActivity.class);
        startActivityForResult(intent, REQUEST_GET_LOCATION);
    }

    // This routine is patterned from createImageMessage()
    private void createAudioMessage(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Could not create audio message with null uri");
            return;
        }

        final StorageReference audioReference = MessageUtil.getStorageReference(mUser, uri);
        UploadTask uploadTask = audioReference.putFile(uri);
        final Uri uriF = uri;

        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload audio message");
                deleteAudioFile(uriF);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        null,
                        audioReference.toString());
                Log.d(TAG, "audioReference url: " + audioReference.toString());
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
                mCapturedTextLength = 0;
                updateSensors(false);
                // Delete audio file to prevent them from piling up
                deleteAudioFile(uriF);
            }
        });
    }

    // Deletes the audio file after the user uploads to prevent a pile up of files
    private void deleteAudioFile(Uri uri) {
        new File(uri.getPath()).delete();

        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
        getContentResolver().delete(uri, null, null);
        try {
            (new File(cursor.getString(index))).delete();
        } catch (SecurityException sE) {
            Log.i(TAG, "No audio file to delete: " + sE.toString());
        }
    }

    private void createImageMessage(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Could not create image message with null uri");
            return;
        }

        final StorageReference imageReference = MessageUtil.getStorageReference(mUser, uri);
        UploadTask uploadTask = imageReference.putFile(uri);

        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload image message");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl, imageReference.toString());
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
                mCapturedTextLength = 0;
                updateSensors(false);
            }
        });
    }

    private Bitmap getBitmapForUri(Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    static Bitmap scaleImage(Bitmap bitmap) {
        int originalHeight = bitmap.getHeight();
        int originalWidth = bitmap.getWidth();
        double scaleFactor =  MAX_LINEAR_DIMENSION / (double)(originalHeight + originalWidth);

        // We only want to scale down images, not scale upwards
        if (scaleFactor < 1.0) {
            int targetWidth = (int) Math.round(originalWidth * scaleFactor);
            int targetHeight = (int) Math.round(originalHeight * scaleFactor);
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        } else {
            return bitmap;
        }
    }

    private Uri savePhotoImage(Bitmap imageBitmap) {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile == null) {
            Log.d(TAG, "Error creating media file");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return Uri.fromFile(photoFile);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileNamePrefix = "chathub-" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(
                imageFileNamePrefix,    /* prefix */
                ".jpg",                 /* suffix */
                storageDir              /* directory */
        );
        return imageFile;
    }

    private void loadMap() {
        Loader<Bitmap> loader = getSupportLoaderManager().initLoader(0, null, new LoaderManager
                .LoaderCallbacks<Bitmap>() {
            @Override
            public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
                return new MapLoader(MainActivity.this);
            }

            @Override
            public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap result) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                mLocationButton.setEnabled(true);

                if (result == null) return;
                // Resize if too big for messaging
                Bitmap resizedBitmap = scaleImage(result);
                Uri uri = null;
                if (result != resizedBitmap) {
                    uri = savePhotoImage(resizedBitmap);
                } else {
                    uri = savePhotoImage(result);
                }
                createImageMessage(uri);
            }

            @Override
            public void onLoaderReset(final Loader<Bitmap> loader) {
            }

        });

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mLocationButton.setEnabled(false);
        loader.forceLoad();
    }

    void showPhotoDialog(DialogFragment dialogFragment) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) { ft.remove(prev); }
        ft.addToBackStack(null);

        dialogFragment.show(ft, "dialog");
    }

}
