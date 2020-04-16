/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdbc_connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Evandro
 */
public abstract class GenericConnection {
    private java.sql.Connection connection;
    private String user;
    private String password;
    private String server;
    private String database;
    private String url_base;// = "jdbc:mysql://"; //"jdbc:postgresql://localhost/BlocoDeNotas"
    private String driverName;
    
    public GenericConnection(String user, String password, String server, String database){
        this.user = user;
        this.password = password;
        this.server = server;
        this.database = database;  
    }
    
    public void loadDriver(){
        try{
            Class.forName(driverName);
            System.out.println(this.getClass().getName()+".loadDriver(): ok.");
        } catch(Exception e){
            System.out.println(this.getClass().getName()+".loadDriver(): ERROR - " + e);
        }
    }
    
    public java.sql.Connection openConnection(){ 
        String url = url_base + server +"/"+ database;
        
        // tentando reaproveitar conexão aberta.
        try{
            if (this.connection != null){
                if (!this.connection.isClosed()){
                    System.out.println(this.getClass().getName()+".openConnection(): connection reopen.");
                    return this.connection;
                }
            }
        } catch (SQLException e){
            
        }
        
        // tentando estabelecer conexão com banco.
        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            System.out.println(this.getClass().getName()+".openConnection(): ERROR - " + ex);
        }
        System.out.println(this.getClass().getName()+".openConnection(): opened.");
        return this.connection;
    }
    
    public boolean closeConnection(){
        try {
            if (this.connection != null){
                this.connection.close();
                System.out.println(this.getClass().getName()+".closeConnection(): closed.");
            }
            return true;
        } catch (Exception e){
            return false;
        }        
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }    

    protected String getUrl_base() {
        return url_base;
    }

    protected void setUrl_base(String url_base) {
        this.url_base = url_base;
    }

    protected String getDriverName() {
        return driverName;
    }

    protected void setDriverName(String driverName) {
        this.driverName = driverName;
    }       
}
