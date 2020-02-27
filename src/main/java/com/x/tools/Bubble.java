package com.x.tools;

import java.util.Comparator;
import java.util.List;

public class Bubble {

    /**
     * 由小到大排列
     * @param list
     * @param comp
     * @return
     */
    public static <T> List<T> sort(List<T> list, Comparator<T> comp) {
        int len = list.size();
        for (int i = 0; i < len - 1; i++) {// 控制趟数
            for (int j = 0; j < len - i - 1; j++) {
                if (comp.compare(list.get(j), list.get(j + 1)) > 0) {
                    list.set(j, list.set(j+1, list.get(j)));
                }
            }
        }
        return list;
    }
    
}
