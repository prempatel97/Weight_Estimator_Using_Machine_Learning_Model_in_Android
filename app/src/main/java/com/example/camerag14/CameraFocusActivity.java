package com.example.camerag14;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import static android.widget.Toast.LENGTH_SHORT;

public class CameraFocusActivity extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener {
    private Camera camera;
    private boolean isPreviewRunning = false;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    TextView txt1,txt2;
    FrameLayout cameraFrame;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mMagnetometer;
    showCamera sc;
    private static final String TAG = "G14";
    private double AngleA = 0.0;
    private double AngleB = 0.0;
    private double X1 = 0.0;
    private double X2 = 0.0;
    private float[] mGravity;
    private float[] mMagnetic;
    private double h;
    private double D;
    private double H;
    private double L;
    private double W;
    float[] value = new float[3];
    //private int unit;//default unit in mts
    //mts-0,cms-1,ft-2,in-3
    private Spinner spinner;
    DecimalFormat d = new DecimalFormat("#.###");
    float pressure;
    float accel[] = new float[3];
    float result[] = new float[3];
    CharSequence test = "Measurements:\nDepth = " + D + " cms\nHeight = " + H + " cms\nLength = " + L + " cms\nCam height = " + h + " cms";
    CharSequence test_weight = "Estimated Weight: \n N/A";

    private PowerManager.WakeLock wl;
    private PowerManager pm;
    Toast toast;
    final String filename = "WeightDatabase.txt";
    String m = "";


    String filepath = "G_14-Storage";
    File myInternalFile;
    File directory;

    //String[] files = new String[15];
    //int count = -1;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate(Bundle mc) {
        Log.d(TAG, "onCreate");
        super.onCreate(mc);
        Log.e(getClass().getSimpleName(), "onCreate");
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.frames); //main or frames
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        final Button distance = (Button) findViewById(R.id.button1);
        final Button height = (Button) findViewById(R.id.button7);
        Button weight = (Button) findViewById(R.id.get_weight);
        final Button rst_valider = (Button) findViewById(R.id.button2);
        txt1 = (TextView) findViewById(R.id.textView1);
        txt2 = (TextView) findViewById(R.id.textView2);
        final Button adjh = (Button) findViewById(R.id.button3);
        final EditText edit = (EditText) findViewById(R.id.editText1);
        final Button length = (Button) findViewById(R.id.button4);
        //SR
        cameraFrame = (FrameLayout) findViewById(R.id.cameraFrame);


        distance.setEnabled(false);
        height.setEnabled(false);
        length.setEnabled(false);
        //weight.setEnabled(false);
        edit.setText("0.0");
        //unit = 0;
        txt1.setText(test);
        txt2.setText(test_weight);

        //surfaceCreated(surfaceHolder);
        //surfaceChanged(surfaceHolder, 0,  176, 144);

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        final File directory = contextWrapper.getDir(filepath, Context.MODE_APPEND);
        myInternalFile = new File(directory, filename);

        final char gender;
        if (mc == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                gender = 'm';
            } else {
                gender = extras.getChar("Gender");
            }
        } else {
            gender= (char) mc.getSerializable("STRING_I_NEED");
        }
		/*try
		{
		BufferedWriter buf = new BufferedWriter(new FileWriter(myInternalFile, true));
	    buf.write("Measures:\n");
	    buf.newLine();
	    buf.close();
	    }
		catch(IOException e)
		{
			e.printStackTrace();
		}*/

        //Toast.makeText(this, "Adjust the height", Toast.LENGTH_SHORT);

        try {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
            wl.acquire();
        } catch (Exception ex) {
            Log.e("exception", "here 1");
        }

        camera.open();
        sc = new showCamera(this, camera);
        cameraFrame.addView(sc);

        //addListenerOnButton();
        //addListenerOnSpinnerItemSelection();
        distance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //old method
                AngleA = getDirection();//taking x value
                AngleA = Math.toRadians(90) - AngleA;
                D = Double.valueOf(d.format(Math.abs(h * (Math.tan((AngleA))))));
                test = "Measurements:\nDepth = " + D + " cms\nHeight = \t" + H + " cms\nLength = \t" + L + " cms\nCam height = \t" + h + " cms";
                test_weight = "Estimated Weight: \n N/A";
                toast = Toast.makeText(getApplicationContext(), "Object distance calculated!", LENGTH_SHORT);
                toast.show();
                txt1.setText(test);
                txt2.setText(test_weight);
            }
        });

        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AngleB = 0;
                while (AngleB == 0) {
                    AngleB = getDirection();//taking x
                }
                H = Double.valueOf(d.format(h + Math.abs(D * Math.tan((AngleB)))));

                test = "Measurements:\nDepth = \t" + D + " cms\nHeight = \t" + H + " cms\nLength = \t" + L + " cms\nCam height = \t" + h + " cms";
                test_weight = "Estimated Weight: \n N/A";
                toast = Toast.makeText(getApplicationContext(), "Object height calculated!", LENGTH_SHORT);
                toast.show();
                txt1.setText(test);
                txt2.setText(test_weight);
            }
        });

        rst_valider.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AngleA = 0.0;
                AngleB = 0.0;
                X1 = 0.0;
                X2 = 0.0;
                D = 0.0;
                H = 0.0;
                L = 0.0;
                toast = Toast.makeText(getApplicationContext(), "Values reset!", LENGTH_SHORT);
                toast.show();
                txt1.setText("Measurements:\nDepth = " + D + " cms\nHeight = \t" + H + " cms\nLength = \t" + L + " cms\nCam height = \t" + h + " cms");
                txt2.setText("Estimated Weight: \n N/A");
            }
        });

        adjh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                h = Double.parseDouble(edit.getText().toString());
                if (h == 0) {
                    toast = Toast.makeText(getApplicationContext(), "Height cannot be 0!", LENGTH_SHORT);
                    toast.show();
                } else {
                    txt1.setText("Camera height = " + h + "\ncms");
                    distance.setEnabled(true);
                    height.setEnabled(true);
                    length.setEnabled(true);
                    toast = Toast.makeText(getApplicationContext(), "Phone height adjusted!", LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        length.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (X1 == 0.0) {
                    X1 = value[0];//taking z

                } else {
                    X2 = (value[0]);//taking z

                    float theta = (float) Math.abs(Math.abs(X1) - Math.abs(X2));
                    //arc of a circle logic;
                    L = Double.valueOf(d.format(theta * D));
                    test = "Measurements:\nDepth = \t" + D + " cms\nHeight = \t" + H + " cms\nLength = \t" + L + " cms\nCam height = \t" + h + " cms";
                    test_weight = "Estimated Weight: \n N/A";
                    toast = Toast.makeText(getApplicationContext(), "Object length calculated!", LENGTH_SHORT);
                    toast.show();
                    txt1.setText(test);
                    txt2.setText(test_weight);
                }
            }
        });
        //final String data = "[{\"score\":0.9779489426051869,\"keypoints\":[{\"score\":0.999846875667572,\"part\":\"nose\",\"position\":{\"x\":301.53484901685385,\"y\":257.65654260299624}},{\"score\":0.9999767541885376,\"part\":\"leftEye\",\"position\":{\"x\":328.8400729556804,\"y\":233.78872112983768}},{\"score\":0.999970018863678,\"part\":\"rightEye\",\"position\":{\"x\":279.44834581772784,\"y\":235.45558286516854}},{\"score\":0.9994299411773682,\"part\":\"leftEar\",\"position\":{\"x\":355.4499746410736,\"y\":252.27030664794006}},{\"score\":0.999362587928772,\"part\":\"rightEar\",\"position\":{\"x\":242.94178175717855,\"y\":256.3932047830836}},{\"score\":0.9926291704177856,\"part\":\"leftShoulder\",\"position\":{\"x\":418.8653831148564,\"y\":402.28732638888886}},{\"score\":0.9835844039916992,\"part\":\"rightShoulder\",\"position\":{\"x\":213.73585752184766,\"y\":415.55511665106116}},{\"score\":0.9972991347312927,\"part\":\"leftElbow\",\"position\":{\"x\":449.2840980024969,\"y\":586.3944483458178}},{\"score\":0.9911842346191406,\"part\":\"rightElbow\",\"position\":{\"x\":185.52234511548062,\"y\":595.6762542915105}},{\"score\":0.9951977133750916,\"part\":\"leftWrist\",\"position\":{\"x\":396.5255344881398,\"y\":702.8404435861423}},{\"score\":0.9770950078964233,\"part\":\"rightWrist\",\"position\":{\"x\":300.9757334581773,\"y\":706.9298142946317}},{\"score\":0.925123929977417,\"part\":\"leftHip\",\"position\":{\"x\":385.26665886392004,\"y\":704.0870298845193}},{\"score\":0.934648871421814,\"part\":\"rightHip\",\"position\":{\"x\":253.53137679463168,\"y\":715.0381359238452}},{\"score\":0.9560098648071289,\"part\":\"leftKnee\",\"position\":{\"x\":411.30486306179773,\"y\":963.5716097066166}},{\"score\":0.9841309189796448,\"part\":\"rightKnee\",\"position\":{\"x\":265.5047889357054,\"y\":985.5657966604244}},{\"score\":0.9764760732650757,\"part\":\"leftAnkle\",\"position\":{\"x\":469.0088561173533,\"y\":1163.5944327403245}},{\"score\":0.9131665229797363,\"part\":\"rightAnkle\",\"position\":{\"x\":253.77960166978778,\"y\":1170.9496917915105}}]}";
        final String data1 = "[{\"score\":0.9779489426051869,\"keypoints\":[{\"score\":0.999846875667572,\"part\":\"nose\",\"position\":{\"x\":301.53484901685385,\"y\":257.65654260299624}},{\"score\":0.9999767541885376,\"part\":\"leftEye\",\"position\":{\"x\":328.8400729556804,\"y\":233.78872112983768}},{\"score\":0.999970018863678,\"part\":\"rightEye\",\"position\":{\"x\":279.44834581772784,\"y\":235.45558286516854}},{\"score\":0.9994299411773682,\"part\":\"leftEar\",\"position\":{\"x\":355.4499746410736,\"y\":252.27030664794006}},{\"score\":0.999362587928772,\"part\":\"rightEar\",\"position\":{\"x\":242.94178175717855,\"y\":256.3932047830836}},{\"score\":0.9926291704177856,\"part\":\"leftShoulder\",\"position\":{\"x\":418.8653831148564,\"y\":402.28732638888886}},{\"score\":0.9835844039916992,\"part\":\"rightShoulder\",\"position\":{\"x\":213.73585752184766,\"y\":415.55511665106116}},{\"score\":0.9972991347312927,\"part\":\"leftElbow\",\"position\":{\"x\":449.2840980024969,\"y\":586.3944483458178}},{\"score\":0.9911842346191406,\"part\":\"rightElbow\",\"position\":{\"x\":185.52234511548062,\"y\":595.6762542915105}},{\"score\":0.9951977133750916,\"part\":\"leftWrist\",\"position\":{\"x\":396.5255344881398,\"y\":702.8404435861423}},{\"score\":0.9770950078964233,\"part\":\"rightWrist\",\"position\":{\"x\":300.9757334581773,\"y\":706.9298142946317}},{\"score\":0.925123929977417,\"part\":\"leftHip\",\"position\":{\"x\":385.26665886392004,\"y\":704.0870298845193}},{\"score\":0.934648871421814,\"part\":\"rightHip\",\"position\":{\"x\":253.53137679463168,\"y\":715.0381359238452}},{\"score\":0.9560098648071289,\"part\":\"leftKnee\",\"position\":{\"x\":411.30486306179773,\"y\":963.5716097066166}},{\"score\":0.9841309189796448,\"part\":\"rightKnee\",\"position\":{\"x\":265.5047889357054,\"y\":985.5657966604244}},{\"score\":0.9764760732650757,\"part\":\"leftAnkle\",\"position\":{\"x\":469.0088561173533,\"y\":1163.5944327403245}},{\"score\":0.9131665229797363,\"part\":\"rightAnkle\",\"position\":{\"x\":253.77960166978778,\"y\":1170.9496917915105}}]}]";
        final String data2 = "hey there";
        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataToServer();
            }

            private void sendDataToServer() {
                final String json = formatDataAsJSON();
                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        return getServerResponse(json);
                    }

                    protected void onPostExecute(String result) {
                        //Toast toastsr = Toast.makeText(getApplicationContext(), result.substring(11,17), LENGTH_SHORT);
                        //toastsr.show();
                        result = result.substring(11);
                        int index = result.indexOf('\"');
                        test_weight = "Estimated Weight: \n"+result.substring(0,index);
                        txt2.setText(test_weight);
                    }
                }.execute();
            }

            private String getServerResponse(String json) {
                HttpURLConnection httpURLConnection = null;
                String JsonResponse = null;
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://13.58.40.172:8079/test");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                    //urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setDoOutput(true);
                } catch (Exception e) {
                    Log.d("JWP", "URL connect problem <SR>");
                }
                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = json.getBytes("utf-8");
                    os.write(input, 0, input.length);
                } catch (Exception e) {
                    Log.d("JWP", e.toString());
                }

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                } catch (Exception e) {
                    Log.d("JWP", e.toString());
                }
                return "No response from server <SR>";
            }

            private String formatDataAsJSON() {
                final JSONObject postData = new JSONObject();
                try {
                    String tmp;
                    if(gender == 'M')
                        tmp = "0";
                    else
                        tmp = "1";
                    postData.put("Gender", tmp);
                    postData.put("height", String.valueOf(H));

                    return postData.toString(1);
                } catch (JSONException e) {
                    Log.d("JWP", "Unable to format JSON!");
                }
                return null;
            }
        });


    }


    @Override
    public void onBackPressed() {
        onCreate(null);
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        Log.d(TAG, "Menu button pressed");
        getMenuInflater().inflate(R.layout.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "selection");
        switch (item.getItemId()) {
            case R.id.menu_save:
                Log.d(TAG, "save button");
                setContentView(R.layout.save);

                final EditText save_name = (EditText) findViewById(R.id.saveText1);
                final Button saveButton = (Button) findViewById(R.id.savebutton1);

                saveButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {
                        Log.d(TAG, "save button pressed");
                        try//on press of save button
                        {
                            //Log.d(TAG,"3");
                            Log.d(TAG, "1");
                            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                            final File directory = contextWrapper.getDir(filepath, Context.MODE_APPEND);
                            myInternalFile = new File(directory, filename);
                            Log.d(TAG, "2");
                            BufferedWriter buf = new BufferedWriter(new FileWriter(myInternalFile, true));
                            buf.append(save_name.getText().toString() + " " + test + "\n");
                            buf.append(test_weight);
                            buf.newLine();
                            buf.close();
                            Log.d(TAG, "3");
                            Log.d(TAG, "written to file");
                        } catch (IOException e) {
                            try {
                                BufferedWriter buf = new BufferedWriter(new FileWriter(myInternalFile, true));
                                buf.write("\n\n" + save_name.getText().toString() + " " + test);
                            } catch (IOException e2) {
                                e.printStackTrace();
                            }
                        }
                        save_name.setText("");
                        Toast toast = Toast.makeText(getApplicationContext(), filename + " saved to Internal Storage...", LENGTH_SHORT);
                        toast.show();
                    }
                });

                return true;

            case R.id.menu_search:
                Log.d(TAG, "view button");
                setContentView(R.layout.view);
                TextView viewText = (TextView) findViewById(R.id.viewtextView1);
                String myData = "";
                //Toast.makeText(MainActivity.this, "Search is Selected", Toast.LENGTH_SHORT).show();
                try {
                    Log.d(TAG, "11");
                    ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                    final File directory = contextWrapper.getDir(filepath, Context.MODE_APPEND);
                    myInternalFile = new File(directory, filename);
                    Log.d(TAG, "12");
                    FileInputStream fis = new FileInputStream(myInternalFile);
                    DataInputStream in = new DataInputStream(fis);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    Log.d(TAG, "13");
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        myData = myData + "\n" + strLine;
                    }
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                viewText.setText(myData);
                Toast toast1 = Toast.makeText(this, "Data retrieved from Internal Storage...", LENGTH_SHORT);
                toast1.show();
                return true;

            case R.id.menu_delete:
                setContentView(R.layout.view);
                viewText = (TextView) findViewById(R.id.viewtextView1);
                try {
                    PrintWriter writer = new PrintWriter(myInternalFile);
                    writer.print("Measurements:\n");
                    writer.close();
                    Toast toast2 = Toast.makeText(this, "Data deleted.", LENGTH_SHORT);
                    toast2.show();
                    viewText.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        try {
            Log.v("On resume called", "------ wl aquire next!");
            wl.acquire();
        } catch (Exception ex) {
        }
        Log.e(getClass().getSimpleName(), "onResume");
        super.onResume();
        //
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
        //
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        try {
            Log.v("on pause called", "on pause called");
            wl.release();
        } catch (Exception ex) {
            Log.e("Exception in on menu", "exception on menu");
        }
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @SuppressLint("LongLogTag")
    @Override
    protected void onUserLeaveHint() {
        try {
            Log.v("on user leave hint pressed", "on userlevve hint pressesd");
            wl.release();
        } catch (Exception ex) {
            Log.e("Exception in on menu", "exception on menu");
        }
        super.onUserLeaveHint();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(getClass().getSimpleName(), "surfaceCreated");
        camera = Camera.open();
        //camera.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
        //Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO = camera.setFocusMode();
        Camera.Parameters parameters = camera.getParameters();

        //SR
        //if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
        parameters.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        parameters.setRotation(90);
        //}
        parameters.setPreviewFrameRate(20);
        parameters.setPreviewSize(176, 144);
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        float[] distances = new float[3];
        parameters.getFocusDistances(distances);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e(getClass().getSimpleName(), "surfaceChanged");
        if (isPreviewRunning) {
            camera.stopPreview();
        }
        Camera.Parameters p = camera.getParameters();
        p.setPreviewSize(w, h);
        camera.setParameters(p);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.startPreview();
        isPreviewRunning = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(getClass().getSimpleName(), "surfaceDestroyed");
        camera.stopPreview();
        isPreviewRunning = false;
        camera.release();
    }

    //
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values.clone();
                //onAccelerometerChanged(values[0],values[1],values[2]);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetic = event.values.clone();
                break;
            case Sensor.TYPE_PRESSURE:
                pressure = event.values[0];
                pressure = pressure * 100;
            default:
                return;
        }
        if (mGravity != null && mMagnetic != null) {
            getDirection();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // TODO Auto-generated method stub

    }

    private float getDirection()
    {
        float[] temp = new float[9];
        float[] R = new float[9];

        //Load rotation matrix into R
        SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);

        //Remap to camera's point-of-view
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);

        //Return the orientation values

        SensorManager.getOrientation(R, value);

        //value[0] - Z, value[1]-X, value[2]-Y in radians

        return value[1];       //return x
    }
}
