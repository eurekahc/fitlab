package com.carrot.fitlab;

import android.hardware.SensorManager;
import android.util.Log;

/**
 * Learn from AtomWare
 */
public class StepCounterM1 extends StepCounter {
	
	private final static String TAG = "StepCounterM1";
		
	private static final float G_STAND = SensorManager.STANDARD_GRAVITY;
	private static final float THRESHOLD_UP = SensorManager.STANDARD_GRAVITY * 1.2f;
	private static final float THRESHOLD_DOWN = SensorManager.STANDARD_GRAVITY * 0.8f;
	private static final float THRESHOLD_WIDE = SensorManager.STANDARD_GRAVITY * 0.4f;
	
	//private int queNumber = 0;
	//private float queValue[] = new float[5];
	private float thrUp, thrDown, thrWide;
	
	private ANote curNote, preNote;
	private ANote peak[] = new ANote[5];
	private ANote valley[] = new ANote[5];
	
	private int speNumber = 0;
	
	private long startTime = 0;
	private boolean isFirst = true;
	private long timeout = 0;
	
	private int stepCount = 0;
	
	private boolean isWalkingDetected = false;
    private int mValidStepCount = 0;
    private long mTimestampRecord[] = new long[3];
	
	private class ANote{
		public float data;
		public long time;
		
		public ANote(){
			data = G_STAND;
			time = 0;
		}
		
		public void reset(){
			data = G_STAND;
			time = 0;
		}
	}
	
	public StepCounterM1(){
		reset();
	}
	
	@Override
	public void reset(){
		mSensorDataString = "";
		isRecordFirst = true;		
		isFirst = true;
		stepCount = 0;		
		
		resetData();
	}
	
	private void resetData(){
		if(curNote == null){
			curNote = new ANote();
		}
		if(preNote == null){
			preNote = new ANote();
		}
		
		thrUp = THRESHOLD_UP;
    	thrDown = THRESHOLD_DOWN;
    	thrWide = THRESHOLD_WIDE;
    	
    	reset(peak);
    	reset(valley);
    	speNumber = 0;
    	
    	//reset(queValue);
    	curNote.reset();
    	preNote.reset();
    	//queNumber = 0;
    	
    	timeout = 0;    	

		isWalkingDetected = false;
		mValidStepCount = 0;
		mTimestampRecord[0] = 0;
		mTimestampRecord[1] = 0;
		mTimestampRecord[2] = 0;
	}
	
	@Override
	public void stepCounting(long curTime, float x, float y, float z){
		
		if(isFirst){
			startTime = curTime;
			isFirst = false;
		}
		
		if( timeout >= 2000000000){
			resetData();
        	startTime = curTime;
		}
		
	    float acSquareRoot = (float) Math.sqrt(x * x + y * y + z * z);
		
		//if(++queNumber == 5)   // queNumber = (++queNumber)%5
		//	queNumber = 0;
		//queValue[queNumber] = acSquareRoot;
		
		//curNote.data = sum(queValue) / 5;
		curNote.data = acSquareRoot;
		curNote.time = curTime - startTime;
		
		timeout = curNote.time;
		
		if(curNote.data >= preNote.data){
			
			if(curNote.data >= peak[speNumber].data){

				peak[speNumber].data = valley[speNumber].data = curNote.data;
				peak[speNumber].time = valley[speNumber].time = curNote.time;
				
				preNote.data = curNote.data;
				preNote.time = curNote.time;
				
			} else if( (curNote.data < thrDown) || ( (peak[speNumber].data - valley[speNumber].data) > thrWide) ){
				
				if(valley[speNumber].time < 200000000){ //<0.2s
					
					preNote.data = curNote.data;
					preNote.time = curNote.time;
					
                }else{
                	
                	if( peak[speNumber].data > thrUp ){
                		
                		
                		if(!isWalkingDetected){
                			
                			if(mValidStepCount>=0 && mValidStepCount <3){
                        		mTimestampRecord[mValidStepCount] = curTime;
                        	}
                        	
                        	mValidStepCount++;
                        	
                        	if(mValidStepCount > 2){
                        		isWalkingDetected = true;
                        		
                        		for (StepListener stepListener : mStepListeners) {
                        			if(isDebug){
                        				Log.d(TAG, "Step detected: " + mTimestampRecord.length);
                        			}
                                    stepListener.onStep(mTimestampRecord.length, mTimestampRecord);
                                }                   		
                        		
                        		mValidStepCount = 0;
                        		mTimestampRecord[0] = 0;
                    			mTimestampRecord[1] = 0;
                    			mTimestampRecord[2] = 0;
                        	}
                			
                		}else{
                			// step count successful
                    		for (StepListener stepListener : mStepListeners) {
                    			if(isDebug){
                    				Log.d(TAG, "Step detected: 1");
                    			}
                                stepListener.onStep(1, new long[]{curTime});
                            }
                		}                		
                		
                		stepCount++;
                		
                		startTime = curTime;
                		timeout = 0;
                        //queNumber = 0;
                        
                        if(speNumber == 4){
                        	
                        	speNumber = 0;
                        	
                        	//thrUp = (sum(peak)/5 + THRESHOLD_UP)/2;
                        	//thrDown =  (sum(valley)/5 + THRESHOLD_DOWN)/2;
                        	//thrWide = ((sum(peak) - sum(valley))/5 + THRESHOLD_WIDE)/2;
                        	
                        	reset(peak);
                        	reset(valley);
                        	
                        	//reset(queValue);
                        	curNote.reset();
                        	preNote.reset();
                        	
                        } else {
                        	
                        	speNumber++;
                        	
                        	peak[speNumber].reset();
                        	valley[speNumber].reset();
                        	
                        	//reset(queValue);
                        	curNote.reset();
                        	preNote.reset();   
                        	
                        }
                	} else {
                		
                		preNote.data = curNote.data;
    					preNote.time = curNote.time;
    					
                	}
                }
			} else {
				
				preNote.data = curNote.data;
				preNote.time = curNote.time;
				
			}
		} else {
			if(curNote.data >= valley[speNumber].data){
				
				preNote.data = curNote.data;
				preNote.time = curNote.time;
				
            } else { // Less than the valley
            	
            	valley[speNumber].data = curNote.data;
            	valley[speNumber].time = curNote.time;

            	preNote.data = curNote.data;
				preNote.time = curNote.time;
				
            }
		}
		
		// For accuracy rating analyze
        if(isDebug){
        	dataRecord(curTime, x, y, z);
        }
		
	}
	
	@SuppressWarnings("unused")
	private float sum(float[] array) {
	    float retval = 0;
	    for (int i = 0; i < array.length; i++) {
	      retval += array[i];
	    }
	    return retval;
	}
	
	@SuppressWarnings("unused")
	private float sum(ANote[] array) {
	    float retval = 0;
	    for (int i = 0; i < array.length; i++) {
	      retval += array[i].data;
	    }
	    return retval;
	}
	
	@SuppressWarnings("unused")
	private void reset(float[] array){
		for (int i = 0; i < array.length; i++) {
		      array[i] = G_STAND;
		}
	}
	
	private void reset(ANote[] array){
		for (int i = 0; i < array.length; i++) {
			if(array[i] == null){
				array[i] = new ANote();
				Log.d("carrot","hint");
			}
			array[i].reset();
		}
	}
	
	// ----------------- For recording ------------------
	// The heap is growing because of the growing string
	// using this for a little data debug
    private boolean isRecordFirst = true;
    private long recordStartTime; 
	
	private void dataRecord(long curTime, float x, float y, float z){
		long actualTime = curTime;
		long relativeTime = 0;
		
		if(isRecordFirst){
			recordStartTime = actualTime;
			isRecordFirst = false;
		}
		relativeTime = actualTime - recordStartTime;
		
		// Movement
	    float acSquareRoot = (float) Math.sqrt(x * x + y * y + z * z);
	    
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
	    /*data += String.valueOf(queValue[0]);
	    data += ", ";
	    data += String.valueOf(queValue[1]);
	    data += ", ";
	    data += String.valueOf(queValue[2]);
	    data += ", ";
	    data += String.valueOf(queValue[3]);
	    data += ", ";
	    data += String.valueOf(queValue[4]);
	    data += ", ";
	    data += String.valueOf(queNumber);
	    data += ", ";*/
	    data += String.valueOf(curNote.data);
	    data += ", ";
	    data += String.valueOf(curNote.time);
	    data += ", ";
	    data += String.valueOf(speNumber);
	    data += ", ";
	    data += String.valueOf(peak[0].data);
	    data += ", ";
	    data += String.valueOf(peak[1].data);
	    data += ", ";
	    data += String.valueOf(peak[2].data);
	    data += ", ";
	    data += String.valueOf(peak[3].data);
	    data += ", ";
	    data += String.valueOf(peak[4].data);
	    data += ", ";
	    data += String.valueOf(valley[0].data);
	    data += ", ";
	    data += String.valueOf(valley[1].data);
	    data += ", ";
	    data += String.valueOf(valley[2].data);
	    data += ", ";
	    data += String.valueOf(valley[3].data);
	    data += ", ";
	    data += String.valueOf(valley[4].data);
	    data += ", ";
	    data += String.valueOf(stepCount);
	    /*data += ", ";
	    data += String.valueOf(thrUp);
	    data += ", ";
	    data += String.valueOf(thrDown);
	    data += ", ";
	    data += String.valueOf(thrWide);*/
	    data += "\n";
	    
	    mSensorDataString += data;
	}


}
