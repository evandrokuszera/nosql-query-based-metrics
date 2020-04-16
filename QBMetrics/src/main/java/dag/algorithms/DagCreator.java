/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.algorithms;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import jdbc_connection.GenericConnection;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public abstract class DagCreator {
    protected GenericConnection connection;    
    protected DatabaseMetaData metadata;
    protected DirectedAcyclicGraph<TableVertex, RelationshipEdge> graph;    

    public DagCreator(GenericConnection connection) {
        this.connection = connection;
    }
    
    // Esse método deve ser implementado de acordo com a abordagem para criar o DAG a partir dos metadados do RDB.
    public abstract DirectedAcyclicGraph create(String main_table) throws SQLException; 
        
    // Verifica se existe edge entre dois vertex. A consulta é pelo nome do vertex.
    protected boolean existEdgeBetween(String sourceTable, String targetTable){
        Iterator<RelationshipEdge> it = graph.edgeSet().iterator();
        
        while (it.hasNext()){
            RelationshipEdge edge = it.next();
            if (
                    edge.getSource().getName().equalsIgnoreCase(sourceTable) &&
                    edge.getTarget().getName().equalsIgnoreCase(targetTable)
                    ||
                    edge.getSource().getName().equalsIgnoreCase(targetTable) &&
                    edge.getTarget().getName().equalsIgnoreCase(sourceTable)
                ) {
                return true;
            }
        }
        return false;
    }

    // Cria TableVertex com base na table_name.
    // Os campos da table_name são extraídos dos metadados do RDB.
    protected TableVertex createVertexFromTable(String table_name) throws SQLException {

        ResultSet rsPrimaryKeys = metadata.getPrimaryKeys("", "", table_name);

        // Obs.: estou considerando somente PKs simples. Suporte a PKs compostas não foi implementado.
        // Recuperando a chave primária da table_name.
        String primaryKeyName = "";
        while (rsPrimaryKeys.next()) {
            primaryKeyName = rsPrimaryKeys.getString("COLUMN_NAME");
        }
        rsPrimaryKeys.close();

        // Criando novo vertex com base nos dados da tabela RDB.
        TableVertex newTableVertex = new TableVertex(table_name, table_name, primaryKeyName);
        // Adicionando os campos do novo vertex com base nos metadados da tabela RDB.
        ResultSet rsTableFields = metadata.getColumns("", "", table_name, "");
        while (rsTableFields.next()) {
            newTableVertex.getFields().add(rsTableFields.getString("COLUMN_NAME"));
        }
        rsTableFields.close();

        return newTableVertex;
    }
    
    // Método de Teste que imprime algumas informações da main_table.
    public void printSomeInformations(String main_table) throws SQLException {
        ResultSet rs;
        main_table = "order";
        
        System.out.println("MAIN_TABLE: " + main_table);
        connection.openConnection();

        metadata = connection.getConnection().getMetaData();

        // getPrimaryKeys: Qual é a chave primária da main_table?
        rs = metadata.getPrimaryKeys("", "", main_table);
        while (rs.next()) {
            System.out.println("getPrimaryKeys: " + rs.getString("TABLE_NAME") + ", " + rs.getString("COLUMN_NAME"));
        }

        // getExportedKeys: Quem aponta para chave primária da main_table?
        rs = metadata.getExportedKeys("", "", main_table);
        while (rs.next()) {
            System.out.println("getExportedKeys: " + rs.getString("PKTABLE_NAME") + ", " + rs.getString("PKCOLUMN_NAME") + ", " + rs.getString("FKTABLE_NAME") + ", " + rs.getString("FKCOLUMN_NAME"));
        }

        // getImportedKeys: Para quem a main_table aponta ?
        rs = metadata.getImportedKeys("", "", main_table);
        while (rs.next()) {
            System.out.println("getImportedKeys: " + rs.getString("PKTABLE_NAME") + ", " + rs.getString("PKCOLUMN_NAME") + ", " + rs.getString("FKTABLE_NAME") + ", " + rs.getString("FKCOLUMN_NAME"));
        }

        // getCrossReference: Quais são as chaves entre as tabelas Parent (PK Side) e Child (FK Side)?
        //  RESPEITA HIERARQUIA: se a tabela Pai não estiver do lado UM e a tabela Filho não estiver do lado MUITOS, não retorna nada!
        rs = metadata.getCrossReference("", "", main_table, "", "", "item");
        while (rs.next()) {
            System.out.println("getCrossReference: " + rs.getString("PKTABLE_NAME") + ", " + rs.getString("PKCOLUMN_NAME") + ", " + rs.getString("FKTABLE_NAME") + ", " + rs.getString("FKCOLUMN_NAME"));
        }

        connection.closeConnection();
    }
}
