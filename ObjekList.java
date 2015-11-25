/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package theftdetection2;

import java.util.List;

/**
 *
 * @author Lenovo
 */
public interface ObjekList {
    public void add(Objek in);
    /**
     * @return size of the list
     */
    public int size();

    public void clear();
    public void startSupervised();

    public void supervisingObjek();
    public List<Objek> getList();
}
