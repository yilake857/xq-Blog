package com.utils;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理新旧数组
 * 得到 交集
 * 得到 新list 和 交集的差集（theSame）
 * 得到 旧list 和 交集的差集
 */
public class ArrayUtils {
    public ArrayUtils() {
    }

    public static <T> List<T>  getTheSameList(List<T> oldList, List<T> newList){
        List<T> theSameList = new ArrayList<>();
        for (T t :oldList) {
            if(newList.contains(t)) theSameList.add(t);
        }
        return theSameList;
    }

    public static <T> List<T> duplicateRemoval(List<T> list,List<T> removeList){
        list.addAll(removeList);
        List<T> collect = list.stream()
                .distinct()
                .collect(Collectors.toList());
        return collect;
    }

}
