/*
 * ATENÇÃO: LEIA OS COMENTÁRIOS ATÉ O FINAL:
 *
 * Esta classe contém todas as métricas relacionadas ao Query Coverage:
 *  - Paths, SubPaths, IndPaths,
 *  - *Paths, *SubPaths, *IndPaths,
 *
 * Foi utilizada na versão enviadas para CoopIS'19, onde não passou pelos seguintes problemas:
 *  - dados não foram normalizados (valor > 1);
 *  - campo score foi mal definido;
 *  - faltou métrica relacionada a JOIN.
 *
 * Estas métricas não consideram profundidade dos sub e ind paths.
 *  - estou reimplementando as métricas em dag_metrics.coverage.
 *  - foco na coleção e melhor caso (onde, qual coleção, achou o melhor caminho no esquema).
 */
package metrics;

import dag.nosql_schema.NoSQLSchema;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class Query extends Metrics {

    public Query(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // VERTEX Coverage
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public ArrayList<TableVertex> getVertexIntersection(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag1, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag2){
        // Estabelecendo a interseção de vértices entre dag1 e dag2...
        ArrayList<TableVertex> vertexIntersection = new ArrayList<>();        
        for (TableVertex vertexDag1 : dag1.vertexSet()){            
            for (TableVertex vertexDag2 : dag2.vertexSet()){
                // se os vértices forem iguais em ambos os grafos (dag1 and dag2)...
                // critério de igualdade: TableName
                if (vertexDag1.getTableName().equalsIgnoreCase(vertexDag2.getTableName()))                    
                    // estabelece interseção...
                    vertexIntersection.add(vertexDag1);                    
            }            
        }
        return vertexIntersection;
    }    
    
    public double getVertexCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query) {
        ArrayList<TableVertex> tableVertexIntersection = this.getVertexIntersection(this.getCollection(collectionName), query);        
        // calculando a cobertura...
        double intersection_vertexs = tableVertexIntersection.size();
        double query_vertexs = query.vertexSet().size();        
        return new BigDecimal( intersection_vertexs / query_vertexs ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();        
    }

    public double getVertexCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query) {    
        double numVertexCorrespondentes = 0;
        double numQueryVertex = query.vertexSet().size();
        
        for (TableVertex vertex : query.vertexSet()){
            if (this.existDocumentInSchema(vertex.getTableName())){
                numVertexCorrespondentes++;
            }
        }
        
        return new BigDecimal( numVertexCorrespondentes / numQueryVertex ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // EDGE Coverage
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public ArrayList<RelationshipEdge> getEdgeIntersection(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag1, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag2){
        // Estabelecendo a interseção de arestas entre dag1 e dag2...
        ArrayList<RelationshipEdge> edgeIntersection = new ArrayList<>();        
        for (RelationshipEdge edgeDag1 : dag1.edgeSet()){            
            for (RelationshipEdge edgeDag2 : dag2.edgeSet()){
                // se as arestas foram iguais em ambos os grafos (dag1 and dag2)...
                // critério de igualdade: TableName
                if (edgeDag1.getSource().getTableName().equalsIgnoreCase(edgeDag2.getSource().getTableName()))
                    if (edgeDag1.getTarget().getTableName().equalsIgnoreCase(edgeDag2.getTarget().getTableName())){
                        // estabelece interseção...
                        edgeIntersection.add(edgeDag1);
                    }
            }            
        }
        return edgeIntersection;
    }
    
    public double getEdgeCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        // retornando a interseção de edges entre collection e path.        
        ArrayList<RelationshipEdge> edgeIntersection = this.getEdgeIntersection(this.getCollection(collectionName), query);        
        // calculando a cobertura...
        double intersection_edges = edgeIntersection.size();
        double query_edges = query.edgeSet().size();  
        if (query_edges == 0) return 0; // evitando NaN...
        return new BigDecimal( intersection_edges / query_edges ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double getEdgeCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numEdgesCorrespondentes = 0;
        double numQueryEdges = query.edgeSet().size();
        boolean flag = false; // flag para interronper o laço interno quando encontrar correspondência entre edgeQuery e edgeCollection.
        
        // percorrer as edges da consulta
        for (RelationshipEdge edgeQuery : query.edgeSet()){           
            flag = false;
            // percorrer as coleções do esquema
            for (String collectionName : this.getListOfCollectionsInSchema()){
                if (flag) continue;
                // para cada coleção do esquema, percorrer suas edges
                for (RelationshipEdge edgeCollection : this.getCollection(collectionName).edgeSet()){
                    // verificar se a edge da consulta é igual a edge da coleção
                    if (edgeQuery.getSource().getTableName().equalsIgnoreCase(edgeCollection.getSource().getTableName()))
                        if (edgeQuery.getTarget().getTableName().equalsIgnoreCase(edgeCollection.getTarget().getTableName())){
                            // conta como edge correspondente
                            numEdgesCorrespondentes++;
                            flag = true;
                            continue;
                        }
                }
            }
        }
        if (numEdgesCorrespondentes==0){
            return 0;
        } 
        return new BigDecimal( numEdgesCorrespondentes / numQueryEdges ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PATH COVERAGE
    // pathIntersetion: neste caso, o caminho compreende os vértices entre nó raiz e nó folha.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public ArrayList<String> getPathIntersection(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag1, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag2, boolean dag_1_contains){
        // Estabelecendo a interseção de caminhos entre dag1 e dag2...
        ArrayList<String> pathIntersection = new ArrayList<>();        
        ArrayList<String> pathsDag1 = this.path().getPaths(dag1);
        ArrayList<String> pathsDag2 = this.path().getPaths(dag2);
        
        for (String pathDag1 : pathsDag1){            
            for (String pathDag2 : pathsDag2){
                if (dag_1_contains){
                    // se os paths do dag2 estão contidos nos paths do dag1...
                    //  adiciona em array de interseção...
                    if (pathDag1.contains(pathDag2)){
                        pathIntersection.add(pathDag1);
                    }
                } else {
                    // se os paths forem iguais em ambos os grafos (dag1 and dag2)...
                    //  adiciona em array de interseção...
                    if (pathDag1.equalsIgnoreCase(pathDag2)){
                        pathIntersection.add(pathDag1);
                    }    
                }
            }            
        }
        return pathIntersection;
    }  
    
    // PATH
    public double getPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        // retornando a interseção de paths entre collection e path.        
        ArrayList<String> pathIntersection = this.getPathIntersection(this.getCollection(collectionName), query, false);
        
        // calculando a cobertura...
        double intersection_paths = pathIntersection.size();
        double paths = this.path().getPaths(query).size();
        
        return new BigDecimal( intersection_paths / paths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    
    public double getPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Para cada caminho da consulta...
        for (String queryPath : this.path().getPaths(query)){
            // Procura correspondência entre caminhos (query e schema).
            if (this.path().hasPath(queryPath)){
                numPathsCorrespondentes++;
            }
        }
        
        if (numPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();                
    }
    
    // SUB PATH
    public double getSubPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        double numPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        for (String queryPath : this.path().getPaths(query)){
            if (this.path().hasSubPath(queryPath, collectionName) > 0){
                numPathsCorrespondentes++;
            }
        }
        
        if (numPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }    
    
    public double getSubPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Considerar apenas consultas com 2 ou mais vértices? Se sim, descomente!
        //if (query.vertexSet().size() < 2) return 0;        
        
        // Para cada caminho da consulta...
        for (String queryPath : this.path().getPaths(query)){
            // Procura correspondência entre caminhos (query e schema).
            if (this.path().hasSubPath(queryPath) > 0){
                numPathsCorrespondentes++;
            }
        }
        
        if (numPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();                
    }
        
    // INVERTED *PATH
    public double getInvertPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numInvertedPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Considerar apenas consultas com 2 ou mais vértices? Se sim, descomente!
        //if (query.vertexSet().size() < 2) return 0;
        
        // Para cada caminho INVERTIDO da consulta...
        for (String invertedQueryPath : this.path().getInvertedPaths(query)){
            // Procura correspondência de caminho no esquema.
            if (this.path().hasPath(invertedQueryPath)){
                numInvertedPathsCorrespondentes++;
            }
        }
        
        if (numInvertedPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numInvertedPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double getInvertPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        double numInvertedPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Considerar apenas consultas com 2 ou mais vértices? Se sim, descomente!
        //if (query.vertexSet().size() < 2) return 0;
        
        for (String invertedQueryPath : this.path().getInvertedPaths(query)){
            if (this.path().hasPath(invertedQueryPath, collectionName)){
                numInvertedPathsCorrespondentes++;
            }
        }        
        if (numInvertedPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numInvertedPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    // INVERTED *SUBPATH
    public double getInvertSubPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numInvertedSubPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Considerar apenas consultas com 2 ou mais vértices? Se sim, descomente!
        //if (query.vertexSet().size() < 2) return 0;
        
        // Para cada caminho INVERTIDO da consulta...
        for (String invertedQueryPath : this.path().getInvertedPaths(query)){
            // Procura correspondência de sub caminho no esquema.
            if (this.path().hasSubPath(invertedQueryPath) > 0){
                numInvertedSubPathsCorrespondentes++;
            }
        }
        
        if (numInvertedSubPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numInvertedSubPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    public double getInvertSubPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){ 
        double numInvertedSubPathsCorrespondentes = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Considerar apenas consultas com 2 ou mais vértices? Se sim, descomente!
        //if (query.vertexSet().size() < 2) return 0;
        
        for (String invertedQueryPath : this.path().getInvertedPaths(query)){
            if (this.path().hasSubPath(invertedQueryPath, collectionName) > 0){
                numInvertedSubPathsCorrespondentes++;
            }
        }        
        if (numInvertedSubPathsCorrespondentes==0) return 0;        
        return new BigDecimal( numInvertedSubPathsCorrespondentes / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    
    
    
    // INDIRECT PATH
    public double getIndirectPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numOfIndirectPathsDetected = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Pré-requisito 1: (queryVertex > 1). Para existir indirect path é necessário que a consulta tenha dois ou mais vértices para análise.
        if (query.vertexSet().size() < 2) return 0.0;
        
        // Para cada caminho da consulta (consulta pode ter vários caminhos)...
        for (String queryPath : this.path().getPaths(query)){
            // Para cada caminho do schema...
            for (String schemaPath : this.path().getSchemaPaths()){
                // Verificação dos pré-requisitos:
                // Pré-requisito 2: pathVertex > 2. Para existir indirect path dentro de um caminho do esquema é necessário que este caminho tenha três ou mais vértices.
                if (this.depth().countNumberOfPathBars(0, schemaPath.length(), schemaPath)<2) continue;
                // Pré-requisito 3: queryVertex < pathVertex. Para existir indirect path, é necessário que o número de vértices da consulta seja menor que o número de vértices do caminho do esquema.
                if (this.depth().countNumberOfPathBars(0, queryPath.length(), queryPath) >= this.depth().countNumberOfPathBars(0, schemaPath.length(), schemaPath)) continue;
                                
                // Criando vetores de vértices...
                String[] vetorQueryVertices = queryPath.split("/");
                String[] vetorSchemaVertices = schemaPath.split("/");
                
                int lastCorrespondenceIndex = -1;
                int numberOfCorrespondences = 0;
                boolean indirectPathDetected = false;
                // Comparando os vértices da query com os vértices do caminho do schema
                for (int i=0; i<vetorQueryVertices.length; i++){
                    for (int j=0; j<vetorSchemaVertices.length; j++){
                        // se encontrar correspondência entre vértices...
                        if (vetorQueryVertices[i].equalsIgnoreCase(vetorSchemaVertices[j])){
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
                        break; // achou correspondência, interrompe laço for (String schemaPath... ) para processar próximo queryPath.
                    }                        
                }
            }
        }
        
        if (numOfIndirectPathsDetected==0) return 0;        
        return new BigDecimal( numOfIndirectPathsDetected / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();                
    }
    
    public double getIndirectPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numOfIndirectPathsDetected = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Pré-requisito 1: (queryVertex > 1). Para existir indirect path é necessário que a consulta tenha dois ou mais vértices para análise.
        if (query.vertexSet().size() < 2) return 0.0;
        
        // Para cada caminho da consulta (consulta pode ter vários caminhos)...
        for (String queryPath : this.path().getPaths(query)){
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
        
        if (numOfIndirectPathsDetected==0) return 0;        
        return new BigDecimal( numOfIndirectPathsDetected / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
    
    
    // INVERTED *INDIRECT PATH
    public double getInvertIndirectPathCoverageInSchema(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numOfIndirectPathsDetected = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Pré-requisito 1: (queryVertex > 1). Para existir indirect path é necessário que a consulta tenha dois ou mais vértices para análise.
        if (query.vertexSet().size() < 2) return 0.0;
        
        // Para cada caminho INVERTIDO da consulta (consulta pode ter vários caminhos)...
        for (String queryPath : this.path().getInvertedPaths(query)){
            // Para cada caminho do schema...
            for (String schemaPath : this.path().getSchemaPaths()){
                // Verificação dos pré-requisitos:
                // Pré-requisito 2: pathVertex > 2. Para existir indirect path dentro de um caminho do esquema é necessário que este caminho tenha três ou mais vértices.
                if (this.depth().countNumberOfPathBars(0, schemaPath.length(), schemaPath)<2) continue;
                // Pré-requisito 3: queryVertex < pathVertex. Para existir indirect path, é necessário que o número de vértices da consulta seja menor que o número de vértices do caminho do esquema.
                if (this.depth().countNumberOfPathBars(0, queryPath.length(), queryPath) >= this.depth().countNumberOfPathBars(0, schemaPath.length(), schemaPath)) continue;
                                
                // Criando vetores de vértices...
                String[] vetorQueryVertices = queryPath.split("/");
                String[] vetorSchemaVertices = schemaPath.split("/");
                
                int lastCorrespondenceIndex = -1;
                int numberOfCorrespondences = 0;
                boolean indirectPathDetected = false;
                // Comparando os vértices da query com os vértices do caminho do schema
                for (int i=0; i<vetorQueryVertices.length; i++){
                    for (int j=0; j<vetorSchemaVertices.length; j++){
                        // se encontrar correspondência entre vértices...
                        if (vetorQueryVertices[i].equalsIgnoreCase(vetorSchemaVertices[j])){
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
                        break; // achou correspondência, interrompe laço for (String schemaPath... ) para processar próximo queryPath.
                    }                        
                }
            }
        }
        
        if (numOfIndirectPathsDetected==0) return 0;        
        return new BigDecimal( numOfIndirectPathsDetected / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();                
    }
    
    public double getInvertIndirectPathCoverage(String collectionName, DirectedAcyclicGraph<TableVertex, RelationshipEdge> query){
        double numOfIndirectPathsDetected = 0;
        double numQueryPaths = this.path().getPaths(query).size();
        
        // Pré-requisito 1: (queryVertex > 1). Para existir indirect path é necessário que a consulta tenha dois ou mais vértices para análise.
        if (query.vertexSet().size() < 2) return 0.0;
        
        // Para cada caminho INVERTIDO da consulta (consulta pode ter vários caminhos)...
        for (String queryPath : this.path().getInvertedPaths(query)){
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
        
        if (numOfIndirectPathsDetected==0) return 0;        
        return new BigDecimal( numOfIndirectPathsDetected / numQueryPaths ).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    
}
