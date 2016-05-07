package com.irodyk.activityapp;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by i.rodyk on 4/6/16.
 */
public class Utils {

    public static <E> ArrayList<E> toList(E[] array){
        ArrayList<E> list = new ArrayList<>();
        Collections.addAll(list, array);
        return list;
    }
}
