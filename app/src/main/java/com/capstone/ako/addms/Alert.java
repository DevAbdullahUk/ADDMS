package com.capstone.ako.addms;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Alert extends AppCompatActivity {

    private ProgressBar mProgressBar;

    private int mProgressStatus = 0;

    TextView label;

    String s;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alert);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mHandler = new Handler();

        s = getIntent().getStringExtra("ID");

        label = (TextView) findViewById(R.id.txt1);

        mProgressBar.getProgressDrawable().setColorFilter(
                Color.parseColor("#FF6A2220"), android.graphics.PorterDuff.Mode.SRC_IN);
        mProgressBar.setScaleY(6f);

        label.setText(s);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < 100){
                    mProgressStatus++;
                    android.os.SystemClock.sleep(100);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                       // code here
                    }
                });

                finish();
            }
        }).start();
    }
}
