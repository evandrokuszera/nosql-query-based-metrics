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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class JoinCoverage extends Metrics {
    private MatrixMetadata metadata = new MatrixMetadata();
    
    public JoinCoverage(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }
    
    public ArrayList<JoinResult> getSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths) {
        ArrayList<JoinResult> joinResults = new ArrayList<>();
        
        ArrayList<String> queryPaths = null;
        if (invertQueryPaths) {
            queryPaths = this.path().getInvertedPaths(query);
        } else {
            queryPaths = this.path().getPaths(query);
        }
        
        // Criando metadados para gerar a lista de coleções a partir da matriz JoinCollection.
        String schemaCollections[] = new String[this.getListOfCollectionsInSchema().size()];
        metadata.collectionNames = this.getListOfCollectionsInSchema().toArray(schemaCollections);

        // Percorrendo os caminhos da consulta...
        for (String queryPath : queryPaths) {
            // Criando metadados para gerar a lista de coleções a partir da matriz JoinCollection.
            metadata.queryVerticesNames = queryPath.split("/");
            
            // Matriz usada para identificar os caminhos entre coleções para responder o query path.
            int[][] matrix = buildJoinCollectionMatrix(queryPath);
//            printAdjacencyMatrix(matrix, queryPath);
            
            ArrayList<String> joinCollectionList = new ArrayList<>();
            
            buildJoinCollectionListFromMatrix(0, 0, matrix, "", joinCollectionList);
//            System.out.println("Combinações de coleções para responder consulta: \n" + joinCollectionList);                      
            
            for (String colList : joinCollectionList){
                String joinPaths = buildJoinPath(colList);
                String joinCollections = mergeSameJoinPaths(colList);
                joinResults.add( new JoinResult(queryPath, joinPaths, joinCollections.split(",").length, joinCollections));
            }
            Collections.sort(joinResults);            
        }
        
        return joinResults;
    }
    
    // Definição do Método:
    //  - Alterou o padrão das demais métricas (Path, SubPath, IndPath). Motivo: uma consulta pode ter 2 ou mais paths.
    //  - É necessário determinar o melhor Join entre coleções para cada Query Path.
    //  - Este método cria e retorna um JoinResult Unindo o melhor JoinResult de cada path da query.
    //
    public JoinResult getMaxSchemaCoverage(DirectedAcyclicGraph<TableVertex, RelationshipEdge> query, boolean invertQueryPaths){
        ArrayList<JoinResult> results = getSchemaCoverage(query, invertQueryPaths);
        JoinResult newJoinResult = null;

        ArrayList<String> queryPaths = null;
        if (invertQueryPaths) {
            queryPaths = this.path().getInvertedPaths(query);
        } else {
            queryPaths = this.path().getPaths(query);
        }
        
        // Usando para armazenar o nome de coleções necessárias para responder a todos os query paths. Set foi usado para não manter coleções duplicadas.
        HashSet<String> collectionsToAnswerAllQueryPaths = new HashSet<>();
        
        // Percorrendo os caminhos da consulta...
        for (String queryPath : queryPaths) {
            // Para cada query path, recuperar o melhor JoinResult (menor número de coleções para responder Query Path).
            JoinResult jr = getSmallestJoinResultByQueryPath(queryPath, results);
            for (String col : jr.getCollectionList().split(",")){
                collectionsToAnswerAllQueryPaths.add(col);
            }
        }
        
        // Montando a lista final de coleções para responder todos os query paths. Esse procedimento foi neccessário pois uma consulta pode ter dois ou mais paths.
        String collectionList = "";
        for (String col : collectionsToAnswerAllQueryPaths){
            if (collectionList.length() > 0) collectionList +=",";
            collectionList += col;
        }
        
        // Criando um novo JoinResult com a nova lista de coleções para responder todos os paths da query.
        newJoinResult = new JoinResult("all", collectionsToAnswerAllQueryPaths.size(), collectionList);
        
        return newJoinResult;
    }
    
    //
    // Melhor JoinResult: aquele que tem o MENOR número de coleções para responder um Query Path.
    //
    public JoinResult getSmallestJoinResultByQueryPath(String queryPath, ArrayList<JoinResult> joinResults){
        JoinResult betterJoinResult = null;
        
        for (JoinResult jr : joinResults){
            if (jr.getQueryPath().equalsIgnoreCase(queryPath)){
                if (betterJoinResult == null) betterJoinResult = jr;
                
                if (jr.getNumberOfCollection() < betterJoinResult.getNumberOfCollection()){
                    betterJoinResult = jr;
                }
            }
        }
        return betterJoinResult;
    }
    

    public int hasVertex(String collectionName, String vertexName) {
        for (TableVertex tableVertex : this.getCollection(collectionName).vertexSet()) {
            if (tableVertex.getTableName().equalsIgnoreCase(vertexName)) {
                return 1;
            }
        }
        return 0;
    }
    
    // Definição do método:
    // Recebe um elemento de junção, que é composto por: queryVertice1:colVertice1,..., queryVerticeN:colVerticeN
    // Retorna os caminhos necessários para responder a consulta. Esses caminhos devem ser "juntados" (lookup) para implementar a consulta.
    // Exemplo de elemento de junção:
        //[orders:customers,orderlines:customers, orders:customers,orderlines:orders, orders:customers,orderlines:products, orders:orders,orderlines:customers, orders:orders,orderlines:orders, orders:orders,orderlines:products]
        //    onde o primeiro elemento de junção é: orders:customers,orderlines:customers
        //    onde orders: é o query vertice e customers é a coleção onde foi encontrado o query vertice, o mesmo processo se repete para orderlines:customers
    public String buildJoinPath(String joinCollectionElement){
        String joinPath = "";
        
        for (String colItem : joinCollectionElement.split(",")){
            String queryVerticeName = colItem.split(":")[0];
            String collectionName = colItem.split(":")[1];

            if (! joinPath.isEmpty()){
                joinPath += ",";
            }                
            joinPath += this.path().getDocumentPaths(queryVerticeName, collectionName).get(0); // INDICE = 0? E SE TIVER MAIS PATHS? PEGAR O MENOR?
        }            
        
        return joinPath;
    }
    
    // Definição do Método:
    //  Junta elementos adjacentes do Join Path, caso ambos estejam na mesma coleção.
    //  Ás vezes, dois vértices da consulta estão na mesma coleção e são consecutivos. 
    //  Neste caso, o função abaixo considera as duas entradas no Join Path como somente uma, reduzindo o tamanho do Join Path.
    public String mergeSameJoinPaths(String joinCollectionElement){
        String newPath = "";
        String collections[] = joinCollectionElement.split(",");

        String c_current = collections[0].split(":")[1];
        newPath =  c_current;
        for (int i=1; i<collections.length; i++){
            String c_next = collections[i].split(":")[1];
            if (!c_current.equalsIgnoreCase(c_next)){
                newPath += "," + c_next;
            }
            c_current = c_next;
        }

        return newPath;
    }
    
    // Definição do método:
    //  Constroi uma matriz relacionando os vértices da consulta com as coleções do esquema.
    //  Estrutura da matriz:
    //   - Linha = Vértices da consulta
    //   - Colunas = Coleções do esquema
    //  A interseção linha x coluna = 1, significa que o Query Vertice está presente na coleção.
    //   - hasVertex(collname, queryvertex) retorna 1 ou 0;
    //  Ao percorrer a matriz de cima para baixo é possível derivar caminhos de junção (Join Paths) entre coleções para responder a consulta como um todo.
    //
    public int[][] buildJoinCollectionMatrix(String queryPath) {
        String[] queryVertices = queryPath.split("/");

        int nbQueryVertices = queryVertices.length;
        int nbCollections = this.getListOfCollectionsInSchema().size();

        int[][] matrix = new int[nbQueryVertices][nbCollections];
        for (int c = 0; c < this.getListOfCollectionsInSchema().size(); c++) {
            for (int v = 0; v < queryVertices.length; v++) {
                String collectionName = this.getListOfCollectionsInSchema().get(c);
                String queryVertex = queryVertices[v];

                matrix[v][c] = hasVertex(collectionName, queryVertex);
            }
        }
        return matrix;
    }
    
    // Definição do método:
    //  Método recursivo para criar os possíveis caminhos de junção entre coleções de acordo com os dados da matriz criada por buildJoinCollectionMatrix.
    //  O resultado é uma coleção de caminhos entre coleções que tem os vértices para responder a consulta.
    //
    public void buildJoinCollectionListFromMatrix(int ii, int jj, int[][] matrix, String path, ArrayList<String> joinCollectionList) {
        String matrixPath = path;
        for (int i = ii; i < matrix.length; i++) {
            for (int j = jj; j < matrix[i].length; j++) {
                if (matrix[i][j] == 1) {
                    if (matrixPath.length() > 0) matrixPath += ",";
                    matrixPath += metadata.queryVerticesNames[i] + ":" + metadata.collectionNames[j];
                    buildJoinCollectionListFromMatrix(i+1, 0, matrix, matrixPath, joinCollectionList);
                    matrixPath = path;
                }
                if(j==matrix[i].length-1) return; // se é a última coluna retorna...
            }
        }
        joinCollectionList.add(path);
        //System.out.println(path);
    }
        
    

    public void printAdjacencyMatrix(int[][] matrix, String queryPath) {
        String[] queryVertices = queryPath.split("/");

        // Imprimindo cabeçalho...
        System.out.print("Query Vertex");
        for (String col : this.getListOfCollectionsInSchema()) {
            System.out.print("\t" + fillWithSpaces(col, 12));
        }
        System.out.println("");

        // Imprindo dados da matrix...
        for (int v = 0; v < matrix.length; v++) {
            System.out.print(fillWithSpaces(queryVertices[v], 12));
            for (int c = 0; c < matrix[v].length; c++) {
                String item = String.valueOf(matrix[v][c]);
                System.out.print("\t" + fillWithSpaces(item, 12));
            }
            System.out.println("");
        }
    }

    public static String fillWithSpaces(String item, int tam) {
        String spaces = "";
        int missingSpaces = tam - item.length();
        
        if (missingSpaces < 0) return item;
        for (int i = 0; i < missingSpaces; i++) {
            spaces += " ";
        }
        item += spaces;
        return item;
    }
    
    // Usado para nomear as coleções e vértices da consulta com base nos índices da matriz de join collection.
    //  é uma classe de apoio.
    private class MatrixMetadata{
        String queryVerticesNames[];
        String collectionNames[];       
    }
}