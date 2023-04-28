/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.nosql_schema;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.utils.GraphUtils;
import java.util.ArrayList;
import java.util.Iterator;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class NoSQLSchema {
    private String name;
    private String description;
    private ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> entities;
    // We used the RelationshipEdge type to represent relationship between entities.
    // However, the relationships are not add to entities DAG.
    private ArrayList<RelationshipEdge> refEntities; 

    public NoSQLSchema(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
        this.refEntities = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public TableVertex getTableVertexById(int id){
        for (int i=0; i<getEntities().size(); i++){
            Iterator<TableVertex> vertexIterator = getEntities().get(i).vertexSet().iterator();
            while (vertexIterator.hasNext()) {
                TableVertex table = vertexIterator.next();
                if (table.getId() == id){
                    return table;
                }
            }
        }
        return null;
    }
    
    public String getEntityName(int index){
        if (index < entities.size()){
            return GraphUtils.getRootVertex(entities.get(index)).getName();
        }
        return "";        
    }

    public ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> getEntities() {
        return entities;
    }

    public void setEntities(ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> entities) {
        this.entities = entities;
    }

    public ArrayList<RelationshipEdge> getRefEntities() {
        return refEntities;
    }

    public void setRefEntities(ArrayList<RelationshipEdge> refEntities) {
        this.refEntities = refEntities;
    }
    
    // Build new Graph with all nosql entities
    public DirectedAcyclicGraph<TableVertex, RelationshipEdge> getDAGSchema(){
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dagSchema = new DirectedAcyclicGraph<>(RelationshipEdge.class);
        
        // Nó raiz do schema
        TableVertex schema = new TableVertex(name, "", "");
        dagSchema.addVertex(schema);
        
        // Para cada entidade (DAG) do esquema, adiciona ela no grafo dagSchema.
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> entityDAG : this.getEntities()){
            
            for (TableVertex v : entityDAG.vertexSet()){
                dagSchema.addVertex(v);
            }
            
            for (RelationshipEdge e : entityDAG.edgeSet()){
                dagSchema.addEdge(e.getSource(), e.getTarget(), e);
            }
            
            RelationshipEdge edgeToRoot = new RelationshipEdge(name, GraphUtils.getRootVertex(entityDAG).getTableName(), "", "");
            
            dagSchema.addEdge(GraphUtils.getRootVertex(entityDAG), schema, edgeToRoot);
        }
        return dagSchema;
    }
    
    @Override
    public String toString() {
        return "NoSQLSchema{" + "name=" + name + ", description=" + description + ", entities=" + entities + '}';
    }
    
    // Print the main the schema elements (vertices, edges, ref-edges), using a JSON format
    public void printSchema(){
        String format = "";
        System.out.printf("NoSQL Schema: %s, \n", getName());
        
        System.out.println("Entities: [");
        for (int i=0; i<getEntities().size(); i++){
            System.out.printf(" { Name: %s, ", getEntityName(i));
            
            System.out.printf("Vertices: [");
            Iterator<TableVertex> vertexIterator = getEntities().get(i).vertexSet().iterator();
            format = "";
            while (vertexIterator.hasNext()) {
                TableVertex table = vertexIterator.next();
                System.out.printf("%s%s(%d)", format, table.getName(), table.getId());
                format = ", "; // only to better readability
            }
            System.out.printf("], ");
            
            System.out.printf("Edges: [");
            Iterator<RelationshipEdge> edgeIterator = getEntities().get(i).edgeSet().iterator();
            format = "";
            while (edgeIterator.hasNext()) {
                RelationshipEdge edge = edgeIterator.next();
                System.out.printf("%s(%s <-- %s)", format, edge.getTarget().getName(), edge.getSource().getName());
                format = ", "; // only to better readability
            }
            System.out.printf("] }\n");
        } // end of for
        System.out.println("], ");
        
        System.out.print("Ref-Entities: [");
        format = "";
        for (int i=0; i<getRefEntities().size(); i++){
            RelationshipEdge edge = getRefEntities().get(i);
            System.out.printf("%s(%s <-- %s)", format, edge.getOneSideEntity(), edge.getManySideEntity());
            format = ", "; // only to better readability
        }
        System.out.print("]\n");
    }
}
