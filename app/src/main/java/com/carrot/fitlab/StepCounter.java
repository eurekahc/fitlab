package com.carrot.fitlab;

import java.util.ArrayList;

public abstract class StepCounter {
	
	protected ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();
	
	protected boolean isDebug = false;
	protected String mSensorDataString = ""; 
	
	/**
	 * Add your StepListener to the method
	 * @param StepListener
	 */
	public void addStepListener(StepListener sl) {
        mStepListeners.add(sl);
    }
	
	/**
	 * Reset the StepCounter if you want recount
	 */
	public abstract void reset();
	
	/**
	 * Send the accelerate to StepCounter
	 * @param accelerate recording time
	 * @param value of x-axis
	 * @param value of y-axis
	 * @param value of z-axis
	 */
	public abstract void stepCounting(long curTime, float x, float y, float z);
	
	/**
	 * Switch of debug or not, if set debug, the method will 
	 * save all the intermediate values for debug, 
	 * it will very affect the use of heap, 
	 * so small test values(20~30 steps) for debug will be fine.
	 */
	public void setDebug(boolean debug){
		isDebug = debug;
	}
	
	/**
	 * Get the values string for debug, 
	 * the values are separated by colons.
	 * @return the string of intermediate values
	 */
	public String getDataString(){
		return mSensorDataString;
	}

}
