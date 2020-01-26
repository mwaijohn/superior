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

import superior.com.superior.adapters.ListViewAdapter;
import superior.com.superior.database.DatabaseHandler;
import superior.com.superior.database.Suppliers;
import superior.com.superior.models.FarmerNames;
import superior.com.superior.utils.Utils;

public class FilterFarmerActivity  extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ListView list;
    ListViewAdapter adapter;
    SearchView editsearch;
    ArrayList<FarmerNames> arraylist = new ArrayList<FarmerNames>();
    RequestQueue mQueue;
    ArrayList<String> suppliers;
    boolean hasInternet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_farmer);


        mQueue = Volley.newRequestQueue(this);

        list = (ListView) findViewById(R.id.listview);

        getSuppliers();

        // Locate the EditText in listview_main.xml
        editsearch = (SearchView) findViewById(R.id.search);
        editsearch.setOnQueryTextListener(this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                FarmerNames jhj = (FarmerNames) list.getItemAtPosition(i);
                //list.setVisibility(View.INVISIBLE);

                //STORE supplier id and contact
                SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("supplier_id",jhj.getSupplier_id());
                editor.putString("farmer_contact",jhj.getContact());
                editor.putString("supp_name",jhj.getSupplier_name());
                editor.commit();
                editor.apply();

               // startActivity(new Intent(FilterFarmerActivity.this,MainActivity.class));
                finish();
                //Toast.makeText(FilterFarmerActivity.this, jhj.getSupplier_name(), Toast.LENGTH_SHORT).show();
            }
        });

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

    //get suppliers
    public void getSuppliers(){
        String url = "http://dairy.digerp.com/milkfarming/farmers/suppliers.php";
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {

                            suppliers = new ArrayList<>();
                            JSONObject jsonObj = new JSONObject(response.toString());
                            JSONArray jsonArray = jsonObj.getJSONArray("farmers");

                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject json = jsonArray.getJSONObject(i);

                                String id = json.getString("supplier_id");
                                String name = json.getString("supp_name");
                                String contact = json.getString("contact");
                                suppliers.add(name);

                                FarmerNames supplierNames = new FarmerNames(name,contact,id);
                                arraylist.add(supplierNames);
                            }

                            // Pass results to ListViewAdapter Class
                            adapter = new ListViewAdapter(FilterFarmerActivity.this, arraylist);

                            // Binds the Adapter to the ListView
                            list.setAdapter(adapter);

                            new Internet().execute();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            new Internet().execute();
                        }
                        Log.d("Response2", String.valueOf(suppliers.size()));
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


    public class Internet extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            hasInternet = Utils.hasInternetAccess(getApplicationContext());
            if(hasInternet){
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                for (FarmerNames farmerNames : arraylist) {
                    db.addSupplier(new Suppliers(farmerNames.getSupplier_name(),farmerNames.getContact(),farmerNames.getSupplier_id()));
                    Log.d("add_suppliers","adding suppliers");
                }

            }else{


                Log.d("conn_ava","connection not available");

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                List<Suppliers> suppliers = db.getAllSuppliers();
                for (Suppliers suppliers1: suppliers){
                    Log.d("suppliers",suppliers1.getSupp_name());
                    arraylist.add(new FarmerNames(suppliers1.getSupp_name(),suppliers1.getContact(),suppliers1.getSupplier_id()));
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!hasInternet){
                // Pass results to ListViewAdapter Class
                adapter = new ListViewAdapter(FilterFarmerActivity.this, arraylist);

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
