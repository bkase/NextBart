package me.hkr.shared;

import android.os.AsyncTask;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Callable;

/**
 * Created by bkase on 8/16/14.
 */
public class FutureAsyncTask<T> extends AsyncTask<Callable<T>, Void, Void> {

  private SettableFuture<T> settableFuture;

  public FutureAsyncTask(SettableFuture<T> settableFuture) {
    this.settableFuture = settableFuture;
  }

  @Override
  protected Void doInBackground(Callable<T>... callables) {
    try {
      settableFuture.set(callables[0].call());
    } catch (Exception e) {
      settableFuture.setException(e);
    }
    return null;
  }
}
