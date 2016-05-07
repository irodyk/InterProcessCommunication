package com.irodyk.activityapp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import com.irodyk.iservice.ICallback;
import com.irodyk.iservice.IService;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private IService iService;

    private ICallback.Stub iCallback;

    private AlertDialog.Builder dialog;
    private EditText etName, etPrice, etCountry, etDelivery;
    private RecyclerAdapter adapter;
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
        i.setClassName("com.irodyk.serviceapp", "com.irodyk.serviceapp.MyService");
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
            iService = IService.Stub.asInterface(service);

            iCallback = new ICallback.Stub() {
                @Override
                public void addedItemId(final long id) throws RemoteException {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Item added. Assigned Id: " + id, Toast.LENGTH_SHORT).show();
                            if (id != -1) {
                                adapter.getProductToAdd().setId(id);
                                adapter.addItem(adapter.getProductToAdd());
                                recycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            } else adapter.cancelItemInsertion();
                        }
                    });
                }

                @Override
                public void isItemDeleted(final boolean isDeleted) throws RemoteException {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Item deleted: " + isDeleted, Toast.LENGTH_SHORT).show();
                            if (isDeleted) adapter.deleteItem(adapter.getItemToDelete());
                            else adapter.cancelItemDeletion();
                        }
                    });
                }
            };

            loadProducts();
            setupRecycler();
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iService = null;
            Log.d(TAG, "Service disconnected");
        }
    };

    private void setupRecycler() {
        recycler = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(this, products);
        recycler.setAdapter(adapter);

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
        adapter.setProductToAdd(p);
        try {
            iService.add(p, iCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts(){
        try {
            products = iService.getAll();
            Log.d(TAG, "Items loading complete");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(int pos){
        adapter.addItemToDelete(pos);
        try{
            iService.delete(adapter.getIdByPos(pos), iCallback);
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
