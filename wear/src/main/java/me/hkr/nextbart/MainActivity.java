package me.hkr.nextbart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.*;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import me.hkr.shared.LocationPayload;
import me.hkr.shared.PendingResults;
import me.hkr.shared.Train;

import java.util.List;

public class MainActivity extends Activity {

  private TextView mStationNameTextView;
  private TextView mStationDistanceTextView;
  private LinearLayout mTrainsLinearLayout;
  private TextView mTrain1Time;
  private TextView mTrain2Time;
  private TextView mTrain3Time;

  private ProgressBar mLoadingProgressBar;
  private TextView mLoadingTextView;

  private GoogleApiClient mGoogleApiClient;

  private final String TAG = this.getClass().getName();

  private Handler mHandler;

  private static double METERS_TO_MILES = 0.000621371;

  private final MessageApi.MessageListener MESSAGE_LISTENER =
      new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
          if (messageEvent.getPath().endsWith("newpayload")) {
            LocationPayload locationPayload = LocationPayload.deserialize(messageEvent.getData());

            swapInLocationPayload(locationPayload);
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    mHandler = new Handler();
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        mStationNameTextView = (TextView) stub.findViewById(R.id.station_name);
        mStationDistanceTextView = (TextView) stub.findViewById(R.id.station_distance);
        mTrainsLinearLayout = (LinearLayout) stub.findViewById(R.id.train_times);
        mTrain1Time = (TextView) stub.findViewById(R.id.train1time);
        mTrain2Time = (TextView) stub.findViewById(R.id.train2time);
        mTrain3Time = (TextView) stub.findViewById(R.id.train3time);

        mLoadingProgressBar = (ProgressBar) stub.findViewById(R.id.loading_bar);
        mLoadingTextView = (TextView) stub.findViewById(R.id.loading_text);
      }
    });

    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle bundle) {
            Log.d(TAG, "onConnected: " + bundle);
            Futures.addCallback(attachAndSendFirstMessage(), new FutureCallback<List<Node>>() {
              @Override
              public void onSuccess(List<Node> nodes) {
                Log.d(TAG, "onSuccess: " + nodes);
                if (nodes.size() == 0) {
                  Toast.makeText(MainActivity.this, "Not connected to Android Phone", Toast.LENGTH_LONG).show();
                }
                // TODO: Handle more than one device connected
              }

              @Override
              public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure: " + t);
              }
            });
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
        .build();

    mGoogleApiClient.connect();
  }

  private void swapInLocationPayload(final LocationPayload payload) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "Runnable running");
        mLoadingTextView.setVisibility(View.GONE);
        mLoadingProgressBar.setVisibility(View.GONE);

        for (View v: ImmutableList.of(mStationNameTextView, mStationDistanceTextView, mTrain1Time, mTrain2Time, mTrain3Time, mTrainsLinearLayout)) {
          v.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Done setting all views to VISIBLE");

        mStationNameTextView.setText(payload.stationName);
        mStationDistanceTextView.setText(String.format("%.2f", payload.distance * METERS_TO_MILES) + "mi");

        Train[] trains = payload.trains;
        if (trains.length >= 1) {
          mTrain1Time.setText("" + trains[0].minutes + "m");
        }
        if (trains.length >= 2) {
          mTrain2Time.setText("" + trains[1].minutes + "m");
        }
        if (trains.length >= 3) {
          mTrain3Time.setText("" + trains[2].minutes + "m");
        }
      }
    });

  }

  @Override
  public void onStop() {
    super.onStop();
    Wearable.MessageApi.removeListener(mGoogleApiClient, MESSAGE_LISTENER);
  }

  private ListenableFuture<List<Node>> sendMessage() {
    return Futures.transform(PendingResults.future(Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)), new AsyncFunction<NodeApi.GetConnectedNodesResult, List<Node>>() {
      @Override
      public ListenableFuture<List<Node>> apply(NodeApi.GetConnectedNodesResult getConnectedNodesResult) throws Exception {
        ImmutableList.Builder<ListenableFuture<Node>> builder = ImmutableList.builder();
        for (final Node node : getConnectedNodesResult.getNodes()) {
          ListenableFuture<MessageApi.SendMessageResult> future = PendingResults.future(Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/when/south", null));
          builder.add(Futures.transform(future, new Function<MessageApi.SendMessageResult, Node>() {
            @Override
            public Node apply(MessageApi.SendMessageResult input) {
              return node;
            }
          }));
        }
        return Futures.allAsList(builder.build());
      }
    });
  }

  private ListenableFuture<List<Node>> attachAndSendFirstMessage() {
    return Futures.transform(PendingResults.future(Wearable.MessageApi.addListener(mGoogleApiClient, MESSAGE_LISTENER)), new AsyncFunction<Status, List<Node>>() {
      @Override
      public ListenableFuture<List<Node>> apply(Status status) throws Exception {
        Log.d(TAG, "Status: " + status);
        return sendMessage();
      }
    });
  }
}
