package superior.com.superior;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class Data extends Service {
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothSocket mySocket;
    BluetoothDevice myDevice = null;
    TextView textView;
    byte[] buffer = new byte[512];
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    int bytes;
    public Data() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //enable bluetooth
        if(bluetoothAdapter==null || !bluetoothAdapter.isEnabled()){

            Intent enaleBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enaleBluetooth,1);
            bluetoothAdapter.getDefaultAdapter().enable();
        }

        Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        //select your device from a list of paired devices
        for (BluetoothDevice device : pairedDevices) {
            String dAddres = device.getAddress();
            Log.d("devices",device.getName());
            if (dAddres.contains("A0:E6:F8:21:A7:92")) {
                myDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
                break;
            }
        }
        GetData();
        return super.onStartCommand(intent, flags, startId);
    }

    public void GetData(){
        String[] data = {} ;
        int index =0;
        try {
            bluetoothAdapter.cancelDiscovery();
            mySocket = myDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            try {
                mySocket.connect();
                Log.i("connected", "bluetooth socket connected");
                try {
                    InputStream mmInputStream = mySocket.getInputStream();
                    DataInputStream mmInStream = new DataInputStream(mmInputStream );
                    //String data;
                    int i=0;

                    /////////////////////////////////
                    int[] sample = {};
                    ////////////////////////////////
                    while(true){
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                        //data[index] = readMessage;
                        i++;
                        ////////////////////////////////////
//                        if(sample[index]<sample[index-1]){
//                            break;
//                        }
                        //////////////////////////////////

                        Log.i("massage", String.valueOf(readMessage.length()));
                        Log.i("massage1", readMessage);
                        makeFile(readMessage);
                        //Log.i("input", "getInputStream succeeded");
                    }

                    //int finalscale = sample[sample.length-1];

                } catch (IOException ex) {
                    Log.i("inputfailed", "getting InputStream failed", ex);
                }
            } catch (IOException ex) {
                Log.i("Excemption", "socket connection  failed", ex);
            }
        } catch (IOException ex) {
            Log.i("Excemption", "creating socket failed", ex);
        }

    }


    //create a file to log this data
    public void makeFile(String weight){
        try
        {
            //File root = new File(Environment.getExternalStorageDirectory()+File.separator+"Music_Folder", "Report Files");
            File root = new File(Environment.getExternalStorageDirectory(), "weight");
            if (!root.exists())
            {
                root.mkdirs();
            }
            File gpxfile = new File(root, "scale.txt");


            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(weight+"\n\n");
            writer.flush();
            writer.close();
            //Toast.makeText(this, "Data has been written to Report File", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();

        }
    }
}
