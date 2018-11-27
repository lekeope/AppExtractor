package io.edgedev.appextractor

import android.graphics.drawable.Drawable

/**
 * Created by OPEYEMI OLORUNLEKE on 9/4/2017.
 */

class AppModel(val id: Int, val appName: String,
               val appPackageName: String,
               val drawable: Drawable,
               val filePathName: String,// to Convert to File use File(getFilePathName)
               val appVersion : String
)
