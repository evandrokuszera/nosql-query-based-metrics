/*
 * 
 * Responsável por medir PathCoverage.
 *
 * Considera apenas caminhos "completos", ou seja, raiz até folha.
 * 
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
public class PathCoverage extends Metrics {
    
    public PathCoverage(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTAÇÕES: PATH COVERAGE
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // 1. PathCoverage By Collection
    private CoverageResult pathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){ 
        double matchingPaths = 0;
        double numOfQueryPaths = this.path().getPaths(query).size();
                
        ArrayList<String> queryPaths = null;
        if (invertQueryPaths){
            queryPaths = this.path().getInvertedPaths(query);
        } else {
            queryPaths = this.path().getPaths(query);
        }
        
        // Percorrendo os caminhos da consulta...
        for (String queryPath : queryPaths){
            // Procura correspondência entre caminhos (query e collection).
            if (this.path().hasPath(queryPath, collectionName)){
                matchingPaths++;
            }
        }
        // se não encontrou correspondência na coleção... registra como 0.0
        if (matchingPaths == 0){
            return new CoverageResult(collectionName, 0.0, 0);
        }
        // calculando a cobertura de caminho.
        double coverage = new BigDecimal( matchingPaths / numOfQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        // criando e retornando objeto resultado...
        return new CoverageResult(collectionName, coverage, 1);
    }
    
    // 2. PathCoverage By Schema
    private ArrayList<CoverageResult> pathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<CoverageResult> results = new ArrayList<>();
        // Percorre todas as coleções do esquema...
        for (String collectionName : this.getListOfCollectionsInSchema()){
            // Retorna o valor da métrica
            results.add(pathCoverage(collectionName, query, invertQueryPaths));
        }
        // Ordena array de resultados
        Collections.sort(results);
        return results;                
    }
    
    // 3. MaxPathCoverage By Schema
    private CoverageResult maxPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<CoverageResult> results = pathCoverageInSchema(query, invertQueryPaths);
        
        if (results.size() > 0){
            return results.get(0);
        } else {
            return null;
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PATH
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getCollectionCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        return pathCoverage(collectionName, query, false);
    }
    
    public ArrayList<CoverageResult> getSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return pathCoverageInSchema(query, false);
    }
    
    public CoverageResult getMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return maxPathCoverageInSchema(query, false);
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INVERTED PATH
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getInvCollectionCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        return pathCoverage(collectionName, query, true);
    }
    
    public ArrayList<CoverageResult> getInvSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return pathCoverageInSchema(query, true);
    }
    
    public CoverageResult getInvMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return maxPathCoverageInSchema(query, true);
    }
    
}
