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
public class JoinResult implements Comparable {
    private String queryPath;
    private int numberOfCollection;
    private String collectionList;
    private String joinPath;
    
    public JoinResult(String queryPath, String joinPath, int numberOfCollection, String collectionList) {
        this.queryPath = queryPath;
        this.joinPath = joinPath;
        this.numberOfCollection = numberOfCollection;
        this.collectionList = collectionList;
    }

    public JoinResult(String queryPath, int numberOfCollection, String collectionList) {
        this.queryPath = queryPath;
        this.numberOfCollection = numberOfCollection;
        this.collectionList = collectionList;
    }
    
    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }

    public int getNumberOfCollection() {
        return numberOfCollection;
    }

    public void setNumberOfCollection(int numberOfCollection) {
        this.numberOfCollection = numberOfCollection;
    }

    public String getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(String collectionList) {
        this.collectionList = collectionList;
    }
    
    public String getJoinPath() {
        return joinPath;
    }

    public void setJoinPath(String joinPath) {
        this.joinPath = joinPath;
    }
    
    @Override
    public int compareTo(Object o) {
        JoinResult otherJoinResult = (JoinResult) o;
        if (this.numberOfCollection < otherJoinResult.numberOfCollection) {
            return -1;
        } else if (this.numberOfCollection > otherJoinResult.numberOfCollection) {
            return 1;
        } else {
            // ATENÇÃO: está comparação é GROSSEIRA! É interessante usar o tamanho do caminho (contando as barras), não apenas o tamanho da string do caminho.
            if (this.joinPath.length()<otherJoinResult.joinPath.length())
                return -1;
            else if (this.joinPath.length()<otherJoinResult.joinPath.length())
                return 1;
            
            return 0;
        }
    }

    @Override
    public String toString() {
//        return "(qpath= " + queryPath + ", numberOfCollection=" + numberOfCollection + ", collectionList=" + collectionList +")";
        return "(qpath= " + queryPath + ", joinPath=" + joinPath + ", numberOfCollection=" + numberOfCollection + ", collectionList=" + collectionList +")";
    }
}
