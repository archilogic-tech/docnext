/*
 * author : tomorrowkey
 * license : Apache License 2.0
 * URL : http://code.google.com/p/tomorrowkey/
 */

package jp.archilogic.docnext.android.setting;

import java.io.File;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class VTextView extends View {

    private static final String FONT_PATH = 
        Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "ipaexm.ttf";

    private static final int TOP_SPACE = 18;

    private static final int BOTTOM_SPACE = 18;

    private static final int FONT_SIZE = 18;

    private Typeface mFace;

    private Paint mPaint;

    private String text = "";

    private int width;

    private int height;

    public VTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(FONT_SIZE);
        mPaint.setColor(Color.WHITE);
        
        if ( ( new File( FONT_PATH ) ).exists() ) {
            mFace = Typeface.createFromFile(FONT_PATH);
            mPaint.setTypeface(mFace);
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = getWidth();
        height = getHeight();
    }
    
    public void setTextSize( int point ) {
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, 
                (float) point , getResources().getDisplayMetrics());
        
        mPaint.setTextSize(pixel);
    }
    
    public void setTextColor( int color ) {
        mPaint.setColor(color);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        float fontSpacing = mPaint.getFontSpacing();
        float lineSpacing = fontSpacing * 2;
        float x = width - lineSpacing;
        float y = TOP_SPACE + fontSpacing * 2;
        String[] s = text.split("");
        boolean newLine = false;

        for (int i = 1; i <= text.length(); i++) {
            newLine = false;

            CharSetting setting = CharSetting.getSetting(s[i]);
            if (setting == null) {
                canvas.drawText(s[i], x, y, mPaint);
            } else {
                canvas.save();
                canvas.rotate(setting.angle, x, y);
                canvas.drawText(s[i], x + fontSpacing * setting.x, y + fontSpacing * setting.y,
                        mPaint);
                canvas.restore();
            }

            if (y + fontSpacing > height - BOTTOM_SPACE) {
                newLine = true;
            } else {
                newLine = false;
            }

            if (newLine) {
                x -= lineSpacing;
                y = TOP_SPACE + fontSpacing;
            } else {
                y += fontSpacing;
            }
        }
    }
}
