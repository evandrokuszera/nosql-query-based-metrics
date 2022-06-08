/*
 * Esta classe cria uma árvore de tabelas relacionadas baseado no algoritmo Deepth-First-Search.
 * A implementação do método "create" é baseada no algoritmo do artigo: (179) "Migration of Relational Database to Document-Oriented Database: Structure Denormalization and Data Transformation"
 * Vértices já visitados não são processados para reduzir a redundância de tabelas na árvore.
 */
package dag.algorithms;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import jdbc_connection.GenericConnection;
import jdbc_connection.PostgresConnection;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class DFSDagCreator extends DagCreator {
    private ArrayList<TableVertex> vertexJaVisitados;

    public DFSDagCreator(GenericConnection connection) {
        super(connection);
    }
    
    public boolean isVertexVisitado(String vertexName){
        for (TableVertex tv : vertexJaVisitados){
            if (tv.getName().equalsIgnoreCase(vertexName)){
                return true;
            }
        }
        return false;
    }

    public DirectedAcyclicGraph create(String main_table) throws SQLException {
        connection.openConnection();
        metadata = connection.getConnection().getMetaData();

        graph = new DirectedAcyclicGraph<>(RelationshipEdge.class);
                
        vertexJaVisitados = new ArrayList<>();

        TableVertex targetVertex = createVertexFromTable(main_table);
        graph.addVertex(targetVertex);
        
        vertexJaVisitados.add(targetVertex);

        System.out.println("DFS - Ordem de visitação das tabelas do RDB:");
        System.out.println("DFS: "+targetVertex.getName());
        dfs_selecting(targetVertex);        

        connection.closeConnection();

        return graph;
    }

    private void dfs_selecting(TableVertex targetVertex) throws SQLException {

        // Retorna tabelas que referenciam 'current_table' (Quem aponta para chave primária da current_table?)
        // current_table lado_1, demais tabelas lado_N.
        ResultSet rs = metadata.getExportedKeys("", "", targetVertex.getName());
        while (rs.next()) {
            
            if (!isVertexVisitado(rs.getString("FKTABLE_NAME"))){
            
                // Criando o vertex relacionado.            
                TableVertex sourceVertex = createVertexFromTable(rs.getString("FKTABLE_NAME"));
                // Adicionando vertex no grafo.
                graph.addVertex(sourceVertex);
                // Criando aresta para conectar sourceVertex para TargetVertex
                RelationshipEdge edge = new RelationshipEdge("embed_many_to_one", rs.getString("PKTABLE_NAME"), rs.getString("FKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FKCOLUMN_NAME"));
                // Adicionando aresta no grafo (sourceVertex --> targetVertex)
                graph.addEdge(sourceVertex, targetVertex, edge);

                vertexJaVisitados.add(sourceVertex); 

                System.out.println("DFS: "+sourceVertex.getName());
                dfs_selecting(sourceVertex);
                
            }
        }

        // Retorna tabelas em que current_table faz referência (Para quem a current_table aponta ?)
        // current_table lado_N, demais tabelas lado_1.
        rs = metadata.getImportedKeys("", "", targetVertex.getName());
        while (rs.next()) {
            
            if (!isVertexVisitado(rs.getString("PKTABLE_NAME"))){
            
                // Criando o vertex relacionado.            
                TableVertex sourceVertex = createVertexFromTable(rs.getString("PKTABLE_NAME"));
                // Adicionando vertex no grafo.
                graph.addVertex(sourceVertex);
                // Criando aresta para conectar sourceVertex para TargetVertex
                RelationshipEdge edge = new RelationshipEdge("embed_one_to_many", rs.getString("PKTABLE_NAME"), rs.getString("FKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FKCOLUMN_NAME"));
                // Adicionando aresta no grafo (sourceVertex --> targetVertex)
                graph.addEdge(sourceVertex, targetVertex, edge);

                vertexJaVisitados.add(sourceVertex);

                System.out.println("DFS: "+sourceVertex.getName());
                dfs_selecting(sourceVertex);
            
            }
        }

    }

    public static void main(String[] args) throws SQLException {
        //PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "university");
        PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "ds2_10mb");
        DagCreator dagCreator = new DFSDagCreator(connection);

//        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = dagCreator.create("course");
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = dagCreator.create("products");

        System.out.println("\nPrinting DAG:" + dag);

        System.out.println("\nPrinting vertexSet and properties:");
        Iterator it = dag.vertexSet().iterator();
        while (it.hasNext()) {
            TableVertex t = (TableVertex) it.next();
            System.out.println(t+":"+t.getFields());
        }

        System.out.println("\nPrinting number of vertexs: " + dag.vertexSet().size());
        System.out.println("Printing number of edges: " + dag.edgeSet().size());
                
    }

}
