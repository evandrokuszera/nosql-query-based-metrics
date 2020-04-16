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
        
        // Recuperando as entities (DAGs)        
        for (int i=0; i<obj.getJSONArray("entities").length(); i++){
            schema.getEntities().add( DAGJson.fromJSON(obj.getJSONArray("entities").getJSONObject(i)) );
        }
                
        return schema;
    }
    
    public static JSONObject toJSON(NoSQLSchema schema){        
        JSONObject obj = new JSONObject();        
        obj.put("name", schema.getName());
        obj.put("description", schema.getDescription());
        
        obj.put("entities", new JSONArray());
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : schema.getEntities()){
            JSONObject dagJSON = DAGJson.toJSON(dag, GraphUtils.getRootVertex(dag).getName()); // getName ou getTableName??
            obj.getJSONArray("entities").put(dagJSON);
        }
        
        return obj;
    }    
     
}
