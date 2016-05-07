package com.irodyk.iservice;

import com.irodyk.product.Product;
import com.irodyk.iservice.ICallback;

interface IService {

    List<Product> getAll();
    oneway void delete(long id, ICallback callback);
    oneway void add(in Product product, ICallback callback);
}
