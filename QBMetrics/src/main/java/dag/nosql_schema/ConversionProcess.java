/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.nosql_schema;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.util.ArrayList;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class ConversionProcess {
    private String name;
    private String driver;
    private String server;
    private String user;
    private String password;
    private String database;
    private String description;
    private ArrayList<NoSQLSchema> schemas;
    private ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries;

    public ConversionProcess() {
        this.schemas = new ArrayList<>();
        this.queries = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NoSQLSchema getSchemaByName(String name){
        for (NoSQLSchema schema : schemas){
            if (schema.getName().equalsIgnoreCase(name)){
                return schema;
            }
        }
        return null;
    }
    
    public ArrayList<NoSQLSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(ArrayList<NoSQLSchema> schemas) {
        this.schemas = schemas;
    }

    public ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> getQueries() {
        return queries;
    }

    public void setQueries(ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries) {
        this.queries = queries;
    }
}
