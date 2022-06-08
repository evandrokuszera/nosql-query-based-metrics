/*
 * Esta classe cria uma árvore de tabelas relacionadas baseado no algoritmo Breath-First-Search.
 * A implementação do método "create" é baseada no algoritmo do artigo: (179) "Migration of Relational Database to Document-Oriented Database: Structure Denormalization and Data Transformation"
 * Links já visitados entre duas tabelas não são processados para reduzir a redundância de tabelas na árvore.
 * As tabelas são processadas conforme ordem de entrada na "queue"
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
public class BFSDagCreator extends DagCreator {
    private ArrayList<TableVertex> queue;
    
    public BFSDagCreator(GenericConnection connection) {
        super(connection);
    }
    
    private void create_queue(){
        queue = new ArrayList<>();
    }
    
    private void enqueue(TableVertex tableVertex) {
        queue.add(tableVertex);
    }

    private TableVertex dequeue() {
        if (queue.size() == 0) { // fila vazia retorna NULL
            return null;
        } else { // caso contrário retorna tableVertex
            TableVertex tableVertex = queue.get(0);
            queue.remove(0);
            return tableVertex;
        }
    }
    
    public DirectedAcyclicGraph create(String main_table) throws SQLException {
        ResultSet rs;
        connection.openConnection();
        metadata = connection.getConnection().getMetaData();
        
        graph = new DirectedAcyclicGraph<>(RelationshipEdge.class);        
        
        TableVertex targetVertex = createVertexFromTable(main_table);
        graph.addVertex(targetVertex);
        
        System.out.println("\nBFS: ordem de execução do algoritmo!");
        create_queue();   
        System.out.println("Printing queue: " + queue);
        enqueue(targetVertex); 
        System.out.println("Printing queue: " + queue);
                
        while (queue.size() > 0){
            targetVertex = dequeue(); 
            System.out.println("dequeue: " + targetVertex.getName());

            // Retorna tabelas que referenciam 'current_table' (Quem aponta para chave primária da current_table?)
            // current_table lado_1, demais tabelas lado_N.
            rs = metadata.getExportedKeys("", "", targetVertex.getName());
            while (rs.next()) {
                if (! existEdgeBetween(rs.getString("FKTABLE_NAME"), targetVertex.getName())){                
                    // Criando o vertex relacionado.            
                    TableVertex sourceVertex = createVertexFromTable(rs.getString("FKTABLE_NAME"));
                    // Adicionando vertex no grafo.
                    graph.addVertex(sourceVertex);
                    // Criando aresta para conectar sourceVertex para TargetVertex
                    RelationshipEdge edge = new RelationshipEdge("embed_many_to_one", rs.getString("PKTABLE_NAME"), rs.getString("FKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FKCOLUMN_NAME"));
                    // Adicionando aresta no grafo (sourceVertex --> targetVertex)
                    graph.addEdge(sourceVertex, targetVertex, edge);
                    //System.out.println("getExportedKeys: "+rs.getString("PKTABLE_NAME") +", "+ rs.getString("PKCOLUMN_NAME") + ", "+rs.getString("FKTABLE_NAME") +", "+ rs.getString("FKCOLUMN_NAME"));                        

                    enqueue(sourceVertex);
                    System.out.println("enqueue: " + sourceVertex.getName());
                }           
            } 

            // Retorna tabelas em que current_table faz referência (Para quem a current_table aponta ?)
            // current_table lado_N, demais tabelas lado_1.
            rs = metadata.getImportedKeys("", "", targetVertex.getName());
            while (rs.next()) {
                if (!existEdgeBetween(rs.getString("PKTABLE_NAME"), targetVertex.getName())){
                    // Criando o vertex relacionado.            
                    TableVertex sourceVertex = createVertexFromTable(rs.getString("PKTABLE_NAME"));
                    // Adicionando vertex no grafo.
                    graph.addVertex(sourceVertex);
                    // Criando aresta para conectar sourceVertex para TargetVertex
                    RelationshipEdge edge = new RelationshipEdge("embed_one_to_many", rs.getString("PKTABLE_NAME"), rs.getString("FKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FKCOLUMN_NAME"));
                    // Adicionando aresta no grafo (sourceVertex --> targetVertex)
                    graph.addEdge(sourceVertex, targetVertex, edge);
                    //System.out.println("getImportedKeys: "+rs.getString("PKTABLE_NAME") +", "+ rs.getString("PKCOLUMN_NAME") + ", "+rs.getString("FKTABLE_NAME") +", "+ rs.getString("FKCOLUMN_NAME"));            

                    enqueue(sourceVertex);
                    System.out.println("enqueue: " + sourceVertex.getName());
                }
            } 
            System.out.println("Printing queue: " + queue);
        } // fim: while(queue.size > 0)

        System.out.println("");
        connection.closeConnection();
        
        return graph;
    }
    
    public static void main(String[] args) throws SQLException {
        //PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "university");
        PostgresConnection connection = new PostgresConnection("postgres", "123456", "localhost", "ds2_10mb");
        DagCreator dagCreator = new BFSDagCreator(connection);

//        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = dagCreator.create("student");
        DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag = dagCreator.create("products");
        
        System.out.println("\nPrinting DAG: "+dag);

        System.out.println("\nPrinting vertexSet and properties:");
        Iterator it = dag.vertexSet().iterator();
        while (it.hasNext()) {
            TableVertex t = (TableVertex) it.next();
            System.out.println(t+":"+t.getFields());
        }
        
        System.out.println("\nPrinting number of vertexs: "+ dag.vertexSet().size());
        System.out.println("Printing number of edges: "+ dag.edgeSet().size());
        
    }
    
}
