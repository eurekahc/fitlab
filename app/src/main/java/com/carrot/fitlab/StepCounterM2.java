package com.carrot.fitlab;

import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Modify from Levente Bagi's Pedometer
 */
public class StepCounterM2 extends StepCounter {
	
	private final static String TAG = "StepCounterM2";
	
	private final static long MIN_TIME = (long) (0.2 * 1000 * 1000 * 1000);
    private final static long MAX_TIME = (long) (2 * 1000 * 1000 * 1000);    
    //private final static float ALPHA = 0.1f;
	
	// 1.97 2.96 4.44 6.66 10.00
    private float   mLimit = 1.98f; //sensitivity
    
    private float   mLastValues = 0f;
    private float   mLastDirections = -2.0f;
    private float   mLastExtremes[] = new float[2];
    private float   mLastDiff = 0;
    private int     mLastMatch = -1;
    
    private boolean isFirst = true;
    private long startTime;    
    private int stepsTotal = 0;
    
    private boolean isWalkingDetected = false;
    private int mValidStepCount = 0;
    private long mTimestampRecord[] = new long[3];
    
    // For recording
    private boolean isRecordFirst = true;
    private long recordStartTime;
    private long lastTime = 0;
    private boolean isShoot = false;
	
	public StepCounterM2(){
		reset();
	}
	
	@Override
	public void reset(){
		mLastValues = -2.0f;
		mLastDirections = -2.0f;
		mLastExtremes[0] = 0;
		mLastExtremes[1] = 0;
		mLastDiff = 0;
		mLastMatch = -1;
		
		mSensorDataString = "";
		isRecordFirst = true;
		
		stepsTotal = 0;
		isFirst = true;
		
		isWalkingDetected = false;
		mValidStepCount = 0;
		mTimestampRecord[0] = 0;
		mTimestampRecord[1] = 0;
		mTimestampRecord[2] = 0;
	}	

	@Override
	public void stepCounting(long curTime, float x, float y, float z){
		isShoot = false;
		if(isFirst){
			startTime = curTime;
			isFirst = false;
		}
		// relative to last valid step
		long relativeTime = curTime - startTime;
		
		lastTime = relativeTime;//for record
		
		if(relativeTime > MAX_TIME){
			isWalkingDetected = false;
			mValidStepCount = 0;
			mTimestampRecord[0] = 0;
			mTimestampRecord[1] = 0;
			mTimestampRecord[2] = 0;
		}
		
		// Movement		
		float v = (float) Math.sqrt(x * x + y * y + z * z);
		
		/*if(mLastValues == -2.0f){
			mLastValues = v;
		}		
		v = mLastValues * ALPHA + v * (1 - ALPHA);*/
        
        float direction = (v > mLastValues ? 1 : (v < mLastValues ? -1 : 0));
        
        if (direction == - mLastDirections){
        	
        	int extType = (direction > 0 ? 0 : 1);
        	mLastExtremes[extType] = mLastValues;
        	
        	float diff = Math.abs(mLastExtremes[extType] - mLastExtremes[1 - extType]);
        	
        	if (diff > mLimit){
        		
        		boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff*3/5);
                boolean isNotTooLarge = diff < mLastDiff*3;
                boolean isNotContra = (mLastMatch != 1 - extType); //Make sure both matches are peaks or valleys
                
                if (isAlmostAsLargeAsPrevious && isNotTooLarge && isNotContra
                		&& relativeTime > MIN_TIME) {
                	if(isDebug){
        				Log.d(TAG, "Step detected");
        			}
                    
                    if(!isWalkingDetected){
                    	
                    	if(mValidStepCount>=0 && mValidStepCount <3){
                    		mTimestampRecord[mValidStepCount] = curTime;
                    	}
                    	
                    	mValidStepCount++;
                    	
                    	if(mValidStepCount > 2){
                    		isShoot = true;
                    		isWalkingDetected = true;
                    		
                    		for (StepListener stepListener : mStepListeners) {
                                stepListener.onStep(mTimestampRecord.length, mTimestampRecord);
                            }                   		
                    		
                    		mValidStepCount = 0;
                    		mTimestampRecord[0] = 0;
                			mTimestampRecord[1] = 0;
                			mTimestampRecord[2] = 0;
                    	}
                    	
                    }else{
                    	isShoot = true;
                    	// One step
                        for (StepListener stepListener : mStepListeners) {
                            stepListener.onStep(1, new long[]{curTime});
                        }
                    	
                    }                    
                    
                    mLastMatch = extType;
                    startTime = curTime;
                    stepsTotal++;                    
                }
                else {
                    mLastMatch = -1;
                }
        		
        	}
        	mLastDiff = diff;        	
        }
        
        mLastDirections = direction;
        mLastValues = v;
        
        // For accuracy rating analyze
        if(isDebug){
        	dataRecord(curTime, x, y, z);
        }
	}
	
	// The heap is growing because of the growing string
	// using this for a little data debug
	private void dataRecord(long actualTime, float x, float y, float z){
		long relativeTime = 0;
		
		if(isRecordFirst){
			recordStartTime = actualTime;
			relativeTime = 0;
			isRecordFirst = false;
		}else{
			relativeTime = actualTime - recordStartTime;
		}
		
		// Movement
	    float acSquareRoot = (float) Math.sqrt(x * x + y * y + z * z);	    
	    
	    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss_SSS");       
		Date curDate = new Date(System.currentTimeMillis());
		String formatTime = formatter.format(curDate);
	    
	    String data = "";				
	    data += String.valueOf(relativeTime);
	    data += ", ";
	    data += String.valueOf(x);
	    data += ", ";
	    data += String.valueOf(y);
	    data += ", ";
	    data += String.valueOf(z);
	    data += ", ";
	    data += String.valueOf(acSquareRoot);
	    data += ", ";
	    data += String.valueOf(mLastValues);
	    data += ", ";
	    data += String.valueOf(mLastDirections);
	    data += ", ";
	    data += String.valueOf(mLastExtremes[0]);
	    data += ", ";
	    data += String.valueOf(mLastExtremes[1]);
	    data += ", ";
	    data += String.valueOf(mLastDiff);
	    data += ", ";
	    data += String.valueOf(mLastMatch);
	    data += ", ";
	    data += String.valueOf(stepsTotal);
	    data += ", ";
	    data += String.valueOf(lastTime);
	    data += ", ";
	    data += formatTime;
	    data += ", ";
	    data += String.valueOf(mValidStepCount);
	    data += ", ";
	    data += String.valueOf(isWalkingDetected);
	    data += ", ";
	    data += String.valueOf(mTimestampRecord[0]);
	    data += ", ";
	    data += String.valueOf(mTimestampRecord[1]);
	    data += ", ";
	    data += String.valueOf(mTimestampRecord[2]);
	    data += ", ";
	    data += String.valueOf(isShoot);
	    data += "\n";
	    
	    mSensorDataString += data;
	}

}
