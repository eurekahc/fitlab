package com.carrot.fitlab;

public interface StepListener {
	/**
	 * If valid steps detected, count of steps and the times of the steps 
	 * will be sent back
	 * @param count of valid steps
	 * @param the time stamps of these valid steps
	 */
    public void onStep(int stepcount, long[] timestamps);
}
