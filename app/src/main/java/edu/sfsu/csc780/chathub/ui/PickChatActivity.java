package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatName;

/**
 * PickChatActivity
 *  This class is used to display the existing chat groups to the user. The user can
 *  select one of those chat groups or create a new one, or go back to the default (none)
 *
 */
public class PickChatActivity extends AppCompatActivity
        implements MessageUtil.MessageLoadListener {
    private static final String LOG_TAG = PickChatActivity.class.getSimpleName();
    private Button mCreateButton;
    private Button mNoneButton;
    private Button mCancelButton;
    private EditText mChatInput;
    private RecyclerView mChatRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mChatGroupTextView;
    private FirebaseRecyclerAdapter<String, MessageUtil.ChatNameViewHolder>
            mFirebaseChatAdapter;

    // If user clicks on existing chat group, switch to that group
    private View.OnClickListener mChatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView chatNameTextView = (TextView) v.findViewById(R.id.chatNameTextView);
            String chatName = chatNameTextView.getText().toString();
            returnChatName(chatName);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_chat);

        // Holds chat group name. Not visible when default chat group
        mChatGroupTextView = (TextView) findViewById(R.id.chatGroupText2);
        if (MessageUtil.mMessagesChild == MessageUtil.MESSAGES_CHILD) {
            mChatGroupTextView.setVisibility(View.GONE);
        } else {
            String tStr = getResources().getString(
                    R.string.chat_group_text) + " " + MessageUtil.mMessagesChild;
            mChatGroupTextView.setText(tStr);
            mChatGroupTextView.setVisibility(View.VISIBLE);
        }

        // Create RecyclerView with Adapter
        mChatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(mLinearLayoutManager);

        // Create adapter
        mFirebaseChatAdapter = MessageUtil.getFirebaseChatAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mChatRecyclerView,
                mChatClickListener);
        mChatRecyclerView.setAdapter(mFirebaseChatAdapter);

        // Get the user input
        mChatInput = (EditText) findViewById(R.id.chatNameEditText);

        // Button to create new chat group
        mCreateButton = (Button) findViewById(R.id.chatCreateButton);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inpStr = mChatInput.getText().toString().trim().replaceAll("[^a-zA-Z0-9]", "");
                // If any non-alphanumeric chars entered, notify user, cannot change group
                if (inpStr.length() != mChatInput.getText().toString().length()) {
                    chatToast();
                } else if (inpStr.length() == 0 ) {
                    mChatInput.setHintTextColor(Color.RED);
                } else {
                    if (MessageUtil.chatNameExists(inpStr)) {
                        // Not a complete catch. Catches some duplicates
                        duplicateToast();  // Notify user of duplicate, but switch to it anyway
                    } else {
                        // Add new chat group name to Firebase list
                        ChatName chatName = new ChatName(inpStr);
                        MessageUtil.sendChatName(chatName);
                    }
                    // Return with chat group name
                    returnChatName(inpStr);
                }
            }
        });

        // None button switches user back to default chat group
        mNoneButton = (Button) findViewById(R.id.noChatButton);
        mNoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Blank line sets user back to default (no) chat group
                returnChatName("");
            }
        });

        // Cancels this activity. No change to chat group
        mCancelButton = (Button) findViewById(R.id.chatCancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnNoName();
            }
        });
    }

    @Override
    public void onLoadComplete() {
    }

    //
    void returnChatName(String chatName) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("chatName", chatName);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    // Cancels chat name activity. Keep the same chat group
    void returnNoName() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("chatName", "");
        // Notifies that no change to chat group
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    private void chatToast() {
        Toast.makeText(this, getResources().getString(R.string.only_alpha_numeric), Toast.LENGTH_SHORT).show();
    }

    private void duplicateToast() {
        Toast.makeText(this, getResources().getString(R.string.duplicate_chat), Toast.LENGTH_SHORT).show();
    }

}
