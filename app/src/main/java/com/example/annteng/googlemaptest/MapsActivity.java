package com.example.annteng.googlemaptest;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MapsActivity extends FragmentActivity {

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker mMarker = null;
    boolean mMarkerExists = false;
    Location lastLocation = null;
    private String CLIENT_TAG = "map_client";
    boolean keepSend = false;
    boolean hasData = false;
    String locationStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        setUpMapIfNeeded();
        textResponse = (TextView)findViewById(R.id.client_info_text);



    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onClientButtonClick(View view)
    {
        Log.d(CLIENT_TAG, "onClientButtonClick");

      /*  mNsdManager = (NsdManager)this.getApplicationContext().getSystemService(Context.NSD_SERVICE);
        initializeDiscoveryListener();

        initializeResolveListener();
        mNsdManager.discoverServices(
                "_http._tcp.", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);*/

        Log.d(CLIENT_TAG, "execute");
        MyClientTask myClientTask = new MyClientTask(
                "192.168.0.112",
                8080);
        keepSend = true;
        myClientTask.execute();

     //    myClientTask.SendMsgToServer("LOL!");
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
      //  mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        GetLocation();

    }

    private void GetLocation()
    {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
               // makeUseOfNewLocation(location);
               // if (mMarkerExists == true) {
                    if (isBetterLocation(location, lastLocation)) {
                        Log.d("googleMapTest", "Better location");
                        if (mMarker != null) {
                            mMarker.remove();
                            mMarkerExists = false;
                        }
                        sendEmail(location);
                        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMarker = mMap.addMarker(new MarkerOptions().position(newLatLng).title("mareker_Ann"));
                        mMarkerExists = true;
                        lastLocation = location;
                       // LatLng coordinate = new LatLng(lat, lng);
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(newLatLng, 15);
                        mMap.animateCamera(yourLocation);
                       /* CameraUpdate center=CameraUpdateFactory.newLatLng(newLatLng);

                        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

                        mMap.moveCamera(center);
                        mMap.animateCamera(zoom);*/
                    }
                    else
                    {
                        Log.d("googleMapTest", "Nor a Better location");
                    }
               // }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

            protected boolean isBetterLocation(Location location, Location currentBestLocation) {
                if (currentBestLocation == null) {
                    // A new location is always better than no location
                    Log.d("googleMapTest", "currentBestLocation == null");
                    return true;
                }

                // Check whether the new location fix is newer or older
                long timeDelta = location.getTime() - currentBestLocation.getTime();
                Log.d("googleMapTest", "timeDelta = " + timeDelta);
                boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
                boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
                boolean isNewer = timeDelta > 0;

                // If it's been more than two minutes since the current location, use the new location
                // because the user has likely moved
                if (isSignificantlyNewer) {
                    Log.d("googleMapTest", "isSignificantlyNewer");
                    return true;
                    // If the new location is more than two minutes older, it must be worse
                } else if (isSignificantlyOlder) {
                    Log.d("googleMapTest", "isSignificantlyOlder");
                    return false;
                }

                // Check whether the new location fix is more or less accurate
                int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
                Log.d("googleMapTest", "accuracyDelta = " + accuracyDelta);
                boolean isLessAccurate = accuracyDelta > 0;
                boolean isMoreAccurate = accuracyDelta < 0;
                boolean isSignificantlyLessAccurate = accuracyDelta > 200;

                // Check if the old and new location are from the same provider
                boolean isFromSameProvider = isSameProvider(location.getProvider(),
                        currentBestLocation.getProvider());

                // Determine location quality using a combination of timeliness and accuracy
                if (isMoreAccurate) {
                    Log.d("googleMapTest", "isMoreAccurate");
                    return true;
                } else if (isNewer && !isLessAccurate) {
                    Log.d("googleMapTest", "isNewer && !isLessAccurate");
                    return true;
                } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                    Log.d("googleMapTest", "isNewer && !isSignificantlyLessAccurate && isFromSameProvider");
                    return true;
                }
                return false;
            }

            /** Checks whether two providers are the same */
            private boolean isSameProvider(String provider1, String provider2) {
                if (provider1 == null) {
                    return provider2 == null;
                }
                return provider1.equals(provider2);
            }

            private void sendEmail(Location location)
            {
                Log.d("googleMapTest","Send Mail!");
                while (hasData) {};

                locationStr = location.toString();
                hasData = true;

                /*Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"annteng@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Hey Ann, update my location to you!");
                i.putExtra(Intent.EXTRA_TEXT   , "my new location is : " + location.toString());
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                   // Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }*/
            }
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
     //   locationManager.requestLocationUpdates()

    }


    private DiscoveryListener mDiscoveryListener;
    private String mServiceName = "AnnChat";
    private NsdManager mNsdManager;
    private ResolveListener mResolveListener;
    private NsdServiceInfo mService;
    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(CLIENT_TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(CLIENT_TAG, "Service discovery success " + service);
                if (!service.getServiceType().equals("_http._tcp.")) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(CLIENT_TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(CLIENT_TAG, "Same machine: " + mServiceName);
                    mNsdManager.resolveService(service, mResolveListener);
                } else if (service.getServiceName().contains(mServiceName)){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(CLIENT_TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(CLIENT_TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(CLIENT_TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(CLIENT_TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(CLIENT_TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(CLIENT_TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(CLIENT_TAG, "Same service name.");
                    return;
                }

                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
            }
        };
    }

    TextView textResponse;
    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        OutputStream outputStream;// = socket.getOutputStream();

        MyClientTask(String addr, int port){
            Log.d(CLIENT_TAG, "MyClientTask");
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                Log.d(CLIENT_TAG, "before new task");
                socket = new Socket(dstAddress, dstPort);

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

               // int bytesRead;
                //InputStream inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                Log.d(CLIENT_TAG, "InputStream inputStream = socket.getInputStream();");
    /*
     * notice:
     * inputStream.read() will block if no data return
     */

                Log.d(CLIENT_TAG, "after read data");
                String str = "Hi Server! how are you!";


                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
                // WHERE YOU ISSUE THE COMMANDS
                out.println("Hey Server!");
                out.println();


                while (keepSend)
                {
                    if (hasData)
                    {
                        Log.d(CLIENT_TAG, "send data");
                        out.println(locationStr);
                        hasData = false;
                    }
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            Log.d(CLIENT_TAG, "finish");
            return null;
        }

        public void SendMsgToServer(String str)
        {
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(str);
            printStream.close();
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }
}
