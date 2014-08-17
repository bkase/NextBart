package me.hkr.shared;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Created by bkase on 8/10/14.
 */
public class PendingResults {
  public static <T extends com.google.android.gms.common.api.Result> ListenableFuture<T> future(final PendingResult<T> p) {
    final SettableFuture<T> settableFuture = SettableFuture.create();
    p.setResultCallback(new ResultCallback<T>() {
      @Override
      public void onResult(T t) {
        settableFuture.set(t);
      }
    });
    return settableFuture;
  }
}
