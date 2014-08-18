package me.hkr.shared;

import android.nfc.Tag;
import android.util.Log;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;

/**
 * Created by bkase on 8/16/14.
 */
public class Train implements Serializable {
  public static final String TAG = Train.class.getName();

  public final int minutes;
  public final int platform;
  public final int length;
  public final String hexcolor;
  public final String destination;

  public Train(int minutes, int platform, int length, String hexcolor, String destination) {
    this.minutes = minutes;
    this.platform = platform;
    this.length = length;
    this.hexcolor = hexcolor;
    this.destination = destination;
  }

  public static Train parseXml(Node estimateNode, String destinationName) {
    int minutes = -1;
    int platform = -1;
    int length = -1;
    String hexcolor = "";

    NodeList children = estimateNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node.getNodeName().equals("minutes")) {
        try {
          minutes = Integer.parseInt(node.getTextContent());
        } catch (Exception e) {
          minutes = 0;
        }
      } else if (node.getNodeName().equals("platform")) {
        try {
          platform = Integer.parseInt(node.getTextContent());
        } catch (Exception e) {
          platform = 0;
        }
      } else if (node.getNodeName().equals("length")) {
        try {
          length = Integer.parseInt(node.getTextContent());
        } catch (Exception e) {
          length = 0;
        }
      } else if (node.getNodeName().equals("hexcolor")) {
        hexcolor = node.getTextContent();
      }
    }

    return new Train(minutes, platform, length, hexcolor, destinationName);
  }
}
