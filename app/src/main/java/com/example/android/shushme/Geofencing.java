package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feder on 17/03/2018.
 */

public class Geofencing implements ResultCallback {
    private static final String TAG = Geofencing.class.getSimpleName();

    private Context context;
    private GoogleApiClient googleApiClient;
    private List<Geofence> mGeofenceList;
    private PendingIntent geofencePendingIntent = null;

    private final long GEOFENCE_TIMEOUT = 86400000;
    private final int GEOFENCE_RADIUS = 50;

    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.googleApiClient = googleApiClient;
        mGeofenceList = new ArrayList<>();
    }

    public void updateGeofencesList(PlaceBuffer placeBuffer){
        mGeofenceList = new ArrayList<>();
        if (placeBuffer == null || placeBuffer.getCount() == 0) return;

        for(Place place : placeBuffer){
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(place.getLatLng().latitude, place.getLatLng().longitude, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);
        }
    }

    public void registerAllGeofences(){
        try {
            LocationServices.GeofencingApi.addGeofences(googleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()).setResultCallback(this);
        } catch(SecurityException ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    public void unRegisterAllGeofences(){
        try {
            LocationServices.GeofencingApi.removeGeofences(googleApiClient,
                    getGeofencePendingIntent()).setResultCallback(this);
        } catch(SecurityException ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build();

        return geofencingRequest;
    }


    private PendingIntent getGeofencePendingIntent(){
        if (geofencePendingIntent != null)
            return geofencePendingIntent;

        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence: %s", result.getStatus().toString()));
    }
}
