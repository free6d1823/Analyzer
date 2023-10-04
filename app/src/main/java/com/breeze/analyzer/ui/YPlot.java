package com.breeze.analyzer.ui;

import com.breeze.analyzer.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;

import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class YPlot extends BasicPlot implements GestureDetector.OnGestureListener, View.OnTouchListener {
	private Paint mTagLine;
	private Paint mTagFill;
	private Paint mTrigerLine;
	private Paint mPeakPaint;
	static String TAG = "YPlot";
	Rect mRcTrigger;
	Rect mRcPosA;
	Rect mRcPosB;
	public YPlot(Context context, AttributeSet attrs) {
		super(context, attrs);

		updateScaler();

		xPaint2= new Paint(Paint.ANTI_ALIAS_FLAG);
		xPaint2.setColor(getContext().getResources().getColor( R.color.teal_700));//rpm
		xPaint2.setStyle(Paint.Style.FILL);
		xPaint2.setStrokeWidth(1);				
		xPaint2.setTextAlign( Align.RIGHT);	
		xPaint2.setTextSize(28); 		
		mPaintHint= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintHint.setColor(getContext().getResources().getColor( R.color.teal_700));//fat
		mPaintHint.setStyle(Paint.Style.FILL);
		mPaintHint.setStrokeWidth(1);				
		mPaintHint.setTextAlign( Align.LEFT);	
		mPaintHint.setTextSkewX((float) -0.25);
		mPaintHint.setTextSize(36);  		
		mTagLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTagLine.setColor(0xff00eeee);
		mTagLine.setStyle(Paint.Style.STROKE);
		mTagLine.setStrokeWidth(1);	
		mTagFill= new Paint(Paint.ANTI_ALIAS_FLAG);
		mTagFill.setColor(0xaa888888); 
		mTagFill.setStyle(Paint.Style.FILL);
		mTrigerLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTrigerLine.setColor(context.getColor(R.color.trigger));
		mTrigerLine.setStyle(Paint.Style.FILL);
		mPeakPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPeakPaint.setColor(0xffff0000);
		mPeakPaint.setStyle(Paint.Style.FILL);
		mPeakPaint.setStrokeWidth(5);
	}
	int findMaxYPos(short data[])
	{
		short nMax=0;
		int i = 0;
		int index = 0;
		for (i=0; i<data.length; i++) {
			if (data[i] > nMax)
				{ nMax = data[i]; index=i;}
		}
		mMaxY = nMax;
		return index;
	}
	short mMaxY = 0;
	int nMaxIndex = nCount;
	int nMinIndex = 0;
	int nTriggerIndex= 0; /* index of MaxY in data*/
	int nTriggerPos = nCount/2;
	public void setSeries(short data[]) {
		//buffer is read

		if (buffer == null) {
			return;
		}

		nTriggerIndex = findMaxYPos(data); //position at original data
		nTriggerPos = nCount/2; //position at display buffer array.
		nMinIndex = nTriggerIndex-nCount*mSubSample/2;
		nMaxIndex = nTriggerIndex+nCount*mSubSample/2;


		if (mSubSample > 1) {
			for (int i = 0; i < nCount; i++) {

				if (nMinIndex + (i) * mSubSample < 0) {
					buffer[i] = 0;
				}
				else if (nMinIndex + (i+1) * mSubSample > data.length) {
					buffer[i] = 0;
				} else {
					long k = 0;
					for (int j = 0; j < mSubSample; j++) {
						k += data[nMinIndex+i *mSubSample + j];
					}
					buffer[i] = k/mSubSample;
				}
			}
		} else {
			for (int i = 0; i < nCount; i++) {
				if (nMinIndex + i >=0 && nMinIndex + i < data.length)
					buffer[i] = data[nMinIndex + i];
				else
					buffer[i] = 0;

			}
		}
		findPeaks();
		invalidate();
		if (mTriggerCallback != null && mMaxY >= mTriggerLevel) {
			Log.d("plot", "triger level=" + mTriggerLevel);
			mTriggerCallback.onTriggered(true, mMaxY);
		}
	}
	/* C=261 523 1046
	   D= 293 587 1174
	   E=329 659 1318 (36.42; 36=1333, 37=  1297
	   F=349 698
	   G=392 784 1568 (30.612; 31= 1548, 30=1600)
	   A=440 880
	   B=493 987
	 */
	private void findPeaks(){
		int n = 0;
		int dir = 0;

		float threshold=0;
		int top = 0;
		for(int i=0; i<nCount; i++)
			peaks[i] = 0;
		if (buffer[nTriggerPos] < 600)
			return;
		for(int i=1; i<nCount; i++) {
			if (dir > 0) {
				if (buffer[i] < 0) {
					//end of phase
					dir = -1;
					if(top>0)
						peaks[top]=1;
					threshold = 0;
				} else if (buffer[i] > threshold) {
					threshold = buffer[i];
					top = i;
				}
			} else {
				//negative phase
				 if (buffer[i] < threshold) {
					threshold = buffer[i];
					top = i;
				}else if (buffer[i] > 0) {
					//end of phase
					dir = 1;
					if(top>0)
						peaks[top]=-1;
					threshold = 0;
				}
			}
		}
		int nPeaks = 0;
		int lastPeak = nTriggerPos;
		int period[] = new int[3];
		float average = 0;
		for (int i= nTriggerPos+1; i<nCount; i++) {
			if (peaks[i] == 1) {
				period[nPeaks] = i -lastPeak;
				lastPeak = i;
				nPeaks ++;
				if (nPeaks ==3)
					break;
			}
		}
		lastPeak = 0;
		for (int i=0; i< nPeaks; i++ ){
			average += period[i];
		}
		if (nPeaks > 0) {
			average /= (float)nPeaks;
			Log.d(TAG, "period="+period[0] +"," + period[1]+"," + period[2]);
			Log.d(TAG, " average ="+average+ " samples = "+average*1000.0f/(float)mSampleRate+ "ms, = "+(float)mSampleRate/average +"Hz");
		}

	}
	int mSampleRate=48000;
	protected int[] peaks = null; //pointer assign by setSeries
	public void setSampleRate(int sp) {
		mSampleRate = sp;
		//we wish to hold 4ms for one screen at least resolution
		nCount = mSampleRate*4/1000;
		buffer = new float[nCount];
		peaks = new int[nCount];
	}
	final int MAX_XSCALE_FACTOR = 6;
	int X_SCALE_FACTOR[] = {1, 2, 4, 10,20,40};
	int mCurXScaleFactor = 1;
	int mSubSample= 2;
	//48000/sec, take 10/div, one div = 1/4800 sec = 0.208ms ~12.5ms
	public int setXScale(int increase) {
		mCurXScaleFactor += increase;
		if(mCurXScaleFactor >= MAX_XSCALE_FACTOR)
			mCurXScaleFactor = MAX_XSCALE_FACTOR-1;
		else if (mCurXScaleFactor <0)
			mCurXScaleFactor = 0;
		mSubSample = X_SCALE_FACTOR[mCurXScaleFactor];
		updateScaler();
		invalidate();
		return X_SCALE_FACTOR[mCurXScaleFactor];
	}
	public int getXScale() {return X_SCALE_FACTOR[mCurXScaleFactor];}
	final int MAX_YSCALE_FACTOR = 12;
	int Y_SCALE_FACTOR[] = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000};
	int mCurYScaleFactor = 6;
	public int setYScale(int increase) {
		mCurYScaleFactor += increase;
		if(mCurYScaleFactor >= MAX_YSCALE_FACTOR)
			mCurYScaleFactor = MAX_YSCALE_FACTOR-1;
		else if (mCurYScaleFactor <0)
			mCurYScaleFactor = 0;
		updateScaler();
		invalidate();
		return Y_SCALE_FACTOR[mCurYScaleFactor];
	}
	public int getYScale() { return Y_SCALE_FACTOR[mCurYScaleFactor];}
	void updateScaler(){
		float yScale = Y_SCALE_FACTOR[mCurYScaleFactor];
		yLMax = 32767/yScale;
		yLMin = -yLMax;
		yL2P = (yPMax - yPMin) /(yLMax - yLMin); //negative YP-coordinate
	}

		@Override
	protected void drawTitle(Canvas canvas){
		String szTitle =getContext().getResources().getString(R.string.unit);
  
		canvas.drawText(szTitle, 0, yPMin/5, mPaintTitle);			
		String szRpm =getContext().getResources().getString(R.string.unit);
	
		canvas.drawText(szRpm, xPMin-5, (yPMin+yPMax)/2, xPaint2);
		
	}
	protected void drawPlot(Canvas canvas){
 	 
        float[] line = new float[4];
        float dx = (xPMax - xPMin)/nCount;
        line[0] = xPMin;        
        line[1] = getY(0)* yL2P + yPOrg;
        if (line[1] > yPMin) line[1] = yPMin-1;
        if (line[1] <yPMax)  line[1] = yPMax+1;
        for(int i=1; i < nCount;i++)
        {
        	line[2] = line[0] + dx;
        	line[3] = getY(i)* yL2P + yPOrg;
			if (line[3] > yPMin) line[3] = yPMin-1;
			if (line[3] <yPMax)  line[3] = yPMax+1;
            canvas.drawLines(line, mPaintLine);
            line[0] = line [2];
            line[1] = line[3];
        	
        }
		line[1] =mMaxY * yL2P + yPOrg;
		String value = Integer.toString((int)mMaxY);
		Path tag = new Path();
		float width = mPaintHint.measureText(value)+30;
		float y = line[1];
		tag.moveTo(xPMax+1, y);
		tag.lineTo(xPMax+15, y-20);
		tag.lineTo(xPMax+width, y-20);
		tag.lineTo(xPMax+width, y+20);
		tag.lineTo(xPMax+15, y+20);
		tag.close();
		canvas.drawPath(tag, mTagFill);
		canvas.drawPath(tag, mTagLine);
		canvas.drawText(value, xPMax+15, y+16, mPaintHint);

		if (nTriggerPos >= 0) {
			line[0] = xPMin + dx* nTriggerPos;
			line[1] = yPMin;
			line[2] = line[0];
			line[3] = yPMax;
			canvas.drawLines(line, mTrigerLine);

		}
		//draw peak
		for(int i=1; i < nCount;i++)
		{
			if (peaks[i] == 1) {
				canvas.drawCircle(xPMin + dx * i, getY(i) * yL2P + yPOrg, 5, mPeakPaint);
			}
		}
	}

	@Override
	public boolean onDown(MotionEvent motionEvent) {
		Log.d(TAG, "onDown "+ motionEvent.getRawX() + " Y="+ motionEvent.getRawY());
		return false;
	}

	@Override
	public void onShowPress(MotionEvent motionEvent) {
		Log.d(TAG, "onShowPress x= "+ motionEvent.getRawX() + " Y="+ motionEvent.getRawY());
	}

	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) {
		Log.d(TAG, "onSingleTapUp x= "+ motionEvent.getRawX() + " Y="+ motionEvent.getRawY());
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		Log.d(TAG, "onScroll v= "+ v + " v1="+ v1);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent motionEvent) {
		Log.d(TAG, "onLongPress x= "+ motionEvent.getRawX() + " Y="+ motionEvent.getRawY());

	}

	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		Log.d(TAG, "onFling x= "+ motionEvent.getRawX() + " Y="+ motionEvent.getRawY()+ "v="+v +" v1="+v1);
		return false;
	}
	GestureDetector	mGestureDetector = new GestureDetector(this);
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		Log.d(TAG, "onTouch");
		return mGestureDetector.onTouchEvent(motionEvent);
	}

	public interface OnTriggered {
		void onTriggered(boolean triggered, short value);
	}
	short mTriggerLevel = (short)0x7fffffff;
	OnTriggered mTriggerCallback = null;
	public void setAutoTrigger(int levelPercent, OnTriggered callback){
		if (callback == null ){
			mTriggerLevel = (short)0x7fff;
			mTriggerCallback = null;
		} else{
			mTriggerLevel = (short)(levelPercent*0x7fff/100);
			mTriggerCallback = callback;
		}
	}
}


