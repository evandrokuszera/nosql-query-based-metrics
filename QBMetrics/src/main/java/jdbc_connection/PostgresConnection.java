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
public class PostgresConnection extends GenericConnection {
    private String url_base = "jdbc:postgresql://";
    private String driverName = "org.postgresql.Driver";

    public PostgresConnection(String user, String password, String server, String database) {        
        super(user, password, server, database);
        setUrl_base(url_base);
        setDriverName(driverName);
        loadDriver();
    }

}
