package com.irodyk.serviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.irodyk.iservice.ICallback;
import com.irodyk.iservice.IService;
import com.irodyk.product.Product;

import java.util.List;

/**
 * Created by i.rodyk on 3/29/16.
 */
public class MyService extends Service {

    private static final String TAG = "MyService";

    private IService.Stub service;
    private DB db;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service: onCreate()");

        db = DB.getInstance(this);
        db.openConnection();

        service = new IService.Stub(){

            @Override
            public List<Product> getAll(){
                return db.getAll();
            }

            @Override
            public void add(Product product, ICallback callback) throws RemoteException {
                long id = db.addProduct(product);
                callback.addedItemId((int)id);
            }

            @Override
            public void delete(long id, ICallback callback) throws RemoteException {
                boolean isDeleted = db.deleteProduct(id);
                callback.isItemDeleted(isDeleted);
            }
        };
    }

    @Override
    public void onDestroy() {
        db.closeConnection();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return service;
    }
}
