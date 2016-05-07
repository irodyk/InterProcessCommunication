package com.irodyk.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by i.rodyk on 3/30/16.
 */
public class Product implements Parcelable, Comparable<Product>{

    private long id;
    private String name;
    private String price;
    private String country;
    private String delivery;

    public Product(){}

    protected Product(Parcel in) {
        id = in.readLong();
        name = in.readString();
        price = in.readString();
        country = in.readString();
        delivery = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(country);
        dest.writeString(delivery);
    }

    @Override
    public int compareTo(@NonNull Product another) {
        return (int) (another.getId() - this.id);
    }
}
