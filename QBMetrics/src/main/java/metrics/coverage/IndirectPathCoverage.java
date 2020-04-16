/*
 * Responsável por medir IndirectPathCoverage.
 *
 * Considera subcaminhos com vértices adicionais inclusos.
 *
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
public class IndirectPathCoverage extends Metrics {
    
    public IndirectPathCoverage(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTAÇÕES: INDIRECT PATH COVERAGE
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private CoverageResult indirectPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        double numOfIndirectPathsDetected = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        int depthOfIndPath = 0;
        
        // Pré-requisito 1: (queryVertex > 1). Para existir indirect path é necessário que a consulta tenha dois ou mais vértices para análise.
        if (query.vertexSet().size() < 2) return new CoverageResult(collectionName, 0.0, 0);
        
        ArrayList<String> queryPaths = null;
        if (invertQueryPaths){
            queryPaths = this.path().getInvertedPaths(query);
        } else {
            queryPaths = this.path().getPaths(query);
        }
        
        // Para cada caminho da consulta (consulta pode ter vários caminhos)...
        for (String queryPath : queryPaths){            
            // Para cada caminho do schema...
            for (String collectionPath : this.path().getCollectionPaths(collectionName)){
                // Verificação dos pré-requisitos:
                // Pré-requisito 2: pathVertex > 2. Para existir indirect path dentro de um caminho do esquema é necessário que este caminho tenha três ou mais vértices.
                if (this.depth().countNumberOfPathBars(0, collectionPath.length(), collectionPath)<2) continue;
                // Pré-requisito 3: queryVertex < pathVertex. Para existir indirect path, é necessário que o número de vértices da consulta seja menor que o número de vértices do caminho do esquema.
                if (this.depth().countNumberOfPathBars(0, queryPath.length(), queryPath) >= this.depth().countNumberOfPathBars(0, collectionPath.length(), collectionPath)) continue;
                                
                // Criando vetores de vértices...
                String[] vetorQueryVertices = queryPath.split("/");
                String[] vetorCollectionVertices = collectionPath.split("/");
                
                int lastCorrespondenceIndex = -1;
                int numberOfCorrespondences = 0;
                boolean indirectPathDetected = false;
                // Comparando os vértices da query com os vértices do caminho do schema
                for (int i=0; i<vetorQueryVertices.length; i++){
                    for (int j=0; j<vetorCollectionVertices.length; j++){
                        // se encontrar correspondência entre vértices...
                        if (vetorQueryVertices[i].equalsIgnoreCase(vetorCollectionVertices[j])){
                            if (depthOfIndPath == 0) depthOfIndPath = j+1; // ATENÇÃO: verificar mais casos, talvez somente essa atribuição não resolva o problema para determinar o depth do indirectPath.
                            
                            // identifica se é um caso de indirect path, onde um caminho do esquema contém os vértices da consulta, no entanto, há vértices intermediários no caminho do esquema que separam os vértices da consulta.
                            if (lastCorrespondenceIndex != -1 && j - lastCorrespondenceIndex > 1){                                
                                indirectPathDetected = true;
                            }
                            lastCorrespondenceIndex = j;
                            numberOfCorrespondences++;
                        }
                    }
                }
                
                if (numberOfCorrespondences >= vetorQueryVertices.length){
                    if (indirectPathDetected) {
                        numOfIndirectPathsDetected++;
                        break; // achou correspondência, interrompe laço for (String collectionPath... ) para processar próximo queryPath.
                    }                        
                }
            }
        }
        
        if (numOfIndirectPathsDetected==0) return new CoverageResult(collectionName, 0.0, 0);  
        
        double coverage = new BigDecimal( numOfIndirectPathsDetected / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        
        return new CoverageResult(collectionName, coverage, depthOfIndPath);
    }
    
    private ArrayList<CoverageResult> indirectPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<CoverageResult> results = new ArrayList<>();
        // Percorre todas as coleções do esquema...
        for (String collectionName : this.getListOfCollectionsInSchema()){
            // Retorna o valor da métrica
            results.add(indirectPathCoverage(collectionName, query, invertQueryPaths));
        }
        // Ordena array de resultados
        Collections.sort(results);
        return results;         
    }
    
    private CoverageResult maxIndirectPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<CoverageResult> results = indirectPathCoverageInSchema(query, invertQueryPaths);
        
        if (results.size() > 0){
            return results.get(0);
        } else {
            return null;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INDIRECT PATH
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getCollectionCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return indirectPathCoverage(collectionName, query, false);
    }
    
    public ArrayList<CoverageResult> getSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return indirectPathCoverageInSchema(query, false);
    }
    
    public CoverageResult getMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return maxIndirectPathCoverageInSchema(query, false);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INVERTED INDIRECT PATH
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CoverageResult getInvCollectionCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return indirectPathCoverage(collectionName, query, true);
    }
    
    public ArrayList<CoverageResult> getInvSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return indirectPathCoverageInSchema(query, true);
    }
    
    public CoverageResult getInvMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        return maxIndirectPathCoverageInSchema(query, true);
    }
}
