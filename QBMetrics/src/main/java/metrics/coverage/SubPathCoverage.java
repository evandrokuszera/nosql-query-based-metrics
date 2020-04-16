/*
 * Responsável por medir SubPathCoverage.
 *
 * Considera subcaminhos, ou seja, caminhos incluídos entre raiz e folha.
 *
 * Classe muito semelhante a QueryPathMetrics. Principal mudança: método this.path().hasSubPath(...) ou invés de hasPath(...)
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
public class SubPathCoverage extends Metrics {
    
    public SubPathCoverage(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTAÇÕES: SUBPATH COVERAGE
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private CoverageResult subPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){ 
        double matchingPaths = 0;
        double numOfQueryPaths = this.path().getPaths(query).size();
        int depthOfSubPath = 0;
        
        ArrayList<String> queryPaths = null;
        if (invertQueryPaths){
            queryPaths = this.path().getInvertedPaths(query);
        } else {
            queryPaths = this.path().getPaths(query);
        }
                
        // Percorrendo os caminhos da consulta...
        for (String queryPath : queryPaths){
            // Procura correspondência entre caminhos (query e collection).
            //if (this.path().hasSubPath(queryPath, collectionName)){
            if (this.path().hasSubPath(queryPath, collectionName) > 0){
                // valor maior que zero representa a profundidade do caminho encontrado.
                depthOfSubPath = this.path().hasSubPath(queryPath, collectionName);
                matchingPaths++;
            }
        }
        // se não encontrou correspondência na coleção... registra como 0.0
        if (matchingPaths == 0){
            return new CoverageResult(collectionName, 0.0, depthOfSubPath);
        }
        // calculando a cobertura de caminho.
        double coverage = new BigDecimal( matchingPaths / numOfQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        // criando e retornando objeto resultado...
        return new CoverageResult(collectionName, coverage, depthOfSubPath);
    }
    
    private ArrayList<CoverageResult> subPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<CoverageResult> results = new ArrayList<>();
        // Percorre todas as coleções do esquema...
        for (String collectionName : this.getListOfCollectionsInSchema()){
            // Retorna o valor da métrica
            results.add(subPathCoverage(collectionName, query, invertQueryPaths));
        }
        // Ordena array de resultados
        Collections.sort(results);
        return results;                
    }
    
    private CoverageResult maxSubPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<CoverageResult> results = subPathCoverageInSchema(query, invertQueryPaths);
        
        if (results.size() > 0){
            return results.get(0);
        } else {
            return null;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUBPATH
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getCollectionCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        return subPathCoverage(collectionName, query, false);
    }
    
    public ArrayList<CoverageResult> getSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return subPathCoverageInSchema(query, false);
    }
    
    public CoverageResult getMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return maxSubPathCoverageInSchema(query, false);
    }
    
        
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INVERTED SUBPATH
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getInvCollectionCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        return subPathCoverage(collectionName, query, true);
    }
    
    public ArrayList<CoverageResult> getInvSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return subPathCoverageInSchema(query, true);
    }
    
    public CoverageResult getInvMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return maxSubPathCoverageInSchema(query, true);
    }
    
}
