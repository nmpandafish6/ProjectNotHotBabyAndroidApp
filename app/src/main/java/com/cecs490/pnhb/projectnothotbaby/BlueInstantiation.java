package com.cecs490.pnhb.projectnothotbaby;

import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nicolas on 3/11/2018.
 */

public class BlueInstantiation {

    public static void init(){
        final String bluetoothMAC = ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"Dummy Addr");
        if(bluetoothMAC.equals("Dummy Addr")){
            DialogFragment dialog = new BluetoothDeviceDialog();
            dialog.show(ResourceMaster.m_activity.getFragmentManager(), "tag_bluetoothdevice");
        }
        BluetoothHelper.initBluetooth();
        // CPU MAC "A0:88:69:30:CB:1C"

        Thread waitForReadyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"Dummy Addr").equals("Dummy Addr")) {
                }
                final BluetoothDevice blueDevice = BluetoothHelper.mblue.getRemoteDevice("20:16:12:12:80:68");
                final BluetoothHelper.BluetoothSocketBehavior blueBehavior  = new BluetoothHelper.BluetoothSocketBehavior() {
                    String tempInputData = "";
                    @Override
                    public void write() {}

                    @Override
                    public void read() {
                        try {
                            String tmp = ResourceMaster.myDevice.read();
                            if(tmp == null) return;
                            tempInputData += tmp;
                            String jsonString = "";
                            if(tempInputData.contains("}") && tempInputData.contains("{")){
                                jsonString = tempInputData.substring(tempInputData.lastIndexOf("{"),
                                        tempInputData.lastIndexOf("}") + 1);
                            }else return;
                            Log.e("BLUETOOTH_TAG","JSON STRING : " + jsonString);
                            JSONObject json = new JSONObject(jsonString);
                            try {
                                ResourceMaster.lastTemperature = json.getDouble("TEMP");
                            }catch(Exception e){}
                            try {
                                ResourceMaster.lastHumidity = json.getDouble("HUMIDITY");
                            }catch(Exception e){}
                            try {
                                ResourceMaster.lastOccupied = json.getBoolean("OCCUPIED");
                            }catch(Exception e){}
                            try {
                                ResourceMaster.lastSeverity = json.getDouble("SEVERITY");
                            }catch(Exception e){}
                            try {
                                ResourceMaster.lastDew = json.getDouble("DEW");
                            }catch(Exception e){}

                            if(ResourceMaster.lastTemperature > -1000){
                                ResourceMaster.conditions_currentTemperature.setText(ResourceMaster.lastTemperature + " °F");
                                ResourceMaster.home_currentTemperature.setText(ResourceMaster.lastTemperature + " °F");
                                DataWriter.writeTemperature(ResourceMaster.lastTemperature);
                            }
                            if(ResourceMaster.lastHumidity > -1000){
                                ResourceMaster.conditions_currentHumidity.setText(ResourceMaster.lastHumidity + "  %");
                                ResourceMaster.home_currentHumidity.setText(ResourceMaster.lastHumidity + "  %");
                            }
                            if(ResourceMaster.lastDew > -1000){
                                DataWriter.writeDewPoint(ResourceMaster.lastDew);
                            }
                            Log.e("BLUETOOTH_TAG","UPDATING SCREEN");
                            ResourceMaster.conditions_isOccupied.setText("" + ResourceMaster.lastOccupied);
                            ResourceMaster.home_isOccupied.setText("" + ResourceMaster.lastOccupied);
                            int scalePos = ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY,1);
                            double scale = Constants.SCALE_CONSTANTS[scalePos];
                            long notificationTime = (long) (Math.max((ResourceMaster.lastSeverity * -10000/37 + 11110000/37)/scale,1));
                            Log.e("NOTIFY_TAG", "" + notificationTime);
                            Log.e("NOTIFY_TAG", "" + ResourceMaster.lastSeverity);
                            if(ResourceMaster.lastSeverity > 0)
                                new NotificationDelayThread(notificationTime);
                            tempInputData = "";
                        } catch (IOException e) {
                            Log.e("BLUETOOTH_TAG","IO EXCEPTION : " + e.getMessage());
                            e.printStackTrace();
                        } catch (JSONException e) {
                            Log.e("BLUETOOTH_TAG","JSONException : " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void connected() {
                        Toast.makeText(ResourceMaster.m_context.getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("SENSITIVITY", Constants.SCALE_CONSTANTS[ResourceMaster.preferences.getInt(Constants.SENSITIVITY_KEY, 0)]);
                            json.put("ADAPTIVE HUMIDITY", ResourceMaster.preferences.getBoolean(Constants.ADAPTIVE_HUMIDITY_STATE_KEY, false));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            ResourceMaster.myDevice.write(json.toString());
                            Toast.makeText(ResourceMaster.m_context.getApplicationContext(), "Wrote: " + json.toString(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("BLUETOOTH_TAG", "TRIED TO WRITE : " + json.toString());
                            Log.e("BLUETOOTH_TAG", e.getMessage());

                        }

                    }

                    @Override
                    public void connecting() {
                        Toast.makeText(ResourceMaster.m_context.getApplicationContext(),
                                "Connecting:" + ResourceMaster.preferences.getString(Constants.BLUETOOTH_MAC_KEY,"20:16:12:12:80:68"),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void none() {
                        Log.e("BLUETOOTH_TAG", "INSIDE NONE");
                    }
                };
                ResourceMaster.m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ResourceMaster.myDevice == null || !ResourceMaster.myDevice.getIsConnected()) {
                                Log.e("BLUETOOTH_TAG", "Connecting to " + bluetoothMAC);
                                ResourceMaster.myDevice = new BluetoothHelper(blueDevice, blueBehavior);
                            }else{
                                Log.e("BLUETOOTH_TAG", "Already Connected to :" + bluetoothMAC);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        waitForReadyThread.start();

    }
}
