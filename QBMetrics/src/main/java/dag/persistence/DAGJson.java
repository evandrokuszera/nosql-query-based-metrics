/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.persistence;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import static dag.utils.GraphUtils.getTableVertexByName;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class DAGJson {
    
    // O arquivo JSON é composto por arrays de vértices e arestas.
    public static JSONObject toJSON(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag, String dagName){
        JSONObject dagJSON = new JSONObject();          
        dagJSON.put("name", dagName);
        dagJSON.put("vertices", new JSONArray());
        dagJSON.put("edges", new JSONArray());
        
        // Put Vertices
        for (TableVertex tVertex : dag.vertexSet()){
            JSONObject vertex = new JSONObject();            
            vertex.put("id", tVertex.getId());
            vertex.put("name", tVertex.getName());
            vertex.put("table_name", tVertex.getTableName());
            vertex.put("pk", tVertex.getPk());
            
            // Creating array of fields of vertex
            vertex.put("fields", new JSONArray());            
            for (String field : tVertex.getFields()){
                vertex.getJSONArray("fields").put(field);
            }
            
            dagJSON.getJSONArray("vertices").put(vertex);
        }
        
        // Put Edges
        for (RelationshipEdge rEdge : dag.edgeSet()){
            JSONObject edge = new JSONObject();
            edge.put("one_side_entity", rEdge.getOneSideEntity());
            edge.put("many_side_entity", rEdge.getManySideEntity());
            edge.put("pk_one_side", rEdge.getPkOneSide());
            edge.put("fk_many_side", rEdge.getFkManySide());  
            edge.put("source", rEdge.getSource().getName());
            edge.put("target", rEdge.getTarget().getName());
                    
            dagJSON.getJSONArray("edges").put(edge);
        }
        
        return dagJSON;
    }
          
    // Cria um dag com base em um arquivo json.
    public static DirectedAcyclicGraph<TableVertex, RelationshipEdge> fromJSON(JSONObject jsonDAG){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = new DirectedAcyclicGraph<>(RelationshipEdge.class);
        
        // Percorre arquivo json e cria vértices do DAG...
        for (int i=0; i<jsonDAG.getJSONArray("vertices").length(); i++){
            // Recupera um vértice do json...
            JSONObject vertex = (JSONObject) jsonDAG.getJSONArray("vertices").get(i);
            // Cria objeto TableVertex com os dados do objeto json...          
            TableVertex tVertex = new TableVertex(vertex.getString("name"), vertex.getString("table_name"), vertex.getString("pk"));
            tVertex.setId(vertex.getInt("id"));
            // Cria os field do TableVertex...
            for (int j=0; j<vertex.getJSONArray("fields").length(); j++){
                tVertex.getFields().add(vertex.getJSONArray("fields").getString(j));
            }            
            // Adiciona o vértice no DAG...
            dag.addVertex(tVertex);
        }
        
        // Percorre arquivo json e cria as arestas do DAG.
        for (int i=0; i<jsonDAG.getJSONArray("edges").length(); i++){
            JSONObject edge = jsonDAG.getJSONArray("edges").getJSONObject(i);
            RelationshipEdge rEdge = new RelationshipEdge(edge.getString("one_side_entity"), edge.getString("many_side_entity"), edge.getString("pk_one_side"), edge.getString("fk_many_side"));
            
            // Adiciona a aresta no DAG...            
            dag.addEdge(
                    getTableVertexByName(edge.getString("source"), dag),
                    getTableVertexByName(edge.getString("target"), dag),
                    rEdge);
        }
        
        return dag;        
    }
}
