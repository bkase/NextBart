package me.hkr.shared;

import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;

/**
 * Created by bkase on 8/16/14.
 */
public class LocationPayload implements Serializable {
  public final double distance;
  public final String stationName;
  public final int[] minutesUntilTrains;
  public final double ts;

  public LocationPayload(double distance, String stationName, int[] minutesUntilTrains, double ts) {
    this.distance = distance;
    this.stationName = stationName;
    this.minutesUntilTrains = minutesUntilTrains;
    this.ts = ts;
  }

  public static LocationPayload deserialize(byte[] data) {
    return (LocationPayload)SerializationUtils.deserialize(data);
  }

  public byte[] serialize() {
    return SerializationUtils.serialize(this);
  }
}
