package carrot.test.example;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.carrot.fitlab.StepCounterM2;
import com.carrot.fitlab.StepCounterM1;
import com.carrot.fitlab.StepListener;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener, StepListener{
	
	private boolean isDebug = false;
	
	private StepCounterM1 mCounter;
	
	private SensorManager mSensorManager;
	
	private int stepCounter;
	
	private Button startBtn;
	private Button stopBtn;
	private Button finishBtn;
	private TextView resultText;
	private TextView stepsText;
	
	private boolean isWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);     
        mSensorManager.registerListener(this,
        		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        
        mCounter = new StepCounterM1();
        mCounter.addStepListener(this);
        mCounter.setDebug(isDebug);
        
        viewIntial();
    }   
    

	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this);
		super.onDestroy();
	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isWorking){
			synchronized (this) {				
				// work
				float[] values = event.values;
				mCounter.stepCounting(event.timestamp, values[0], values[1], values[2]);			
			}
		}
		
	}
	
	private void viewIntial(){
		resultText = (TextView) findViewById(R.id.result);
        stepsText = (TextView) findViewById(R.id.stepcounter);
        
        startBtn = (Button)findViewById(R.id.button_start);
        stopBtn = (Button)findViewById(R.id.button_stop);
        finishBtn = (Button)findViewById(R.id.button_finish);
        stopBtn.setEnabled(false);
        finishBtn.setEnabled(false);
        
        startBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				startWork();
				stopBtn.setEnabled(true);
				startBtn.setEnabled(false);
				finishBtn.setEnabled(false);				
			}
        
        });
        
        
        stopBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				stopWork();
				finishBtn.setEnabled(true);
				stopBtn.setEnabled(false);
				startBtn.setEnabled(false);
			}
        
        });
        
        finishBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				
				startBtn.setEnabled(true);
				stopBtn.setEnabled(false);
				finishBtn.setEnabled(false);
				
				if(!isDebug){
					resultText.setText("Thanks for your time!"
							+"\nMyCount = "+String.valueOf(stepCounter));
					return;
				}
				
				String fileName = genNewFileName();
				try {
					saveToSDCard(fileName,genFileContent());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				resultText.setText("Thanks for your time!"
						+"\nMyCount = "+String.valueOf(stepCounter)
						+"\nFile saved: "+fileName);
			}
        
        });
	}
	
	public void startWork(){
		stepCounter = 0;
		mCounter.reset();
		isWorking = true;
		updateStepView();
		resultText.setText("Thanks for your time!");
	}
	
	public void stopWork(){
		isWorking = false;
	}

	@Override
	public void onStep(int stepcount, long[] timestamps) {
		// TODO Auto-generated method stub
		stepCounter+=stepcount;
		updateStepView();
	}
	
	private void updateStepView(){
		stepsText.setText(String.valueOf(stepCounter));
	}
	
	private String genFileContent(){
    	String data = mCounter.getDataString();
    	EditText userInputSteps = (EditText) findViewById(R.id.stepsInput);
    	String inputSteps = userInputSteps.getText().toString();
    	return data+"\nMyCount="+String.valueOf(stepCounter)+"\nUserCount="+inputSteps;
    }
	
	public void saveToSDCard(String filename, String content) throws Exception {
		//Log.d(TAG,content);
		
        File sdcardDir = Environment.getExternalStorageDirectory();
        File path = new File(sdcardDir.getPath()+"/fitlab");
        if (!path.exists()) {
    	   path.mkdirs();
    	}
		
		File file = new File(path, filename);
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(content.getBytes());
		outStream.close();
	}
	
	private String genNewFileName(){
		EditText idInput = (EditText) findViewById(R.id.idValue);
		String idStr = idInput.getText().toString();
		if(idStr.equals("")){
			idStr = "xx";
		}
		
		String fileName = "New_Pedometer_";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");       
		Date curDate = new Date(System.currentTimeMillis());
		fileName += idStr;
		fileName += "_";
		fileName += formatter.format(curDate); 
		fileName += ".csv";
		return fileName;
	}
}
