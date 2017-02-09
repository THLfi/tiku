package fi.thl.pivot.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Functions {

    private Functions() {
    };

    /**
     * @param maxValue
     *            maximum value of range
     * @return a contiguous modifiable set of integer between 0 and maxvalue
     */
    public static IntLinkedOpenHashSet setUpto(int maxValue) {
        IntLinkedOpenHashSet set = new IntLinkedOpenHashSet(maxValue);
        for(int i = 0; i < maxValue; ++i) {
            set.add(i);
        }
        return set; 
    }
    
    public static IntArrayList listUpto(int maxValue) {
        
        IntArrayList list = new IntArrayList(maxValue);
        for(int i = 0; i < maxValue; ++i) {
            list.add(i);
        }
        return list; 
    }

}
