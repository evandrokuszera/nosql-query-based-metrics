/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.model;

/**
 *
 * @author evand
 */
public class TableColumnVertex {
    private boolean pk;
    private String columnName;
    private String columnType;

    public TableColumnVertex() { }
    
    public TableColumnVertex(String columnName, String columnType, boolean isPk) {
        this.pk = isPk;
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    @Override
    public String toString() {
        return columnName + " (" + columnType + ")" + (isPk() ? " - PK" : "");
    }
}