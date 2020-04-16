/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.persistence;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.ConversionProcess;
import dag.nosql_schema.NoSQLSchema;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class ConversionProcessJson {
    
    public static ConversionProcess fromJSON(JSONObject obj){        
        ConversionProcess cProcess = new ConversionProcess();
        if (obj.has("database")) cProcess.setDatabase(obj.getString("database"));
        if (obj.has("description")) cProcess.setDescription(obj.getString("description"));
        if (obj.has("driver")) cProcess.setDriver(obj.getString("driver"));
        if (obj.has("name")) cProcess.setName(obj.getString("name"));
        if (obj.has("password")) cProcess.setPassword(obj.getString("password"));
        if (obj.has("server")) cProcess.setServer(obj.getString("server"));
        if (obj.has("user")) cProcess.setUser(obj.getString("user"));
                
        // Recuperando os schemas   
        if (obj.has("schemas")){
            for (int i=0; i<obj.getJSONArray("schemas").length(); i++){
                cProcess.getSchemas().add( NoSQLSchemaJson.fromJSON( obj.getJSONArray("schemas").getJSONObject(i)) );
            }
        }

        // Recuperando as Queries ******************************
        if (obj.has("queries")){
            for (int i=0; i<obj.getJSONArray("queries").length(); i++){
                cProcess.getQueries().add( DAGJson.fromJSON( obj.getJSONArray("queries").getJSONObject(i)) );
            }
        }
        return cProcess;
    }
    
    public static JSONObject toJSON(ConversionProcess conversionProcess){        
        JSONObject obj = new JSONObject();  
        obj.put("database", conversionProcess.getDatabase());
        obj.put("description",conversionProcess.getDescription());
        obj.put("driver",conversionProcess.getDriver());
        obj.put("name",conversionProcess.getName());
        obj.put("password",conversionProcess.getPassword());
        obj.put("server",conversionProcess.getServer());
        obj.put("user",conversionProcess.getUser());
                
        obj.put("schemas", new JSONArray());
        for (NoSQLSchema schema : conversionProcess.getSchemas()){
            JSONObject schemaJSON = NoSQLSchemaJson.toJSON(schema);
            obj.getJSONArray("schemas").put(schemaJSON);
        }
        
        // setando as Queries ******************************
        obj.put("queries", new JSONArray());
        int count = 1;
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : conversionProcess.getQueries()){
            JSONObject dagJSON = DAGJson.toJSON(dag, "query " + count++);
            obj.getJSONArray("queries").put(dagJSON);
        }
        
        return obj;
    }
}