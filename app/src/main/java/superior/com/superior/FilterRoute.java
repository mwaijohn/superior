package superior.com.superior;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import superior.com.superior.adapters.RoutesAdapter;
import superior.com.superior.database.DatabaseHandler;
import superior.com.superior.database.Routes;
import superior.com.superior.models.RouteNames;
import superior.com.superior.utils.Utils;

public class FilterRoute extends AppCompatActivity implements SearchView.OnQueryTextListener{

    boolean hasInternet;
    SearchView editsearch;
    ListView list;
    RequestQueue mQueue;
    RoutesAdapter adapter;
    ArrayList<RouteNames> arraylist = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_route);

        mQueue = Volley.newRequestQueue(this);


        list = (ListView) findViewById(R.id.listview);

        getRoutes();
        // Locate the EditText in listview_main.xml
        editsearch = (SearchView) findViewById(R.id.search);
        editsearch.setOnQueryTextListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                RouteNames route = (RouteNames) list.getItemAtPosition(i);
                //list.setVisibility(View.INVISIBLE);

                SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("route_id",route.getName());
                editor.commit();

                Toast.makeText(FilterRoute.this,route.getName(),Toast.LENGTH_SHORT).show();

                // startActivity(new Intent(FilterFarmerActivity.this,MainActivity.class));
                finish();
                //Toast.makeText(FilterFarmerActivity.this, jhj.getSupplier_name(), Toast.LENGTH_SHORT).show();
            }
        });

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

                            ArrayList<String> myroutes = new ArrayList<String>();
                            ArrayList<RouteNames> routeNames = new ArrayList<>();
                            JSONObject jsonObj = new JSONObject(response.toString());
                            JSONArray jsonArray = jsonObj.getJSONArray("routes");

                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject json = jsonArray.getJSONObject(i);

                                String route = json.getString("rname");

                                routeNames.add(new RouteNames(route));
                                myroutes.add(route);
                                //Log.i("routes",route);
                            }
                            adapter = new RoutesAdapter(FilterRoute.this,routeNames);

                            // Binds the Adapter to the ListView
                            list.setAdapter(adapter);

                            Log.i("routes",String.valueOf(jsonArray.length()));
                            Log.i("nice",String.valueOf(myroutes.size()));
                            new Internet().execute();


                        } catch (JSONException e) {
                            new Internet().execute();

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


    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        adapter.filter(text);
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        list.setAdapter(adapter);
    }


    public class Internet extends AsyncTask<Void,Void,Void> {

        ArrayList<RouteNames> routeNames = new ArrayList<>();
        @Override
        protected Void doInBackground(Void... voids) {

            hasInternet = Utils.Ping();
            if(hasInternet){
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                for (RouteNames route : routeNames) {
                    db.addRoutes(new Routes(route.getName()));
                    Log.d("add_routes","adding routes");
                }

            }else{


                Log.d("conn_ava","connection not available");

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                List<Routes> routes = db.getAllRoutes();
                for (Routes routes1: routes){
                    Log.d("routes",routes1.getName());
                    arraylist.add(new RouteNames(routes1.getName()));
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!hasInternet){
                // Pass results to ListViewAdapter Class
                adapter = new RoutesAdapter(FilterRoute.this, arraylist);

                // Binds the Adapter to the ListView
                list.setAdapter(adapter);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
