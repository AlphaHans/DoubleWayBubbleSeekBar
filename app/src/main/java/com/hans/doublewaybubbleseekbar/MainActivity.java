package com.hans.doublewaybubbleseekbar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private DoubleWaySeekBar mDoubleWaySeekBar;
    private RangeSeekBar mRsb;
    private Button mBtnOpenRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDoubleWaySeekBar = (DoubleWaySeekBar) findViewById(R.id.main_dwsb_value);
        mDoubleWaySeekBar.setOnSeekProgressListener(new DoubleWaySeekBar.OnSeekProgressListener() {
            @Override
            public void onSeekDown() {
                Log.i(TAG, "onSeekDown: ");
            }

            @Override
            public void onSeekUp() {
                Log.i(TAG, "onSeekUp: ");
            }

            @Override
            public void onSeekProgress(float progress) {
                Log.i(TAG, "onSeekProgress: " + progress);
            }
        });
        mRsb = (RangeSeekBar) findViewById(R.id.main_range_sb);

        findViewById(R.id.main_btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleWaySeekBar.resetSeekBar();
                mRsb.resetRangePos();
            }
        });
        mRsb.setOnRangeProgressListener(new RangeSeekBar.OnRangeProgressListener() {
            @Override
            public void onSeekDown() {
                Log.i(TAG, "onSeekDown: ");
            }

            @Override
            public void onSeekUp() {
                Log.i(TAG, "onSeekUp: ");
            }

            @Override
            public void onSeekProgress(int lpProgress, int rpProgress) {
                Log.i(TAG, "onSeekProgress: lp =  " + lpProgress + " rp = " + rpProgress);
            }
        });
        mBtnOpenRange = (Button) findViewById(R.id.main_btn_is_show);
        mBtnOpenRange.setSelected(true);
        mBtnOpenRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRsb.setRangeEnable(!mBtnOpenRange.isSelected());
                mBtnOpenRange.setSelected(!mBtnOpenRange.isSelected());
            }
        });
    }


}
