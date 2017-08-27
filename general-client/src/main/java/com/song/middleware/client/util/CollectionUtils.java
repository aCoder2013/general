package com.song.middleware.client.util;

import java.util.Collection;

/**
 * Created by song on 2017/8/6.
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

}
