package io.edgedev.appextractor;

import android.graphics.drawable.Drawable;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/4/2017.
 */

public class AppModel {
    private final int id;
    private final String mAppName;
    private final String mAppPackageName;
    private final Drawable mDrawable;
    private final String mFilePathName; // to Convert to File use File(getFilePathName)

    public AppModel(int id, String appName,
                    String appPackageName,
                    Drawable drawable,
                    String filePathName) {

        this.id = id;
        mAppName = appName;
        mAppPackageName = appPackageName;
        mDrawable = drawable;
        mFilePathName = filePathName;
    }

    public String getAppName() {
        return mAppName;
    }

    public String getAppPackageName() {
        return mAppPackageName;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public String getFilePathName() {
        return mFilePathName;
    }

    public int getId() {
        return id;
    }
}
