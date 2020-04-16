/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdbc_connection;

/**
 *
 * @author Evandro
 */
public class TestPostgresConnection {
    
    public static void main(String[] args) {
        PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "metamorfose");        
        connection.openConnection();
        connection.closeConnection();
    }
    
}
