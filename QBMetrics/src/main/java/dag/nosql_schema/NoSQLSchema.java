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
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class NoSQLSchema {
    private String name;
    private String description;
    private ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> entities;

    public NoSQLSchema(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
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
}
