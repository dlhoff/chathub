package edu.sfsu.csc780.chathub;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.sfsu.csc780.chathub.ui.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
// import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
// import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
// import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
// import static android.support.test.espresso.matcher.ViewMatchers.withTagKey;
// import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
// import static org.hamcrest.Matchers.endsWith;
// import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * Created by David on 11/26/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    // Add instrumentation test here

    /* Provided as a sample by instructor
    @Test
    public void ensureMessageFieldClearsAfterSend() {
        //Enter some text into the message field
        onView(withId(R.id.messageEditText)).perform(typeText("Testing 1, 2, 3"),
                closeSoftKeyboard());
        //Perform a click on the send button
        onView(withId(R.id.sendButton)).perform(click());
        //Check that the message field is now empty
        onView(withId(R.id.messageEditText)).check(matches(withText("")));
    }
    */

    @Test
    public void ensureMessageFieldFillsWithChars() {
        //Enter some text into the message field
        onView(withId(R.id.messageEditText)).perform(typeText("Testing ensureMessageFieldFillsWithChars"),
                closeSoftKeyboard());
        //Check that the message field still holds characters typed
        onView(withId(R.id.messageEditText)).check(matches(withText("Testing ensureMessageFieldFillsWithChars")));
    }

    @Test
    public void ensureOverflowAppears() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Check that one of the extra buttons appears.
        onView(withId(R.id.shareAudioButton)).check(matches(isDisplayed()));
        // I tried to verify that all 4 appear, but Espresso gave an error
        // onView(withId(R.id.pOnPButton)).check(matches(isDisplayed()));
        // onView(withId(R.id.chatGroupButton)).check(matches(isDisplayed()));
        // onView(withId(R.id.gestureButton)).check(matches(isDisplayed()));
        // Go back to main UI
        pressBack();
    }

    /* Could not get this test to work. Future project
    @Test
    public void ensureEntryTextAppears() {
        //Enter some text into the message field
        onView(withId(R.id.messageEditText)).perform(typeText("ensureEntryTextAppears"),
                closeSoftKeyboard());
        //Perform a click on the send button
        onView(withId(R.id.sendButton)).perform(click());

        onView(withId(R.id.messageRecyclerView))
                .check(matches(hasDescendant(withText("ensureEntryTextAppears"))));
    }
    */

    @Test
    public void ensureEmojiDialogAppears() {
        // Click Emoji selection
        onView(withId(R.id.emojiTextView)).perform(click());
        // Verify that emoji dialog modal appears by checking the title
        onView(withText("Select Emoji")).check(matches(isDisplayed()));
        // Go back to main UI
        pressBack();
    }

    @Test
    public void ensureGestureDialog1Appears() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on gesture button to bring up gesture dialog modal
        onView(withId(R.id.gestureButton)).perform(click());
        // Verify that gesture dialog modal appears by checking the title
        onView(withText("Gesture Selection")).check(matches(isDisplayed()));
        // Go back to main UI
        pressBack();
    }

    @Test
    public void ensureGestureViewDialogAppears() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on gesture button to bring up gesture dialog modal
        onView(withId(R.id.gestureButton)).perform(click());
        // Click on button to view emoji / gesture assignment
        onView(withId(R.id.rd_1)).perform(click());
        // Verify that proper view gesture / emoji dialog appears by title
        onView(withText("View Gesture Emojis")).check(matches(isDisplayed()));
        // Must do double back to get back to main UI
        pressBack();
    }

    @Test
    public void ensureGestureRecordDialogAppears() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on gesture button to bring up gesture dialog modal
        onView(withId(R.id.gestureButton)).perform(click());
        // Click on button to record gesture and assign to emoji
        onView(withId(R.id.rd_2)).perform(click());
        // Verify that proper view record gesture dialog appears by title
        onView(withText("Capture Gesture")).check(matches(isDisplayed()));
        // Go back to main UI
        pressBack();
    }

    @Test
    public void ensureEnableGestureWorks() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on gesture button to bring up gesture dialog modal
        onView(withId(R.id.gestureButton)).perform(click());
        // Toggle gesture enable/disable button. Should remain in dialog modal
        onView(withId(R.id.rd_t1)).perform(click());
        // Verify that UI remains in gesture dialog modal
        onView(withText("Gesture Selection")).check(matches(isDisplayed()));
        // Go back to main UI
        pressBack();
    }

    @Test
    public void ensureChatDialogAppears() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on chat group button
        onView(withId(R.id.chatGroupButton)).perform(click());
        // Verify that chat group dialog modal appears
        onView(withText(R.string.select_chat_header)).check(matches(isDisplayed()));
        // Go back to main UI
        pressBack();
    }

    @Test
    public void ensureChat1Works() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on chat group button
        onView(withId(R.id.chatGroupButton)).perform(click());
        // Select the chat1 group
        onView(withId(R.id.chatNameEditText)).perform(typeText("chat1"),
                closeSoftKeyboard());
        //Perform a click to select that chat group
        onView(withText("Create")).perform(click());
        // Verify that chat1 group is dispalyed
        onView(withText("Group: chat1")).check(matches(isDisplayed()));
    }

    @Test
    public void ensureNoChatGroupWorks() {
        // Click on bottom overflow button to bring up 4 other options
        onView(withId(R.id.bottomOverflow)).perform(click());
        // Click on chat group button
        onView(withId(R.id.chatGroupButton)).perform(click());
        // Click on none to go back to the default chat onversation
        onView(withText("None")).perform(click());
        // Verify that we are in default by the lack of chat group display
        onView(withId(R.id.chatGroupText)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}