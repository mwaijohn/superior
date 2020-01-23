package superior.com.superior;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateDB extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(checkConnection()){
                    //read and save back up
                    readBackup();
                    Log.i("udating data","Updating data");
                }else {
                    Log.i("No_internet","No internet connection");
                }
            }
        },60000,60000);
        return super.onStartCommand(intent, flags, startId);
    }


    private boolean checkConnection(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo==null){
            return false;
        }

        return networkInfo.isConnected() && networkInfo.isAvailable() && networkInfo!=null;
    }

    private void readBackup(){
        RequestQueue mQueue = Volley.newRequestQueue(this);
        File root = new File(Environment.getExternalStorageDirectory(), "weight");

        Date d = new Date();
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        String exact_time = dateFormater.format(d);

        if(root.exists()){
            try {
                File file = new File(root,exact_time+"backup.txt");
                FileInputStream fins = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fins));

//                String line;
//                int linecount=0;
//                while ((line= reader.readLine())!= null){
//                    linecount++;
//                }
                //String supplier_id,route_id,shift,total,ord_date;
                String url = "http://192.168.137.1/milkfarming/farmers/get_farmer_id.php";
                String read_line;
                while ((read_line= reader.readLine())!= null){
                     final String [] data = read_line.split(" ");
                    StringRequest id = new StringRequest(Request.Method.POST,url,new Response.Listener<String>(){
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObj = new JSONObject(response.toString());
                                JSONArray jsonArray = jsonObj.getJSONArray("id");

                                JSONObject json = jsonArray.getJSONObject(0);
                                String supp_id = json.getString("supplier_id");
                                saveData(data[0],supp_id,data[2],data[3],data[4],data[5]);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }){
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<String, String>();

                            params.put("member_no",data[1]);

                            return params;
                        }

                    };
                    mQueue.add(id);
                    //for(int i=0;;i++){
                        //saveData(data[0],data[1],data[2],data[3],data[4]);
                    //}
                }

                //file.delete();

            }catch (Exception  io){
                io.printStackTrace();
                Log.i("error","Error reading back up");
            }
        }else {
            Log.i("error","back up resource does not exist");
        }
    }

    public void saveData( final String route_id,final String supplier_id , final String shift,
                          final String ord_date,final String username,final String total){
        RequestQueue mQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.137.1/milkfarming/farmers/insert.php";
        StringRequest update = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Date d = new Date();
                        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
                        String exact_time = dateFormater.format(d);
                        try{
                            File root = new File(Environment.getExternalStorageDirectory(), "weight");
                            File file = new File(root,exact_time+"backup.txt");
                            file.delete();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        Log.i("update_success","Your backup data was saved");
                    }
                },new Response.ErrorListener(){
            //on error log this data to a file
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("update_error",error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                params.put("route_id",route_id);
                params.put("supplier_id",supplier_id);
                params.put("shift", shift);
                params.put("total",total);
                params.put("ord_date",ord_date);
                params.put("username",username);

                return params;
            }

        };
        mQueue.add(update);
    }
}
