package fi.thl.pivot.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Represents a multidimensional dataset where each value is represented as a
 * relation of (key1,key2,...,keym)->(value).
 * 
 * When DataSet is used please make sure that all classifiying dimensions are
 * listed as key values. This is important because if only a subset of keys are
 * used user of this API cannot be certain which way a specific relation should
 * be fetched.
 * 
 * Please note that the DataSet object is meant to be used as a data transfer
 * object not as a database. DataSet should be populated by a subset of the
 * actual database that contains only the information required to present the
 * requested report user interface.
 * 
 * @author aleksiyrttiaho
 *
 */
public class Dataset {

    private static final Joiner JOINER = Joiner.on(';');

    private static final Logger LOG = Logger.getLogger(Dataset.class);

    private Map<Integer, Object> values = new TreeMap<>();

    /**
     * Adda new value to the dataset with (key<sub>1</sub>, key<sub>2</sub>,
     * ..., key<sub>n</sub>)->(value). The keys are used as the second argument
     * allow use of varargs and thus array autoboxing.
     * 
     * @param value
     * @param keys
     */
    @SuppressWarnings("unchecked")
    public void put(String value, List<IDimensionNode> keys) {
        if (null == keys || keys.isEmpty()) {
            LOG.trace("Cannot add value to dataset: No keys set for value");
            return;
        }
        if (null == value) {
            LOG.trace("Cannot add value to dataset: Null value detected for " + stringIds(keys));
            return;
        }

        Map<Integer, Object> v = values;
        List<Integer> ids = ids(keys);
        for (int i = 0; i < ids.size() - 1; ++i) {
            if (!v.containsKey(ids.get(i))) {
                v.put(ids.get(i), new TreeMap<Integer, Object>());
            }
            v = (TreeMap<Integer, Object>) v.get(ids.get(i));
        }
        v.put(ids.get(ids.size() - 1), value);
    }

    /**
     * 
     * Returns the value of the relation (key<sub>1</sub>, key<sub>2</sub>, ...,
     * key<sub>n</sub>)->(value)
     * 
     * @param keys
     * @return
     */
    public String get(Collection<IDimensionNode> keys) {
        return getWithIds(ids(keys));
    }

    @SuppressWarnings("unchecked")
    public String getWithIds(Collection<Integer> keys) {
        Map<Integer, Object> v = values;
        List<Integer> ids = new ArrayList<>(keys);
        int lastIdIndex = ids.size() - 1;
        for (int i = 0; i < lastIdIndex; ++i) {
            if (!v.containsKey(ids.get(i))) {
                return null;
            }
            Object o = v.get(ids.get(i));
            if (o instanceof TreeMap) {
                v = (TreeMap<Integer, Object>) o;
            } else {
                LOG.warn("Could not resolve id " + ids.get(i) + " as treemap");
                return (String) v.get(ids.get(lastIdIndex));
            }
        }
        return (String) v.get(ids.get(lastIdIndex));
    }

    private String stringIds(Collection<IDimensionNode> keys) {
        return JOINER.join(ids(keys));
    }

    /**
     * Returns a sorted list of dimension node ids. This is done to ensure that
     * each combination is saved only once instead of saving each permutation.
     * 
     * @param keys
     *            key<sub>1</sub>, key<sub>2</sub>, ..., key<sub>n</sub>
     * @return sorted list of dimension node identifiers in ascending order
     */
    private List<Integer> ids(Collection<IDimensionNode> keySet) {
        List<Integer> keyList = Lists.newArrayList();
        for (IDimensionNode s : keySet) {
            keyList.add(s.getSurrogateId());
        }
        Collections.sort(keyList);
        return keyList;
    }
    
    public int size() {
        return values.size();
    }

}
