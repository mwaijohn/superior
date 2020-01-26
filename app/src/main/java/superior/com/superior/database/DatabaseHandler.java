package superior.com.superior.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import superior.com.superior.Login;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datamanager";
    private static final String TABLE_SUPPLIERS = "suppliers";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_SIPPLIER_ID = "supplier_id";

    private static final String TABLE_ROUTES = "routes";
    private static final String ROUTE = "route";

    //table login
    private static final String TABLE_LOGIN = "login";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final String LOCATION_CODE = "loc_code";
    private static final String LOCATION_NAME = "loc_name";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SUPPLIERS_TABLE = "CREATE TABLE " + TABLE_SUPPLIERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT," + KEY_SIPPLIER_ID + " TEXT" + ")";

        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + USERNAME + " TEXT,"
                + PASSWORD + " TEXT," + EMAIL + " TEXT," + LOCATION_NAME + " TEXT,"
                + LOCATION_CODE + " TEXT" + ")";

        String CREATE_ROUTES_TABLE = "CREATE TABLE " + TABLE_ROUTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + ROUTE + " TEXT"  + ")";


        db.execSQL(CREATE_SUPPLIERS_TABLE);
        db.execSQL(CREATE_ROUTES_TABLE);
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPLIERS);

        // Create tables again
        onCreate(db);
    }

    //add logins
    public void addLogins(Logins logins){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(USERNAME,logins.username);
        values.put(EMAIL,logins.email);
        values.put(LOCATION_NAME,logins.location);
        values.put(LOCATION_CODE,logins.loc_code);

        db.insert(TABLE_LOGIN,null,values);
        db.close();
    }

    // code to get all contacts in a list view
    public List<Logins> getAllLogins() {
        List<Logins> loginsList = new ArrayList<Logins>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Logins login = new Logins();
                login.setUsername(cursor.getString(1));
                login.setPassword(cursor.getString(2));
                login.setEmail(cursor.getString(3));
                login.setLocation(cursor.getString(4));
                login.setLoc_code(cursor.getString(5));
                // Adding contact to list
                loginsList.add(login);
            } while (cursor.moveToNext());
        }

        return loginsList;
    }

    //get one login
    public Logins getLogin(String username, String password){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOGIN, new String[] {USERNAME,
                        PASSWORD, EMAIL, LOCATION_NAME,LOCATION_CODE  }, PASSWORD + "= ? AND " + USERNAME + " = ?" ,
                new String[] { password, username }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;

        Logins logins = new Logins(cursor.getString(1),cursor.getString(2),cursor.getString(3),
                cursor.getString(4),cursor.getString(5));

        return logins;
    }


    //add routes
    public void addRoutes(Routes routes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ROUTE,routes.name);
        db.insert(TABLE_ROUTES,null,values);
        db.close();
    }

    // code to get all contacts in a list view
    public List<Routes> getAllRoutes() {
        List<Routes> routesList = new ArrayList<Routes>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ROUTES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Routes route = new Routes();
                route.setName(cursor.getString(1));
                // Adding contact to list
                routesList.add(route);
            } while (cursor.moveToNext());
        }

        return routesList;
    }

    // code to add the new contact
    public void addSupplier(Suppliers supplier) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, supplier.supp_name);
        values.put(KEY_PH_NO, supplier.contact);
        values.put(KEY_SIPPLIER_ID,supplier.supplier_id);

        // Inserting Row
        db.insert(TABLE_SUPPLIERS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get the single contact
    Suppliers getSupplier(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SUPPLIERS, new String[]{KEY_ID,
                        KEY_NAME, KEY_PH_NO}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Suppliers supplier = new Suppliers(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3));
        // return contact
        return supplier;
    }

    // code to get all contacts in a list view
    public List<Suppliers> getAllSuppliers() {
        List<Suppliers> supplierList = new ArrayList<Suppliers>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SUPPLIERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Suppliers supplier = new Suppliers();
                supplier.set_id(Integer.parseInt(cursor.getString(0)));
                supplier.setSupp_name(cursor.getString(1));
                supplier.setContact(cursor.getString(2));
                supplier.setSupplier_id(cursor.getString(3));
                // Adding contact to list
                supplierList.add(supplier);
            } while (cursor.moveToNext());
        }

        // return contact list
        return supplierList;
    }
}

