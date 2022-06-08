/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

import metrics.coverage.PathCoverage;
import metrics.coverage.IndirectPathCoverage;
import metrics.coverage.SubPathCoverage;
import dag.nosql_schema.NoSQLSchema;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.utils.GraphUtils;
import metrics.coverage.EdgeCoverage;
import metrics.coverage.JoinCoverage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class Metrics {
    private NoSQLSchema nosqlSchema;
    public static String[] METRICS = {"vertex", "path", "subpath", "indpath", "*path", "*subpath", "*indpath"};
    
    
    public Metrics(NoSQLSchema nosqlSchema) {
        this.nosqlSchema = nosqlSchema;
    }

    public NoSQLSchema getNosqlSchema() {
        return nosqlSchema;
    }

    public void setNosqlSchema(NoSQLSchema nosqlSchema) {
        this.nosqlSchema = nosqlSchema;
    }
    
    public Path path(){
        return new Path(nosqlSchema);
    }
    
    public Depth depth(){
        return new Depth(nosqlSchema);
    }
    
    public Size size(){
        return new Size(nosqlSchema);
    }
    
    // Através desse objeto acesso as métricas versão 1.0, onde não eram retornados os melhores casos, mas sim a cobertura de caminhos do esquema.
    // Artigo CoopIS19 usou essas métricas. Não passou!
    public Query query(){
        return new Query(nosqlSchema);
    }
    
    // Através dos objetos abaixo, versão 2.0, as métricas retornam o melhor caso de cobertura do esquema, analisando coleção por coleção. 
    // A profundidade do caminho determina o melhor caso.
    // Início da versao 2.0:
    public PathCoverage queryPath(){
        return new PathCoverage(nosqlSchema);
    }
    
    public SubPathCoverage querySubPath(){
        return new SubPathCoverage(nosqlSchema);
    }
    
    public IndirectPathCoverage queryIndirectPath(){
        return new IndirectPathCoverage(nosqlSchema);
    }
    
    public JoinCoverage queryJoin(){
        return new JoinCoverage(nosqlSchema);
    }
    
    public EdgeCoverage queryEdge(){
        return new EdgeCoverage(nosqlSchema);
    }
    // Fim da versão 2.0;
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public DirectedAcyclicGraph<TableVertex, RelationshipEdge> getCollection(String collectionName) {
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> collection : this.nosqlSchema.getEntities()) {
            if (GraphUtils.getRootVertex(collection).getName().equalsIgnoreCase(collectionName)) {
                return collection;
            }
        }
        return null;
    }
    
    public boolean existDocumentInSchema(String documentName){
        for (String doc : this.getListOfDocumentsInSchema()){
            if (doc.equalsIgnoreCase(documentName)){
                return true;
            }
        }
        return false;
    }
            
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public double getRedundancyRate(){
        double numberOfDocumentsInSchema = this.size().getNumberOfDocumentsInSchema();
        double numberOfDistinctsDocumentsInSchema = this.size().getDocumentCopiesMap().size();
        
        double rate = (numberOfDocumentsInSchema * 100) / numberOfDistinctsDocumentsInSchema - 100;
        
        BigDecimal rateDecimal = new BigDecimal(rate);
        rateDecimal = rateDecimal.setScale(2, RoundingMode.HALF_EVEN);
        
        if (rateDecimal.doubleValue() > 0){
            return rateDecimal.doubleValue();
        } else {
            return 0;
        }
    }
    
    public double getRedundancyFactor(){
        double numberOfDocumentsInSchema = this.size().getNumberOfDocumentsInSchema();
        double numberOfDistinctsDocumentsInSchema = this.size().getDocumentCopiesMap().size();
        
        double rate = numberOfDocumentsInSchema / numberOfDistinctsDocumentsInSchema;
        
        BigDecimal rateDecimal = new BigDecimal(rate);
        rateDecimal = rateDecimal.setScale(2, RoundingMode.HALF_EVEN);
        
        if (rateDecimal.doubleValue() > 0){
            return rateDecimal.doubleValue();
        } else {
            return 0;
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public ArrayList<String> getListOfCollectionsInSchema() {
        ArrayList<String> collections = new ArrayList<>();
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : this.nosqlSchema.getEntities()) {
            collections.add(GraphUtils.getRootVertex(dag).getName());
        }
        return collections;
    }
    
    public ArrayList<String> getListOfDocumentsInSchema() {
        ArrayList<String> documents = new ArrayList<>();
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : this.nosqlSchema.getEntities()) {
            for (TableVertex vertex : dag.vertexSet()){
                documents.add(vertex.getName());
            }            
        }        
        return documents;
    }
    
    public ArrayList<String> getListOfArraysOfDocumentInSchema() {
        ArrayList<String> arrays = new ArrayList<>();        
        for (String collectionName : this.getListOfCollectionsInSchema()){
            arrays.addAll( this.getListOfArraysOfDocuments(collectionName) );
        }        
        return arrays;
    }

    public ArrayList<String> getListOfDocuments(String collectionName) {
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> collection = getCollection(collectionName);

        if (collection != null) {
            ArrayList<String> listOfDocuments = new ArrayList<>();            
            TableVertex rootVertex = GraphUtils.getRootVertex(collection);
            Iterator<TableVertex> it = collection.vertexSet().iterator();
            while (it.hasNext()){
                TableVertex vertex = it.next();
                if (vertex != rootVertex){
                    listOfDocuments.add(vertex.getName());
                }                
            }
            return listOfDocuments;
        }
        return null;
    }
    
    public ArrayList<String> getListOfArraysOfDocuments(String collectionName) {
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> collection = getCollection(collectionName);

        if (collection != null) {
            ArrayList<String> listOfArrays = new ArrayList<>();     
            Iterator<TableVertex> it = collection.vertexSet().iterator();
            while (it.hasNext()){
                TableVertex vertex = it.next();
                
                // Para aresta de saída do vertex...
                for (RelationshipEdge edge : collection.outgoingEdgesOf(vertex)){
                    if (edge.getTypeofNesting().equalsIgnoreCase("embed_many_to_one")){
                        listOfArrays.add(vertex.getName());
                    }
                }         
            }
            return listOfArrays;
        }
        return null;
    } 
}
