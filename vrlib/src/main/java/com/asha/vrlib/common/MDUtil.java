package com.asha.vrlib.common;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

/**
 * Created by hzqiujiadi on 2016/11/1.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDUtil {
    public static Uri getDrawableUri(Context context, int resId){
        Resources resources = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }
}
