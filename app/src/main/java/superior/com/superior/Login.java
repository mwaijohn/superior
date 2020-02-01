package superior.com.superior;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import superior.com.superior.database.DatabaseHandler;
import superior.com.superior.database.Logins;
import superior.com.superior.utils.Utils;

public class Login extends AppCompatActivity {

    EditText email_username,password;
    Button login;
    RequestQueue mQueue;
    ProgressDialog progressDialog;
    ArrayList<String> myroutes ;
    boolean hasInternet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);

        mQueue = Volley.newRequestQueue(this);
        myroutes = new ArrayList<String>();;
        //getRoutes();

        new Internet().execute();
        //Log.i("arraylist",String.valueOf(myroutes.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent1 = new Intent(this,UpdateDB.class);
        //context.startService(intent1);
        startService(intent1);
    }

    public class Internet extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            hasInternet = Utils.hasInternetAccess(getApplicationContext());
            hasInternet = Utils.Ping();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(hasInternet == false){
                Toast.makeText(Login.this, "No internet connection", Toast.LENGTH_SHORT).show();
                // Pass results to ListViewAdapter Class
                //adapter = new ListViewAdapter(FilterFarmerActivity.this, arraylist);

                // Binds the Adapter to the ListView
                //list.setAdapter(adapter);
               login.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       DatabaseHandler db = new DatabaseHandler(getApplicationContext());

//                       Log.d("count", String.valueOf(db.getAllLogins().size()));
//
                       List<Logins> hhh = db.getAllLogins();
                       for (Logins l: hhh){

                           Log.d("jhj",l.getPassword() +" and " + l.getUsername()
                                   +" and " + l.getEmail() +" and " + l.getLoc_code());
                       }
                       Logins logins = db.getLogin(email_username.getText().toString(),password.getText().toString());

                       //Log.d("count", String.valueOf(db.getAllLogins().size()));
                       if(logins != null){
                           if(((email_username.getText().toString().equals(logins.getUsername())))
                                   && db.MD5(password.getText().toString()).equals(logins.getPassword()) ){

                               //STORE AUTHENTICATED USER DETAILS
                               SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                               SharedPreferences.Editor editor = sharedPreferences.edit();
                               editor.putString("email", logins.getEmail());
                               editor.putString("loc_code",logins.getLoc_code());
                               editor.putString("username",logins.getUsername());
                               editor.putString("grader_name",logins.getLocation());
                               editor.apply();

                               Log.d("password",logins.getPassword() + " " + db.MD5(password.getText().toString()) );

                               Intent intent = new Intent(Login.this, MainActivity.class);
                               password.setText("");
                               email_username.setText("");
                               startActivity(intent);

                               finish();

                           }else {
                               password.setError("Wrong password!");
                           }
                       }else {
                           password.setError("Wrong password!!");

                       }

//                       Log.d("logins", String.valueOf(db.getAllLogins().size()));
//
//                       Log.d("logins",logins.getUsername() + " " + email_username.getText().toString() );
//                       Log.d("logins",logins.getPassword() + " " + db.MD5(password.getText().toString()) );

                   }
               });


            }else {
                //Toast.makeText(Login.this, "INTERNET IKO", Toast.LENGTH_SHORT).show();
                login.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){

                        Intent intent1 = new Intent(Login.this,UpdateDB.class);
                        //context.startService(intent1);
                        startService(intent1);

                        progressDialog = new ProgressDialog(Login.this);
                        progressDialog.setMessage("Logging in");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        String url = "http://dairy.digerp.com/milkfarming/accounts/login.php";
                        // String url = "http://192.168.137.1/milkfarming/accounts/login.php";

                        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>()
                                {
                                    @Override
                                    public void onResponse(String response) {

                                        Log.d("response__",response);

                                        try {

                                            progressDialog.dismiss();
                                            Log.i("er_r",response);
                                            JSONObject jsonObj = new JSONObject(response.toString());
                                            JSONArray jsonArray = jsonObj.getJSONArray("details");

                                            String email="",loc="",username="";
                                            JSONObject json = jsonArray.getJSONObject(0);

                                            email = json.getString("email");
                                            loc = json.getString("loc_code");
                                            username= json.getString("username");
                                            String g_name = json.getString("location_name");
                                            //String error = json.getString("error");

                                            Log.d(loc,email);
                                            if(!(email.equals("null"))){

                                                //STORE AUTHENTICATED USER DETAILS
                                                SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("email", email);
                                                editor.putString("loc_code",loc);
                                                editor.putString("username",username);
                                                editor.putString("grader_name",g_name);
                                                editor.apply();

                                                Intent intent = new Intent(Login.this, MainActivity.class);
                                                password.setText("");
                                                email_username.setText("");
                                                startActivity(intent);
                                                finalize();

                                            }else {
                                                password.setError("Wrong password");
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.i("error",e.toString());
                                            e.printStackTrace();
                                        } catch (Throwable throwable) {
                                            throwable.printStackTrace();
                                        }
                                        //Log.d("Response", response);
                                    }
                                },
                                new Response.ErrorListener()
                                {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // error
                                        progressDialog.dismiss();
                                        Log.d("Error.Response",error.getLocalizedMessage());
                                        Toast.makeText(Login.this,error.toString(),Toast.LENGTH_LONG).show();
                                    }
                                }
                        ) {
                            @Override
                            protected Map<String, String> getParams()
                            {
                                Map<String, String>  params = new HashMap<String, String>();
//                                params.put("email", email_username.getText().toString());//email_username.getText().toString()
//                                params.put("password", password.getText().toString()); //password.getText().toString()

                                params.put("email", "alexmaina");//email_username.getText().toString()
                                params.put("password", "1234");

                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String,String> map = new HashMap<String, String>();
                                //map.put("Content-Type", "application/json");
                                map.put("Content-Type", "application/x-www-form-urlencoded");
                                map.put("Accept", "application/json");
                                map.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240 ");
                                map.put("Cookie","_test=0d4a44568afa1863a28902b89fd3fec80d4a44568afa1863a28902b89fd3fec8;expires=Thu, 31 Dec 2037 23:55:55 GMT;path=/");
                                return map;
                            }
                        };

                        if(email_username.getText().equals("") || password.getText().equals("")){
                            email_username.setError("Enter email");
                            password.setError("Enter correct password");
                        }{
                            mQueue.add(postRequest);
                        }
                    }
                });
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