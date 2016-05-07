package com.irodyk.serverapp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.irodyk.product.Product;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by i.rodyk on 5/6/16.
 */
public class MyService extends Service {

    private static final String TAG = "MyService";

    private static final int ACTION_ADD_NEW_ITEM = 1;
    private static final int ACTION_DELETE_ITEM = 2;
    private static final int ACTION_GET_ALL_ITEMS = 3;
    private static final int RESPONSE_ADDED_ITEM_ID = 4;
    private static final int RESPONSE_ALL_ITEMS = 5;
    private static final int RESPONSE_IS_DELETED = 6;

    private Messenger messenger;
    private DB db;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service: onCreate()");

        db = DB.getInstance(this);
        db.openConnection();

        messenger = new Messenger(new ServiceMessageHandler(this));
    }

    @Override
    public void onDestroy() {
        db.closeConnection();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    private static class ServiceMessageHandler extends Handler {

        private WeakReference<MyService> serviceReference;

        public ServiceMessageHandler(MyService service){
            serviceReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MyService service = serviceReference.get();
            if(service == null) return;

            DB db = service.db;

            Message response;
            Bundle b;
            switch (msg.what){
                case ACTION_ADD_NEW_ITEM:
                    Bundle data = msg.getData();
                    data.setClassLoader(Product.class.getClassLoader());
                    long newItemId = db.addProduct((Product) data.getParcelable("product"));
                    response = Message.obtain(null, RESPONSE_ADDED_ITEM_ID);
                    b = new Bundle();
                    b.putLong("id", newItemId);
                    response.setData(b);
                    try {
                        msg.replyTo.send(response);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_DELETE_ITEM:
                    long id = msg.getData().getLong("id");
                    boolean isDeleted = db.deleteProduct(id);
                    response = Message.obtain(null, RESPONSE_IS_DELETED);
                    b = new Bundle();
                    b.putBoolean("isDeleted", isDeleted);
                    response.setData(b);
                    try {
                        msg.replyTo.send(response);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_GET_ALL_ITEMS:
                    ArrayList<Product> products = (ArrayList<Product>) db.getAll();
                    response = Message.obtain(null, RESPONSE_ALL_ITEMS);
                    b = new Bundle();
                    b.putParcelableArrayList("products", products);
                    response.setData(b);
                    try {
                        msg.replyTo.send(response);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}