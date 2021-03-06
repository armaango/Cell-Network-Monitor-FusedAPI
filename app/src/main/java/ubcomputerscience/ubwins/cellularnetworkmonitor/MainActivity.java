/**
 *   UbWins Lab
 *   University at Buffalo, The State University of New York.
 *
 */

package ubcomputerscience.ubwins.cellularnetworkmonitor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , ActivityCompat.OnRequestPermissionsResultCallback {
    protected GoogleApiClient mGoogleApiClient;

    public final String TAG = "[CELNETMON-ACTIVITY]";
    Button track;
    DBstore dbStore;
    CellularDataRecorder cdr;
    LocationFinder locationFinder;
    Location location;
    private View mLayout;
    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_STORAGE = 1;
    private static final int REQUEST_PHONE = 2;

    //Exports SQLiteDB to CSV file in Phone Storage
    public void exportToCSV()
    {
        //code for storage permission goes here
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.

            requestStoragePermission();

        } else {

            // Storage permission is already available.
            Log.v(TAG,
                    "Storage permission has already been granted.");
            //carry on
        }
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state))
        {
            Toast.makeText(this, "MEDIA MOUNT ERROR!", Toast.LENGTH_LONG).show();
        }
        else
        {
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
                Log.v(TAG, "Directory made");
            }

            File file = new File(exportDir, "CellularData.csv") ;
            PrintWriter printWriter = null;
            try
            {
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));
                DBHandler dbHandler = new DBHandler(getApplicationContext());
                SQLiteDatabase sqLiteDatabase = dbHandler.getReadableDatabase();
                Cursor curCSV = sqLiteDatabase.rawQuery("select * from cellRecords", null);
                printWriter.println("Latitude,Longitude,locality,city,state,country,NETWORK_PROVIDER,TIMESTAMP,NETWORK_TYPE,NETWORK_STATE,NETWORK_RSSI,DATA_STATE,DATA_ACTIVITY");
                while(curCSV.moveToNext())
                {
                    Double latitude = curCSV.getDouble(curCSV.getColumnIndex("LAT"));
                    Double longitude = curCSV.getDouble(curCSV.getColumnIndex("LONG"));
                    String networkProvider = curCSV.getString(curCSV.getColumnIndex("NETWORK_PROVIDER"));
                    String locality = curCSV.getString(curCSV.getColumnIndex("LOCALITY"));
                    String city = curCSV.getString(curCSV.getColumnIndex("CITY"));
                    String stateName = curCSV.getString(curCSV.getColumnIndex("STATE"));
                    String country = curCSV.getString(curCSV.getColumnIndex("COUNTRY"));

                    String timeStamp = curCSV.getString(curCSV.getColumnIndex("TIMESTAMP"));
                    String networkType = curCSV.getString(curCSV.getColumnIndex("NETWORK_TYPE"));
                    String networkState = curCSV.getString(curCSV.getColumnIndex("NETWORK_STATE"));
                    String networkRSSI = curCSV.getString(curCSV.getColumnIndex("NETWORK_RSSI"));
                    String dataState = curCSV.getString(curCSV.getColumnIndex("DATA_STATE"));
                    String dataActivity = curCSV.getString(curCSV.getColumnIndex("DATA_ACTIVITY"));
                    String record = latitude + "," + longitude + "," + locality + "," + city + "," + stateName + "," + country + "," + networkProvider + "," + timeStamp + "," + networkType + "," + networkState + "," + networkRSSI+ "," + dataState + "," + dataActivity;
                    Log.v(TAG, "attempting to write to file");
                    printWriter.println(record);
                    Log.v(TAG, "data written to file");
                }
                curCSV.close();
                sqLiteDatabase.close();
            }

            catch(Exception exc)
            {
                exc.printStackTrace();
                Toast.makeText(this, "ERROR!", Toast.LENGTH_LONG).show();
            }
            finally
            {
                if(printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            Toast.makeText(this, "DB Exported to CSV file!", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteDB()
    {
        boolean result = this.deleteDatabase("mainTuple");
        if (result==true)
        {
            Toast.makeText(this, "DB Deleted!", Toast.LENGTH_LONG).show();
        }
    }
    private void exportDB()
    {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source ;
        FileChannel destination ;
        String currentDBPath = "/data/" + "ubwins.ubcomputerscience.netanalyzer" + "/databases/" + "mainTuple";
        String backupDBPath = "mainTuple";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if (currentDB.exists())
        {
            try
            {
                source = new FileInputStream(currentDB).getChannel();
                destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //building Google API Client
        buildGoogleApiClient();
        Log.v(TAG,"NetAnalyzer Service Started");
        mLayout = findViewById(R.id.activity_main_layout);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        Boolean started = sharedPref.getBoolean("Started", false);

        if(started)
        {
            Button button = (Button) findViewById(R.id.button);
            button.setEnabled(false);
        }

        track = (Button) findViewById(R.id.button1);
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg1)
            {
                deleteDB();
            }
        });

        track = (Button) findViewById(R.id.button2);
        track.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg1)
            {
                exportDB();
            }
        });

        track = (Button) findViewById(R.id.button3);
        track.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg1)
            {
                exportToCSV();
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.btnShowLocation);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Log.v(TAG, "inside onClick");
                //write code for locations Permission here
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // LOCATION permission has not been granted.

                    requestLocationPermission();

                } else {

                    // LOCATION permission is already available.
                    Log.v(TAG,
                            "LOCATION permission has already been granted.");
                    //carry on
                }



                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                locationFinder = new LocationFinder(getApplicationContext());
                if(location!=null)
                    {
                        //code for phone state goes here
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // phone permission has not been granted.

                            requestPhonePermission();

                        } else {

                            // phone permissions is already available.
                            Log.v(TAG, "Phone permission has already been granted.");
                            //carry on
                        }

                        final TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        cdr = new CellularDataRecorder();
                        Log.v(TAG, "Calling getLocalTimeStamp and getCellularInfo");
                        String timeStamp = cdr.getLocalTimeStamp();
                        String cellularInfo = cdr.getCellularInfo(telephonyManager);
                        String dataActivity = cdr.getCurrentDataActivity(telephonyManager);
                        String dataState = cdr.getCurrentDataState(telephonyManager);

                        Log.v(TAG, "TIME STAMP: " + timeStamp);
                        Log.v(TAG, "CELLULAR INFO: " + cellularInfo);
                        Log.v(TAG, "DATA ACTIVITY: "+dataActivity);
                        Log.v(TAG, "DATA STATE: " +dataState);
                        dbStore = new DBstore(MainActivity.this);
                        dbStore.insertIntoDB(location, timeStamp, cellularInfo,dataActivity,dataState);

                        locationFinder.addressResolver(location);
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String countryCode = locationFinder.getCountryCode();
                        String adminArea = locationFinder.getAdminArea();
                        String locality = locationFinder.getLocality();
                        String throughFare = locationFinder.getThroughFare();

                        Toast.makeText(getApplicationContext(), "You are at - " + throughFare + ", " + locality + ", " + adminArea + ", " + countryCode + "\n" +
                                "Latitude: " + latitude + "\nLongitude: " + longitude, Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        Log.v(TAG, "Waiting to get location from NETWORK_PROVIDER");
                        Toast.makeText(getApplicationContext(), "Waiting to get location from the network", Toast.LENGTH_LONG).show();
                    }
            }



        });
    }

    private void requestLocationPermission() {
        Log.i(TAG, "LOCATION permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(location_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        // END_INCLUDE(location_permission_request)
    }
    private void requestPhonePermission() {
        Log.i(TAG, "Phone permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(phone_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying Phone permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_phone_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    REQUEST_PHONE);
                        }
                    })
                    .show();
        } else {

            // Phone state permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_PHONE);
        }
        // END_INCLUDE(phone_permission_request)
    }
    private void requestStoragePermission() {
        Log.i(TAG, "STORAGE permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(storage_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE);
                        }
                    })
                    .show();
        } else {

            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        }
        // END_INCLUDE(location_permission_request)
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for location permission.
            Log.i(TAG, "Received response for Location permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(TAG, "Location permission has now been granted.");
                Snackbar.make(mLayout, R.string.permission_available_location,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Location permission was NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else if (requestCode == REQUEST_STORAGE) {
            Log.i(TAG, "Received response for storage permissions request.");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Storage permission has been granted
                Log.i(TAG, "Storage permission has now been granted.");
                Snackbar.make(mLayout, R.string.permission_available_storage,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Storage permission was NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
        }
            else if (requestCode == REQUEST_PHONE) {
                Log.i(TAG, "Received response for phone permissions request.");
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Phone permission has been granted
                    Log.i(TAG, "Phone permission has now been granted.");
                    Snackbar.make(mLayout, R.string.permission_available_phone,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Phone permission was NOT granted.");
                    Snackbar.make(mLayout, R.string.permissions_not_granted,
                            Snackbar.LENGTH_SHORT).show();

                }

            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

    private String getIMEI()
    {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    private String getService()
    {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    private String getModel()

    {
        return android.os.Build.MANUFACTURER+":"+android.os.Build.MODEL;
    }

    private String getOS()

    {
        return android.os.Build.VERSION.RELEASE;
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line ;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public boolean isConnected()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


    public String POST(String url)
    {
        InputStream inputStream ;
        String result = "";
            try {

                String IMEI = getIMEI();
                String service = getService();
                String modelMake = getModel();
                String androidVersion = getOS();

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("IMEI", IMEI);
                jsonObject.accumulate("SERVICE", service);
                jsonObject.accumulate("MDOEL", modelMake);
                jsonObject.accumulate("OS_VERSION", androidVersion);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();
                // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                // ObjectMapper mapper = new ObjectMapper();
                // json = mapper.writeValueAsString(person);

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content   
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            }
            catch (Exception e)
            {
                Log.d("InputStream", e.getLocalizedMessage());
            }

        // 11. return result
        return result;
        }

    public void onRegisterClicked(View view)
    {
        boolean isConnected = isConnected();
        if(isConnected)
        {
            Log.v(TAG, "isConnected = TRUE");
            //TODO
            new HttpAsyncTask().execute("YOUR_URL_HERE");
        }
        else
        {
            Log.v(TAG, "isConnected = FALSE");
            Toast.makeText(getBaseContext(), "Device has. No Internet Connectivity! Please check your Network Connection and try again", Toast.LENGTH_LONG).show();
        }

    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            Log.v(TAG, "inside AsyncTask");
            return POST(urls[0]);
        }
        @Override
        protected void onPostExecute(String result)
        {
            Toast.makeText(getBaseContext(), "Device Registered!", Toast.LENGTH_LONG).show();
        }
    }

}
