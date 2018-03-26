package io.edgedev.appextractor;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.codemybrainsout.ratingdialog.RatingDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;

public class MainActivity extends AppCompatActivity implements AppExtractorFragment.FileSaver, SearchView.OnQueryTextListener {

    public static final int SAVE_APP_IN_BACKGROUND_LOADER = 100;
    public static final String APP_DIR_KEY = "app.dir.key";

    private static final String TAG = "MainActivity";
    //    private AdView mAdView;
//    private InterstitialAd mInterstitialAd;
    private int count = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

   /*     AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9297690518647609/8432678271");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();*/

        /*FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = AppExtractorFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }*/

        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .session(7)
                .threshold(3)
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        Log.e(TAG, "onFormSubmitted_Feedback: " + feedback);
                    }
                }).build();
        ratingDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

 /*   private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        // Resume the AdView.
//        mAdView.resume();
    }

    @Override
    public void onPause() {
        // Pause the AdView.
//        mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mAdView.destroy();

    }

    @Override
    public void saveFile(final String app, final View view, final String appName) {
        displayAd();
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Extracting");
        dialog.setCancelable(false);
        dialog.show();
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        final File dir = new File(sdcard.getAbsolutePath() + "/Apps Extracted/");
        // create this directory if not already created
        dir.mkdir();

        final AsyncTask task = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    // create the file in which we will write the contents
                    File directory = new File(dir, appName + ".apk");
                    FileInputStream inputStream = new FileInputStream(app);
                    FileOutputStream outputStream = new FileOutputStream(directory);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }

                    inputStream.close();
                    outputStream.close();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "saveToFile: ", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                dialog.dismiss();
                if ((boolean) o) {
                    make(new View(MainActivity.this), "Extracted to path " + dir.toString() + "/" + appName, LENGTH_LONG).show();
                } else {
                    make(new View(MainActivity.this), "Error", LENGTH_LONG).show();
                }
            }
        };
        task.execute();
    }

    void dosomething() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Extracting");
        dialog.setCancelable(false);
        dialog.show();
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        final File dir = new File(sdcard.getAbsolutePath() + "/Apps Extracted/");
        // create this directory if not already created
        dir.mkdir();
        Bundle saveApp = new Bundle();
        //saveApp.putString(APP_DIR_KEY, dir);
    }

    @Override
    public void rateApp() {
        displayAd();
        RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .threshold(3)
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        Log.e(TAG, "onFormSubmitted_Feedback: " + feedback);
                    }
                }).build();
        ratingDialog.show();
    }

    void displayAd() {
        count++;
        /*if ((count % 2) == 0) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                requestNewInterstitial();
            }
        }*/

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


//    class saveInBackgroundAsyncTask extends AsyncTask<String, Void, Boolean> {
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            try {
//                // create the file in which we will write the contents
//                File directory = new File(dir, appName + ".apk");
//                FileInputStream inputStream = new FileInputStream(app);
//                FileOutputStream outputStream = new FileOutputStream(directory);
//
//                byte[] buf = new byte[1024];
//                int len;
//                while ((len = inputStream.read(buf)) > 0) {
//                    outputStream.write(buf, 0, len);
//                }
//
//                inputStream.close();
//                outputStream.close();
//                return true;
//            } catch (Exception e) {
//                Log.e(TAG, "saveToFile: ", e);
//                return false;
//            }
//        }
//
//
//        @Override
//        protected void onPostExecute(Boolean bool) {
//            super.onPostExecute(bool);
//            //dialog.dismiss();
//            if (bool) {
//                make(mBinding.getRoot(), "Extracted to path " + dir.toString() + "/" + appName, LENGTH_LONG).show();
//            } else {
//                make(mBinding.getRoot(), "Error", LENGTH_LONG).show();
//            }
//        }
//    }
}
