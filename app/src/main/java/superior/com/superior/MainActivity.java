package superior.com.superior;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;

import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import superior.com.superior.database.DatabaseHandler;
import superior.com.superior.database.Logins;
import superior.com.superior.database.Routes;
import superior.com.superior.utils.Utils;

public class MainActivity extends BaseActivity {

    public boolean hasInternet;

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothSocket mySocket = null;
    BluetoothDevice myDevice = null;

    TextView name;
    EditText confirmname;
    TextView supp_id;
    Button btnweight,confirm,save,weigh,report,print;
    byte[] buffer = new byte[1024];
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    int bytes;
    ArrayList<String> myroutes = new ArrayList<>();

    Spinner spinner,shift;
    RequestQueue mQueue;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design);


        shift = (Spinner) findViewById(R.id.shift) ;
        name = (TextView) findViewById(R.id.name);
        //confirm = (Button) findViewById(R.id.confirm);
        confirmname = (EditText) findViewById(R.id.confirm);

        supp_id = (TextView) findViewById(R.id.confirm_name);

        save = (Button) findViewById(R.id.save);
        weigh = (Button) findViewById(R.id.weigh);
        report = (Button) findViewById(R.id.sendreport);
        print = (Button) findViewById(R.id.print) ;

        //make a directory to store app files
        File root = new File(Environment.getExternalStorageDirectory(), "weight");
        if (!root.exists())
        {
            root.mkdirs();
        }

        //set name of the grader
        SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);
        String g_name = details.getString("loc_code","Name");
        name.setText(g_name);


        //set evening or morning shift
        GregorianCalendar time = new GregorianCalendar();
        String[] myshifts = {};
        int hour = time.get(Calendar.HOUR_OF_DAY);

        if(hour>=12){

            myshifts = new String[]{"EVENING","MORNING"};
        }else{
            myshifts = new String[]{"MORNING","EVENING"};
        }

        ArrayAdapter<String> spinnerAdaptershift = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,
                myshifts);
       spinnerAdaptershift.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       spinnerAdaptershift.setDropDownViewResource(R.layout.spinner_item);
        shift.setAdapter(spinnerAdaptershift);

        shift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String user_shift = parent.getItemAtPosition(position).toString();
                //STORE selected shift default is selected of no action
                SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("shift",user_shift);
                editor.commit();

                //Toast.makeText(MainActivity.this,route,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnweight = (Button) findViewById(R.id.button) ;
        //textView.setText("Hello Kenya");
        spinner = (Spinner) findViewById(R.id.spinner2);
        btnweight.setText("weight");

        mQueue = Volley.newRequestQueue(this);
        // filter for bluetooth pairing
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        //get bluetooth adapter
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //enable bluetooth if not enabled
        if(bluetoothAdapter==null || !bluetoothAdapter.isEnabled()){

            Intent enaleBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enaleBluetooth,1);

        }



        bluetoothAdapter.startDiscovery();

        //establshing a bluetooth connection
        //Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

//        BluetoothDevice mbDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(details.get(strNames[item].toString()));
//        Log.d("deviceclicked",mbDevice.getAddress().toString());
//        try {
//            mySocket = mbDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//            mySocket.connect();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }




//        for (BluetoothDevice device : pairedDevices) {
//            String dAddres = device.getAddress();
//            Log.d("devices",dAddres + device.getName());
//            if (dAddres.contains("A0:E6:F8:21:A7:92")) { //B0:B4:48:AB:55:93 A0:E6:F8:21:A7:92
//                //gem line code
//                myDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
//                break;
//            }
//        }
        registerReceiver(mReceiver, filter);

        getRoutes();

        weigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if bluetooth permision is granted
                if (!bluetoothAdapter.isEnabled()){

                    Toast.makeText(MainActivity.this,"Bluetoth permission denied",Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    try {

                        GetData();

                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
//        try {
//            mySocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        confirmname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmName();
//                if(supp_id.getText().toString().equals("") && confirmname.getText().toString().equals("")){
//
//                    supp_id.setError("Enter farmer number");
//
//                }else{
                    //confirmName();
                    //Toast.makeText(MainActivity.this,"Data saved successfuly",Toast.LENGTH_LONG).show();
                }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String route = parent.getItemAtPosition(position).toString();

                //STORE selected route default is selected of no action
                SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("route_id",route);
                editor.commit();

                Toast.makeText(MainActivity.this,route,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(supp_id.getText().toString().equals("") ){
                    supp_id.setError("Enter farmer number");
                    Toast.makeText(MainActivity.this,"Enter farmer number",Toast.LENGTH_LONG).show();
                }else if(spinner.getSelectedItem().toString().equals("-")){

                    Toast.makeText(MainActivity.this, "Select a valid route", Toast.LENGTH_SHORT).show();

                }else if(btnweight.getText().toString().equals("weight")){

                    Toast.makeText(MainActivity.this,"Make sure to record a valid weight",Toast.LENGTH_LONG).show();
                    //Toast.makeText(MainActivity.this, spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

                }else{
                    try {
                        String value = btnweight.getText().toString();
                        String hghg = value.replace("ST,GS,","").replace("B    ","")
                                .replace("�","").replace(" B ","")
                                .replace("ST,","").replace("NT,","").replace("lb","")
                                .replace("T,GS,","").replace("US,GS,","")
                                .replace("ST,NT,","");
                        float weight = Float.parseFloat(hghg);

                        if(weight<=0){
                            Toast.makeText(MainActivity.this,"Make sure to record a valid weight",Toast.LENGTH_LONG).show();
                        }else {
                            //saveData();
                           // supp_id.setText("");
                           // btnweight.setText("weight");
                        }
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"Make sure to record a valid weight",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        //print or send reports
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMassage();

            }
        });

        //print
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(supp_id.getText().toString().equals("")){
                    supp_id.setError("Enter farmer number");
                    Toast.makeText(MainActivity.this,"Enter farmer number",Toast.LENGTH_LONG).show();
                }else if(btnweight.getText().toString().equals("WEIGHT")){
                    Toast.makeText(MainActivity.this,"Make sure to record a valid weight",Toast.LENGTH_LONG).show();

                }else{
                    genPDFReport();

                    PrintManager printManager = (PrintManager) MainActivity.this.getSystemService(Context.PRINT_SERVICE);
                    try
                    {
                        File root = new File(Environment.getExternalStorageDirectory(), "weight");
                        File file = new File(root,"report.pdf");

                        PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(MainActivity.this,file.toString() );
                        printManager.print("printing report", printAdapter,new PrintAttributes.Builder().build());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

//                    Intent print = new Intent(MainActivity.this,Print.class);
//                    startActivity(print);
                }
            }
        });

       // new Internet().execute();

//        if(hasInternet){
//            Log.d("conn_ava","connection available");
//        }else {
//            Log.d("conn_ava","connection not available");
//        }

        DatabaseHandler db = new DatabaseHandler(this);
        //db.addContact();
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        ArrayList<String> names = new ArrayList<>();
        HashMap<String,String>  details = new HashMap<>();
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                names.add(device.getName());
                String deviceHardwareAddress = device.getAddress();
                Log.i("devices",deviceHardwareAddress);
                Log.i("names",device.getName());
                details.put(device.getName(),device.getAddress());

                //Toast.makeText(MainActivity.this,deviceHardwareAddress,Toast.LENGTH_LONG).show();
            }

            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setTitle("Select your device");

            //array convertion
            Object[] objNames = names.toArray();
            final String[] strNames = Arrays.copyOf(objNames, objNames.length, String[].class);
            dialogBuilder.setItems(strNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    //String selectedText = names[item].toString();  //Selected item in listview
                    bluetoothAdapter.cancelDiscovery();


                    myDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(details.get(strNames[item].toString()));
                    Log.d("deviceclicked",myDevice.getAddress().toString());
                    myDevice.createBond();
                    //myDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mbDevice.getAddress());
//                        mySocket = mbDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//                        mySocket.connect();

                    unregisterReceiver(mReceiver);

                    dialog.dismiss();
                    //dialog.cancel();

                    //Toast.makeText(MainActivity.this,details.get(strNames[item].toString()),Toast.LENGTH_LONG).show();

                }
            });

            if(mySocket == null){
                AlertDialog alertDialogObject = dialogBuilder.create();
                //Show the dialog
                alertDialogObject.show();
            }

            //Create alert dialog object via builder


//            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
//                    android.R.layout.simple_list_item_1, names);
//            lv.setAdapter(adapter);
//            adapter.notifyDataSetChanged();
        }
    };



    public void GetData(){
        //String[] data = {} ;
        int index =0;
        bluetoothAdapter.cancelDiscovery();
        //mySocket = myDevice.createInsecureRfcommSocketToServiceRecord(uuid);

        //connect with the bluetooth device
        try {
            mySocket = myDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mySocket.connect();
            Log.i("connected", "bluetooth socket connected");
            try {
                InputStream mmInputStream = mySocket.getInputStream();
                DataInputStream mmInStream = new DataInputStream(mmInputStream );
                int available_bytes = mmInputStream.available();
                Log.i("available", String.valueOf(available_bytes));
                //ObjectInputStream mmInStream = new ObjectInputStream(mmInputStream);

                PushbackInputStream pushbackInputStream = new PushbackInputStream(mmInputStream);
                int b;
                b = pushbackInputStream.read();
                if ( b == -1 ) {
                    Log.d("push","no data to read");
                }else {
                    Log.d("push","data is to read");
                    //pushbackInputStream.unread(new byte[1024]);
                }


                // Log.i("availablee", String.valueOf(objectInputStream.available()));

                Log.i("available", String.valueOf(available_bytes));
                //String data;
                int i=0;

               // byte[] buffer = new byte[available_bytes];

                bytes = pushbackInputStream.read(buffer);

                String readMessage = new String(buffer, 0, bytes);

//                StringBuilder stringBuilder = new StringBuilder();
//                String string = new StringBuilder(readMessage).reverse().toString();

//                for (int j = string.length() - 1; j >= 0; j--) {
//                    char c = string.charAt(i);
//                    int datai = (int)c;
//                    datai = datai - j;
//                    datai = (datai)/2;
//                    stringBuilder.append((char)datai);
//                }
//
//                Log.i("print3",stringBuilder.toString() + "null");
                //String readMassage2 = HexUtil.byteToBit(buffer)
                Log.i("print2",readMessage + "null");
                readMessage.replaceAll("[^A-Z]","");
                while(true){
                    if(index==10){

                        String filtered = readMessage.replace("ST,GS,","").replace("B    ","")
                                .replace("�","").replace(" B ","")
                                .replace("ST,","").replace("NT,","").replace("lb","")
                                .replace("T,GS,","").replace("US,GS,","")
                                .replace("ST,NT,","");

                        btnweight.setText(filtered);
                        //storeReadings(readMessage);
                        mmInStream.close();
                        mmInputStream.close();
                       // mySocket.close();

//                        //STORE supplier quantity supplied
                        SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        //editor.putString("reading",readMessage.replace("ST,GS,","").replace("lb","").replace("T,GS,","").replace("US,GS,","").replace("ST,NT,",""));
                        editor.putString("reading",filtered);
                        editor.commit();

                        Log.i("print",readMessage);
                        break;
                    }
                    index++;

                    Log.i("massage", String.valueOf(readMessage.length()));
                    Log.i("massage1", readMessage);
                    //makeFile(readMessage);
                }

            } catch (IOException ex) {
                Log.i("inputfailed", "getting InputStream failed", ex);
            }
        } catch (IOException ex) {
            Log.i("Excemption", "socket connection  failed", ex);
        }
    }
//    //create a file to log this data
//    public void makeFile(String weight){
//        try
//        {
//            File root = new File(Environment.getExternalStorageDirectory(), "weight");
//            if (!root.exists())
//            {
//                root.mkdirs();
//            }
//            File file = new File(root, "scale.txt");
//
//
//            FileWriter writer = new FileWriter(file,true);
//            writer.append(weight+"\n\n");
//            writer.flush();
//            writer.close();
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//
//        }
//    }

//    //log the reading for spesific day and time int a file reading.txt
//    public void storeReadings(String weight){
//
//        try
//        {
//            File root = new File(Environment.getExternalStorageDirectory(), "weight");
//            if (!root.exists())
//            {
//                root.mkdirs();
//            }
//            Date d = new Date();
//            DateFormat dateFormater = new SimpleDateFormat("dd-MM-yy");
//            String exact_time = dateFormater.format(d);
//            File file = new File(root, exact_time + "readings.txt");
//
//
//            FileWriter writer = new FileWriter(file,true);
//            writer.append(exact_time + "\t" + weight.replace("ST,GS,","").replace("lb","")  +"\n");
//            writer.flush();
//            writer.close();
//            Log.i("file1","Log file creation and write successful");
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//            Log.i("file","file creation failed");
//
//        }
//    }


    //store farmers details offline
    public void storeLogins(){
        String url = "http://dairy.digerp.com/milkfarming/farmers/logins.php";
        final ArrayList<Logins> logins = new ArrayList<>();

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {

                            Log.d("logins",response);
                            JSONObject jsonObj = new JSONObject(response.toString());
                            JSONArray jsonArray = jsonObj.getJSONArray("details");

                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject json = jsonArray.getJSONObject(i);

                                String email = json.getString("email");
                                String loc_code = json.getString("loc_code");
                                String location = json.getString("location_name");
                                String username = json.getString("username");
                                String password = json.getString("password");

                                Logins lg = new Logins(username,password,email,location,loc_code);
                                logins.add(lg);

                            }

                            Log.d("logins",String.valueOf(logins.size()));

                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            for (Logins login : logins){
                                db.addLogins(login);
                                Log.d("logins",login.getEmail());
                            }

                            new Internet().execute();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //progressDialog.dismiss();
                        Log.d("Error.Response","error getting logins");
                       // new Internet().execute();
                    }
                });
        mQueue.add(postRequest);

    }

    //get routes
    public void getRoutes(){
        String url = "http://dairy.digerp.com/milkfarming/routes/routes.php";
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {

                            myroutes = new ArrayList<String>();
                            JSONObject jsonObj = new JSONObject(response.toString());
                            JSONArray jsonArray = jsonObj.getJSONArray("routes");

                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject json = jsonArray.getJSONObject(i);

                                String route = json.getString("rname");
                                myroutes.add(route);
                                //Log.i("routes",route);
                            }

                            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,
                                    myroutes.toArray(new String[myroutes.size()]));
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(spinnerAdapter);
                            Log.i("routes",String.valueOf(jsonArray.length()));
                            Log.i("nice",String.valueOf(myroutes.size()));
                            new Internet().execute();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Response2", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //progressDialog.dismiss();
                        Log.d("Error.Response","error");
                        new Internet().execute();
                    }
                });
        mQueue.add(postRequest);
    }


    //confirm supplier name
    public void confirmName(){
        Intent intent = new Intent(MainActivity.this,FilterFarmerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivityForResult(,1);
        startActivityForResult(intent, 1);

        //startActivity(intent);

//        String url = "http://dairy.digerp.com/milkfarming/farmers/confirm_name.php?id="+supp_id.getText().toString();
//
//        StringRequest getRequest = new StringRequest(Request.Method.GET,url,new Response.Listener<String>(){
//            @Override
//            public void onResponse(String response) {
//
//                try {
//                    JSONObject jsonObj = new JSONObject(response.toString());
//                    JSONArray jsonArray = jsonObj.getJSONArray("details");
//
//                    JSONObject json = jsonArray.getJSONObject(0);
//                    String supp_name = json.getString("supp_name");
//                    String supp_id = json.getString("supplier_id");
//                    String contact = json.getString("contact");
//
//                    if(supp_name.equals("null")){
//                        confirmname.setError("Ivalid member number");
//                    }
//
//                    //STORE supplier id and contact
//                    SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("supplier_id",supp_id);
//                    editor.putString("farmer_contact",contact);
//                    editor.putString("supp_name",supp_name);
//
//                    editor.commit();
//
//
//
//                    confirmname.setText(supp_name);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    supp_id.setText("");
//                    supp_id.setError("Farmer does not exist");
//                    confirmname.setText("");
//                }
//                Log.i("confirm",response);
//            }
//        },new Response.ErrorListener(){
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//       });//{
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("id", supp_id.getText().toString());
//                return params;
//            }
        //};

       // mQueue.add(getRequest);
    }

    public void saveData(){
        String url = "http://dairy.digerp.com/milkfarming/farmers/insert.php";
        final String supplier_id = supp_id.getText().toString();
        StringRequest update = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.i("update_succes",response.toString());
                        Toast.makeText(MainActivity.this, "Data Saved Successfully", Toast.LENGTH_SHORT).show();
                    }
                },new Response.ErrorListener(){
            //on error log this data to a file
            @Override
            public void onErrorResponse(VolleyError error) {

                //get data
                SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);
                String route_id = details.getString("route_id","Name");
                String grader_username = details.getString("username","");
                //String supplier_id = details.getString("supplier_id","Name");
                //String total = btnweight.getText().toString().trim();
                String shift = details.getString("shift","");
                String total = details.getString("reading","");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String ord_date = df.format(new Date()).toString();
                try
                {
                    File root = new File(Environment.getExternalStorageDirectory(), "weight");
                    if (!root.exists())
                    {
                        root.mkdirs();
                    }
                    Date d = new Date();
                    DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
                    String exact_time = dateFormater.format(d);
                    File file = new File(root, exact_time + "backup.txt");


                    FileWriter writer = new FileWriter(file,true);

                    writer.append(route_id.trim() + " " + supplier_id.trim()  + " " + shift
                    + " " + ord_date.trim() + " " + grader_username.trim() + " " + total.trim() +"\n");

                    writer.flush();
                    writer.close();
                    Log.i("backup","Log back up file created and written successfully");
                    Toast.makeText(MainActivity.this,"Data saved successfully",Toast.LENGTH_LONG).show();

                   // sendMassage();

                    //genPDFReport();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Log.i("backup_f","back up failed");

                }
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);
                String route_id = details.getString("route_id","Name");
                String supplier_id = details.getString("supplier_id","Name");
                String total = details.getString("reading","");//btnweight.getText().toString().trim();
                String shift = details.getString("shift","");
                String grader_username = details.getString("username","");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String ord_date = df.format(new Date()).toString();

                params.put("supplier_id",supplier_id);
                params.put("route_id",route_id);
                params.put("shift",shift);
                params.put("total",total);
                params.put("ord_date",ord_date);
                params.put("username",grader_username);

                return params;
            }

        };
        mQueue.add(update);
    }

    public void sendMassage(){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String massage = "";

            //get massage body
            SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);

            //farmer contact
            String phone = details.getString("farmer_contact","");

            if(phone.startsWith("7")){
                phone = "0" + phone;
            }

            String reading = details.getString("reading",""); //weight readings
            String shift = details.getString("shift","");
            String farmer_id = details.getString("supplier_id","");
            String farmer_name = details.getString("supp_name","");
            String grader_name = details.getString("grader_name","");


            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            String date = df.format(new Date()).toString();

            massage += farmer_name + ": " + farmer_id + "\n";
            massage += "Quantity supplied: " + reading.trim() + "\n";
            massage += "Collected by: " + grader_name + "\n";
            massage += shift + " shift on " + date;

            if(supp_id.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(),"Enter supplier id",Toast.LENGTH_LONG).show();
            }else{
                smsManager.sendTextMessage(phone, null, massage, null, null);
                Toast.makeText(getApplicationContext(),"Massage sent successfully",Toast.LENGTH_LONG).show();
            }

        } catch (Exception ex) {

            Toast.makeText(getApplicationContext(),"Report was not sent",Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    //generate pdf report
    public void genPDFReport() {
        Document document = new Document(PageSize.A4);

        File root = new File(Environment.getExternalStorageDirectory(), "weight");
        File file = new File(root,"report.pdf");

        //get content
        String massage = "";

        //get massage body
        SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);

        String reading = details.getString("reading",""); //weight readings
        String shift = details.getString("shift","");
        String farmer_id = details.getString("supplier_id","");
        String farmer_name = details.getString("supp_name","");
        String grader_name = details.getString("grader_name","");


        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String date = df.format(new Date()).toString();

        massage += farmer_name + ": " + farmer_id + "\n";
        massage += "Quantity supplied: " + reading.trim() + "\n";
        massage += "Collected by: " + grader_name + "\n";
        massage += shift + " shift on " + date;

        try {
            PdfWriter.getInstance(document,new FileOutputStream(file));
            document.open();
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        //create paragraphs
        //Paragraph para = new Paragraph("Second paragraph");
        //content
        Paragraph content = new Paragraph(massage);
        try {
            //document.add(para);
            document.add(content);
            Toast.makeText(getApplicationContext(),"Data written to pdf",Toast.LENGTH_LONG).show();
        } catch (DocumentException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        document.close();
    }

    //class to print pdf
    public class PdfDocumentAdapter extends PrintDocumentAdapter {

        Context context = null;
        String pathName = "";
        public PdfDocumentAdapter(Context ctxt, String pathName) {
            context = ctxt;
            this.pathName = pathName;
        }
        @Override
        public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
            if (cancellationSignal.isCanceled()) {
                layoutResultCallback.onLayoutCancelled();
            }
            else {
                PrintDocumentInfo.Builder builder=
                        new PrintDocumentInfo.Builder("report.pdf");
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build();
                layoutResultCallback.onLayoutFinished(builder.build(),
                        !printAttributes1.equals(printAttributes));
            }
        }

        @Override
        public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            InputStream in=null;
            OutputStream out=null;
            File root = new File(Environment.getExternalStorageDirectory(), "weight");
            if(!root.exists()){
                root.mkdirs();
            }
            try {
                File file = new File(root,"report.pdf");
                in = new FileInputStream(file);
                out=new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                byte[] buf=new byte[16384];
                int size;

                while ((size=in.read(buf)) >= 0
                        && !cancellationSignal.isCanceled()) {
                    out.write(buf, 0, size);
                }

                if (cancellationSignal.isCanceled()) {
                    writeResultCallback.onWriteCancelled();
                }
                else {
                    writeResultCallback.onWriteFinished(new PageRange[] { PageRange.ALL_PAGES });
                }
            }
            catch (Exception e) {
                writeResultCallback.onWriteFailed(e.getMessage());
                //Logger.logError( e);
            }
            finally {
                try {
                    in.close();
                    out.close();
                }
                catch (IOException e) {
                    //Logger( e);
                }
            }
        }}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("supplier_id",null);
        editor.putString("supp_name",null);
        editor.commit();
    }
    //remember the scale
    //download list of suppliers
    //enable real time searching

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //unregisterReceiver(mReceiver);
        //bluetoothAdapter.cancelDiscovery();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);

        String supp_name = details.getString("supp_name",null);
        confirmname.setText(supp_name);

        String supp_id_ = details.getString("supplier_id",null);

        confirmname.setText(supp_name);
        supp_id.setText("Member No. : "+supp_id_);

        //Log.d("jhjh",supp_id_);
    }


    public class Internet extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

           // hasInternet = Utils.hasInternetAccess(getApplicationContext());
            hasInternet = Utils.Ping();

            if(hasInternet){
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                for (String rt : myroutes){
                    db.addRoutes(new Routes(rt));
                    Log.d("rtts",rt);
                }
                storeLogins();
            }else{


                Log.d("conn_ava","connection not available");

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                List<Routes> routes = db.getAllRoutes();
                Log.d("rtts_size",String.valueOf(routes.size()));
                for (Routes rt: routes){
                    myroutes.add(rt.getName());
                }

                Log.d("routes_s",String.valueOf(myroutes.size()));

//                // Pass results to ListViewAdapter Class
//                adapter = new ListViewAdapter(FilterFarmerActivity.this, arraylist);
//
//                // Binds the Adapter to the ListView
//                list.setAdapter(adapter);

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!hasInternet){
                // Pass results to ListViewAdapter Class
                //adapter = new ListViewAdapter(FilterFarmerActivity.this, arraylist);

                // Binds the Adapter to the ListView
                //list.setAdapter(adapter);

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,
                        myroutes.toArray(new String[myroutes.size()]));
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
            }else {

            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
