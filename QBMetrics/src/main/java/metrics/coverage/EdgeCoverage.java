/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics.coverage;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.NoSQLSchema;
import metrics.Metrics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class EdgeCoverage extends Metrics {

    public EdgeCoverage(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTAÇÕES: RETORNO DO NÚMERO DE ARESTAS CORRESPONDENTES ENTRE ESQUEMA E CONSULTA (INTERSEÇÃO DE ARESTAS DIRETAS, INVERTIDAS, QUALQUER TIPO)
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private ArrayList<RelationshipEdge> directEdgeIntersection(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag1, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag2){
        // Estabelecendo a interseção de arestas entre dag1 e dag2...
        ArrayList<RelationshipEdge> edgeIntersection = new ArrayList<>();        
        for (RelationshipEdge edgeDag1 : dag1.edgeSet()){            
            for (RelationshipEdge edgeDag2 : dag2.edgeSet()){
                // se as arestas foram iguais em ambos os grafos (dag1 and dag2)...
                // critério de igualdade: TableName
                if (edgeDag1.getSource().getTableName().equalsIgnoreCase(edgeDag2.getSource().getTableName()))
                    if (edgeDag1.getTarget().getTableName().equalsIgnoreCase(edgeDag2.getTarget().getTableName())){
                        // interseção localizada...
                        edgeIntersection.add(edgeDag1);
                    }
            }            
        }
        return edgeIntersection;
    }
    
    private ArrayList<RelationshipEdge> invertedEdgeIntersection(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag1, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag2){
        // Estabelecendo a interseção de arestas entre dag1 e dag2...
        ArrayList<RelationshipEdge> edgeIntersection = new ArrayList<>();        
        for (RelationshipEdge edgeDag1 : dag1.edgeSet()){            
            for (RelationshipEdge edgeDag2 : dag2.edgeSet()){
                // se as arestas foram iguais em ambos os grafos (dag1 and dag2)...
                // critério de igualdade: TableName
                if (edgeDag1.getSource().getTableName().equalsIgnoreCase(edgeDag2.getTarget().getTableName()))
                    if (edgeDag1.getTarget().getTableName().equalsIgnoreCase(edgeDag2.getSource().getTableName())){
                        // interseção localizada...
                        edgeIntersection.add(edgeDag1);
                    }
            }            
        }
        return edgeIntersection;
    }
    
    private ArrayList<RelationshipEdge> allEdgeIntersection(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag1, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag2){
        // Estabelecendo a interseção de arestas entre dag1 e dag2...
        ArrayList<RelationshipEdge> edgeIntersection = new ArrayList<>();        
        for (RelationshipEdge edgeDag1 : dag1.edgeSet()){            
            for (RelationshipEdge edgeDag2 : dag2.edgeSet()){
                // se as arestas foram iguais em ambos os grafos (dag1 and dag2)...
                // critério de igualdade: TableName
                // não importa a direção das arestas, mas se conecta dois vértices.
                if (edgeDag1.getSource().getTableName().equalsIgnoreCase(edgeDag2.getTarget().getTableName())){
                    if (edgeDag1.getTarget().getTableName().equalsIgnoreCase(edgeDag2.getSource().getTableName())){
                        // interseção localizada... sentido invertido.
                        edgeIntersection.add(edgeDag1);
                    }
                } else if (edgeDag1.getSource().getTableName().equalsIgnoreCase(edgeDag2.getSource().getTableName())){
                    if (edgeDag1.getTarget().getTableName().equalsIgnoreCase(edgeDag2.getTarget().getTableName())){
                        // interseção localizada... sentido direto
                        edgeIntersection.add(edgeDag1);
                    }
                }
            }            
        }
        return edgeIntersection;
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. DIRECT EDGE COVERAGE:
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getCollectionDirectEdgeCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numberOfDirectEdges = directEdgeIntersection(this.getCollection(collectionName), query).size();
        double numberOfQueryEdges = query.edgeSet().size();
        
        //System.out.println("collectionName: " + collectionName + "\tnumberOfDirectEdges: " + numberOfDirectEdges + "\tnumberOfQueryPaths = " +  numberOfQueryEdges);
        
        // não encontrou correspondência entre arestas
        if (numberOfDirectEdges == 0){
            return new CoverageResult(collectionName, 0.0, 0);
        }
        
        double coverage = new BigDecimal( numberOfDirectEdges / numberOfQueryEdges ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        
        return new CoverageResult(collectionName, coverage, 0);
    }
    
    public ArrayList<CoverageResult> getSchemaDirectEdgeCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        ArrayList<CoverageResult> results = new ArrayList<>();        
        for (String collectionName : this.getListOfCollectionsInSchema()){
            results.add(getCollectionDirectEdgeCoverage(collectionName, query) );
        }
        // Ordena results pelo campo value e depth.
        Collections.sort(results);
        return results;
    }
    
    public CoverageResult getMaxSchemaDirectEdgeCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        ArrayList<CoverageResult> results = getSchemaDirectEdgeCoverage(query);
        
        if (results.size() > 0){
            return results.get(0);
        } else {
            return null;
        }
    }
    
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 2. INVERTED EDGE COVERAGE:
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getCollectionInvertedEdgeCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numberOfInvertedEdges = invertedEdgeIntersection(this.getCollection(collectionName), query).size();
        double numberOfQueryEdges = query.edgeSet().size();
        //System.out.println("collectionName: " + collectionName + "\tnumberOfInvertedEdges: " + numberOfInvertedEdges + "\tnumberOfQueryPaths = " +  numberOfQueryEdges);
        // não encontrou correspondência entre arestas
        if (numberOfInvertedEdges == 0){
            return new CoverageResult(collectionName, 0.0, 0);
        }
        
        double coverage = new BigDecimal( numberOfInvertedEdges / numberOfQueryEdges ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        
        return new CoverageResult(collectionName, coverage, 0);
    }
    
    public ArrayList<CoverageResult> getSchemaInvertedEdgeCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        ArrayList<CoverageResult> results = new ArrayList<>();        
        for (String collectionName : this.getListOfCollectionsInSchema()){
            results.add(getCollectionInvertedEdgeCoverage(collectionName, query) );
        }
        // Ordena results pelo campo value e depth.
        Collections.sort(results);
        return results;
    }
    
    public CoverageResult getMaxSchemaInvertedEdgeCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        ArrayList<CoverageResult> results = getSchemaInvertedEdgeCoverage(query);
        
        if (results.size() > 0){
            return results.get(0);
        } else {
            return null;
        }
    }
    
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 3. ALL EDGE COVERAGE:
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getCollectionAllEdgeCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numberOfAllEdges = allEdgeIntersection(this.getCollection(collectionName), query).size();
        double numberOfQueryEdges = query.edgeSet().size();
        //System.out.println("collectionName: " + collectionName + "\tnumberOfAllEdges: " + numberOfAllEdges + "\tnumberOfQueryPaths = " +  numberOfQueryEdges);
        // não encontrou correspondência entre arestas
        if (numberOfAllEdges == 0){
            return new CoverageResult(collectionName, 0.0, 0);
        }
        double coverage = new BigDecimal( numberOfAllEdges / numberOfQueryEdges ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        
        return new CoverageResult(collectionName, coverage, 0);
    }
    
    public ArrayList<CoverageResult> getSchemaAllEdgeCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        ArrayList<CoverageResult> results = new ArrayList<>();        
        for (String collectionName : this.getListOfCollectionsInSchema()){
            results.add(getCollectionAllEdgeCoverage(collectionName, query) );
        }
        // Ordena results pelo campo value e depth.
        Collections.sort(results);
        return results;
    }
    
    public CoverageResult getMaxSchemaAllEdgeCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        ArrayList<CoverageResult> results = getSchemaAllEdgeCoverage(query);
        
        if (results.size() > 0){
            return results.get(0);
        } else {
            return null;
        }
    }
    
}
