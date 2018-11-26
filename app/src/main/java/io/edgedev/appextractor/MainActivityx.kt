package io.edgedev.appextractor

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import com.codemybrainsout.ratingdialog.RatingDialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class MainActivityx : AppCompatActivity(), ClickedApp, SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    private val ALPHABETICAL_COMPARATOR = Comparator<AppModel> { a, b -> a.appName.compareTo(b.appName) }

    override fun onQueryTextChange(query: String?): Boolean {
        val filteredModelList: List<AppModel>
        if (query == null)
            filteredModelList = filter(Singleton.list, "")
        else
            filteredModelList = filter(Singleton.list, query)
        appsAdapter.replaceAll(filteredModelList)
        recyclerView?.scrollToPosition(0)
        return true
    }

    private fun filter(models: List<AppModel>, query: String): List<AppModel> {
        val lowerCaseQuery = query.toLowerCase()
        val filteredModelList = ArrayList<AppModel>()
        for (model in models) {
            val text = model.appName.toLowerCase()
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model)
            }
        }
        return filteredModelList
    }

    val appsAdapter = AppsAdapter(this, ALPHABETICAL_COMPARATOR)
    var recyclerView: RecyclerView? = null
    val EXTERNAL_STORAGE_PERMISSION_CONSTANT = 5
    val STORAGE_PERMISSION_REQUEST_CODE = 10
    val LOAD_APPS_ASYNC_ID = 100
    val EXTRACT_APPS_ASYNC_ID = 200
    var coordinator_layout: CoordinatorLayout? = null
    var indeterminateSnackBar: Snackbar? = null

    var laodAppsDialog: AlertDialog? = null

    var appNameTinyDb: TinyDB? = null
    var storagePermissionStatus: TinyDB? = null

    val APP_NAME_TINY_DB_KEY = "app.name.tinydb.Key"
    val STORAGE_PERMISSION_KEY = "storage.permision.key"


    companion object {
        val TAG = "MainActivity"
        val APP_NAME_KEY = "App_Name.Key"
        val FILE_PATH_NAME_KEY = "App_File_Path_Name.Key"
        val SAVE_FILE_LOCATION = "/Apk Extractor/"
    }


    override fun onClickInfo(position: Int) {
        val app = appsAdapter.mSortedList[position]
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", app.appPackageName, null)
        intent.data = uri
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onClickDownload(position: Int) {
        val app = appsAdapter.mSortedList[position]
        appNameTinyDb!!.putString(APP_NAME_TINY_DB_KEY, app.appName)
        val bundle = Bundle()
        bundle.putString(APP_NAME_KEY, app.appName)
        bundle.putString(FILE_PATH_NAME_KEY, app.filePathName)
        extractApp(bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)
        appNameTinyDb = TinyDB(this)
        storagePermissionStatus = TinyDB(this)

        val searchView: SearchView = findViewById(R.id.search_apps)
        searchView.setOnQueryTextListener(this)

        coordinator_layout = findViewById(R.id.coordinator_layout)
        Log.d("MainActivityX", toolbar.toString())
        Log.d("MainActivityX", "OnCreate")
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = appsAdapter
        refreshRecyclerView()

        RatingDialog.Builder(this)
                .session(5)
                .threshold(4f)
                .build()
                .show()

        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.extracting_app_progress_layout)
        laodAppsDialog = builder.create()
        laodAppsDialog!!.setCancelable(false)

        setLoadAppsDialog(true)
        showIndeterminateSnackBar(this, appNameTinyDb!!.getString(APP_NAME_TINY_DB_KEY))

        grantStoragePermission()
        supportLoaderManager.initLoader(LOAD_APPS_ASYNC_ID, null, LoadAppsCallback(this))
        supportLoaderManager.initLoader(EXTRACT_APPS_ASYNC_ID, null, ExtractAppsCallback(this))
    }

    private fun setLoadAppsDialog(show: Boolean) {
        if (show)
            laodAppsDialog!!.show()
        else
            laodAppsDialog!!.dismiss()
    }

    private fun refreshRecyclerView() {
        appsAdapter.addApps(Singleton.list)
        appsAdapter.notifyDataSetChanged()
    }

    fun reloadApps() {
        val loader: Loader<Long>? = supportLoaderManager.getLoader<Long>(LOAD_APPS_ASYNC_ID)
        if (loader == null)
            supportLoaderManager.initLoader(LOAD_APPS_ASYNC_ID, null, LoadAppsCallback(this))
        else
            supportLoaderManager.restartLoader(LOAD_APPS_ASYNC_ID, null, LoadAppsCallback(this))
        setLoadAppsDialog(true)
        appsAdapter.mSortedList.clear()
    }

    fun extractApp(bundle: Bundle) {
        grantStoragePermission()
        val loader: Loader<Long>? = supportLoaderManager.getLoader<Long>(EXTRACT_APPS_ASYNC_ID)
        if (loader == null)
            supportLoaderManager.initLoader(EXTRACT_APPS_ASYNC_ID, bundle, ExtractAppsCallback(this))
        else
            supportLoaderManager.restartLoader(EXTRACT_APPS_ASYNC_ID, bundle, ExtractAppsCallback(this))

        showIndeterminateSnackBar(this, appNameTinyDb!!.getString(APP_NAME_TINY_DB_KEY))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.rate_app -> rateApp()
            R.id.reload_apps -> reloadApps()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun grantStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(WRITE_EXTERNAL_STORAGE),
                        EXTERNAL_STORAGE_PERMISSION_CONSTANT)
                // this is where you can explain why you need the permission more or less like the first time
            } else if (storagePermissionStatus!!.getBoolean(STORAGE_PERMISSION_KEY)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Need Storage Permission")
                builder.setMessage("This app needs storage permission.")
                builder.setPositiveButton("Grant") { dialog, which ->
                    dialog.cancel()
                    launchSettings(STORAGE_PERMISSION_REQUEST_CODE, "Go to Permissions to Grant \"Storage\"")
                }
                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
                builder.show()
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), EXTERNAL_STORAGE_PERMISSION_CONSTANT)
            }
            storagePermissionStatus!!.putBoolean(STORAGE_PERMISSION_KEY, true)
        } else {
        }
    }

    private fun launchSettings(REQUEST_CODE: Int, message: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_CODE)
        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
    }

    fun rateApp() {
        RatingDialog.Builder(this)
                .threshold(4f)
                .build()
                .show()
    }

    fun showToast(app_name: String) {
        if (app_name.trim().length < 1) return
        Toast.makeText(this,
                "Successfully Extracted \"$app_name\" to $SAVE_FILE_LOCATION", Toast.LENGTH_LONG).show()
        vibrate()
    }

    class LoadAppsAsyncTaskLoader(context: Context,
                                  val packageManager: PackageManager)
        : AsyncTaskLoader<Boolean>(context) {

        override fun onStartLoading() {
            super.onStartLoading()
            forceLoad()
        }


        override fun loadInBackground(): Boolean {
            Log.d(TAG, "Inside Load In Background")
            val startupIntent = Intent(ACTION_MAIN)
            startupIntent.addCategory(CATEGORY_LAUNCHER)
            val appModels: MutableList<AppModel> = ArrayList<AppModel>()
            val activities: List<ResolveInfo> = packageManager.queryIntentActivities(startupIntent, 0)
            var count = 0
            for (info in activities) {
                if (isLoadInBackgroundCanceled) return false
                val appModel = AppModel(
                        count++
                        , info.loadLabel(packageManager).toString()
                        , info.activityInfo.packageName
                        , info.loadIcon(packageManager)
                        , info.activityInfo.applicationInfo.publicSourceDir
                )
                appModels.add(appModel)
                Log.d(TAG, "Count = $count")
            }
            Collections.sort(appModels, object : kotlin.Comparator<AppModel> {
                override fun compare(app1: AppModel?, app2: AppModel?): Int {
                    return String.CASE_INSENSITIVE_ORDER.compare(app1?.appName, app2?.appName)
                }
            })
            Singleton.list = appModels
            return true
        }
    }

    inner class LoadAppsCallback(val context: Context) : LoaderCallbacks<Boolean> {

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Boolean> {
            return LoadAppsAsyncTaskLoader(context, packageManager)
        }

        override fun onLoadFinished(loader: Loader<Boolean>, data: Boolean?) {
            refreshRecyclerView()
            setLoadAppsDialog(false)
            Log.d(TAG, "onLoad - APPS - Finished")
        }

        override fun onLoaderReset(loader: Loader<Boolean>) {}
    }

    fun showIndeterminateSnackBar(context: Context, app_name: String) {
        if (app_name.trim().length < 1) return
        indeterminateSnackBar = Snackbar.make(coordinator_layout!!, "Keep Calm. Extracting \"$app_name\"", Snackbar.LENGTH_INDEFINITE)
/*indeterminateSnackBar!!.setAction(android.R.string.cancel, object : View.OnClickListener {
    override fun onClick(v: View?) {
        // cancel app extraction
    }
})*/
        val snack_view = indeterminateSnackBar!!.view as Snackbar.SnackbarLayout
        snack_view.addView(ProgressBar(context))
        indeterminateSnackBar!!.show()
    }

    private fun dismissIndeterminateSnackBar() {
        indeterminateSnackBar?.dismiss()
    }

    class ExtractAppAsyncTaskLoader(context: Context, val bundle: Bundle?) : AsyncTaskLoader<Boolean>(context) {
        override fun onStartLoading() {
            super.onStartLoading()
            forceLoad()
        }

        override fun loadInBackground(): Boolean {
            val appName = bundle?.getString(APP_NAME_KEY)
            val filePathName = bundle?.getString(FILE_PATH_NAME_KEY)
            if (appName == null || filePathName == null) {
                Log.d(TAG, "App Name = $appName, FilePathName = $filePathName")
                return false
            }

            val appFile = File(filePathName)

            val sdcard = Environment.getExternalStorageDirectory()
            val dir = File(sdcard.absolutePath + SAVE_FILE_LOCATION)
            dir.mkdir()
            try {
                val directory = File(dir, appName + ".apk")
                val inputStream = FileInputStream(appFile)
                val outputStream = FileOutputStream(directory)

                val buf = ByteArray(1024)
                var len = 1
                while (len > 0) {
                    len = inputStream.read(buf)
                    outputStream.write(buf, 0, len)
                    if (isLoadInBackgroundCanceled) {
                        inputStream.close()
                        outputStream.close()
                        //the delete the file
                        return true
                    }
                }
                inputStream.close()
                outputStream.close()
                return true
            } catch (e: Exception) {
                Log.e(TAG, "saveToFile: ", e)
                return false
            }
        }
    }

    inner class ExtractAppsCallback(val context: Context) : LoaderCallbacks<Boolean> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Boolean> {
            return ExtractAppAsyncTaskLoader(context, args)
        }

        override fun onLoadFinished(loader: Loader<Boolean>, data: Boolean?) {
            dismissIndeterminateSnackBar()
            showToast(appNameTinyDb!!.getString(APP_NAME_TINY_DB_KEY))
            appNameTinyDb!!.putString(APP_NAME_TINY_DB_KEY, "")
            Log.d(TAG, "onLoad - EXTRACTION - Finished")
        }

        override fun onLoaderReset(loader: Loader<Boolean>) {}
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(VibrationEffect.createOneShot(100, 10))
        else
            vibrator.vibrate(100)
    }
}

/*
*
override fun onLoadFinished(loader: Loader<Boolean>?, data: Boolean?) {
dismissIndeterminateSnackBar()
vibrate()
showAppExtractedFinishToast()
Log.d(TAG, "onLoad - EXTRACTION - Finished")
}

override fun onLoaderReset(loader: Loader<Boolean>?) {}
}

fun showAppExtractedFinishToast(){
if (!shouldShowAppExtractedFinishToast) return
Toast.makeText(this, "Successfully Extracted \"$APP_NAME_4_ROTATION\"", Toast.LENGTH_LONG).show()
}
* */