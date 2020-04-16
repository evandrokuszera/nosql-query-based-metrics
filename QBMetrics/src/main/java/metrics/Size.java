/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import dag.nosql_schema.NoSQLSchema;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class Size extends Metrics {
    public int documentWeight = 2;
    public int arrayOfDocumentWeight = 3;
    
    public Size(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public int getDocumentSize(String name) { // pode ser pelo path no lugar do name.
        return -1;
    }

    public int getCollectionSize(String collectionName, boolean with_weight) {
        // Pesos para diferenciar documentos e arrays de documentos no esquema.
        if (with_weight){
            documentWeight = 2;
            arrayOfDocumentWeight = 3;
        } else {
            documentWeight = 1;
            arrayOfDocumentWeight = 1;
        }
        
        int collectionSize = 0;        
        collectionSize += this.getNumberOfAtomicAttributes(collectionName);
        collectionSize += this.getNumberOfDocuments(collectionName) * documentWeight;
        collectionSize += this.getNumberOfArraysOfDocuments(collectionName) * arrayOfDocumentWeight;        
        return collectionSize;
    }
    
    public int getSchemaSize(boolean with_weight){
        int sizeOfSchema = 0;
        for (String collectionName : this.getListOfCollectionsInSchema()){
            sizeOfSchema += getCollectionSize(collectionName, with_weight);
        }
        return sizeOfSchema;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       
    public int getNumberOfCollectionsInSchema(){
        return this.getNosqlSchema().getEntities().size();
    }
    
    public int getNumberOfDocumentsInSchema(){
        int numberOfDocuments = 0;
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : this.getNosqlSchema().getEntities()) {
            numberOfDocuments += dag.vertexSet().size();
        }
        return numberOfDocuments;
    }
    
    public int getNumberOfArraysOfDocumentsInSchema(){
        int numberOfArrays = 0;
        for (String collectionName : this.getListOfCollectionsInSchema()){
            numberOfArrays += this.size().getNumberOfArraysOfDocuments(collectionName);
        }
        return numberOfArrays;
    }
    
    public int getNumberOfAtomicAttributesInSchema(){
        int numberOfAtomic = 0;
        for (String collectionName : this.getListOfCollectionsInSchema()){
            numberOfAtomic += this.size().getNumberOfAtomicAttributes(collectionName);
        }
        return numberOfAtomic;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
    public int getNumberOfAtomicAttributes(String collectionName) {
        int numberOfAttributes = 0;
        
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> collection = getCollection(collectionName);        
        for (TableVertex vertex : collection.vertexSet()){
            numberOfAttributes += vertex.getFields().size();
        }        
        return numberOfAttributes;
    }

    public int getNumberOfDocuments(String collectionName) {                
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> collection = getCollection(collectionName);                        
        return collection.vertexSet().size();        
    }

    public int getNumberOfArraysOfDocuments(String collectionName) {
        int numberOfArraysOfDocuments = 0;
        
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> collection = getCollection(collectionName);

        if (collection != null) {
            Iterator<TableVertex> it = collection.vertexSet().iterator();
            while (it.hasNext()){
                TableVertex vertex = it.next();
                
                // Para cada aresta de saída do vertex...
                for (RelationshipEdge edge : collection.outgoingEdgesOf(vertex)){
                    if (edge.getTypeofNesting().equalsIgnoreCase("many_embedded")){
                        numberOfArraysOfDocuments++;
                    }
                }         
            }            
        }
        return numberOfArraysOfDocuments;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public int getNumberOfDocumentCopies(String collectionName, String docName){
        int numberOfCopies = 0;
        for (String doc : this.getListOfDocuments(collectionName)){
            if (doc.equalsIgnoreCase(docName)){
                numberOfCopies++;
            }
        }
        return numberOfCopies;
    }
    
    public Map<String,Integer> getDocumentCopiesMap(){
        Map<String,Integer> mapa = new HashMap<String,Integer>();    
        for (String doc : this.getListOfDocumentsInSchema()){
            int count = 0;
            if (mapa.get(doc) != null)
                count = mapa.get(doc);
            count++;
            mapa.put(doc, count);
        }
        return mapa;
    }
}