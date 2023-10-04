package com.breeze.analyzer.ui;
import com.breeze.analyzer.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class BasicPlot extends View {

		protected Paint mPaintText; //x-axis label text
		protected Paint xPaint1; //Y-axis right label and scale	
		protected Paint xPaint2; //left label and scale			
		protected Paint mPaintFrame; //plot frame
		protected Paint mPaintAxis;
		protected Paint mPaintGride;  //gride
		protected Paint mPaintLine;   //curve
		protected Paint mPaintBkgnd;  //plot bkgnd
		protected Paint mPaintDivider; //scale
		protected Paint mPaintFill;
		protected Paint mPaintHint; //for drawing hint
		protected Paint mPaintTitle; //for drawing hint		
		protected int nCount = 400;
		protected float[] buffer = null; //pointer assign by setSeries

		//graphic options
		protected float yMaxValue = 0;
		protected float yLMin = -30000; //logical Y min value
		protected float yLMax = 30000;
		protected float xLMin = 0; //logical x min value
		protected float xLMax = 160;
		protected int   nYDiv = 40; //gride line in horiz
		protected int   nYDiv2 =8;
		protected int   nXDiv = 40;
		protected int   nXDiv2 =8;
		
		protected float yPMin; //physical vertical position
		protected float yPMax;
		protected float xPMin; //physical horiz position
		protected float xPMax;
		
		protected float xPOrg; //physical origin
		protected float yPOrg;
		protected float xL2P; //logical 1 to phisical pixel
		protected float yL2P;
 
		//data
		
		public BasicPlot(Context context, AttributeSet attrs) {
			super(context, attrs);
 
			mPaintBkgnd = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintBkgnd.setColor(context.getColor(R.color.background));
			mPaintBkgnd.setStyle(Paint.Style.FILL);  
		 
			mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintText.setColor(Color.WHITE);
			mPaintText.setStyle(Paint.Style.FILL);
			mPaintText.setTextAlign(Paint.Align.CENTER);
			mPaintText.setTextSize(24); 
			mPaintTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintTitle.setColor(Color.WHITE);
			mPaintTitle.setStyle(Paint.Style.FILL);
			mPaintTitle.setTextAlign(Paint.Align.LEFT);
			mPaintTitle.setTextSize(30); 			
			mPaintFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintFrame.setColor(context.getColor(R.color.frame));
			mPaintFrame.setStyle(Paint.Style.STROKE);
			mPaintFrame.setStrokeWidth(5);
			mPaintAxis = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintAxis.setColor(context.getColor(R.color.axis));
			mPaintAxis.setStyle(Paint.Style.STROKE);
			mPaintAxis.setStrokeWidth(3);
			mPaintGride = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintGride.setColor(context.getColor(R.color.grid));
			mPaintGride.setStyle(Paint.Style.FILL);
			mPaintGride.setStrokeWidth(1);	

			mPaintDivider= new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintDivider.setColor(context.getColor(R.color.divider));
			mPaintDivider.setStyle(Paint.Style.FILL);
			mPaintDivider.setStrokeWidth(2);
			mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintLine.setColor(context.getColor(R.color.trace));
			mPaintLine.setStyle(Paint.Style.STROKE);
			mPaintLine.setStrokeWidth(1);

			mPaintFill= new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintFill.setColor(context.getColor(R.color.trace));
			mPaintFill.setStyle(Paint.Style.FILL);
			
			//right axis label
			xPaint1= new Paint(Paint.ANTI_ALIAS_FLAG);
			xPaint1.setColor(context.getColor(R.color.axis));//rpm
			xPaint1.setStyle(Paint.Style.FILL);
			xPaint1.setStrokeWidth(1);				
			xPaint1.setTextAlign( Align.LEFT);	
			xPaint1.setTextSize(24);				 		
			
			xLMax = nCount;
		}

		public int getSampleCounts(){return nCount;}
		//clear the y data
		public void clear(){
			buffer = null;
		}
		//
		public void setSeries(float[] pData){
			buffer = pData;
			/* don't changescale */
			/*
			yMaxValue = findYmax();
			yLMax = findYmaxScale(yMaxValue);
			yLMin = -yLMax;
	    	yL2P = (yPMax - yPMin) /(yLMax - yLMin); //negative Y
 			*/
			invalidate();
		}
		protected float findYmax(){
			float ymax = 0;
			for(int i=0;i< buffer.length; i++)
				if(buffer[i] > ymax) ymax = buffer[i];
			return ymax;
		}

/*
		protected float findYmaxScale(float y){
			int b = 1;
			float t =  y;
			float yo;
	 
			while(t>=10)
			{
				b = b*10;
				t = t/10;
			}
	 	
			if(t>5)
				yo = 10*b;
			else if(t>2)
				yo = 5*b;	
			else if(t>1)		
				yo = 2*b;
			else
				yo = b;
	 		if(yo < 50) yo = 50;
			return yo;
		}
*/
		protected float getY(int i)
		{
			if (buffer == null)
				return 0;

			return buffer[i];
		}
		protected void drawPlot(Canvas canvas){
 
		}


		protected void drawPostBackground(Canvas canvas){
		}
		protected void drawBackground(Canvas canvas){
		
		    RectF rcFlame = new RectF(xPMin,yPMax,xPMax,yPMin);	    
			float dx = (xPMax - xPMin)/(float)nXDiv;
			float dy = (yPMin - yPMax)/(float)nYDiv;
			//canvas.drawRGB(0, 128, 180);
	        //p.setAlpha(99);
	        canvas.drawRect(rcFlame, mPaintBkgnd);	    
	        rcFlame.bottom +=3;
	        rcFlame.left --;
	        rcFlame.top --;
	        rcFlame.right +=2;
	        canvas.drawRect(rcFlame, mPaintFrame);

	        float[] line = new float[4];
	        //draw horizontal lines

	        line[0] = xPMin;
	        line[1] = yPMin-dy;
	        line[2] = xPMax;
	        line[3] = yPMin-dy;
	        while(line[1] > yPMax)
	        {
	            canvas.drawLines(line, mPaintGride);
	            line[1] -= dy;
	            line[3] = line[1];
	        }
			/* draw horizontal */
			dy = (yPMin - yPMax)/(float)nYDiv2;
			line[0] = xPMin;
			line[1] = yPMin-dy;
			line[2] = xPMax;
			line[3] = yPMin-dy;
			while(line[1] > yPMax)
			{
				canvas.drawLines(line, mPaintDivider);
				line[1] -= dy;
				line[3] = line[1];
			}
			//*************** draw vertical
	        line[0] = xPMin+dx;
	        line[1] = yPMin;
	        line[2] = xPMin+dx;
	        line[3] = yPMax;
	        while( line[0] < xPMax)
	        {
	            canvas.drawLines(line, mPaintGride);
	            line[0] += dx;
	            line[2] = line[0];
	        }
			/* draw vertical divider line */
			dx = (xPMax - xPMin)/(float)nXDiv2;
			line[0] = xPMin+dx;
			line[1] = yPMin;
			line[2] = xPMin+dx;
			line[3] = yPMax;
			while(line[0] < xPMax)
			{
				canvas.drawLines(line, mPaintDivider);
				line[0] += dx;
				line[2] = line[0];
			}


		}
		//overridable
		protected void drawXScale(Canvas canvas){
			//draw y axis
			float[] line = new float[4];
			line[0] = xPOrg;
			line[1] = yPMin;
			line[2] = xPOrg;
			line[3] = yPMax;

			canvas.drawLines(line, mPaintAxis);
			/*
	        float dp = (xPMax - xPMin)/(float)nXDiv2; //physic interval
	        float dl = xLMax/nXDiv2; //logic interval
  
 

			float x = xPMax; //start x-position
			float y = yPMin+30; //y-position
			
			int vx = 0;
			for(int i=0;i<= nXDiv2;i++)
	        {
	            canvas.drawText(Integer.toString(vx), x, y, mPaintText);
	            x -= dp;
	            vx += dl;
	        }	
			*/
		}
		//overridable
		protected void drawYScale(Canvas canvas){
			/* draw X axis */
			float[] line = new float[4];
			line[0] = xPMin;
			line[1] = yPOrg;
			line[2] = xPMax;
			line[3] = yPOrg;

			canvas.drawLines(line, mPaintAxis);
			/*
	        float dp = (yPMax - yPMin)/(float)nYDiv2; //physic interval
	        int dl = (int) yLMax/nYDiv2; //logic interval
 
			float x = xPMax+12; //x-position
			float y = yPMin+12; //y-position
 
			int vy = (int)yLMin;
			for(int i=0;i<= nYDiv2;i++)
	        {
	            canvas.drawText(Integer.toString(vy), x, y, xPaint1);
	            y += dp;
	            vy += dl;
	        }	
			*/
		}	
		//overridable
		protected void drawTitle(Canvas canvas){
		
		}
		 @Override
	    protected void onSizeChanged(int w, int h, int oldw, int oldh)
	    {
	        super.onSizeChanged(w, h, oldw,oldh);
	    	yPMin = (float) (h*0.85); //physical vertical position
	    	yPMax = (float) (h*0.05);
	    	xPMin = (float) (w*0.05); //physical horiz position
	    	xPMax = (float) (w*0.9);
	    	
	    	xPOrg = (xPMin+xPMax)/2; //physical origin
	    	yPOrg = (yPMax+yPMin)/2;
	    	xL2P = w/nCount; //logical 1 to phisical pixel
	    	yL2P = (yPMax - yPMin) /(yLMax - yLMin); //negative Y

	    }
 
		@Override
		protected void onDraw(Canvas canvas){

			drawBackground(canvas);
			drawPostBackground(canvas);
			drawTitle(canvas);
			drawXScale(canvas);
			drawYScale(canvas);			
			drawPlot(canvas);
		           
		}
	}
