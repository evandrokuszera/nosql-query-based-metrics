/*
 * Objetivo:
 *  - Esta classe é um facilitador para testar as métricas sobre um conjunto de esquemas e consultas
 *  - Fornece uma estrutura para encapsular:
 *      - uma coleção de esquemas e 
 *      - uma coleção de consultas;
 *
 *  - É usada como parâmetro pelas classes do pacote: dag_metrics.coverage.printers
 *  - O usuário deve extendê-la e completar os métodos loadQueries e loadSchemas.
 *  - As classes Printer fazem a leitura dos esquemas e consultas para imprimir as métricas.
 *  - O objetivo final é facilitar a execução de testes das métricas Coverage.
 */
package metrics.test_loader;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.NoSQLSchema;
import metrics.Metrics;
import metrics.coverage.printers.CoveragePrinter;
import metrics.scores.SchemaScorePrinter;
import java.util.ArrayList;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public abstract class AbstractTestInputLoader {
    private ArrayList<NoSQLSchema> schemas = new ArrayList<>();
    private ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries = new ArrayList<>();
    private CoveragePrinter coveragePrinter;
    private SchemaScorePrinter schemaPrinter;
    
    public AbstractTestInputLoader(){
        this.loadQueries();
        this.loadSchemas();
        this.coveragePrinter = new CoveragePrinter(this);
        this.schemaPrinter = new SchemaScorePrinter(this);
    }
    
    public abstract void loadQueries();
    
    public abstract void loadSchemas();

    public CoveragePrinter coveragePrinter() {
        return coveragePrinter;
    }

    public SchemaScorePrinter schemaPrinter() {
        return schemaPrinter;
    }
    
    public ArrayList<NoSQLSchema> getSchemas() {
        return schemas;
    }
    
    public NoSQLSchema getSchemaByIndex(int index) {
        return schemas.get(index);
    }
    
    public NoSQLSchema getSchemaByName(String name){
        for (NoSQLSchema schema : this.schemas){
            if (schema.getName().equalsIgnoreCase(name)){
                return schema;
            }
        }
        return null;
    }

    public void setSchemas(ArrayList<NoSQLSchema> schemas) {
        this.schemas = schemas;
    }

    public void setQueries(ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> queries) {
        this.queries = queries;
    }
    
    public ArrayList<DirectedAcyclicGraph<TableVertex, RelationshipEdge>> getQueries() {
        return queries;
    }
    
    public DirectedAcyclicGraph<TableVertex, RelationshipEdge> getQueryByIndex(int index) {
        return queries.get(index);
    }
    
    public void printQueries(){
        System.out.println("Queries Paths:");
        for (int i=0; i<getQueries().size(); i++){
            System.out.println("query"+(i+1)+": "+new Metrics(null).path().getPaths(getQueries().get(i)));
        }
    }
    
    public void printSchemaPaths(){
        for (NoSQLSchema schema : this.getSchemas()){
            System.out.println("Schema Paths: "+schema.getName());
            
            for (int i=0; i<schema.getEntities().size(); i++){
                System.out.println(new Metrics(null).path().getPaths(schema.getEntities().get(i)));
            }
        }
    }
}
