/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreader;

import java.util.*;
/**
 *
 * @author gabri
 */
public class EntryPair {
    
    public Entry entry= null;
    public ArrayList<Entry> pair= null;
    
    public EntryPair(Entry entry){
        this.entry=entry;
        pair= new ArrayList<>();
        
    }
    
    
    
}
