package com.yidianhulian.ydmemo.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SideBar extends View {
    private OnItemClickListener mOnItemClickListener;
    String[] mSections = {};
    List<String> mAvailablelSections = new ArrayList<String>();
    int mChoose = -1;
    Paint mPaint = new Paint();
    boolean showBkg = false;
    private PopupWindow mPopupWindow;
    private TextView mPopupText;
    private Handler mHandler = new Handler();

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }
    
    public void setSections(String[] sections){
        mSections = sections;
    }
    
    public void setAvailablelSections(String[] sections){
        mAvailablelSections = Arrays.asList(sections);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBkg) {
            canvas.drawColor(Color.parseColor("#00000000"));
        }
        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / (mSections.length==0 ? 1 : mSections.length);
        

        int pixel= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                      20, getResources().getDisplayMetrics());
        float xPos = width / 2 - pixel / 2;
        
        for (int i = 0; i < mSections.length; i++) {
            
            mPaint.setTypeface(Typeface.MONOSPACE);
            mPaint.setFakeBoldText(true);
            mPaint.setTextSize(pixel);
            mPaint.setAntiAlias(true);
            if (i == mChoose) {
                mPaint.setColor(Color.parseColor("#3399ff"));
            }
            if(mAvailablelSections.indexOf(mSections[i]) == -1){
                mPaint.setColor(getContext().getResources().getColor(com.yidianhulian.ydmemo.R.color.lightgray));
            }else{
                mPaint.setColor(Color.BLACK);
            }
            
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(mSections[i], xPos, yPos, mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = mChoose;
        final int c = (int) (y / getHeight() * mSections.length);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            showBkg = true;
            if (oldChoose != c) {
                if (c > 0 && c < mSections.length) {
                    performItemClicked(c);
                    mChoose = c;
                    invalidate();
                }
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (oldChoose != c) {
                if (c > 0 && c < mSections.length) {
                    performItemClicked(c);
                    mChoose = c;
                    invalidate();
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            showBkg = false;
            mChoose = -1;
            dismissPopup();
            invalidate();
            break;
        }
        return true;
    }

    private void showPopup(int item) {
        if (mPopupWindow == null) {
            mHandler.removeCallbacks(dismissRunnable);
            mPopupText = new TextView(getContext());
            mPopupText.setBackgroundResource(com.yidianhulian.ydmemo.R.drawable.context_menu_bg);
            mPopupText.setTextColor(getContext().getResources().getColor(com.yidianhulian.ydmemo.R.color.drawer_bg_color));
            mPopupText.setTextSize(50);
            mPopupText.setGravity(Gravity.CENTER_HORIZONTAL
                    | Gravity.CENTER_VERTICAL);
            mPopupWindow = new PopupWindow(mPopupText, 200, 200);
        }
        String text = "";
        if (item == 0) {
            text = "#";
        } else {
            text = Character.toString((char) ('A' + item - 1));
        }
        mPopupText.setText(text);
        if (mPopupWindow.isShowing()) {
            mPopupWindow.update();
        } else {
            mPopupWindow.showAtLocation(getRootView(),
                    Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        }
    }

    private void dismissPopup() {
        mHandler.postDelayed(dismissRunnable, 800);
    }

    Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mPopupWindow != null) {
                mPopupWindow.dismiss();
            }
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void performItemClicked(int item) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(mSections[item]);
            showPopup(item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String s);
    }
}
