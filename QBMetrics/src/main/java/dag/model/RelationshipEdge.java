/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.model;

import java.io.Serializable;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Evandro
 */
public class RelationshipEdge extends DefaultEdge implements Serializable {
    private String typeofNesting;
    private RelationshipEdgeType relationshipType;
    private String oneSideEntity;
    private String manySideEntity;
    private String pkOneSide;
    private String fkManySide;
    private int oneSideEntityId;
    private int manySideEntityId;
    
    public RelationshipEdge(String oneSideEntity, String manySideEntity, String pkOneSide, String fkManySide) {        
        this.oneSideEntity = oneSideEntity;
        this.manySideEntity = manySideEntity;
        this.pkOneSide = pkOneSide;
        this.fkManySide = fkManySide;  
    }

    // TODO: excluir este construtor após validações do novo atributo RelationshipeEdgeTypeEnum
    public RelationshipEdge(String typeofNesting, String oneSideEntity, String manySideEntity, String pkOneSide, String fkManySide) {
        this.typeofNesting = typeofNesting;
        this.oneSideEntity = oneSideEntity;
        this.manySideEntity = manySideEntity;
        this.pkOneSide = pkOneSide;
        this.fkManySide = fkManySide;            
    }
    
    public RelationshipEdge(RelationshipEdgeType typeOfRelationship, String oneSideEntity, String manySideEntity, String pkOneSide, String fkManySide) {
        this.relationshipType = typeOfRelationship;
        this.oneSideEntity = oneSideEntity;
        this.manySideEntity = manySideEntity;
        this.pkOneSide = pkOneSide;
        this.fkManySide = fkManySide;            
    }

    public String getTypeofNesting() {
        // se typeofNesting não foi configurado pelo construtor desta classe, determinar através da análise das chaves
        if (typeofNesting == null){
            // se o vértice source da aresta for a entidade 'oneSideEntity', então o aninhamento é one_embedding.
            if (getSource().getTableName().equalsIgnoreCase(oneSideEntity)){
                typeofNesting = "embed_one_to_many";
            // se o vértice source da aresta for a entidade 'manySideEntity', então o aninhamento é many_embedding.
            } else if (getSource().getTableName().equalsIgnoreCase(manySideEntity)){
                typeofNesting = "embed_many_to_one";
            }            
        }
        return typeofNesting;
    }

    public void setTypeofNesting(String typeofNesting) {
        this.typeofNesting = typeofNesting;
    }

    public String getOneSideEntity() {
        return oneSideEntity;
    }

    public void setOneSideEntity(String oneSideEntity) {
        this.oneSideEntity = oneSideEntity;
    }

    public String getManySideEntity() {
        return manySideEntity;
    }

    public void setManySideEntity(String manySideEntity) {
        this.manySideEntity = manySideEntity;
    }

    public String getPkOneSide() {
        return pkOneSide;
    }

    public void setPkOneSide(String pkOneSide) {
        this.pkOneSide = pkOneSide;
    }

    public String getFkManySide() {
        return fkManySide;
    }

    public void setFkManySide(String fkManySide) {
        this.fkManySide = fkManySide;
    }

    public String getKeySource() {
        // Determinando os campos key do vertex source e target conforme indicação do lado 1 e lado muitos.
        if (this.getSource().getTableName().equalsIgnoreCase(oneSideEntity)){
            return pkOneSide;            
        } else {
            return fkManySide;            
        }
    }

    public String getKeyTarget() {
        // Determinando os campos key do vertex source e target conforme indicação do lado 1 e lado muitos.
        if (this.getTarget().getTableName().equalsIgnoreCase(oneSideEntity)){
            return pkOneSide;            
        } else {
            return fkManySide;            
        }        
    }

    public int getOneSideEntityId() {
        return oneSideEntityId;
    }

    public void setOneSideEntityId(int oneSideEntityId) {
        this.oneSideEntityId = oneSideEntityId;
    }

    public int getManySideEntityId() {
        return manySideEntityId;
    }

    public void setManySideEntityId(int manySideEntityId) {
        this.manySideEntityId = manySideEntityId;
    }

    public RelationshipEdgeType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipEdgeType relationshipType) {
        this.relationshipType = relationshipType;
    }
    
    @Override
    public String toString() {
        // Alterei este método para melhor visualização no DAG (interface gráfica).
        String msg = "";
        if (typeofNesting != null) {
            if (typeofNesting.equalsIgnoreCase("embed_one_to_many")){
                msg = "obj";
            } else {
                msg = "array";
            }
        }
        return msg;
    }

    @Override
    public TableVertex getSource() {
        return (TableVertex) super.getSource(); 
    }

    @Override
    public TableVertex getTarget() {
        return (TableVertex) super.getTarget(); 
    }
}
