/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.persistence;

import dag.model.RelationshipEdge;
import dag.model.RelationshipEdgeType;
import dag.model.TableColumnVertex;
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
    
    // ***************************************************************
    // O arquivo JSON é composto por arrays de vértices e arestas.
    public static JSONObject toJSON(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag, String dagName){
        JSONObject dagJSON = new JSONObject();          
        dagJSON.put("name", dagName);
        dagJSON.put("vertices", new JSONArray());
        dagJSON.put("edges", new JSONArray());
        
        // Put Vertices
        for (TableVertex tVertex : dag.vertexSet()){
            JSONObject jsonVertex = new JSONObject();            
            jsonVertex.put("id", tVertex.getId());
            jsonVertex.put("name", tVertex.getName());
            jsonVertex.put("table_name", tVertex.getTableName());
            jsonVertex.put("pk", tVertex.getPk());
            
            // Creating array of fields of vertex
            jsonVertex.put("fields", new JSONArray());            
            for (String field : tVertex.getFields()){
                jsonVertex.getJSONArray("fields").put(field);
            }
            
            // Creating array of fields of vertex
            jsonVertex.put("typedFields", new JSONArray());            
            for (TableColumnVertex typedField : tVertex.getTypeFields()){
                JSONObject type = new JSONObject();
                type.put("name", typedField.getColumnName());
                type.put("type", typedField.getColumnType());
                type.put("pk", typedField.isPk());
                type.put("fk", typedField.isFk());
                jsonVertex.getJSONArray("typedFields").put(type);
            }
            
            dagJSON.getJSONArray("vertices").put(jsonVertex);
        }
        
        // Put Edges
        for (RelationshipEdge relEdge : dag.edgeSet()){
            JSONObject jsonEdge = new JSONObject();
            jsonEdge.put("one_side_entity", relEdge.getOneSideEntity());
            jsonEdge.put("many_side_entity", relEdge.getManySideEntity());
            jsonEdge.put("pk_one_side", relEdge.getPkOneSide());
            jsonEdge.put("fk_many_side", relEdge.getFkManySide());  
            jsonEdge.put("source", relEdge.getSource().getName());
            jsonEdge.put("target", relEdge.getTarget().getName());
//            jsonEdge.put("type", relEdge.getTypeofNesting());
            String relationshipTypeName = relEdge.getRelationshipType().name().toLowerCase();
            jsonEdge.put("type", relationshipTypeName);
            jsonEdge.put("one_side_id", relEdge.getOneSideEntityId());
            jsonEdge.put("many_side_id", relEdge.getManySideEntityId());
                    
            dagJSON.getJSONArray("edges").put(jsonEdge);
        }
        
        return dagJSON;
    }
          
    // ***************************************************************
    // Cria um dag com base em um arquivo json.
    public static DirectedAcyclicGraph<TableVertex, RelationshipEdge> fromJSON(JSONObject jsonDAG){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = new DirectedAcyclicGraph<>(RelationshipEdge.class);
        
        // Percorre arquivo json e cria vértices do DAG...
        for (int i=0; i<jsonDAG.getJSONArray("vertices").length(); i++){
            // Recupera um vértice do json...
            JSONObject jsonVertex = (JSONObject) jsonDAG.getJSONArray("vertices").get(i);
            // Cria objeto TableVertex com os dados do objeto json...          
            TableVertex tVertex = new TableVertex(jsonVertex.getString("name"), jsonVertex.getString("table_name"), jsonVertex.getString("pk"));
            tVertex.setId(jsonVertex.getInt("id"));
            // Cria os field do TableVertex...
            for (int j=0; j<jsonVertex.getJSONArray("fields").length(); j++){
                tVertex.getFields().add(jsonVertex.getJSONArray("fields").getString(j));
            } 
            
            if (!jsonVertex.isNull("typedFields")){
               // Cria os typedFields do TableVertex...
                for (int j=0; j<jsonVertex.getJSONArray("typedFields").length(); j++){
                    String columnName = jsonVertex.getJSONArray("typedFields").getJSONObject(j).getString("name");
                    String columnType = jsonVertex.getJSONArray("typedFields").getJSONObject(j).getString("type");
                    boolean isPk = jsonVertex.getJSONArray("typedFields").getJSONObject(j).getBoolean("pk");
                    boolean isFk = jsonVertex.getJSONArray("typedFields").getJSONObject(j).getBoolean("fk");
                    tVertex.getTypeFields().add(new TableColumnVertex(columnName, columnType, isPk, isFk));
                } 
            } else {
                System.out.println("DAGJson.fromJSON() --> WARNING: Field 'typedFields' not found in the schema (JSON file). The values are not loaded!");
            }
                
            // Adiciona o vértice no DAG...
            dag.addVertex(tVertex);
        }
        
        // Percorre arquivo json e cria as arestas do DAG.
        for (int i=0; i<jsonDAG.getJSONArray("edges").length(); i++){
            JSONObject jsonEdge = jsonDAG.getJSONArray("edges").getJSONObject(i);
            
            RelationshipEdgeType type = RelationshipEdgeType.valueOf(jsonEdge.getString("type").toUpperCase());
            
            RelationshipEdge relEdge = new RelationshipEdge(
//                    jsonEdge.getString("type"),
                    type,
                    jsonEdge.getString("one_side_entity"), 
                    jsonEdge.getString("many_side_entity"), 
                    jsonEdge.getString("pk_one_side"), 
                    jsonEdge.getString("fk_many_side")
            );
            
            if (jsonEdge.isNull("source_id") || jsonEdge.isNull("target_id")){
                System.out.println("DAGJson.fromJSON() --> WARNING: Field 'source_id' or 'targer_id' not found in the schema (JSON file). The values are not loaded!");
            } else {
                relEdge.setOneSideEntityId(jsonEdge.getInt("one_side_id") );
                relEdge.setManySideEntityId(jsonEdge.getInt("many_side_id") );
            }
            
            // Adiciona a aresta no DAG...            
            dag.addEdge(
                    getTableVertexByName(jsonEdge.getString("source"), dag),
                    getTableVertexByName(jsonEdge.getString("target"), dag),
                    relEdge
            );
        }
        
        return dag;        
    }
}
