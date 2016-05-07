package com.irodyk.iservice;

import com.irodyk.product.Product;

oneway interface ICallback {

    void addedItemId(long id);
    void isItemDeleted(boolean isDeleted);
}
