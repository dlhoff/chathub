/**
 *
 */
package edu.sfsu.csc780.chathub.ui;

import android.graphics.Bitmap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ScaleImageTest {

    @Mock
    Bitmap mMockBitmap;

    int mMockWidth = 200;
    int mMockHeight = 200;

    @Test
    public void testScaleImage() {
        int widthResult, heightResult;
        int expectedResult = 400;

        when(mMockBitmap.getWidth())
                .thenReturn(mMockWidth);
        when(mMockBitmap.getHeight())
                .thenReturn(mMockHeight);

        Bitmap bmpResult = MainActivity.scaleImage(mMockBitmap);
        widthResult = bmpResult.getWidth();
        heightResult = bmpResult.getHeight();

        assertEquals("scaleImage: bitmap failed", expectedResult, widthResult + heightResult, 1);
    }

}

