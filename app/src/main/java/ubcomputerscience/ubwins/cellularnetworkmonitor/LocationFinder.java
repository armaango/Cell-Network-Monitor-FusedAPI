/**
 *   UbWins Lab
 *   University at Buffalo, The State University of New York.
 *
 */

package ubcomputerscience.ubwins.cellularnetworkmonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;


public class LocationFinder extends Service implements LocationListener
{
    private final Context mContext;
    public Geocoder geocoder;

    Location location;
    double latitude;
    double longitude;
    String locality;
    String adminArea;
    String countryCode;
    String throughFare;

    private static final long distance = 10;
    private static final long updateInterval = 30000;
    static final String TAG = "[CELNETMON-LOCFINDER]";
    protected LocationManager locationManager;

    public LocationFinder(Context context)
    {
        this.mContext = context;
        Log.v(TAG,"Context Constructor Fired.");
    }

    public void addressResolver(Location location)
    {

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        geocoder = new Geocoder(mContext);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        try {
            if (isConnected)
            {
                Log.v(TAG, "Attempting to resolve address");
                List<Address> locationList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (locationList.get(0).getLocality() != null)
                {
                    locality = locationList.get(0).getLocality();
                    Log.v(TAG, "[LOCALITY]" + locality);
                }
                if (locationList.get(0).getAdminArea() != null)
                {
                    adminArea = locationList.get(0).getAdminArea();
                    Log.v(TAG, "[ADMIN AREA]" + adminArea);
                }
                if (locationList.get(0).getCountryName() != null)
                {
                    countryCode = locationList.get(0).getCountryName();
                    Log.v(TAG, "[COUNTRY]" + countryCode);
                }
                if (locationList.get(0).getThoroughfare() != null)
                {
                    throughFare = locationList.get(0).getThoroughfare();
                    Log.v(TAG, "[THROUGH FARE]" + throughFare);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public String getLocality()
    {
        return locality;
    }
    public String getCountryCode()
    {
        return countryCode;
    }
    public String getAdminArea()
    {
        return adminArea;
    }
    public String getThroughFare()
    {
        return throughFare;
    }


    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

}
