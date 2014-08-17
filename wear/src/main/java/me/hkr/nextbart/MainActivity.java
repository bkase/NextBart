package me.hkr.nextbart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Message;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
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
import me.hkr.shared.PendingResults;

import java.util.List;

public class MainActivity extends Activity {

  private TextView mTextView;
  private GoogleApiClient mGoogleApiClient;

  private final String TAG = this.getClass().getName();

  private final MessageApi.MessageListener MESSAGE_LISTENER =
      new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
          Log.d(TAG, "Got message: " + messageEvent);
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        mTextView = (TextView) stub.findViewById(R.id.text);
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
                } else {
                  // Node node = nodes.get(0);
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
          ListenableFuture<MessageApi.SendMessageResult> future = PendingResults.future(Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/when/north", null));
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
