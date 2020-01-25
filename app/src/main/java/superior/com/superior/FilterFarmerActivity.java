package superior.com.superior;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Arrays;

import superior.com.superior.adapters.ListViewAdapter;
import superior.com.superior.models.AnimalNames;
import superior.com.superior.models.FarmerNames;

public class FilterFarmerActivity  extends AppCompatActivity implements SearchView.OnQueryTextListener {

    // Declare Variables
    ListView list;
    ListViewAdapter adapter;
    SearchView editsearch;
    String[] animalNameList;
    ArrayList<AnimalNames> arraylist = new ArrayList<AnimalNames>();
    ArrayList<FarmerNames> farmerNameslist = new ArrayList<>();
    //HashMap<String,String> suppliers;
    RequestQueue mQueue;
    ArrayList<String> suppliers;
    //String id,name,contact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_farmer);


        mQueue = Volley.newRequestQueue(this);


        // Generate sample data

//        animalNameList = new String[]{"Lion", "Tiger", "Dog",
//                "Cat", "Tortoise", "Rat", "Elephant", "Fox",
//                "Cow","Donkey","Monkey"};

        // Locate the ListView in listview_main.xml
        list = (ListView) findViewById(R.id.listview);

        getSuppliers();

        // Locate the EditText in listview_main.xml
        editsearch = (SearchView) findViewById(R.id.search);
        editsearch.setOnQueryTextListener(this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                AnimalNames jhj = (AnimalNames) list.getItemAtPosition(i);
                //list.setVisibility(View.INVISIBLE);

                //STORE supplier id and contact
                SharedPreferences sharedPreferences = getSharedPreferences("APP_DETAILS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("supplier_id",jhj.getSupplier_id());
                editor.putString("farmer_contact",jhj.getContact());
                editor.putString("supp_name",jhj.getSupplier_name());
                editor.commit();

                startActivity(new Intent(FilterFarmerActivity.this,MainActivity.class));
                Toast.makeText(FilterFarmerActivity.this, jhj.getSupplier_name(), Toast.LENGTH_SHORT).show();
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

    //get routes
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

                                AnimalNames animalNames = new AnimalNames(name,id,contact);
                                arraylist.add(animalNames);
                            }

//                            Object[] objArr = suppliers.toArray();
//                            String[] str = Arrays.copyOf(objArr, objArr.length,String[].class);
////
//                            for (int i = 0; i < str.length; i++) {
//                                AnimalNames animalNames = new AnimalNames(str[i]);
//                                // Binds all strings into an array
//                                arraylist.add(animalNames);
//                            }

                            // Pass results to ListViewAdapter Class
                            adapter = new ListViewAdapter(FilterFarmerActivity.this, arraylist);

                            // Binds the Adapter to the ListView
                            list.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
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
                    }
                });
        mQueue.add(postRequest);
    }
}
