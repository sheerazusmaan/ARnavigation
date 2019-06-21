package na.myindooratlasapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import android.Manifest;

import android.content.pm.PackageManager;

import android.graphics.Bitmap;

import android.graphics.drawable.Drawable;

import android.os.Bundle;

import android.support.design.widget.*;

import android.support.v4.app.ActivityCompat;

import android.support.v4.app.FragmentActivity;

import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.Circle;

import com.google.android.gms.maps.model.CircleOptions;

import com.google.android.gms.maps.model.GroundOverlay;

import com.google.android.gms.maps.model.GroundOverlayOptions;

import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;

import com.google.android.gms.maps.model.PolylineOptions;

import com.indooratlas.android.sdk.IALocation;

import com.indooratlas.android.sdk.IALocationListener;

import com.indooratlas.android.sdk.IALocationManager;

import com.indooratlas.android.sdk.IALocationRequest;

import com.indooratlas.android.sdk.IAOrientationListener;

import com.indooratlas.android.sdk.IAOrientationRequest;

import com.indooratlas.android.sdk.IARegion;

import com.indooratlas.android.sdk.IARoute;

import com.indooratlas.android.sdk.IAWayfindingListener;

import com.indooratlas.android.sdk.IAWayfindingRequest;

//import com.indooratlas.android.sdk.examples.R;

//import com.indooratlas.android.sdk.examples.SdkExample;

import com.indooratlas.android.sdk.resources.IAFloorPlan;

import com.indooratlas.android.sdk.resources.IALatLng;

import com.indooratlas.android.sdk.resources.IALocationListenerSupport;


import java.io.IOException;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;


public class MapsActivity2 extends FragmentActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback {
    AutoCompleteTextView av;
    ImageButton ibutton;
    TextView textView;
    LinearLayoutCompat linearLayoutCompat;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 42;


    private static final String TAG = "IndoorAtlasExample";



    /* used to decide when bitmap should be downscaled */

    private static final int MAX_DIMENSION = 2048;


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.


    private Circle mCircle;

    private IARegion mOverlayFloorPlan = null;

    private GroundOverlay mGroundOverlay = null;

    private IALocationManager mIALocationManager;


    private boolean mCameraPositionNeedsUpdating = true; // update on first location

    private Marker mDestinationMarker;

    private Marker mHeadingMarker;

    private List<Polyline> mPolylines = new ArrayList<>();

    private IARoute mCurrentRoute;


    private IAWayfindingRequest mWayfindingDestination;

    private IAWayfindingListener mWayfindingListener = new IAWayfindingListener() {

        @Override

        public void onWayfindingUpdate(IARoute route) {

            mCurrentRoute = route;

            if (hasArrivedToDestination(route)) {

                // stop wayfinding

                showInfo("You're there!");

                mCurrentRoute = null;

                mWayfindingDestination = null;

                mIALocationManager.removeWayfindingUpdates();

            }

            updateRouteVisualization();

        }

    };


    private IAOrientationListener mOrientationListener = new IAOrientationListener() {

        @Override

        public void onHeadingChanged(long timestamp, double heading) {

            updateHeading(heading);

        }


        @Override

        public void onOrientationChange(long timestamp, double[] quaternion) {

            // we do not need full device orientation in this example, just the heading

        }

    };


    private int mFloor;


    private void showLocationCircle(LatLng center, double accuracyRadius) {

        if (mCircle == null) {
            showInfo("please enter the destination");
            // location can received before map is initialized, ignoring those updates

            if (mMap != null) {

                mCircle = mMap.addCircle(new CircleOptions()

                        .center(center)

                        .radius(accuracyRadius)

                        .fillColor(0x201681FB)

                        .strokeColor(0x500A78DD)

                        .zIndex(1.0f)

                        .visible(true)

                        .strokeWidth(5.0f));
                //change marker when navigation starts to marker2
                mHeadingMarker = mMap.addMarker(new MarkerOptions()

                        .position(center)

                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mymkr))

                        .anchor(0.1f, 0.1f)

                        .flat(true));

            }
            else
                showInfo("fail");

        } else {

            // move existing markers position to received location

            mCircle.setCenter(center);

            mHeadingMarker.setPosition(center);

            mCircle.setRadius(accuracyRadius);

        }

    }


    private void updateHeading(double heading) {

        if (mHeadingMarker != null) {

            mHeadingMarker.setRotation((float) heading);

        }

    }


    /**
     * Listener that handles location change events.
     */

    private IALocationListener mListener = new IALocationListenerSupport() {


        /**

         * Location changed, move marker and camera position.

         */

        @Override

        public void onLocationChanged(IALocation location) {


            Log.d(TAG, "new location received with coordinates: " + location.getLatitude()

                    + "," + location.getLongitude());


            if (mMap == null) {

                // location received before map is initialized, ignoring update here

                return;

            }


            final LatLng center = new LatLng(location.getLatitude(), location.getLongitude());


            final int newFloor = location.getFloorLevel();

            if (mFloor != newFloor) {

                updateRouteVisualization();

            }

            mFloor = newFloor;


            showLocationCircle(center, location.getAccuracy());


            // our camera position needs updating if location has significantly changed

            if (mCameraPositionNeedsUpdating) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 17.5f));

                mCameraPositionNeedsUpdating = false;

            }

        }

    };
    /**
     * Listener that changes overlay if needed
     */

    private IARegion.Listener mRegionListener = new IARegion.Listener() {

        @Override

        public void onEnterRegion(IARegion region) {

            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {

                Log.d(TAG, "enter floor plan " + region.getId());

                mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera

                if (mGroundOverlay != null) {

                    mGroundOverlay.remove();

                    mGroundOverlay = null;

                }

                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)

                fetchFloorPlanBitmap(region.getFloorPlan());

            }

        }


        @Override

        public void onExitRegion(IARegion region) {

        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        final Context context= this;
        linearLayoutCompat = (LinearLayoutCompat)findViewById(R.id.banner);
        linearLayoutCompat.setVisibility(View.INVISIBLE);
        textView =(TextView)findViewById(R.id.maptv2);
        // prevent the screen going to sleep while app is on foreground

        findViewById(android.R.id.content).setKeepScreenOn(true);
        SharedPreferences sharedPreferences= getSharedPreferences("run",MODE_PRIVATE);
        String status = sharedPreferences.getString("initialised","no");
        SharedPreferences.Editor editor=sharedPreferences.edit();
        String demo="";
        av= (AutoCompleteTextView)findViewById(R.id.autocomp);
        if(status.compareTo("no")==0) {
            final AssetManager assetManager = getAssets();
            try {
                demo = DHelper.storeToDB(assetManager.open(DHelper.file), context);
                editor.putString("initialised","done");
                editor.commit();
                Toast.makeText(getApplicationContext(),"successful db updated ",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        try {     SQLHelper sqlHelper = new SQLHelper(context);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (this,android.R.layout.select_dialog_item,sqlHelper.getarray());
            av.setAdapter(adapter);
            av.setThreshold(1);

        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();

        }

        final String de2=demo;
        ibutton=(ImageButton)findViewById(R.id.srchbutton);
        try{ibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=av.getText().toString().toUpperCase();
                SQLHelper sqlHelper = new SQLHelper(context);
                Destination destination= sqlHelper.getTarget(text);
                if(destination.getName().compareTo("")==0)
                {
                    Toast.makeText(getApplicationContext(),"Not Found ",Toast.LENGTH_SHORT).show();

                }
                else   //String s =sqlHelper.getdes(text);
                {   LatLng point = destination.getPoint();
                    int Floor = destination.getfloor();
                    if (mMap != null) {



                        mWayfindingDestination = new IAWayfindingRequest.Builder()

                                .withFloor(Floor)

                                .withLatitude(point.latitude)

                                .withLongitude(point.longitude)

                                .build();

                        mHeadingMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker2));

                        //set banner
                        linearLayoutCompat.setVisibility(View.VISIBLE);
                        textView.setText(text);
                        showInfo("please follow the line shown in map ");
                        mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);



                        if (mDestinationMarker == null) {

                            mDestinationMarker = mMap.addMarker(new MarkerOptions()

                                    .position(point)

                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        } else {

                            mDestinationMarker.setPosition(point);

                        }

                        Log.d(TAG, "Set destination: (" + mWayfindingDestination.getLatitude() + ", " +

                                mWayfindingDestination.getLongitude() + "), floor=" +

                                mWayfindingDestination.getFloor());

                    }

                }

                //serach location
            }
        });}
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
        //Intent i = new Intent(getApplicationContext(),MainActivity.class);
        //startActivity(i);
        // instantiate IALocationManager

        mIALocationManager = IALocationManager.create(this);


        // disable indoor-outdoor detection (assume we're indoors)

        mIALocationManager.lockIndoors(true);


        // Request GPS locations


        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);

            return;
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override

    protected void onDestroy() {

        super.onDestroy();


        // remember to clean up after ourselves

        mIALocationManager.destroy();

    }


    @Override

    protected void onResume() {

        super.onResume();


        // start receiving location updates & monitor region changes

        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);

        mIALocationManager.registerRegionListener(mRegionListener);

        mIALocationManager.registerOrientationListener(

                // update if heading changes by 1 degrees or more

                new IAOrientationRequest(1, 0),

                mOrientationListener);


        if (mWayfindingDestination != null) {

            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);

        }

    }


    @Override

    protected void onPause() {

        super.onPause();

        // unregister location & region changes

        mIALocationManager.removeLocationUpdates(mListener);

        mIALocationManager.unregisterRegionListener(mRegionListener);

        mIALocationManager.unregisterOrientationListener(mOrientationListener);


        if (mWayfindingDestination != null) {

            mIALocationManager.removeWayfindingUpdates();

        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override

    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // do not show Google's outdoor location

        //mMap.setMyLocationEnabled(false);

        mMap.setOnMapClickListener(this);

    }


    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */

    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap) {


        if (mGroundOverlay != null) {

            mGroundOverlay.remove();

        }


        if (mMap != null) {

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

            IALatLng iaLatLng = floorPlan.getCenter();

            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);

            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()

                    .image(bitmapDescriptor)

                    .zIndex(0.0f)

                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())

                    .bearing(floorPlan.getBearing());


            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);

        }

    }

    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan) {


        if (floorPlan == null) {

            Log.e(TAG, "null floor plan in fetchFloorPlanBitmap");

            return;

        }


        final String url = floorPlan.getUrl();
        Toast.makeText(getApplicationContext(),url,Toast.LENGTH_SHORT).show();
        Log.d(TAG, "loading floor plan bitmap from " + url);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.emap);
        setupGroundOverlay(floorPlan, bitmap);


    }
    private void showInfo(String text) {

        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text,

                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Dismiss", new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                snackbar.dismiss();

            }

        });

        snackbar.show();

    }



    @Override

    public void onMapClick(LatLng point) {

        if (mMap != null) {



            mWayfindingDestination = new IAWayfindingRequest.Builder()

                    .withFloor(mFloor)

                    .withLatitude(point.latitude)

                    .withLongitude(point.longitude)

                    .build();
            showInfo("please follow the line shown in map ");
            mHeadingMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker2));
            //enable route

            //set banner
            linearLayoutCompat.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(point.latitude)+","+String.valueOf(point.longitude));
            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);



            if (mDestinationMarker == null) {

                mDestinationMarker = mMap.addMarker(new MarkerOptions()

                        .position(point)

                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            } else {

                mDestinationMarker.setPosition(point);

            }

            Log.d(TAG, "Set destination: (" + mWayfindingDestination.getLatitude() + ", " +

                    mWayfindingDestination.getLongitude() + "), floor=" +

                    mWayfindingDestination.getFloor());

        }

    }



    private boolean hasArrivedToDestination(IARoute route) {

        // empty routes are only returned when there is a problem, for example,

        // missing or disconnected routing graph

        if (route.getLegs().size() == 0) {

            return false;

        }



        final double FINISH_THRESHOLD_METERS = 8.0;

        double routeLength = calculateDistance(route);

        //for (IARoute.Leg leg : route.getLegs()) routeLength += leg.getLength();
        //disable lower bar
        if(routeLength < FINISH_THRESHOLD_METERS) {
            linearLayoutCompat.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }
    double calculateDistance(IARoute route)
    {
        double routeLength = 0;

        for (IARoute.Leg leg : route.getLegs()) routeLength += leg.getLength();
        return routeLength;
    }


    /**

     * Clear the visualizations for the wayfinding paths

     */

    private void clearRouteVisualization() {

        for (Polyline pl : mPolylines) {

            pl.remove();

        }

        mPolylines.clear();

    }



    /**

     * Visualize the IndoorAtlas Wayfinding route on top of the Google Maps.

     */

    private void updateRouteVisualization() {



        clearRouteVisualization();



        if (mCurrentRoute == null) {

            return;

        }



        for (IARoute.Leg leg : mCurrentRoute.getLegs()) {



            if (leg.getEdgeIndex() == null) {

                // Legs without an edge index are, in practice, the last and first legs of the

                // route. They connect the destination or current location to the routing graph.

                // All other legs travel along the edges of the routing graph.



                // Omitting these "artificial edges" in visualization can improve the aesthetics

                // of the route. Alternatively, they could be visualized with dashed lines.

                continue;

            }



            PolylineOptions opt = new PolylineOptions();

            opt.add(new LatLng(leg.getBegin().getLatitude(), leg.getBegin().getLongitude()));

            opt.add(new LatLng(leg.getEnd().getLatitude(), leg.getEnd().getLongitude()));



            // Here wayfinding path in different floor than current location is visualized in

            // a semi-transparent color

            if (leg.getBegin().getFloor() == mFloor && leg.getEnd().getFloor() == mFloor) {

                opt.color(0xFF0000FF);

            } else {

                opt.color(0x300000FF);

            }



            mPolylines.add(mMap.addPolyline(opt));

        }

    }

}
