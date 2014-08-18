package me.hkr.nextbart;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.*;
import me.hkr.shared.AsyncTaskFuture;
import me.hkr.shared.LocationPayload;
import us.monoid.web.Resty;
import us.monoid.web.XMLResource;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by bkase on 8/10/14.
 */
public class MainService extends WearableListenerService {
  private static final String TAG = MainService.class.getName();

  private final String NORTH_DIRECTION = "n";
  private final String SOUTH_DIRECTION = "s";

  private static final Map<String, String> ABREV_TO_FULL =
      ImmutableMap.<String, String>builder()
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

  private static final Map<String, LatLng> ABREV_TO_LATLONG =
      ImmutableMap.<String, LatLng>builder()
      .put("12th", new LatLng(37.803664, -122.271604))
      .put("16th", new LatLng(37.765062, -122.419694))
      .put("19th", new LatLng(37.80787, -122.269029))
      .put("24th", new LatLng(37.752254, -122.418466))
      .put("ashb", new LatLng(37.853024, -122.26978))
      .put("balb", new LatLng(37.72198087, -122.4474142))
      .put("bayf", new LatLng(37.697185, -122.126871))
      .put("cast", new LatLng(37.690754, -122.075567))
      .put("civc", new LatLng(37.779528, -122.413756))
      .put("cols", new LatLng(37.754006, -122.197273))
      .put("colm", new LatLng(37.684638, -122.466233))
      .put("conc", new LatLng(37.973737, -122.029095))
      .put("daly", new LatLng(37.70612055, -122.4690807))
      .put("dbrk", new LatLng(37.869867, -122.268045))
      .put("dubl", new LatLng(37.701695, -121.900367))
      .put("deln", new LatLng(37.925655, -122.317269))
      .put("plza", new LatLng(37.9030588, -122.2992715))
      .put("embr", new LatLng(37.792976, -122.396742))
      .put("frmt", new LatLng(37.557355, -121.9764))
      .put("ftvl", new LatLng(37.774963, -122.224274))
      .put("glen", new LatLng(37.732921, -122.434092))
      .put("hayw", new LatLng(37.670399, -122.087967))
      .put("lafy", new LatLng(37.893394, -122.123801))
      .put("lake", new LatLng(37.797484, -122.265609))
      .put("mcar", new LatLng(37.828415, -122.267227))
      .put("mlbr", new LatLng(37.599787, -122.38666))
      .put("mont", new LatLng(37.789256, -122.401407))
      .put("nbrk", new LatLng(37.87404, -122.283451))
      .put("ncon", new LatLng(38.003275, -122.024597))
      .put("orin", new LatLng(37.87836087, -122.1837911))
      .put("pitt", new LatLng(38.018914, -121.945154))
      .put("phil", new LatLng(37.928403, -122.056013))
      .put("powl", new LatLng(37.784991, -122.406857))
      .put("rich", new LatLng(37.936887, -122.353165))
      .put("rock", new LatLng(37.844601, -122.251793))
      .put("sbrn", new LatLng(37.637753, -122.416038))
      .put("sfia", new LatLng(37.616035, -122.392612))
      .put("sanl", new LatLng(37.72261921, -122.1613112))
      .put("shay", new LatLng(37.63479954, -122.0575506))
      .put("ssan", new LatLng(37.664174, -122.444116))
      .put("ucty", new LatLng(37.591208, -122.017867))
      .put("wcrk", new LatLng(37.905628, -122.067423))
      .put("wdub", new LatLng(37.699759, -121.928099))
      .put("woak", new LatLng(37.80467476, -122.2945822))
      .build();


  private GoogleApiClient mGoogleApiClient;

  private MessageEvent shouldSendOnConnect = null;

  private Resty mResty;

  private Handler mHandler;

  @Override
  public void onCreate() {
    super.onCreate();

    mHandler = new Handler();
    mResty = new Resty();
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle bundle) {
            Log.d(TAG, "onConnected: " + bundle);
            if (shouldSendOnConnect != null) {
              Log.d(TAG, "calling sendLocationToWatch");
              handleMessageEvent(shouldSendOnConnect);
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

  private String stationTimesUrl(String station, String direction) {
    return "http://api.bart.gov/api/etd.aspx?cmd=etd&orig=" + station + "&dir=" + direction + "&key=MW9S-E7SL-26DU-VV8V";
  }

  private StationDistance closestStation(Location location) {
    float bestDistance = Float.MAX_VALUE;
    Map.Entry<String, LatLng> bestEntry = ABREV_TO_LATLONG.entrySet().iterator().next();

    for (Map.Entry<String, LatLng> entry: ABREV_TO_LATLONG.entrySet()) {
      float[] results = new float[1];
      Location.distanceBetween(location.getLatitude(),
          location.getLongitude(),
          entry.getValue().latitude,
          entry.getValue().longitude,
          results);
      float distance = results[0];
      if (distance < bestDistance) {
        bestDistance = distance;
        bestEntry = entry;
      }
    }

    return new StationDistance(bestEntry.getKey(), bestDistance);
  }

  private void handleMessageEvent(MessageEvent messageEvent) {
    if (messageEvent.getPath().endsWith("north")) {
      sendLocationToWatch(NORTH_DIRECTION, messageEvent.getSourceNodeId());
    } else if (messageEvent.getPath().endsWith("south")) {
      sendLocationToWatch(SOUTH_DIRECTION, messageEvent.getSourceNodeId());
    }
  }

  private void sendLocationToWatch(final String direction, final String sourceNodeId) {
    Log.d(TAG, "sending location to watch: " + direction);
    ListenableFuture<ResponseStationInfo> reqFuture = Futures.transform(getLocationOnce(), new AsyncFunction<Location, ResponseStationInfo>() {
      @Override
      public ListenableFuture<ResponseStationInfo> apply(Location location) throws Exception {
        final StationDistance stationDistance = closestStation(location);
        Log.d(TAG, "getting result for stationAbbrev: " + stationDistance.station);

        return AsyncTaskFuture.future(new Callable<ResponseStationInfo>() {
          @Override
          public ResponseStationInfo call() throws Exception {
            Log.d(TAG, "making xml call");
            return new ResponseStationInfo(
                mResty.xml(stationTimesUrl(stationDistance.station, direction)),
                stationDistance.distance,
                stationDistance.station);
          }
        });
      }
    });

    Futures.addCallback(reqFuture, new FutureCallback<ResponseStationInfo>() {
      @Override
      public void onSuccess(ResponseStationInfo responseStationInfo) {
        LocationPayload payload = LocationPayload.parseXml(
            responseStationInfo.response,
            responseStationInfo.distance,
            ABREV_TO_FULL.get(responseStationInfo.stationAbbrev)
        );
        if (payload == null) {
          Log.d(TAG, "Payload is null");
        } else {
          Log.d(TAG, "Payload parsed");
          Wearable.MessageApi.sendMessage(mGoogleApiClient, sourceNodeId, "/newpayload", payload.serialize()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
              Log.d(TAG, "newpayload result: " + sendMessageResult);
            }
          });
        }
      }

      @Override
      public void onFailure(Throwable t) {
        Log.d(TAG, "failed xmlresource with: " + t);
      }
    });
  }

  private ListenableFuture<Location> getLocationOnce() {
    return Futures.withFallback(getHighAccuracyLocationOnce(), new FutureFallback<Location>() {
      @Override
      public ListenableFuture<Location> create(Throwable t) throws Exception {
        return Futures.immediateFuture(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
      }
    });
  }

  private ListenableFuture<Location> getHighAccuracyLocationOnce() {
    final int EXPIRATION_TIMEOUT_MS = 5 * 1000;

    LocationRequest request =
        LocationRequest.create()
            .setExpirationDuration(EXPIRATION_TIMEOUT_MS)
            .setNumUpdates(1)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    Log.d(TAG, "Attempting to get location");
    final SettableFuture<Location> settableFuture = SettableFuture.create();
    final LocationListener LOCATION_LISTENER = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        settableFuture.set(location);
      }
    };
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, LOCATION_LISTENER);
    mHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LOCATION_LISTENER);
        settableFuture.setException(new TimeoutException());
      }
    }, EXPIRATION_TIMEOUT_MS);
    return settableFuture;
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Log.d(TAG, "onMessageReceived: " + messageEvent);

    if (mGoogleApiClient.isConnected()) {
      handleMessageEvent(messageEvent);
    } else {
      shouldSendOnConnect = messageEvent;
    }
  }

  private class StationDistance {
    public final String station;
    public final double distance;

    public StationDistance(String station, double distance) {
      this.station = station;
      this.distance = distance;
    }
  }

  private class ResponseStationInfo {
    public final XMLResource response;
    public final double distance;
    public final String stationAbbrev;

    public ResponseStationInfo(XMLResource response, double distance, String stationAbbrev) {
      this.response = response;
      this.distance = distance;
      this.stationAbbrev = stationAbbrev;
    }
  }
}
