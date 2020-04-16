/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.utils;

import dag.persistence.DAGJson;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.persistence.JSONPersistence;
import java.util.ArrayList;
import java.util.Iterator;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class GraphUtils {
    
    
    //
    // CODIGOs COPIADOS DA CLASSE COMMANDGENERATOR !!!!
    //
    
    // Recupera todos os vértices folhas.
    // Vértice Folha = vertexs que NÃO possuem arestas de entrada.
    public static ArrayList<TableVertex> getLeafVertex(DirectedAcyclicGraph<TableVertex, RelationshipEdge> g) {
        ArrayList<TableVertex> leafVertexSet = new ArrayList<>();
        // Percorrendo conjunto de vértices do graph 'g'
        Iterator<TableVertex> vertexIterator = g.vertexSet().iterator();
        while (vertexIterator.hasNext()) {
            TableVertex vertex = vertexIterator.next();
            // Recuperando apenas vértices sem arestas de entrada (vértices folha).
            if (g.inDegreeOf(vertex) == 0) {
                leafVertexSet.add(vertex);
            }
        }
        return leafVertexSet;
    }
    
    // Recupera o vertex raiz. Esse vertex representa a entidade alvo da transformação.
    public static TableVertex getRootVertex(DirectedAcyclicGraph<TableVertex, RelationshipEdge> g) {
        TableVertex root = null;
        // Percorrendo conjunto de vértices do graph 'g'
        Iterator<TableVertex> vertexIterator = g.vertexSet().iterator();
        while (vertexIterator.hasNext()) {
            TableVertex vertex = vertexIterator.next();
            // Recuperando apenas o vértice sem arestas de saída. Esse vertice é raiz do grafo.
            if (g.outDegreeOf(vertex) == 0) {
                root = vertex;
            }
        }
        return root;
    }
    
    // Recupera um TableVertex com base no atributo 'name'.
    public static TableVertex getTableVertexByName(String name, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag){
        for (TableVertex tVertex : dag.vertexSet()){
            if (tVertex.getName().equalsIgnoreCase(name)){
                return tVertex;
            }
        }
        return null;
    }
   
    // Cria um dag com base em um arquivo json salvo no disco.
    public static DirectedAcyclicGraph<TableVertex, RelationshipEdge> loadDagFromJsonFile(String filePath){        
        // 1. Carrega json do arquivo filePath...        
        JSONObject jsonDAG = dag.persistence.JSONPersistence.loadJSONfromFile(filePath);
        // 2. Convert JSON to DAG
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = DAGJson.fromJSON(jsonDAG);      
        // 3. Return the DAG
        return dag;        
    }       
        
    // Salva em disco um DAG como um arquivo JSON. 
    public static void saveDagAsJsonObject(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag, String dagName, String filePath){  
        // 1. Convert the DAG to JSON
        JSONObject dagJSON = DAGJson.toJSON(dag, dagName);        
        // 2. Save JSON in filePath...         
        JSONPersistence.saveJSONtoFile(dagJSON, filePath);
    }
    
    
}