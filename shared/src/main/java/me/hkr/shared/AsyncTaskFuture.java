package me.hkr.shared;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Callable;

/**
 * Created by bkase on 8/16/14.
 */
public class AsyncTaskFuture {

  public static <T> ListenableFuture<T> future(Callable<T> callable) {
    SettableFuture<T> settableFuture = SettableFuture.create();
    new FutureAsyncTask<T>(settableFuture).execute(callable);
    return settableFuture;
  }
}
