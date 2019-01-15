package com.example.william.mqttskeleton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    MqttHelper mqttHelper;

    SeekBar bar;

    boolean on = false;
    //The default progress
    int prog = 50;
    //The SoundMeter, which is a class in the package
    SoundMeter meter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        meter = new SoundMeter();

        setContentView(R.layout.activity_main);

        bar = findViewById(R.id.seekBar);
        bar.setMax(100);
        bar.setProgress(50);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                prog = progress;
                if(progress <=60 && progress >= 40){
                    //Esablishes eadzone from (40-60)
                    Log.d("progress bar", "to the stopped");
                    byte[] send = {0};
                    mqttHelper.publish(send, "Stop");
                }else if(progress <=40){
                    //Checks if progress bar is less than 40, (deadzone from 40-60)
                    Log.d("progress bar", "to the to the left");
                    int count = 40-progress;
                    byte[] send = new byte[5];
                    for(int i =0; i < 5; i++){
                        if(count-8>0){
                            send[i] = 8;
                            count = count-8;
                        }else{
                            send[i] = (byte)count;
                            count = 0;
                        }
                    }
                    Log.d("print toSend", Arrays.toString(send));
                    mqttHelper.publish(send, "Left");
                }else if(progress >=60){
                    //Checks if progress bar is greater than 60, (deadzone from 40-60)
                    Log.d("progress bar", "to the right");
                    int count = progress-60;
                    byte[] send = new byte[5];
                    for(int i =0; i < 5; i++){
                        if(count-8>0){
                            send[i] = 8;
                            count = count-8;
                        }else{
                            send[i] = (byte)count;
                            count = 0;
                        }
                    }

                    Log.d("print toSend", Arrays.toString(send));

                    mqttHelper.publish(send, "Right");
                }
                seekBar.setMax(100);
            }
        });
        startMqtt();
        meter.start();


        //Starts a separate thread listening for loud noises constantly
        new Thread(new Runnable () {
            public void run(){
                while(true){
                    double x = meter.getAmplitude();
                    Log.d("AmplitudeValue", String.valueOf(x));
                    if(x > 31000) {
                        byte[] send = {0, 0, 0, 0, 0};
                        mqttHelper.publish(send, "Clap");
                    }
                    try {
                        Thread.sleep(75);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onResume(){
        super.onResume();
        meter.start();
        //Restarts recording the sound
    }

    @Override
    public void onPause(){
        super.onPause();
        meter.stop();
        //Stops recording the sound
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        meter.release();
    }
    public void Off(View view){
        on = false;
        byte[] send = {0,0,0,0,0};
        mqttHelper.publish(send, "Off");
        bar.setProgress(50);
        //Sends a message to turn the thing off
    }

    public void On(View view){
        on = true;
        byte[] send = {0,0,0,0,0};
        mqttHelper.publish(send, "On");
        //TODO: Check if this triggers the "On Progress Changed"
        //If it does, no problem. If it doesn't, change back to like 0 and then prog
        int currentProg = prog;
        //Quick reset for on/off, sets it to off then starts
        bar.setProgress(50);
        bar.setProgress(currentProg);
    }


    //WARNING: This will exit the python code running on the Pi, so will need to reboot pi to restart.
    public void Exit(View view){
        byte[] send = {0,0,0,0,0};
        mqttHelper.publish(send, "Exit");
    }

    private void startMqtt(){
        //Starts MQTT, will run on onCreate anyways
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}