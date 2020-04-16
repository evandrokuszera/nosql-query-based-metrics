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
public class MySQLConnection_Nao_testei extends GenericConnection {
    private String url_base = "jdbc:mysql://";
    private String driverName = "com.mysql.jdbc.Driver";

    public MySQLConnection_Nao_testei(String user, String password, String server, String database) {        
        super(user, password, server, database);
        setUrl_base(url_base);
        setDriverName(driverName);
        loadDriver();
    }

}
