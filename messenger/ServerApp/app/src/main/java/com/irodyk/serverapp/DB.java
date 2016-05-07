package com.irodyk.serverapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.irodyk.product.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i.rodyk on 5/6/16.
 */
public class DB {

    private static final String TAG = "DB";

    private static final String TABLE = "product";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String COUNTRY = "country";
    private static final String DELIVERY = "delivery";

    private static DB instance;
    private SQLiteDatabase database;
    private DBHelper helper;

    private DB(Context ctx){
        helper = new DBHelper(ctx);
    }

    public static DB getInstance(Context ctx){
        if (instance == null){
            instance = new DB(ctx);
        }
        return instance;
    }

    public void closeConnection(){
        database.close();
        helper.close();
    }

    public void openConnection(){
        database = helper.getWritableDatabase();
    }

    public long addProduct(Product product){
        ContentValues cv = new ContentValues();
        cv.put(NAME, product.getName());
        cv.put(PRICE, product.getPrice());
        cv.put(COUNTRY, product.getCountry());
        cv.put(DELIVERY, product.getDelivery());

        return database.insert(TABLE, null, cv);
    }

    public boolean deleteProduct(long id){
        return database.delete(TABLE, ID + " = " + id, null) == 1;
    }

    public List<Product> getAll(){
        Cursor c = database.query(TABLE, null, null, null, null, null, null);
        int count = c.getCount();
        List<Product> products = new ArrayList<>();
        c.moveToFirst();
        Product p;
        for(int i = 0; i < count; i++){
            p = new Product();
            p.setId(c.getInt(c.getColumnIndex(ID)));
            p.setName(c.getString(c.getColumnIndex(NAME)));
            p.setPrice(c.getString(c.getColumnIndex(PRICE)));
            p.setCountry(c.getString(c.getColumnIndex(COUNTRY)));
            p.setDelivery(c.getString(c.getColumnIndex(DELIVERY)));

            products.add(p);
            c.moveToNext();
        }
        c.close();
        return products;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "productDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE + " ("
                    + ID + " integer primary key autoincrement,"
                    + NAME + " text,"
                    + PRICE + " text,"
                    + COUNTRY + " text,"
                    + DELIVERY + " integer" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}