/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package theftdetection2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.opencv.core.Point;
import org.opencv.core.Rect;
/**
 *
 * @author Lenovo
 */

public class ObjekLinkedList implements ObjekList{
    public List<Objek> list = new LinkedList<Objek>();
    public static List<Objek> objekDiawasi = null;
    public static int toleranceCenter, toleranceArea, numberFrames, idxObjek, time;
    
    public ObjekLinkedList() {
        
    }
    
    @Override
    public List<Objek> getList(){
        return list;
    }
    @Override
    public void add(Objek in) {
        if(list.isEmpty()){
            in.track_id = 1;
            in.haveOccured = true;
            list.add(in);
            idxObjek = 1;
        }
        else{
            ObjekLinkedList.idxObjek += 1;
            in.track_id = ObjekLinkedList.idxObjek;
            list.add(in);
        }
    }
    
    /**
     * Returns the first index of the object (in list) that similar to "in".
     * similar here is roughly not difference in center point and size of area.
     * The similarity is determined by toleranceCenter and toleranceArea
     * 
     * @param list the list that want to be checked
     * @param in element whose presence in this list is to be tested
     * @return the index of the first occurrence of the specified element in this list, 
     * or -1 if this list does not contain the element that similar
     */
    private int similar(Objek in) {
        Iterator<Objek> it = list.iterator();
        Objek temp;
        //while(it.hasNext()){
        for(int i=0; i<list.size(); i++){
            //temp = it.next();
            temp = list.get(i);
            //System.out.println("jarak: "+(int)euclideanDist(temp.point, in.point));
            //System.out.println("tolerance: "+toleranceCenter);
            if((int)euclideanDist(temp.point, in.point) < toleranceCenter){
                //in.occurCount = temp.occurCount + numberFrames;
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns true if both object similar, false otherwise
     * The similarity is determined by toleranceCenter and toleranceArea
     * 
     * @param in1 first element
     * @param in2 second element
     * @return true if both object similar, false otherwise
     */
    private boolean similarWith(Objek in1, Objek in2) {
        if((int)euclideanDist(in1.point, in2.point) <= toleranceCenter){
            return true;
        }
        return false;
    }
    
    /**
     * Return the euclidean distance of 2 points
     * @param p
     * @param q
     * @return euclidean distance of 2 points
     */
    double euclideanDist(Point p, Point q) {
		Point diff = new Point(p.x - q.x, p.y - q.y);
		return Math.sqrt(diff.x * diff.x + diff.y * diff.y);
	}

    public static void setToleranceCenter(int toleranceCenter) {
        ObjekLinkedList.toleranceCenter = toleranceCenter;
    }

    public static void setToleranceArea(int toleranceArea) {
        ObjekLinkedList.toleranceArea = toleranceArea;
    }

    public static void setNumberFrames(int numberFrames) {
        ObjekLinkedList.numberFrames = numberFrames;
    }
    @Override
    public int size() {
        return list.size();
    }

    @Override
    public void clear() {
        list.clear();
    }
    @Override
    public void startSupervised() {
        objekDiawasi = new LinkedList<Objek>();
        Iterator<Objek> it = list.iterator();
        Objek temp, toAdd;
        while(it.hasNext()){
            temp = it.next();
            toAdd = new Objek(temp.point, temp.rectangle);
            toAdd.haveOccured = false;
            toAdd.missingCount = 0;
            toAdd.occurCount = temp.occurCount;
            toAdd.track_id = temp.track_id;
            ObjekLinkedList.objekDiawasi.add(toAdd);
        }
    }
@Override
public void supervisingObjek() {
    Iterator<Objek> it = list.iterator();
    Objek tempObjek, tempObjekDiawasi, toAdd;
    while(it.hasNext()){
        tempObjek = it.next();
        Iterator<Objek> itDiawasi = objekDiawasi.iterator();
        while(itDiawasi.hasNext()){
            tempObjekDiawasi = itDiawasi.next();
            if(similarWith(tempObjek, tempObjekDiawasi)){
                tempObjekDiawasi.haveOccured = true;
                tempObjekDiawasi.missingCount = 0;
            }
        }
    }
}
}
