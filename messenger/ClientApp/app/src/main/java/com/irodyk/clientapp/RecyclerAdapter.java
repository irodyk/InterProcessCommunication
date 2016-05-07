package com.irodyk.clientapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.irodyk.product.Product;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by i.rodyk on 5/6/16.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

    private Context ctx;
    //source for adapter
    private List<Product> list;
    //position of items to remove (our service is asynchronous. Can be multiple removal at time)
    private Queue<Integer> removalQueue;
    //Products to add (our service is asynchronous.
    // fow any case can slow down the operation we should hold all the items for addition until return true)
    private Queue<Product> additionQueue;

    public RecyclerAdapter(Context ctx, List<Product> list) {
        this.ctx = ctx;
        this.list = list;
        removalQueue = new PriorityQueue<>();
        additionQueue = new PriorityQueue<>();
    }

    public void addItemToDelete(int pos) {
        removalQueue.add(pos);
    }

    public int getItemToDelete() {
        return removalQueue.peek();
    }

    public void cancelItemDeletion() {
        removalQueue.remove();
    }

    public void deleteItem(int pos){
        list.remove(pos);
        removalQueue.remove();
        notifyItemRemoved(pos);
    }

    public Product getProductToAdd(){
        return additionQueue.peek();
    }

    public void setProductToAdd(Product product){
        additionQueue.add(product);
    }

    public void cancelItemInsertion(){
        additionQueue.remove();
    }

    public void addItem(Product product) {
        list.add(product);
        additionQueue.remove();
        notifyItemInserted(list.size()-1);
    }

    public long getIdByPos(int pos){
        return list.get(pos).getId();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.recycler_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int pos) {
        holder.name.setText(list.get(pos).getName());
        String price = ctx.getString(R.string.price) + list.get(pos).getPrice();
        holder.price.setText(price);
        String country = ctx.getString(R.string.country) + list.get(pos).getCountry();
        holder.country.setText(country);
        String delivery = ctx.getString(R.string.delivery) + list.get(pos).getDelivery();
        holder.delivery.setText(delivery);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        private TextView name;
        private TextView price;
        private TextView country;
        private TextView delivery;

        public Holder(View item) {
            super(item);
            this.name = (TextView)item.findViewById(R.id.name);
            this.price = (TextView)item.findViewById(R.id.price);
            this.country = (TextView)item.findViewById(R.id.country);
            this.delivery = (TextView)item.findViewById(R.id.delivery);
        }
    }
}