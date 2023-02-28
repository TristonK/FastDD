package de.metanome.algorithms.dcfinder.input;

import java.util.*;

import com.koloboke.collect.map.hash.HashObjIntMap;
import com.koloboke.collect.map.hash.HashObjIntMaps;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

public class ParsedColumn<T extends Comparable<T>> {

    private final String columnName;
    private final int index;
    private final HashObjIntMap<T> valueSet = HashObjIntMaps.newMutableMap();
    private final List<T> values = new ArrayList<>();
    private final Class<T> type;

    private Double maxNum;
    private Double minNum;
    private List<Double> thresholds;
    private final IndexProvider<T> indexProvider;

    public ParsedColumn(String columnName, Class<T> type, int index, IndexProvider<T> indexProvider) {
        this.columnName = columnName;
        this.type = type;
        this.index = index;
        this.indexProvider = indexProvider;
        this.maxNum = Double.MIN_VALUE;
        this.minNum = Double.MAX_VALUE;
    }

    public void addLine(T value) {
        valueSet.addValue(value, 1, 0);
        values.add(value);
        if(type == Double.class){
            maxNum = Math.max(maxNum, (Double)value);
            minNum = Math.min(minNum, (Double)value);
        }else if(type == Long.class){
            maxNum = Math.max(maxNum, ((Long) value).doubleValue());
            minNum = Math.min(minNum, ((Long) value).doubleValue());
        }
        indexProvider.getIndex(value);
    }

    public int size() {
        return values.size();
    }

    public void remove(int[] ascRemoveRows) {
        for (int i = ascRemoveRows.length - 1; i >= 0; i--)
            values.remove(ascRemoveRows[i]);
    }

    public void remove(List<Integer> removeRows, boolean reverseSorted) {
        if (!reverseSorted)
            removeRows.sort(Collections.reverseOrder());

        for (int removeRow : removeRows)
            values.remove(removeRow);
    }

    public boolean isNum() {
        return type != String.class;
    }

    public boolean isLong(){
        return type == Long.class;
    }

    public boolean isDouble(){
        return type == Double.class;
    }

    public int getIndexAt(int row) {
        return indexProvider.getIndex(values.get(row));
    }

    public T getValue(int line) {
        return values.get(line);
    }

    public List<T> getValues() {
        return new ArrayList<>(values);
    }

    public List<T> getValues(int stt, int end) {
        return values.subList(stt, end);
    }

    public String getColumnName() {
        return columnName;
    }

    public int getIndex() {
        return index;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return columnName;
    }

    public boolean isComparableType() {
        return getType().equals(Double.class) || getType().equals(Long.class);
    }

    public double getAverage() {
        double avg = 0.0d;
        int size = values.size();
        if (type.equals(Double.class)) {
            for (T value : values) {
                double l = (Double) value;
                double tmp = l / size;
                avg += tmp;
            }
        } else if (type.equals(Long.class)) {
            for (T value : values) {
                Long l = (Long) value;
                double tmp = l.doubleValue() / size;
                avg += tmp;
            }
        }

        return avg;
    }

    public double getSharedPercentage(ParsedColumn<?> c2) {
        int totalCount = 0;
        int sharedCount = 0;
        for (var e : valueSet.entrySet()) {
            int thisCount = e.getValue();
            int otherCount = c2.valueSet.getInt(e.getKey());
            sharedCount += Math.min(thisCount, otherCount);
            totalCount += Math.max(thisCount, otherCount);
        }
        return ((double) sharedCount) / ((double) totalCount);
    }

    public String getColumnIdentifier() {
        return columnName;
    }

    public List<Double> getThresholds(){
        return thresholds;
    }

    public void setThresholds(List<Double> thresholds){
        this.thresholds = new ArrayList<>(thresholds);
    }

    public Double getMaxNum(){return maxNum;}

    public Double getMinNum() {return minNum;}

    public int getTypeIndex() {
        if(type.equals(Long.class)){return 0;}
        else if(type.equals(Double.class)){return 1;}
        else {
            return 2;
        }
    }
}
