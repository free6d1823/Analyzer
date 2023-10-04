package com.breeze.analyzer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.breeze.analyzer.ui.YPlot;

@SuppressWarnings("ALL")
public class MainActivity extends Activity{
    static String TAG = "Analyzer";
    YPlot mWaiveView;
    TextView mTvInfo;
    TextView mTvX;
    TextView mTvY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWaiveView = (YPlot) findViewById(R.id.waveView);
        mTvInfo = (TextView)findViewById(R.id.tvInfo);
        mTvX = (TextView)findViewById(R.id.tvXaxis);
        mTvY = (TextView)findViewById(R.id.tvYaxis);
        initCallbacks();

    }
    BroadcastReceiver mReceiver = null;
    public static String UPDATE_WAVE = "update_view";
    void initCallbacks(){
        mReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UPDATE_WAVE.equals(action)) {
                    mWaiveView.setSeries(mBuffer);
                }


            }

        };
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        if (startRecording()) {
            filter.addAction(UPDATE_WAVE);
            registerReceiver(mReceiver, filter);
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        stopRecording();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        unregisterReceiver(mReceiver);
    }
    private boolean isRecording = false;
    private AudioRecord mRecorder = null;
    private Thread recordingThread = null;
    private boolean startRecording() {
        Log.d(TAG, "startRecording");

        if(mRecorder == null) {
            if (!createAudioRecord()) {
                Toast.makeText(this, "ERROR!! Cannot create recorder.", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        mRecorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                Log.d(TAG, "recordingThread");
                readAudioData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
        return true;
    }
    private void stopRecording() {
        // stops the recording activity

        if (null != mRecorder) {
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            Log.d(TAG, "stopRecording");
            mRecorder = null;

            recordingThread = null;
        }
    }
    int mSampleRate = 48000;
    int mMinBuffer;
    short mBuffer[] = null;

    private boolean createAudioRecord() {
        for (int sampleRate : new int[] { 48000, 44100, 32000, 16000, 8000 }) {
            try {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
                    return false;
                }

                mMinBuffer = mSampleRate*16/100;//take 160ms
                mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        mMinBuffer);

                if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    /* take mMinBuffer*/
                    mBuffer = new short[mMinBuffer];
                    Log.d(TAG, "init audio sp="+sampleRate);
                    mWaiveView.setSampleRate(mSampleRate);
                    updateUi();
                    return true;
                }
                mRecorder.release();
                mRecorder = null;
            } catch (Exception e) {
                // Do nothing
            }
        }
        return false;
    }
    private void readAudioData() {

        while (isRecording) {
            // gets the voice output from microphone to byte format

            if (mPause == 0) {
                mRecorder.read(mBuffer, 0, mMinBuffer);
            }

                final Intent intent = new Intent(UPDATE_WAVE);
                sendBroadcast(intent);

        }
    }

    public void onClickAmptitude(View view) {
        int yScaleFactor = (view.getId() == R.id.btnUp)?1:-1;
        int scale = mWaiveView.setYScale(yScaleFactor);
        mTvY.setText("x"+scale);
    }
    public void onClickSample(View view) {
        int xScaleFactor = (view.getId() == R.id.btnHi)?-1:1;
        int scale = mWaiveView.setXScale(xScaleFactor);

        int us = mWaiveView.getSampleCounts()*scale*125000/mSampleRate;
        if(us < 1000)
            mTvX.setText(us+ "us/div");
        else
            mTvX.setText(us/1000+ "ms/div");
    }
    int mPause = 0;
    public void onClickTrigger(View view) {
        mPause = 1-mPause;
    }
    public void onClickAuto(View view) {
        if (mPause != 0) {
            Log.d(TAG, "pause="+mPause);
            mWaiveView.setAutoTrigger((short) 0, null);
            mPause = 0;
        }
        else {
            Log.d(TAG, "setAutoTrigger level = 0x1000");
            mWaiveView.setAutoTrigger( 33, new YPlot.OnTriggered() {
                @Override
                public void onTriggered(boolean triggered, short value) {
                    if (triggered) {
                        mPause = 1;
                        Log.d(TAG, "Ymax = " + value);
                        mWaiveView.setAutoTrigger((short) 0, null);
                    }

                }
            });
        }

    }
    void updateUi()
    {
        int us = mWaiveView.getSampleCounts()*mWaiveView.getXScale()*125000/mSampleRate;
        if(us < 1000)
            mTvX.setText(us+ "us/div");
        else
            mTvX.setText((int)us/1000+ "ms/div");
        int scale = mWaiveView.getYScale();
        mTvY.setText("x"+scale);
    }

}