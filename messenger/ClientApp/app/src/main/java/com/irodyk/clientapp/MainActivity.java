package com.irodyk.clientapp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.irodyk.product.Product;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private static final int ACTION_ADD_NEW_ITEM = 1;
    private static final int ACTION_DELETE_ITEM = 2;
    private static final int ACTION_GET_ALL_ITEMS = 3;
    private static final int RESPONSE_ADDED_ITEM_ID = 4;
    private static final int RESPONSE_ALL_ITEMS = 5;
    private static final int RESPONSE_IS_DELETED = 6;

    private Messenger messengerService;
    private Messenger incomingMessenger = new Messenger(new ResponseMessageHandler(this));

    private AlertDialog.Builder dialog;
    private EditText etName, etPrice, etCountry, etDelivery;
    private RecyclerView recycler;

    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddNewItemDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent();
        i.setClassName("com.irodyk.serverapp", "com.irodyk.serverapp.MyService");
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service connected");
            messengerService = new Messenger(service);

            loadProducts();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
            messengerService = null;
        }
    };

    private static class ResponseMessageHandler extends Handler {

        private WeakReference<MainActivity> activityReference;

        public ResponseMessageHandler(MainActivity activity){
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = activityReference.get();
            if(activity == null) return;

            RecyclerView recycler = activity.recycler;

            switch (msg.what){
                case RESPONSE_ALL_ITEMS:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(Product.class.getClassLoader());
                    activity.products = msg.getData().getParcelableArrayList("products");
                    activity.setupRecycler();
                    Log.d(TAG, "Items loading complete");
                    break;
                case RESPONSE_ADDED_ITEM_ID:
                    if(recycler == null) return;
                    long id = msg.getData().getLong("id");
                    Toast.makeText(activity, "Item added. Assigned Id: " + id, Toast.LENGTH_SHORT).show();
                    if (id != -1) {
                        ((RecyclerAdapter) recycler.getAdapter()).getProductToAdd().setId(id);
                        ((RecyclerAdapter) recycler.getAdapter()).addItem(((RecyclerAdapter) recycler.getAdapter()).getProductToAdd());
                        recycler.smoothScrollToPosition((recycler.getAdapter()).getItemCount() - 1);
                    } else ((RecyclerAdapter) recycler.getAdapter()).cancelItemInsertion();
                    break;
                case RESPONSE_IS_DELETED:
                    if(recycler == null) return;
                    boolean isDeleted = msg.getData().getBoolean("isDeleted");
                    Toast.makeText(activity, "Item deleted: " + isDeleted, Toast.LENGTH_SHORT).show();
                    if (isDeleted) ((RecyclerAdapter) recycler.getAdapter()).deleteItem(((RecyclerAdapter) recycler.getAdapter()).getItemToDelete());
                    else ((RecyclerAdapter) recycler.getAdapter()).cancelItemDeletion();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private void setupRecycler() {
        recycler = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(new RecyclerAdapter(this, products));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
                deleteItem(holder.getAdapterPosition());
            }
        });

        itemTouchHelper.attachToRecyclerView(recycler);
    }

    private void addItem(Product p){
        ((RecyclerAdapter) recycler.getAdapter()).setProductToAdd(p);
        Message msg = Message.obtain(null, ACTION_ADD_NEW_ITEM);
        msg.replyTo = incomingMessenger;
        Bundle b = new Bundle();
        b.putParcelable("product", p);
        msg.setData(b);
        try {
            messengerService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts(){
        Message msg = Message.obtain(null, ACTION_GET_ALL_ITEMS);
        msg.replyTo = incomingMessenger;
        try {
            messengerService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(int pos){
        ((RecyclerAdapter) recycler.getAdapter()).addItemToDelete(pos);
        Message msg = Message.obtain(null, ACTION_DELETE_ITEM);
        msg.replyTo = incomingMessenger;
        Bundle b = new Bundle();
        b.putLong("id", ((RecyclerAdapter) recycler.getAdapter()).getIdByPos(pos));
        msg.setData(b);
        try {
            messengerService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add:
                createAddNewItemDialog();
                dialog.show();
                break;
        }
    }

    private void createAddNewItemDialog(){
        dialog = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog, null);
        dialog.setView(v);

        etName = (EditText) v.findViewById(R.id.etName);
        etPrice = (EditText) v.findViewById(R.id.etPrice);
        etCountry = (EditText) v.findViewById(R.id.etCountry);
        etDelivery = (EditText) v.findViewById(R.id.etDelivery);

        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Product p = new Product();
                p.setName(etName.getText().toString().trim());
                p.setPrice(etPrice.getText().toString().trim());
                p.setCountry(etCountry.getText().toString().trim());
                p.setDelivery(etDelivery.getText().toString().trim());

                addItem(p);
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
