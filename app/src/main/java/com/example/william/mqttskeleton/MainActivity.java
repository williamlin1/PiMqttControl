package com.example.william.mqttskeleton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    MqttHelper mqttHelper;

    SeekBar bar;

    boolean on = false;

    String topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                if(progress <=60 && progress >= 40){
                    Log.d("progress bar", "to the stopped");
                    byte[] send = {0};
                    mqttHelper.publish(send, "Stop");
                }else if(progress <=40){
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



    }

    public void Off(View view){
        on = false;
        byte[] send = {0};
        mqttHelper.publish(send, "Left");
    }

    public void On(View view){
        on = true;
        byte[] send = {0};
        mqttHelper.publish(send, "Right");
    }


    private void startMqtt(){
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