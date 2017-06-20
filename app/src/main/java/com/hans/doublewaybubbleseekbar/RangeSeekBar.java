package com.hans.doublewaybubbleseekbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by hanszhli on 2017/6/16.
 */

public class RangeSeekBar extends View {
    private static final String TAG = "RangeSeekBar";
    private int mViewWidth;
    private int mViewHeight;

    private int mSeekBarWidth;
    private int mSeekBarHeight;
    private float mSbLeft, mSbTop, mSbRight, mSbBottom;
    private float mSbRound;

    private float mLPLeft;//left-pointer  left
    private float mLPRight;//right-pointer right
    private float mLPOffset;
    private float mLPLastX;

    private float mRPLeft;//right-pointer left
    private float mRPRight;//right-pointer right
    private float mRPOffset;
    private float mRPLastX;

    private Drawable mPointerDrawable;

    private Paint mNormalPaint;
    private Paint mProgressPaint;

    private boolean mIsDragLeft;
    private boolean mIsDragRight;
    private boolean mLastIsDragLeft;

    private int mMaxRange;
    private OnRangeProgressListener mListener;

    private boolean mIsRangeEnable;

    private int mLpProgress;
    private int mRpProgress;


    public RangeSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setRangeEnable(boolean isRangeEnable) {
        mIsRangeEnable = isRangeEnable;
        invalidate();
    }

    /**
     * 重置Range位置
     */
    public void resetRangePos() {
        mLPOffset = 0;
        mRPOffset = 0;
        if (mLPLeft != 0) {
            ValueAnimator lpResetAni = ValueAnimator.ofFloat(mLPLeft, 0);
            lpResetAni.setDuration(200);
            lpResetAni.setInterpolator(new AccelerateDecelerateInterpolator());
            lpResetAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mLPLeft = (float) animation.getAnimatedValue();
                    calculateLPRect();
                    invalidate();
                }
            });
            lpResetAni.start();
        }
        if (mRPRight != mViewWidth) {
            ValueAnimator rpResetAni = ValueAnimator.ofFloat(mRPRight, mViewWidth);
            rpResetAni.setDuration(200);
            rpResetAni.setInterpolator(new AccelerateDecelerateInterpolator());
            rpResetAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRPRight = (float) animation.getAnimatedValue();
                    calculateRPRect();
                    invalidate();
                }
            });
            rpResetAni.start();
        }
    }

    /**
     * 设置range范围
     *
     * @param range
     */
    public void setMaxRange(int range) {
        if (mRpProgress == mMaxRange) {
            mRpProgress = range - 1;
        }
        mMaxRange = range - 1;
    }


    public void setRangeProgress(int lpProgress, int rpProgress) {
        if (lpProgress > rpProgress || rpProgress >= mMaxRange) {
            throw new IllegalStateException("非法!");
        }
        mLpProgress = lpProgress;
        mRpProgress = rpProgress;
        calculateLPPos(lpProgress);
        calculateRPPos(rpProgress);
        invalidate();
    }

    public int getLpProgress() {
        return mLpProgress;
    }

    public int getRpProgress() {
        return mRpProgress;
    }

    private void init(AttributeSet attrs) {
        mIsRangeEnable = true;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
        mPointerDrawable = a.getDrawable(R.styleable.RangeSeekBar_rsb_pointerBackground);
        int progressColor = a.getColor(R.styleable.RangeSeekBar_rsb_progressColor,
                Color.parseColor("#FF4081"));
        int backgroundColor = a.getColor(R.styleable.RangeSeekBar_rsb_backgroundColor,
                Color.parseColor("#BBBBBB"));
        mMaxRange = a.getInt(R.styleable.RangeSeekBar_rsb_maxRange, 100) - 1;
        a.recycle();
        mRpProgress = mMaxRange;
        mLpProgress = 0;
        mNormalPaint = new Paint();
        mNormalPaint.setColor(backgroundColor);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        mSeekBarWidth = mViewWidth - mPointerDrawable.getIntrinsicWidth();
        mSeekBarHeight = mViewHeight - 20;

        int halfDrawableWidth = mPointerDrawable.getIntrinsicWidth() / 2;
        mSbLeft = halfDrawableWidth;
        mSbTop = 10;
        mSbBottom = mViewHeight - 10;
        mSbRight = mViewWidth - halfDrawableWidth;
//        mSbRound =MsB / 2;
        mSbRound = mSeekBarHeight / 2;

        mLPLeft = 0;
        mRPRight = mViewWidth;

        calculateLPRect();

        calculateRPRect();

        mLPLastX = mLPLeft;
        mRPLastX = mRPRight;

    }

    private void calculateRPRect() {
        mRPRight = getRPRight(mRPOffset);
        mRPLeft = mRPRight - mPointerDrawable.getIntrinsicWidth();
    }

    private void calculateLPRect() {
        mLPLeft = getLPLeft(mLPOffset);
        mLPRight = mLPLeft + mPointerDrawable.getIntrinsicWidth();
    }


    private float getLPLeft(float offset) {
        return mLPLeft + offset;
    }

    private float getRPRight(float offset) {
        return mRPRight + offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        //draw  seek bar bg
        RectF rectF = new RectF();
        rectF.left = mSbLeft;
        rectF.right = mSbRight;
        rectF.top = mSbTop;
        rectF.bottom = mSbBottom;
        //draw  bg
        canvas.drawRoundRect(rectF, mSbRound, mSbRound, mNormalPaint);

        if (mIsRangeEnable) {
            //draw progress
            canvas.drawRect(
                    mLPLeft + mPointerDrawable.getIntrinsicWidth() / 2,
                    mSbTop,
                    mRPRight - mPointerDrawable.getIntrinsicWidth() / 2,
                    mSbBottom,
                    mProgressPaint);

            //draw left pointer
            Rect leftRect = new Rect();
            leftRect.left = (int) mLPLeft;
            leftRect.right = (int) mLPRight;
            leftRect.top = 0;
            leftRect.bottom = mViewHeight;

            mPointerDrawable.setBounds(leftRect);
            mPointerDrawable.draw(canvas);

            //draw right pointer
            Rect rightRect = new Rect();
            rightRect.left = (int) mRPLeft;
            rightRect.right = (int) mRPRight;
            rightRect.top = 0;
            rightRect.bottom = mViewHeight;

            mPointerDrawable.setBounds(rightRect);
            mPointerDrawable.draw(canvas);
        }
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
        if (mIsDragLeft || mIsDragRight) {
            mIsDragLeft = false;
            mIsDragRight = false;
            if (mListener != null) {
                mListener.onSeekUp();
            }
            return true;
        }
        return false;
    }


    private boolean handleMoveEvent(MotionEvent event) {
        if (mIsDragLeft || mIsDragRight) {
            float x = event.getX();
            if (mIsDragLeft) {
                mLPOffset = x - mLPLastX;
                mLPLastX = x;
                float tmpLPRight = mLPLeft + mLPOffset + mPointerDrawable.getIntrinsicWidth();
                if (tmpLPRight > mRPRight) {
                    mLPOffset = mRPRight - mLPRight;
                    mLPLastX = mRPRight;
                    mIsDragLeft = false;
                }
                float tmpLPLeft = tmpLPRight - mPointerDrawable.getIntrinsicWidth() / 2;
                if (tmpLPLeft <= mSbLeft) {
                    mIsDragLeft = false;
                    return true;
                }
                calculateLPRect();
                invalidate();
            }
            if (mIsDragRight) {
                mRPOffset = x - mRPLastX;
                mRPLastX = x;
                float tmpRPLeft = mRPRight + mRPOffset - mPointerDrawable.getIntrinsicWidth();
                if (tmpRPLeft < mLPLeft) {
                    mRPOffset = mLPLeft - mRPLeft;
                    mRPLastX = mLPLeft;
                    mIsDragRight = false;
                }
                if (tmpRPLeft + mPointerDrawable.getIntrinsicWidth() >= mViewWidth) {
                    mIsDragRight = false;
                    return true;
                }
                calculateRPRect();
                invalidate();
            }
            callbackListener();
            return true;
        }
        return false;
    }

    private void callbackListener() {
        mLpProgress = calculateLPProgress();
        mRpProgress = calculateRPProgress();
        if (mListener != null) {
            mListener.onSeekProgress(mLpProgress, mRpProgress);
        }
    }

    private boolean handleDownEvent(MotionEvent event) {
        if (!mIsRangeEnable) return false;
        float x = event.getX();
        float y = event.getY();
        if (x >= mLPLeft - 30 && x <= mLPRight + 30) {
            mIsDragLeft = true;
            mLPLastX = x;
        }
        if (x <= mRPRight + 30 && x >= mRPLeft - 30) {
            mIsDragRight = true;
            mRPLastX = x;
        }

        if (mIsDragLeft && mIsDragRight) {
            if (mLastIsDragLeft) {
                mIsDragLeft = true;
                mIsDragRight = false;
                mLastIsDragLeft = true;
            } else {
                mIsDragLeft = false;
                mIsDragRight = true;
                mLastIsDragLeft = false;
            }
        }

        mLastIsDragLeft = mIsDragLeft;

        if (mIsDragRight || mIsDragLeft) {
            if (mListener != null)
                mListener.onSeekDown();
            return true;
        }
        return false;
    }


    private int calculateLPProgress() {
        float lpCenter = mLPLeft + mPointerDrawable.getIntrinsicWidth() / 2;
        if (lpCenter == mSbLeft) {
            return 0;
        } else {
            float leftDis = lpCenter - mSbLeft;
            float percent = leftDis / mSeekBarWidth;
            if (percent <= 0.005) percent = 0;
            return (int) (percent * mMaxRange);
        }
    }

    private void calculateLPPos(int lpProgress) {
        if (lpProgress == 0) {
            mLPLeft = mSbLeft - mPointerDrawable.getIntrinsicWidth() / 2;
        } else {
            float percent = lpProgress * 1.0f / mMaxRange;
            float leftDis = percent * mSeekBarWidth;
            float lpCenter = leftDis + mSbLeft;
            mLPLeft = lpCenter - mPointerDrawable.getIntrinsicWidth() / 2;
        }
        mLPOffset = 0;
        calculateLPRect();
    }

    private int calculateRPProgress() {
        float rpCenter = mRPRight - mPointerDrawable.getIntrinsicWidth() / 2;
        if (rpCenter == mSbRight) {
            return 1 * mMaxRange;
        } else {
            float rightDis = mSbRight - rpCenter;
            float percent = rightDis / mSeekBarWidth;
            if (percent <= 0.005) percent = 0;
            return (int) ((1 - percent) * mMaxRange);
        }
    }

    private void calculateRPPos(int rpProgress) {
        if (rpProgress == mMaxRange) {
            mRPRight = mSbRight + mPointerDrawable.getIntrinsicWidth() / 2;
        } else {
            float percent = 1 - rpProgress * 1.0f / mMaxRange;
            float rightDis = percent * mSeekBarWidth;
            float rpCenter = mSbRight - rightDis;
            mRPRight = rpCenter + mPointerDrawable.getIntrinsicWidth() / 2;
        }
        mRPOffset = 0;
        calculateRPRect();
    }


    public void setOnRangeProgressListener(OnRangeProgressListener listener) {
        mListener = listener;
    }

    public interface OnRangeProgressListener {
        void onSeekDown();

        void onSeekUp();

        void onSeekProgress(int lpProgress, int rpProgress);
    }

}
