package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
// import android.support.v4.app.DialogFragment;
// import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;

import com.bumptech.glide.Glide;
// import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatName;

public class MessageUtil {
    public static final String LOG_TAG = MessageUtil.class.getSimpleName();
    public static final String MESSAGES_CHILD = "messages";
    public static final String CHAT_ROOT = "chat_root";
    public static String mMessagesChild = MESSAGES_CHILD;
    static final int MAX_LANG = 20;
    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    private static MessageLoadListener sAdapterListener;
    private static MessageLoadListener sChatAdapterListener;
    private static FirebaseAuth sFirebaseAuth;
    private static ArrayList<String> mChatNames;
    static Activity mActivity;
    public static int sTransLang;
    static String[] sLangCodes = new String[MAX_LANG];
    public interface MessageLoadListener { public void onLoadComplete(); }

    public static void send(ChatMessage chatMessage) {
        sFirebaseDatabaseReference.child(mMessagesChild).push().setValue(chatMessage);
    }

    /**
     * This method crates a new chat group name and puts it in Firebase so that it can be
     * retrieved later to display as an option
     * @param chatName the ChatName object to store. This contains the name of the chat group
     */
    public static void sendChatName(ChatName chatName) {
        sFirebaseDatabaseReference.child(CHAT_ROOT).push().setValue(chatName);
    }


    public static class ChatNameViewHolder extends RecyclerView.ViewHolder {
        public static View.OnClickListener sChatNameViewListener;
        public TextView chatNameTextView;
        public ChatNameViewHolder(View v) {
            super(v);
            chatNameTextView = (TextView) itemView.findViewById(R.id.chatNameTextView);
            v.setOnClickListener(sChatNameViewListener);
        }
    }

    /**
     * @param activity is the Activity calling the method. It provides context
     * @param listener provides the listener for the onLoadComplete() method call
     * @param linearManager is the Linear Layout Manager
     * @param recyclerView is the RecyclerView for this adapter
     * @param clickListener is the on click listener when an item is clicked on
     * @return the FirebaseRecyclerAdapter for PickChatActivity to display the existing chat grpup*
     *
     */
    public static FirebaseRecyclerAdapter getFirebaseChatAdapter(final Activity activity,
                                                             MessageLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView,
                                                             final View.OnClickListener clickListener) {
        mChatNames = new ArrayList<>();
        sChatAdapterListener = listener;
        ChatNameViewHolder.sChatNameViewListener = clickListener;
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<ChatName,
                ChatNameViewHolder>(
                ChatName.class,
                R.layout.chat_name,
                ChatNameViewHolder.class,
                sFirebaseDatabaseReference.child(CHAT_ROOT)) {
            @Override
            protected void populateViewHolder(final ChatNameViewHolder viewHolder,
                                              final ChatName chatName, int position) {
                sChatAdapterListener.onLoadComplete();
                String chatNameStr = chatName.getName();
                viewHolder.chatNameTextView.setText(chatNameStr);
                viewHolder.chatNameTextView.setVisibility(View.VISIBLE);
                mChatNames.add(chatNameStr);
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = linearManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        return adapter;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public static View.OnClickListener sMessageViewListener;
        public TextView messageTextView;
        public TextView messageTransView;
        public ImageView messageImageView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public TextView timestampTextView;
        public View messageLayout;
        public ImageView messageAudioIcon;
        public int messageTextViewR;
        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageTransView = (TextView) itemView.findViewById(R.id.messageTransView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            timestampTextView = (TextView) itemView.findViewById(R.id.timestampTextView);
            messageLayout = (View) itemView.findViewById(R.id.messageLayout);
            messageAudioIcon = (ImageView) itemView.findViewById(R.id.messageAudioIcon);
            messageTextViewR = R.id.messageImageView;
            v.setOnClickListener(sMessageViewListener);
        }
    }

    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             MessageLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView,
                                                             final View.OnClickListener clickListener) {
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        mActivity = activity;
        sAdapterListener = listener;
        MessageViewHolder.sMessageViewListener = clickListener;
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<ChatMessage,
                MessageViewHolder>(
                ChatMessage.class,
                R.layout.item_message,
                MessageViewHolder.class,
                sFirebaseDatabaseReference.child(mMessagesChild)) {
            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              final ChatMessage chatMessage, int position) {
                sAdapterListener.onLoadComplete();
                viewHolder.messageTextView.setText(chatMessage.getText());
                viewHolder.messengerTextView.setText(chatMessage.getName());
                if (chatMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(activity,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    SimpleTarget target = new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                            viewHolder.messengerImageView.setImageBitmap(bitmap);
                            final String palettePreference = activity.getString(R.string
                                    .auto_palette_preference);

                            if (preferences.getBoolean(palettePreference, false)) {
                                DesignUtils.setBackgroundFromPalette(bitmap, viewHolder
                                        .messageLayout);
                            } else {
                                viewHolder.messageLayout.setBackground(
                                        activity.getResources().getDrawable(
                                                R.drawable.message_background));
                            }

                        }
                    };
                    Glide.with(activity)
                            .load(chatMessage.getPhotoUrl())
                            .asBitmap()
                            .into(target);
                }

                if (chatMessage.getImageUrl() != null) {
                    viewHolder.messageImageView.setVisibility(View.VISIBLE);
                    viewHolder.messageTextView.setVisibility(View.GONE);
                    viewHolder.messageAudioIcon.setVisibility(View.GONE);
                    try {
                        final StorageReference gsReference =
                                sStorage.getReferenceFromUrl(chatMessage.getImageUrl());
                        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(activity)
                                        .load(uri)
                                        .into(viewHolder.messageImageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e(LOG_TAG, "Could not load image for message", exception);
                            }
                        });
                    } catch (IllegalArgumentException e) {
                        viewHolder.messageTextView.setText("Error loading image");
                        Log.e(LOG_TAG, e.getMessage() + " : " + chatMessage.getImageUrl());
                    }
                } else if (chatMessage.getAudioUrl() != null) {
                    // Indicates audio message. Image view is off. Text and audio are on
                    viewHolder.messageAudioIcon.setVisibility(View.VISIBLE);
                    viewHolder.messageImageView.setVisibility(View.GONE);
                    // Text message is on, only if there is a message. Optional with audio
                    if (viewHolder.messageTextView.length() != 0) {
                        viewHolder.messageTextView.setVisibility(View.VISIBLE);;
                    } else {
                        viewHolder.messageTextView.setVisibility(View.GONE);
                    }
                    // Add onClick listener. This allows user to click on audio icon and listen to the
                    // recording
                    viewHolder.messageAudioIcon.setClickable(true);
                    viewHolder.messageAudioIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final View vI = v;
                            try {
                                final StorageReference gsReference =
                                        sStorage.getReferenceFromUrl(chatMessage.getAudioUrl());
                                gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // If successful in obtaining audio file, then play recording
                                        Intent iPA = new Intent();
                                        iPA.setAction(android.content.Intent.ACTION_VIEW);
                                        iPA.setDataAndType(uri, "audio/*");

                                        PackageManager packageManager = activity.getPackageManager();
                                        // Verifies that the implicit activity can be handled. If not, tell user by toast
                                        if (iPA.resolveActivity(packageManager) != null) {
                                            vI.getContext().startActivity(iPA);
                                        } else {
                                            Toast.makeText(activity, "Unable to play audio",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.e(LOG_TAG, "Could not load audio for message", exception);
                                    }
                                });
                            } catch (IllegalArgumentException e) {
                                viewHolder.messageTextView.setText("Error loading audio");
                                Log.e(LOG_TAG, e.getMessage() + " : " + chatMessage.getImageUrl());
                            }
                        }
                    });
                } else {
                    viewHolder.messageImageView.setVisibility(View.GONE);
                    viewHolder.messageAudioIcon.setVisibility(View.GONE);
                    viewHolder.messageTextView.setVisibility(View.VISIBLE);
                }

                viewHolder.messageTransView.setVisibility(View.GONE);
                if ((viewHolder.messageTextView != null) && (viewHolder.messageTextView.getText().length() > 0)) {
                    viewHolder.messageTextView.setVisibility(View.VISIBLE);
                    if (sTransLang != 0) {
                        TranslateTask rT = new TranslateTask();
                        rT.setTV(viewHolder.messageTransView);
                        rT.execute(viewHolder.messageTextView.getText().toString());
                    }
                }

                long timestamp = chatMessage.getTimestamp();
                if (timestamp == 0 || timestamp == chatMessage.NO_TIMESTAMP ) {
                    viewHolder.timestampTextView.setVisibility(View.GONE);
                } else {
                    viewHolder.timestampTextView.setText(DesignUtils.formatTime(activity,
                            timestamp));
                    viewHolder.timestampTextView.setVisibility(View.VISIBLE);
                }

            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = linearManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        return adapter;
    }

    public static StorageReference getStorageReference(FirebaseUser user, Uri uri) {
        //Create a blob storage reference with path : bucket/userId/timeMs/filename
        long nowMs = Calendar.getInstance().getTimeInMillis();

        return sStorage.getReference().child(user.getUid() + "/" + nowMs + "/" + uri
                .getLastPathSegment());
    }

    /**
     * This loads the language name with the resource names
     */
    public static void loadLangArray() {
        sLangCodes = mActivity.getResources().getStringArray(R.array.langCodeArray);
    }

    /**
     * Initializes the language name array with no strings. This is for a potential timing issue.
     * Avoids a null pointer
     */
    public static void initLangArray() {
        Arrays.fill(sLangCodes, "");
    }

    /**
     * Checks for duplicate chat group names, at least those visible to the RecyclerView.
     * This reduces the duplication of chat names.
     * @param name to check if duplicate chat group name
     * @return true indicates that name already exists. At least catches those items seen
     */
    public static Boolean chatNameExists(String name) {
        return mChatNames.contains(name);
    }

}


class TranslateTask extends AsyncTask<String, String, String> {
    public static final String LOG_TAG = TranslateTask.class.getSimpleName();
    private static final String TRANSLATE_KEY = "AIzaSyAH0VhX0gdixuvdxFDXuS01Ju6S8iztetQ";
    private int viewID;
    private TextView tv;

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        String inpText = "";

        try {
            inpText = URLEncoder.encode(params[0], "utf-8");
            String query = "https://www.googleapis.com/language/translate/v2?key=";
            query += TRANSLATE_KEY;
            query += "&source=en&target=" + MessageUtil.sLangCodes[MessageUtil.sTransLang] + "&q=";
            query += inpText;

            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
                InputStream it = new BufferedInputStream(conn.getInputStream());
                InputStreamReader read = new InputStreamReader(it);
                BufferedReader buff = new BufferedReader(read);
                String chunks ;
                while((chunks = buff.readLine()) != null)
                {
                    result += chunks;
                }
                buff.close();
                read.close();
                it.close();
            } else {
                Log.d(LOG_TAG, "=== HTTP Connection failed: " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "=== UnsupportedEncodingException: " + e.toString());
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, "=== MalformedURLException: " + e.toString());
        } catch (IOException e) {
            Log.d(LOG_TAG, "=== IOException: " + e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        putInTranslation(viewID, extractTranslation(result));
    }

    void putInTranslation(int viewID, String result) {
        tv.setText(result);
        tv.setVisibility(View.VISIBLE);
    }

    // Extracts the translated text from the GoogleAPI JSON structure
    static String extractTranslation(String inpStr) {
        String result = "";
        String keyWord = "translatedText";
        int idx1 = 0, idx2 = 0, idx3 = 0, idx4 = 0;
        if ((idx1 = inpStr.indexOf(keyWord)) != -1) {
            if ((idx2 = inpStr.substring(idx1).indexOf(':')) != -1) {
                if ((idx3 = inpStr.substring(idx1 + idx2).indexOf('"')) != -1) {
                    if (((idx4 = inpStr.substring(idx1 + idx2 + idx3 + 1).indexOf('"')) != -1) && (idx4 > 0)) {
                        result = inpStr.substring(idx1 + idx2 + idx3 + 1, idx1 + idx2 + idx3 + idx4 + 1);
                    }
                }
            }
        }
        return result;
    }

    void setTV(TextView tvInp) {
        tv = tvInp;
    }

}

