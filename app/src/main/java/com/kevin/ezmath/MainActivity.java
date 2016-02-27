package com.kevin.ezmath;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.myscript.atk.maw.MathWidgetApi;
import com.kevin.ezmath.MyCertificate;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MathWidgetApi.OnConfigureListener,
        MathWidgetApi.OnRecognitionListener,
        MathWidgetApi.OnGestureListener,
        MathWidgetApi.OnWritingListener,
        MathWidgetApi.OnTimeoutListener,
        MathWidgetApi.OnSolvingListener,
        MathWidgetApi.OnUndoRedoListener{

    String currentUrl = "";
    private static final String key = "79XT2W-3WXQVGTJ48";
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "EZMath";
    /** Notify the user that a MSB resource is not found or invalid. */
    public static final int DIALOG_ERROR_RESSOURCE = 0;
    /** Notify the user that a MSB certificate is missing or invalid. */
    public static final int DIALOG_ERROR_CERTIFICATE = 1;
    /** Notify the user that maximum number of items has been reached. */
    public static final int DIALOG_ERROR_RECOTIMEOUT = 2;
    /** One error dialog at a time. */
    private boolean mErrorDlgDisplayed = false;

    private MathWidgetApi mWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mWidget = (MathWidgetApi) findViewById(R.id.myscript_maw);
        mWidget.setOnConfigureListener(this);
        mWidget.setOnRecognitionListener(this);
        mWidget.setOnGestureListener(this);
        mWidget.setOnWritingListener(this);
        mWidget.setOnTimeoutListener(this);

        final String[] resources = new String[]{"math-ak.res", "math-grm-maw.res"};

        // Prepare resources
        final String subfolder = "";
        final String resourcePath = new String(getFilesDir().getPath() + java.io.File.separator + subfolder);
        try {
            Log.e("path", getAssets().list("")[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleResourceHelper
                .copyResourcesFromAssets(getAssets(), subfolder /* from */, resourcePath /* to */, resources /* resource names */);

        // Configure math widget
        mWidget.setResourcesPath(resourcePath);
        mWidget.configure(this, resources, MyCertificate.getBytes(), MathWidgetApi.AdditionalGestures.DefaultGestures);
        new APIRequest().execute("\\int_{x}^{2}");
        Log.e("stuff", currentUrl);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConfigurationBegin()
    {
        if (DBG)
            Log.d(TAG, "Equation configuration begins");
    }

    @Override
    public void onConfigurationEnd(final boolean success)
    {
        if (DBG)
        {
            if (success)
                Log.d(TAG, "Equation configuration succeeded");
            else
                Log.d(TAG, "Equation configuration failed (" + mWidget.getErrorString() + ")");
        }

        if (DBG)
        {
            if (success)
                Log.d(TAG, "Equation configuration loaded successfully");
            else
                Log.d(
                        TAG,
                        "Equation configuration error - did you copy the equation resources to your SD card? ("
                                + mWidget.getErrorString() + ")");
        }

        // Notify user using dialog box
        if (!success)
            showErrorDlg(DIALOG_ERROR_RESSOURCE);
    }

    // ----------------------------------------------------------------------
    // Math Widget styleable library - equation recognition process

    //sends the math request to Wolfram

    @Override
    public void onRecognitionBegin()
    {
        if (DBG)
            Log.d(TAG, "Equation recognition begins");
    }

    @Override
    public void onRecognitionEnd()
    {
        if (DBG)
            Log.d(TAG, "Equation recognition end");
    }

    @Override
    public void onUsingAngleUnitChanged(final boolean used)
    {
        if (DBG)
            Log.d(TAG, "An angle unit usage has changed in the current computation and is currently " + used);
    }

    // ----------------------------------------------------------------------
    // Math Widget styleable library - equation recognition gestures

    @Override
    public void onEraseGesture(final boolean partial)
    {
        if (DBG)
            Log.d(TAG, "Erase gesture handled by current equation and is partial " + partial);
    }

    // ----------------------------------------------------------------------
    // Math Widget styleable library - ink edition

    @Override
    public void onWritingBegin()
    {
        if (DBG)
            Log.d(TAG, "Start writing");
    }

    @Override
    public void onWritingEnd()
    {
        if (DBG)
            Log.d(TAG, "End writing");
    }

    @Override
    public void onRecognitionTimeout()
    {
        showErrorDlg(DIALOG_ERROR_RECOTIMEOUT);
    }

    // ----------------------------------------------------------------------
    // Math Widget styleable library - Undo / Redo

    @Override
    public void onUndoRedoStateChanged()
    {
        if (DBG)
            Log.d(TAG, "End writing");
    }

    // ----------------------------------------------------------------------
    // Math Widget styleable library - Errors

    // showDialog is deprecated but still used to simplify the example.
    @SuppressWarnings("deprecation")
    private void showErrorDlg(final int id)
    {
        if (DBG)
            Log.i(TAG, "Show error dialog");
        if (!mErrorDlgDisplayed)
        {
            mErrorDlgDisplayed = true;
            showDialog(id);
        }
    }

    private class APIRequest extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String info = params[0];
            String url = "http://api.wolframalpha.com/v2/query?appid=" + key + "&input=";
            for (int i = 0; i < info.length(); i++) {
                if (!(info.charAt(i) >= 97 && info.charAt(i) <= 122)) {
                    url += '%' + Integer.toHexString(info.charAt(i) | 0x10000).substring(3).toUpperCase();
                } else {
                    url += info.charAt(i);
                }
            }
            url += "&format=image,plaintext";

            HttpURLConnection c = null;
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        currentUrl = sb.toString();
                }

            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }
}

