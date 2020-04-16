/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.to_json_schema;

import dag.algorithms.BFSDagCreator;
import dag.algorithms.DagCreator;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.sql.SQLException;
import jdbc_connection.PostgresConnection;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class Test_DAGtoJSONSchema {
    
    public static void main(String[] args) throws SQLException {
        // Dag com Apenas três tabelas!
        //generating_json_schema_from_dag_customer_order_orderline();
        
        // Dag com 9 tabelas!
        generating_json_schema_from_dag_university_db();
        
    }
    
    public static void generating_json_schema_from_dag_university_db() throws SQLException {
        PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "university");
        DagCreator dagCreator = new BFSDagCreator(connection);

        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = dagCreator.create("student");        
        
        System.out.println( DAGtoJSONSchema.convert(dag) );
    }    
    
    public static void generating_json_schema_from_dag_customer_order_orderline() throws SQLException {
        PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "ds2_3_tables");
        DagCreator dagCreator = new BFSDagCreator(connection);

        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = dagCreator.create("customers");
        
        System.out.println( DAGtoJSONSchema.convert(dag) );
    }    
}
