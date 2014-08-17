package me.hkr.shared;

import android.util.Log;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.SerializationUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import us.monoid.web.XMLResource;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bkase on 8/16/14.
 */
public class LocationPayload implements Serializable {
  private final static String TAG = LocationPayload.class.getName();

  public final double distance;
  public final String stationName;
  public final Train[] trains;
  public final long ts;

  public LocationPayload(double distance, String stationName, Train[] trains, long ts) {
    this.distance = distance;
    this.stationName = stationName;
    this.trains = trains;
    this.ts = ts;
  }

  public static LocationPayload parseXml(XMLResource xmlResource, double distance) {
    try {
      String stationName = xmlResource.get("/root/station/name").item(0).getNodeValue();
      NodeList etds = xmlResource.get("//etd");
      ImmutableList.Builder<Train> trainsBuilder = ImmutableList.builder();

      for (int i = 0; i < etds.getLength(); i++) {
        NodeList etdChildren = etds.item(i).getChildNodes();

        for (int j = 0; j < etdChildren.getLength(); j++) {
          Node etdChild = etdChildren.item(j);
          String name = etdChild.getNodeName();
          String destinationName = "";
          if (name.equals("destination")) {
            destinationName = etdChild.getTextContent();
          } else if (name.equals("estimate")) {
            trainsBuilder.add(Train.parseXml(etdChild, destinationName));
          }
        }
      }

      ImmutableList<Train> trains = trainsBuilder.build();
      return new LocationPayload(distance, stationName, trains.toArray(new Train[trains.size()]), new Date().getTime());
    } catch (Exception e) {
      Log.d(TAG, "Exception: " + e);
      return null;
    }
  }

  public static LocationPayload deserialize(byte[] data) {
    return (LocationPayload)SerializationUtils.deserialize(data);
  }

  public byte[] serialize() {
    return SerializationUtils.serialize(this);
  }
}
