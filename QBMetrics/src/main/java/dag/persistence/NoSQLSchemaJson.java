/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.persistence;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.utils.GraphUtils;
import dag.nosql_schema.NoSQLSchema;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class NoSQLSchemaJson {
    
    public static NoSQLSchema fromJSON(JSONObject obj){        
        NoSQLSchema schema = new NoSQLSchema("");
        
        if (obj.has("name")) schema.setName( obj.getString("name") );
        if (obj.has("description")) schema.setDescription( obj.getString("description") );
        
        // Retrieving entities (DAGs)        
        for (int i=0; i<obj.getJSONArray("entities").length(); i++){
            schema.getEntities().add( DAGJson.fromJSON(obj.getJSONArray("entities").getJSONObject(i)) );
        }
        
        // Retrieving references between entities
        if (obj.isNull("ref_edges")){
            System.out.println("NoSQLSchemaJson.fromJSON() --> WARNING: Field 'ref_edges' not found in the schema (JSON file). The values are not loaded!");
        } else {
            for (int i=0; i<obj.getJSONArray("ref_edges").length(); i++){
                JSONObject jsonEdge = obj.getJSONArray("ref_edges").getJSONObject(i);
                
                RelationshipEdge relEdge = new RelationshipEdge(
                        jsonEdge.getString("type"), 
                        jsonEdge.getString("one_side_entity"), 
                        jsonEdge.getString("many_side_entity"), 
                        jsonEdge.getString("pk_one_side"), 
                        jsonEdge.getString("fk_many_side")
                );
                
                relEdge.setOneSideEntityId(jsonEdge.getInt("one_side_id") );
                relEdge.setManySideEntityId(jsonEdge.getInt("many_side_id") );
                
                schema.getRefEntities().add( relEdge );
            }
        }
                
        return schema;
    }
    
    public static JSONObject toJSON(NoSQLSchema schema){        
        JSONObject obj = new JSONObject();        
        obj.put("name", schema.getName());
        obj.put("description", schema.getDescription());
        
        obj.put("entities", new JSONArray());
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : schema.getEntities()){
            JSONObject dagJSON = DAGJson.toJSON(
                    dag, 
                    GraphUtils.getRootVertex(dag).getName()
            );
            obj.getJSONArray("entities").put(dagJSON);
        }
        
        // The 'ref_edges' array stores the relationshipe between entities in the schema.
        // It is important to note that this relationships are not nesting relationships, but reference relationships 
        obj.put("ref_edges", new JSONArray());
        for (RelationshipEdge refEdges : schema.getRefEntities()){
            JSONObject jsonRefEdge = new JSONObject();
            jsonRefEdge.put("one_side_entity", refEdges.getOneSideEntity());
            jsonRefEdge.put("many_side_entity", refEdges.getManySideEntity());
            jsonRefEdge.put("pk_one_side", refEdges.getPkOneSide());
            jsonRefEdge.put("fk_many_side", refEdges.getFkManySide());  
          //jsonRefEdge.put("source", refEdges.getSource().getName()); // this field is not used in the ref_edges
          //jsonRefEdge.put("target", refEdges.getTarget().getName()); // this field is not used in the ref_edges
            jsonRefEdge.put("type", refEdges.getRelationshipType().toString().toLowerCase());
            jsonRefEdge.put("one_side_id", refEdges.getOneSideEntityId());
            jsonRefEdge.put("many_side_id", refEdges.getManySideEntityId());
                    
            obj.getJSONArray("ref_edges").put(jsonRefEdge);
        }
        
        return obj;
    }    
     
}
