/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import dag.nosql_schema.NoSQLSchema;

/**
 *
 * @author Evandro
 */
public class Depth extends Metrics {

    public Depth(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    /////////////  3 - DEPTH METHODS                                   /////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    // Método de apoio que conta o número de caracteres '/' dentro da String path.
    public int countNumberOfPathBars(int inicio, int fim, String path) {
        int numberOfBars = 0;
        for (int i = inicio; i < fim; i++) {
            if (path.charAt(i) == '/') {
                numberOfBars++;
            }
        }
        return numberOfBars;
    }

    public Map<String, Integer> getDepthOfDocument(String collection, String document) {
        Map<String, Integer> depth = new HashMap<>();

        for (String path : this.path().getCollectionPaths(collection)) {
            String[] pathVet = path.split("/");
            for (int i=0; i<pathVet.length; i++){
                if (pathVet[i].equalsIgnoreCase(document)){
                    depth.put(path, i+1); // verificar se ficou certo a adição de 1 (+1)
                }
            }
        }
        return depth;
    }

    public int getMaxDocDepth(String docName) {
        int maxDepth = 0;
        for (String collection : this.getListOfCollectionsInSchema()) {
            Set<String> chaves = this.getDepthOfDocument(collection, docName).keySet();
            for (String chave : chaves) {
                int depth = this.getDepthOfDocument(collection, docName).get(chave);
                if (maxDepth < depth) {
                    maxDepth = depth;
                }
            }
        }
        return maxDepth;
    }

    public int getMinDocDepth(String docName) {
        int minDepth = -1;
        for (String collection : this.getListOfCollectionsInSchema()) {
            Set<String> chaves = this.getDepthOfDocument(collection, docName).keySet();
            for (String chave : chaves) {
                int depth = this.getDepthOfDocument(collection, docName).get(chave);
                if (minDepth == -1) minDepth = depth;
                if (minDepth > depth) {
                    minDepth = depth;
                }
            }
        }
        return minDepth;
    }

    public int getDepthOfCollection(String collectionName) {
        int maxDepth = 0;
        // Para cada caminho da coleção...
        for (String path : this.path().getCollectionPaths(collectionName)) {
            int numberOfBars = countNumberOfPathBars(0, path.length(), path);
            if (maxDepth < numberOfBars) {
                maxDepth = numberOfBars;
            }
        }
        // A altura ou profudindade da coleção é igual a número de '\' + 1.
        return maxDepth + 1;
    }

    public int getDepthOfSchema() {
        int maxDepthOfSchema = 0;
        for (String collectionName : this.getListOfCollectionsInSchema()) {
            int colDepth = getDepthOfCollection(collectionName);
            if (maxDepthOfSchema < colDepth) {
                maxDepthOfSchema = colDepth;
            }
        }
        return maxDepthOfSchema;
    }
}
