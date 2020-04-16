/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metrics;

import dag.nosql_schema.NoSQLSchema;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.utils.GraphUtils;
import java.util.ArrayList;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class Path extends Metrics {

    public Path(NoSQLSchema nosqlSchema) {
        super(nosqlSchema);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PATHS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
    public ArrayList<String> getPaths(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag){
        ArrayList<String> paths = new ArrayList<>();                
        TableVertex root = GraphUtils.getRootVertex(dag);
        
        dfs_path(root, dag, root.getName(), paths);         
        return paths;
    }
    
    private void dfs_path(TableVertex vertex, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag, String parentPath, ArrayList<String> paths) {
        
        for (TableVertex current : Graphs.predecessorListOf(dag, vertex)) {            
            String parentPathBackup = parentPath;
            
            // Registra caminhos de vértices
            parentPath += "/" + current.getName();            
            // Recursão...
            dfs_path(current, dag, parentPath, paths);  
            // Retorna parentPath ao estado anterior para processar demais predecessores...
            parentPath = parentPathBackup;
        } 
        // Se for vertex folha... adiciona caminho da raiz até folha.
        if (Graphs.predecessorListOf(dag, vertex).size() == 0){            
            paths.add(parentPath);        
        }        
    }
    
    public ArrayList<String> getFieldPaths(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag){
        ArrayList<String> paths = new ArrayList<>();        
        TableVertex root = GraphUtils.getRootVertex(dag);
        
        paths.add(root.getName());        
        for (String field : root.getFields()){
            paths.add(root.getName() + "/" + field);
        }
        
        dfs_field_path(root, dag, root.getName(), paths);         
        return paths;
    }
    
    private void dfs_field_path(TableVertex vertex, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag, String parentPath, ArrayList<String> paths) {        
        for (TableVertex current : Graphs.predecessorListOf(dag, vertex)) {
            // Registra caminhos de vértices
            parentPath += "/" + current.getName();
            paths.add(parentPath);
            
            // Recursão...
            dfs_traverse(current, dag, parentPath, paths);  
            
            //Adiciona todos os fields do vértice como caminho.
            for (String field : current.getFields()){
                paths.add(parentPath + "/" + field);
            }            
        } 
    }
    
    public ArrayList<String> getDocumentPaths(String documentName){
        ArrayList<String> paths = new ArrayList<>();
        for (String path : getSchemaPaths()){
            if (path.contains(documentName)){
                paths.add(path);
            }
        }
        return paths;        
    }
    
    public ArrayList<String> getDocumentPaths(String documentName, String collectionName){
        ArrayList<String> paths = new ArrayList<>();
        for (String path : getCollectionPaths(collectionName)){
            if (path.contains(documentName)){
                paths.add(path);
            }
        }
        return paths;        
    }
    
    public ArrayList<String> getArrayPath(String arrayName) {
        // preciso definir uma codificação para identificar quando é coleção ou documento dentro de um path.
        //
        //
        //
        return null;
    }
    
    public ArrayList<String> getCollectionPaths(String collectionName){ 
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag  = this.getCollection(collectionName);        
        return getPaths(dag);        
    }
    
    public ArrayList<String> getSchemaPaths(){ 
        ArrayList<String> schemaPaths = new ArrayList<>();
        for (String collectionName : this.getListOfCollectionsInSchema()){
            schemaPaths.addAll(getCollectionPaths(collectionName));
        }
        return schemaPaths;
    }
    
    public String getInvertedPath(String path){
        // Recupera os vértices do path
        String[] pathVertices = path.split("/");

        // Invertendo os vértices do path
        int head = 0;
        int tail = pathVertices.length - 1;
        while (head < tail){
            String aux = pathVertices[head];
            pathVertices[head] = pathVertices[tail];
            pathVertices[tail] = aux;
            head++;
            tail--;
        }

        // Montando o caminho invertido do path
        String invertedPath = pathVertices[0];
        for (int i=1; i<pathVertices.length; i++){
            invertedPath += "/" + pathVertices[i];
        }
        
        return invertedPath;
    }
    
    public ArrayList<String> getInvertedPaths(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag){
        ArrayList<String> invertedPaths = new ArrayList<>();        
        for (String path : this.getPaths(dag)){
            invertedPaths.add( getInvertedPath(path) );
        }        
        return invertedPaths;
    }
    
    public boolean hasPath(String path){
        for (String p : getSchemaPaths()){
            if (p.equalsIgnoreCase(path)){
                return true;
            }
        }
        return false;
    }   
    
    public boolean hasPath(String path, String collectionName){
        for (String p : getCollectionPaths(collectionName)){
            if (p.equalsIgnoreCase(path)){
                return true;
            }
        }
        return false;
    }
    
    // Retorna 0 quando não acha subpath; Retorna > 0 (1,2,...,n), representando a profundidade do subpath encontrado.
    public int hasSubPath(String path){
        for (String schemaPath : getSchemaPaths()){
            if (schemaPath.contains(path)){
                // código para determinar depth do subpath
                if (schemaPath.indexOf(path) == 0) return 1;
                // subpath não começa na raiz... calcular profundidade onde foi encontrado o subpath no esquema.
                int depth = depth().countNumberOfPathBars(0, schemaPath.indexOf(path), schemaPath);
                return depth + 1;
            }
        }
        return 0;
    }
    
    // Retorna 0 quando não acha subpath; Retorna > 0 (1,2,...,n), representando a profundidade do subpath encontrado.
    public int hasSubPath(String path, String collectionName){
        for (String colPath : getCollectionPaths(collectionName)){
            if (colPath.contains(path)){
                // código para determinar depth do subpath
                if (colPath.indexOf(path) == 0) return 1;
                // subpath não começa na raiz... calcular profundidade onde foi encontrado o subpath na coleção.
                int depth = depth().countNumberOfPathBars(0, colPath.indexOf(path), colPath);
                return depth + 1;
            }
        }
        return 0;
    }
    
    private void dfs_traverse(TableVertex vertex, DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag, String parentPath, ArrayList<String> paths) {
        
        for (TableVertex current : Graphs.predecessorListOf(dag, vertex)) {
            // Registra caminhos de vértices
            parentPath += "/" + current.getName();
            paths.add(parentPath);
            
            // Recursão...
            dfs_traverse(current, dag, parentPath, paths);  
            
            // Adiciona todos os fields do vértice como caminho.
            for (String field : current.getFields()){
                paths.add(parentPath + "/" + field);
            }
            
        } 
    }
}
