/*
package edu.sfsu.csc780.chathub.ui;

import android.graphics.Bitmap;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;


import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(MockitoJUnitRunner.class)
public class ScaleImageTest {

    @Mock
    Bitmap mMockBitmap, mMockBitmap2, mMockBitmap3;

    int mMockWidth = 200;
    int mMockHeight = 200;

    int mMockWidth2, mMockHeight2;

    Boolean flag;


    @RunWith(PowerMockRunner.class)
    @PrepareForTest(Bitmap.class)
    public class StubBitmapStatic {
        @Test
        public void test() {
            PowerMockito.mockStatic(Bitmap.class);
            when(Bitmap.createScaledBitmap(mMockBitmap2, mMockWidth2, mMockHeight2, flag)).thenReturn(mMockBitmap);

        }
    }



    @Test
    public void testScaleImageBig() {

        when(mMockBitmap.getWidth())
                .thenReturn(mMockWidth);
        when(mMockBitmap.getHeight())
                .thenReturn(mMockHeight);

        when(mMockBitmap2.getWidth())
                .thenReturn(mMockWidth2);
        when(mMockBitmap2.getHeight())
                .thenReturn(mMockHeight2);

        mMockBitmap3 = MainActivity.scaleImage(mMockBitmap);
        int whResult = mMockBitmap3.getWidth() + mMockBitmap3.getHeight();

        assertEquals("scaleImage: Big bitmap failed", 1,
                whResult, 1);
    }

}

*/