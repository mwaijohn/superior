package superior.com.superior;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Map;
import java.util.ArrayList;

public class Login extends AppCompatActivity {

    EditText email_username,password;
    Button login;
    RequestQueue mQueue;
    ProgressDialog progressDialog;
    ArrayList<String> myroutes ;

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
                                Log.d("Error.Response","request error");
                                Toast.makeText(Login.this,error.toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("email", "alexmaina" );//email_username.getText().toString()
                        params.put("password", "1234"); //password.getText().toString()

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
        Log.i("arraylist",String.valueOf(myroutes.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent1 = new Intent(this,UpdateDB.class);
        //context.startService(intent1);
        startService(intent1);
    }
}