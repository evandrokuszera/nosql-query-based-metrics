/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.model;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class TableVertex implements Serializable {
    private static int tableVertexCount = 0;
    private int id;
    private String name;
    private String tableName;
    private String pk;
    private ArrayList<String> fields;
    private JSONObject jsonSchema;
    
    private ArrayList<TableColumnVertex> typeFields;
    private String entityClassName;

    public ArrayList<TableColumnVertex> getTypeFields() {
        return typeFields;
    }

    public void setTypeFields(ArrayList<TableColumnVertex> typeFields) {
        this.typeFields = typeFields;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }
    // remove field from fields and typeFields collections
    public void removeField(String fieldName){
        
        for (String field : fields){
            if (field.equals(fieldName)){
                fields.remove(field);
                break;
            }
        }
        
        for (TableColumnVertex columnField : typeFields){
            if (columnField.getColumnName().equals(fieldName)){
                typeFields.remove(columnField);
                break;
            }
        }
        
    }

    public TableVertex(String name, String tableName, String pk) {
        this.name = name;
        this.tableName = tableName;
        this.pk = pk;
        this.fields = new ArrayList<>();
        this.typeFields = new ArrayList<>();
        this.id = ++tableVertexCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }

    public JSONObject getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(JSONObject jsonSchema) {
        this.jsonSchema = jsonSchema;
    }
    
    @Override
    public String toString() {
        //return "TableVertex{" + "name=" + name + ", tableName=" + tableName + ", pk=" + pk + ", fields=" + fields + '}';
        return name+" ("+this.id+")";
    }
    
}
