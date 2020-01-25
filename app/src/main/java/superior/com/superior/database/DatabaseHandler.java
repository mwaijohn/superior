package superior.com.superior.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datamanager";
    private static final String TABLE_SUPPLIERS = "suppliers";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_SIPPLIER_ID = "supplier_id";

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
        db.execSQL(CREATE_SUPPLIERS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPLIERS);

        // Create tables again
        onCreate(db);
    }

    // code to add the new contact
    public void addContact(Suppliers supplier) {
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
    Suppliers getContact(int id) {
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
    public List<Suppliers> getAllContacts() {
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

