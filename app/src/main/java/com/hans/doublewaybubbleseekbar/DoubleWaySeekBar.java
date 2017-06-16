package com.hans.doublewaybubbleseekbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by hanszhli on 2017/6/16.
 * <p>
 * 可以向两个方向滑动的seekbar
 */
public class DoubleWaySeekBar extends View {
    private static final String TAG = "DoubleWaySeekBar";
    private int mWidth;
    private int mHeight;

    private int mNormalBgStartX;
    private int mNormalBgEndX;

    private int mBgTop;
    private int mBgBottom;
    private int mRoundSize;

    //终点位置
    private int mViewMiddleXPos;


    private Paint mNormalPaint;
    private Paint mPointerPaint;
    private Paint mProgressPaint;


    private float mPointerLeft;
    private float mPointerRight;
    private float mPointerTop;
    private float mPointerBottom;

    private float mCurrentLeftOffset = 0;
    //是否处于点击状态
    private boolean mIsOnDrag;
//    private int mTouchSlop;

    private float mLastX;

    private Drawable mPointerDrawable;
    private int mHalfDrawableWidth;
    private int mRange;

    public DoubleWaySeekBar(Context context) {
        super(context);
        init(null);
    }

    public DoubleWaySeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DoubleWaySeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        int progressColor = Color.YELLOW;
        int backgroundColor = Color.GRAY;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DoubleWaySeekBar);
            mPointerDrawable = a.getDrawable(R.styleable.DoubleWaySeekBar_pointerBackground);
            mHalfDrawableWidth = mPointerDrawable.getIntrinsicWidth() / 2;
            progressColor = a.getColor(R.styleable.DoubleWaySeekBar_progressColor,
                    Color.BLACK);
            backgroundColor = a.getColor(R.styleable.DoubleWaySeekBar_backgroundColor,
                    Color.BLACK);
            mRange = a.getInteger(R.styleable.DoubleWaySeekBar_range, 100);
            a.recycle();
        }
        mNormalPaint = new Paint();
        mNormalPaint.setColor(backgroundColor);

        mPointerPaint = new Paint();
        mPointerPaint.setColor(Color.RED);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mNormalBgStartX = mHalfDrawableWidth;
        mNormalBgEndX = mWidth - mHalfDrawableWidth;

        mBgTop = 10;
        mBgBottom = mHeight - 10;
        mRoundSize = mHeight / 2;

        mViewMiddleXPos = mWidth / 2;

        mPointerLeft = mViewMiddleXPos - mHalfDrawableWidth;
        mLastX = mPointerLeft;
        calculatePointerRect();

        Log.i(TAG, "onSizeChanged: width = " + mWidth + " height =  " + mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw  bg
        canvas.drawRoundRect(mNormalBgStartX, mBgTop, mNormalBgEndX, mBgBottom, mRoundSize, mRoundSize, mNormalPaint);

        //draw progress
        // draw left
        if (mPointerRight < mViewMiddleXPos) {
            canvas.drawRect(mPointerRight - mHalfDrawableWidth, mBgTop, mViewMiddleXPos, mBgBottom, mProgressPaint);
        }
        // draw right
        if (mPointerLeft > mViewMiddleXPos) {
            canvas.drawRect(mViewMiddleXPos, mBgTop, mPointerLeft + mHalfDrawableWidth, mBgBottom, mProgressPaint);
        }

        //draw pointer
        Rect rect = new Rect();
        rect.left = (int) mPointerLeft;
        rect.top = (int) mPointerTop;
        rect.right = (int) mPointerRight;
        rect.bottom = (int) mPointerBottom;

        mPointerDrawable.setBounds(rect);
        mPointerDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        boolean isHandle = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isHandle = handleDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isHandle = handleMoveEvent(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isHandle = handleUpEvent(event);
                break;

        }
        return isHandle;
    }

    private boolean handleUpEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsOnDrag) {
            mIsOnDrag = false;
            if (mListener != null) {
                mListener.onSeekUp();
            }
            return true;
        }
        return false;
    }

    private boolean handleMoveEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsOnDrag) {
            mCurrentLeftOffset = x - mLastX;
            //计算出标尺的Rect
            calculatePointerRect();
            if (mPointerRight - mHalfDrawableWidth <= mNormalBgStartX) {
                mPointerLeft = 0;
                mPointerRight = mPointerLeft + mPointerDrawable.getIntrinsicWidth();
            }
            if (mPointerLeft + mHalfDrawableWidth >= mNormalBgEndX) {
                mPointerRight = mWidth;
                mPointerLeft = mWidth - mPointerDrawable.getIntrinsicWidth();
            }
            invalidate();
            callbackProgress();
            mLastX = x;
            return true;
        }
        return false;
    }

    private void callbackProgress() {
        if (mPointerLeft == 0) {
            callbackProgressInternal(-mRange);
        } else if (mPointerRight == mWidth) {
            callbackProgressInternal(mRange);
        } else {
            float pointerMiddle = mPointerLeft + mHalfDrawableWidth;
            if (pointerMiddle == mViewMiddleXPos) {
                callbackProgressInternal(0);
            } else {
                double percent = Math.abs(mViewMiddleXPos - pointerMiddle) / mViewMiddleXPos * 1.0;
                if (mPointerLeft < mViewMiddleXPos) {
                    callbackProgressInternal((int) (-percent * mRange));
                } else {
                    callbackProgressInternal((int) (percent * mRange));
                }
            }
        }
    }

    private void callbackProgressInternal(int progress) {
        Log.i(TAG, "callbackProgressInternal: " + progress);
        if (mListener != null) {
            mListener.onSeekProgress(progress);
        }
    }


    private boolean handleDownEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (x >= mPointerLeft && x <= mPointerRight && y >= mPointerTop && y <= mPointerBottom) {
            if (mListener != null)
                mListener.onSeekDown();
            mIsOnDrag = true;
            return true;
        }
        return false;
    }

    private void calculatePointerRect() {
        //draw pointer
        float pointerLeft = getPointerLeft(mCurrentLeftOffset);
        float pointerRight = pointerLeft + mPointerDrawable.getIntrinsicWidth();
        mPointerLeft = pointerLeft;
        mPointerRight = pointerRight;
        mPointerTop = 0;
        mPointerBottom = mHeight;
    }

    /**
     * 进行复位
     */
    public void resetSeekBar() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mPointerLeft, mViewMiddleXPos);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPointerLeft = (float) animation.getAnimatedValue();
                mPointerRight = mPointerLeft + mPointerDrawable.getIntrinsicWidth();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    private float getPointerLeft(float offset) {
        return mPointerLeft + offset;
    }

    private OnSeekProgressListener mListener;

    public void setOnSeekProgressListener(OnSeekProgressListener listener) {
        mListener = listener;
    }

    public interface OnSeekProgressListener {
        void onSeekDown();

        void onSeekUp();

        void onSeekProgress(float progress);
    }
}
