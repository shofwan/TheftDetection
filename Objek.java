/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package theftdetection2;

import java.util.Vector;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * Track.java TODO:
 * 
 * @author Kim Dinh Son Email:sonkdbk@gmail.com
 */

public class Objek {

    public static int NextTrackID;
    public int track_id;
    public int missingCount;
    public int occurCount;
    public Point point;
    public Rect rectangle;
    public boolean haveOccured;
    /**
     * @param pt
     * @param rc
     */
    public Objek(Point pt, Rect rc) {
        rectangle = rc;
        point = pt;
        //track_id=id;
    }
}
