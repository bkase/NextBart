package me.hkr.nextbart;

import android.app.Service;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import me.hkr.shared.PendingResults;
import us.monoid.web.Resty;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by bkase on 8/10/14.
 */
public class MainService extends WearableListenerService {
  private final String TAG = this.getClass().getName();

  private final String NORTH_DIRECTION = "n";
  private final String SOUTH_DIRECTION = "s";

  private static final BiMap<String, String> ABREV_TO_FULL =
      ImmutableBiMap.<String, String>builder()
      .put("12th", "12th St. Oakland City Center")
      .put("16th", "16th St. Mission (SF)")
      .put("19th", "19th St. Oakland")
      .put("24th", "24th St. Mission (SF)")
      .put("ashb", "Ashby (Berkeley)")
      .put("balb", "Balboa Park (SF)")
      .put("bayf", "Bay Fair (San Leandro)")
      .put("cast", "Castro Valley")
      .put("civc", "Civic Center (SF)")
      .put("cols", "Coliseum/Oakland Airport")
      .put("colm", "Colma")
      .put("conc", "Concord")
      .put("daly", "Daly City")
      .put("dbrk", "Downtown Berkeley")
      .put("dubl", "Dublin/Pleasanton")
      .put("deln", "El Cerrito del Norte")
      .put("plza", "El Cerrito Plaza")
      .put("embr", "Embarcadero (SF)")
      .put("frmt", "Fremont")
      .put("ftvl", "Fruitvale (Oakland)")
      .put("glen", "Glen Park (SF)")
      .put("hayw", "Hayward")
      .put("lafy", "Lafayette")
      .put("lake", "Lake Merritt (Oakland)")
      .put("mcar", "MacArthur (Oakland)")
      .put("mlbr", "Millbrae")
      .put("mont", "Montgomery St. (SF)")
      .put("nbrk", "North Berkeley")
      .put("ncon", "North Concord/Martinez")
      .put("orin", "Orinda")
      .put("pitt", "Pittsburg/Bay Point")
      .put("phil", "Pleasant Hill")
      .put("powl", "Powell St. (SF)")
      .put("rich", "Richmond")
      .put("rock", "Rockridge (Oakland)")
      .put("sbrn", "San Bruno")
      .put("sfia", "San Francisco Int'l Airport")
      .put("sanl", "San Leandro")
      .put("shay", "South Hayward")
      .put("ssan", "South San Francisco")
      .put("ucty", "Union City")
      .put("wcrk", "Walnut Creek")
      .put("wdub", "West Dublin")
      .put("woak",	"West Oakland")
      .build();

  private GoogleApiClient mGoogleApiClient;

  private String shouldSendOnConnect = null;

  private Resty mResty;

  @Override
  public void onCreate() {
    super.onCreate();

    mResty = new Resty();
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle bundle) {
            Log.d(TAG, "onConnected: " + bundle);
            if (shouldSendOnConnect != null) {
              sendLocationToWatch(shouldSendOnConnect);
              shouldSendOnConnect = null;
            }
          }

          @Override
          public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended: " + i);
          }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed: " + connectionResult);
          }
        })
        .addApi(Wearable.API)
        .addApi(LocationServices.API)
        .build();

    Log.d(TAG, "Trying to connect");
    mGoogleApiClient.connect();
  }

  private void sendLocationToWatch(String direction) {
    getLocationOnce();
    mResty.xml()
  }

  private ListenableFuture<Location> getLocationOnce() {
    LocationRequest request =
        LocationRequest.create()
            .setExpirationDuration(10 * 1000)
            .setNumUpdates(1)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    final SettableFuture<Location> settableFuture = SettableFuture.create();
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
          request,
          new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
              settableFuture.set(location);
            }
          }).setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(Status status) {
        Log.d(TAG, "Result callback is: " + status);
      }
    });
    return settableFuture;
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Log.d(TAG, "onMessageReceived: " + messageEvent);

    if (messageEvent.getPath().endsWith(NORTH_DIRECTION)) {
      if (mGoogleApiClient.isConnected()) {
        sendLocationToWatch(NORTH_DIRECTION);
      } else {
        shouldSendOnConnect = NORTH_DIRECTION;
      }
    } else {

    }
  }
}
