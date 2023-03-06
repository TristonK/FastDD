package ddfinder.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tristonK 2023/3/4
 */
public class ObjectIndexBijection<T> {
    private Map<T, Integer> o2indexes;
    private List<T> objects;
    private int allocateId;

    public ObjectIndexBijection(){
        o2indexes = new HashMap<>();
        objects = new ArrayList<>();
        allocateId = 0;
    }

    public Integer getIndex(T object) {
        Integer index = o2indexes.putIfAbsent(object, allocateId);
        if (index == null) {
            index = allocateId++;
            objects.add(object);
        }
        return index;
    }

    public T getObject(int index) {
        return objects.get(index);
    }

    public int size() {
        return allocateId;
    }

    public List<T> getObjects() {
        return objects;
    }
}
