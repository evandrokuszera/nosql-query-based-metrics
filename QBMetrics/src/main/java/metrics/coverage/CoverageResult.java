/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics.coverage;

/**
 *
 * @author Evandro
 */
public class CoverageResult implements Comparable {

    private String collection;
    private double value;
    private int depth;

    public CoverageResult(String collection, double value, int depth) {
        this.collection = collection;
        this.value = value;
        this.depth = depth;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

//    @Override
//    public int compareTo(Object o) {
//        CoverageResult otherMetricResult = (CoverageResult) o;
//        if (this.value < otherMetricResult.value) {
//            return -1;
//        } else if (this.value > otherMetricResult.value) {
//            return 1;
//        } else { // são iguais... a profundidade deve desempatar...
//            if (this.depth > otherMetricResult.depth) {
//                return -1;
//            } else if (this.depth < otherMetricResult.depth) {
//                return 1;
//            } else {
//                return 0;
//            }
//        }
//    }
    
    @Override
    public int compareTo(Object o) {
        CoverageResult otherMetricResult = (CoverageResult) o;
        if (this.value > otherMetricResult.value) {
            return -1;
        } else if (this.value < otherMetricResult.value) {
            return 1;
        } else { // são iguais... a profundidade deve desempatar...
            if (this.depth < otherMetricResult.depth) {
                return -1;
            } else if (this.depth > otherMetricResult.depth) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {
//        return "(collection=" + collection + ", value=" + value + ", depth=" + depth + ')';
        return "(value=" + value + ", depth=" + depth + ", collection=" + collection + ')';
    }
}
