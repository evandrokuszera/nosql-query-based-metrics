/*
 * Esta classe retorna métricas estruturais sobre o NoSQLSchema.
 */
package dag.to_json_schema;

import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class JSONMetrics {

//    private NoSQLSchema schema;
//
//    public JSONMetrics(NoSQLSchema schema) {
//        this.schema = schema;
//    }
//
//    public NoSQLSchema getSchema() {
//        return schema;
//    }
//
//    public void setSchema(NoSQLSchema schema) {
//        this.schema = schema;
//    }
//
//    public JSONObject getCollection(String name) {
//        for (JSONObject collection : this.schema.getEntities()) {
//            if (collection.getString("title").equalsIgnoreCase(name)) {
//                return collection;
//            }
//        }
//        return null;
//    }
//
//    ////////////////////////////////////////////////////////////////////////////
//    // MÉTRICAS
//    ////////////////////////////////////////////////////////////////////////////
//    public ArrayList<String> getListOfCollections() {
//        ArrayList<String> collections = new ArrayList<>();
//
//        for (JSONObject obj : this.schema.getEntities()) {
//            collections.add(obj.getString("title"));
//        }
//
//        return collections;
//    }
//
//    public ArrayList<String> getListOfDocuments(String collectionName) {
//        JSONObject collection = getCollection(collectionName);
//
//        if (collection != null) {
//            ArrayList<String> listOfDocuments = new ArrayList<>();
//
//            goNextJsonObj(collection, listOfDocuments);
//
//            return listOfDocuments;
//        }
//        return null;
//    }
//
////    private void goNextJsonObj(JSONObject obj, ArrayList<String> listOfDocuments){
////        if (! obj.has("properties"))
////            return;
////        else{
////            for (String field : obj.getJSONObject("properties").keySet()){
////                goNextJsonObj(obj.getJSONObject("properties").getJSONObject(field), listOfDocuments);                
////            }
////            listOfDocuments.add(obj.getString("title"));
////        }
////    }
//    private void goNextJsonObj(JSONObject obj, ArrayList<String> listOfDocuments) {
//        if (obj.getString("type").equalsIgnoreCase("object")) {
//            
//            for (String field : obj.getJSONObject("properties").keySet()){
//                goNextJsonObj(obj.getJSONObject("properties").getJSONObject(field), listOfDocuments);                
//            }
//            listOfDocuments.add(obj.getString("title"));
//            
//        } else if (obj.getString("type").equalsIgnoreCase("array")) {
//            
//            if (obj.getJSONObject("items").getString("type").equalsIgnoreCase("object")){
//                for (String field : obj.getJSONObject("items").getJSONObject("properties").keySet()){
//                    goNextJsonObj(obj.getJSONObject("items").getJSONObject("properties").getJSONObject(field), listOfDocuments);                
//                }
//                listOfDocuments.add(obj.getJSONObject("items").getString("title"));
//            }  
//        
//        } else {
//            return;
//        }
//    }
//
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    public ArrayList<String> getListOfDocumentsInSchema() {
//        return null;
//    }
//
//    public int getDocumentSize(String name) { // pode ser pelo path no lugar do name.
//        return 0;
//    }
//
//    public int getCollectionSize(String name) {
//        return 0;
//    }
//
//    public int getNumberOfAtomicAttributes(String collection) {
//        return 0;
//    }
//
//    public int getNumberOfDocuments(String collection) {
//        return 0;
//    }
//
//    public int getNumberOfArrays(String collection) {
//        return 0;
//    }
//
//    public int getDepthOfCollection(String collection) {
//        return 0;
//    }
//
//    public int getDepthOfDocument(String collection, String document) {
//        return 0;
//    }
//
//    public int getDepthOfSchema() {
//        return 0;
//    }
//
//    public int getMaxDocDepth(String name) {
//        return 0;
//    }
//
//    public int getMinDocDepth(String name) {
//        return 0;
//    }
//
//    public String getDocumentPath(String name) { // pode haver mais que um caminho para um documento.
//        return null;
//    }
//
//    public String getArrayPath(String name) { // pode haver mais que um caminho para um array.
//        return null;
//    }
}
