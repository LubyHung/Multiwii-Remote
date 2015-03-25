package com.mobilejohnny.multiwiiremote.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/*
 Default baud rate: 115200
 Default Pair code: 1234
 */


public class Bluetooth {

    /**
	 * 
	 */
    private Activity activity;
private String device_name=null;
  private static final String TAG="tBlue";
  private BluetoothAdapter localAdapter=null;
  private BluetoothDevice remoteDevice=null;
  public BluetoothSocket socket=null;
  public OutputStream outStream = null;
  public InputStream inStream=null;
 // private boolean failed=false;

    public Bluetooth()
    {
        this(null);
    }

  public Bluetooth(Activity activity)
  {
    this.activity = activity;
	localAdapter = BluetoothAdapter.getDefaultAdapter(); 
    if (!localAdapter.isEnabled() && activity !=null)
    {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      this.activity.startActivityForResult(enableBtIntent,0);
    }
//    else {
//      connect();
//    }
  }

  public Bluetooth(Activity activity, String device_name)
  {
      this.activity = activity;
	this.device_name=device_name;
    localAdapter = BluetoothAdapter.getDefaultAdapter();
    if ((localAdapter!=null) && localAdapter.isEnabled()) {
      Log.i(TAG, "Bluetooth adapter found and enabled on phone. ");
    } 
    else {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.activity.startActivityForResult(enableBtIntent,0);
      Log.e(TAG, "Bluetooth adapter NOT FOUND or NOT ENABLED!");
      return;
    }

      findDevice();
  }

    public Set<BluetoothDevice> getBondedDevices()
    {

        return localAdapter.getBondedDevices();
    }

    public void findDevice()
    {
        for (BluetoothDevice dp : localAdapter.getBondedDevices()) {
            if (dp.getName().equals(device_name))
                remoteDevice = dp;
        }

        Log.i(TAG, "Creating RFCOMM socket...");
        try {
            Method m = remoteDevice.getClass().getMethod
                    ("createRfcommSocket", new Class[] { int.class});
            socket = (BluetoothSocket) m.invoke(remoteDevice, 1);
//        socket = remoteDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Log.i(TAG, "RFCOMM socket created.");
        }
        catch (NoSuchMethodException e) {
            Log.i(TAG, "Could not invoke createRfcommSocket.");
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            Log.i(TAG, "Bad argument with createRfcommSocket.");
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            Log.i(TAG, "Illegal access with createRfcommSocket.");
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            Log.i(TAG, "Invocation target exception: createRfcommSocket.");
            e.printStackTrace();
        }

        Log.i(TAG, "Got socket for device "+socket.getRemoteDevice());
        localAdapter.cancelDiscovery();
    }

  public void connect()
  {

    Log.i(TAG, "Connecting socket...");
    try {
      socket.connect(); 
      Log.i(TAG, "Socket connected.");
    }
    catch (IOException e) {
      try {
        Log.e(TAG, "Failed to connect socket. ", e);
        socket.close();
        Log.e(TAG, "Socket closed because of an error. ", e);
      } 
      catch (IOException eb) {
        Log.e(TAG, "Also failed to close socket. ", eb);
      }
      return;
    }

    try {
      outStream = socket.getOutputStream(); 
      Log.i(TAG, "Output stream open.");
      inStream = socket.getInputStream();
      Log.i(TAG, "Input stream open.");
    } 
    catch (IOException e) {
      Log.e(TAG, "Failed to create output stream.", e);  
    }
    return;
  }

  public void write(String s) 
  {
    //No tags here for performance
    //Log.i(TAG, "Sending \""+s+"\"... "); 
    byte[] outBuffer= s.getBytes(); 
    write(outBuffer);
  }

  public void write(byte[] outBuffer) 
  {
    //No tags here for performance
    //Log.i(TAG, "Sending "); 
    try {
      outStream.write(outBuffer);
    } 
    catch (IOException e) {
      Log.e(TAG, "Write failed.", e);
    }
  }

  public boolean streaming() 
  {
    return ( (socket!=null) && (inStream!=null) && (outStream!=null) );
  }
  
  public boolean connected() 
  {
    return (socket!=null);
  }


  /*public String readString() 
  {
    if (!streaming()) return ""; 
    String inStr="";
    try {
      if (0<inStream.available()) {
        byte[] inBuffer = new byte[1024];
        int bytesRead = inStream.read(inBuffer);
        inStr = new String(inBuffer, "ASCII");
        inStr=inStr.substring(0, bytesRead); 
        Log.i(TAG, "byteCount: "+bytesRead+ ", inStr: "+inStr);
      }
    } 
    catch (IOException e) {
      Log.e(TAG, "Read failed", e);
    }
    return inStr;
  }*/


private byte[] inBuffer = new byte[32];//try?error
private int bytesRead = 0;
  public void read() 
  {    
    try {
      if (inStream.available()>0) {
        bytesRead = inStream.read(inBuffer);  
//        this.floneRemote.parseMSPMessage(inBuffer,bytesRead);
      }
    } 
    catch (Exception e) {
      Log.e(TAG, "Read failed", e);
      inBuffer = null;
    }
  }

//Falla
  public void close()
  {
    Log.i(TAG, "Bluetooth closing... ");
    try {
        if(outStream!=null)
      outStream.close();
        if(inStream!=null)
      inStream.close();
        if(socket!=null)
      socket.close();
      Log.i(TAG, "BT closed");
    } 
    catch (Exception e2) {
      Log.e(TAG, "Failed to close socket. ", e2);
    }
  }
}