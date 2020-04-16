/*
 * Esta classe converte um DAG para um Json-Schema.
 * Método convert recebe por parâmetro o DAG para ser convertido.
 * 
 * O esquema Json produzido é destinado para bancos NoSQL orientados a documento.
 * Não avaliei como fica a questão dos bancos NoSQL orientados a famílias de colunas.
 */
package dag.to_json_schema;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.util.ArrayList;
import java.util.Iterator;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Evandro
 */
public class DAGtoJSONSchema {
    
    // Este método consome os vértices do DAG e cria um JSONSchema representando os objetos e seus aninhamentos.
    public static JSONObject convert(DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag){         
        // Processa o dag até que o número de vertex seja igual a 1 (vertex final encapsula o jsonSchema final).
        while (dag.vertexSet().size()>1){
            // Busca por vertexs folhas
            ArrayList<TableVertex> leafVertexs = getVertexsWithZeroInDegree(dag);            
            // Para cada leafVertex...
            for (TableVertex leafVertex : leafVertexs){
                // Cria JSONSchema para leafVertex, se ele não existe ainda...
                if (leafVertex.getJsonSchema() == null){
                    leafVertex.setJsonSchema( createJSONObject(leafVertex) );
                }                 
                
                // Enquanto o leafVertex tiver sucessores...
                int count_successor = 0;
                while (Graphs.successorListOf(dag, leafVertex).size() > count_successor){
                    // Recupera vertex successor...
                    TableVertex successorVertex = (TableVertex) Graphs.successorListOf(dag, leafVertex).get(count_successor);
                    
                    // Recuperando aresta entre leaf e sucessor vertexs
                    RelationshipEdge edge = dag.getEdge(leafVertex, successorVertex);
                    
                    // Cria JSONSchema para sucessorVertex, se ele não existe ainda...
                    if (successorVertex.getJsonSchema() == null){
                        successorVertex.setJsonSchema( createJSONObject(successorVertex) );
                    }
                    
                    // Identifica o tipo de aresta do grafo, ou seja, qual é o tipo de aninhamento entre leaf e successor.
                    if (edge.getTypeofNesting().equalsIgnoreCase("many_embedded")){
                        // aninhamento como array: successor recebe leaf como array de objeto.
                        JSONObject arrayType = new JSONObject();        
                        arrayType.put("type", "array");
                        arrayType.put("$id", leafVertex.getName());
                        arrayType.put("items", leafVertex.getJsonSchema());
                        successorVertex.getJsonSchema().getJSONObject("properties").put(leafVertex.getName(), arrayType);                        
                    } else if (edge.getTypeofNesting().equalsIgnoreCase("one_embedded")) {
                        // aninhamento como objeto: successor recebe leaf como objeto.
                        successorVertex.getJsonSchema().getJSONObject("properties").put(leafVertex.getName(), leafVertex.getJsonSchema());
                    }
                                                           
                    count_successor++;
                } // FIM: While                
                // Remove leafVertex já processado...
                dag.removeVertex(leafVertex); 
            } // FIM: For            
        } // FIM: While
        
        // Para o último vertex do dag (nó raiz), adicionar os elementos $id e $schema. 
        //  ... Talvez adicionar uma descrição do banco de dados usado para criar o DAG.
        JSONObject jsonSchema = new JSONObject();
        if (dag.vertexSet().size() == 1){
            jsonSchema = dag.vertexSet().iterator().next().getJsonSchema();
            jsonSchema.put("$schema", "http://json-schema.org/draft-07/schema#");
        }
        
        return jsonSchema;
    }
           
    public static JSONObject createJSONObject(TableVertex vertex){        
        JSONObject jsonSchema = new JSONObject();        
        
        jsonSchema.put("$id", vertex.getName());
        jsonSchema.put("title", vertex.getName());
        jsonSchema.put("description", vertex.getName());
        jsonSchema.put("type", "object");
        
        // Criando as properties do json-schema
        jsonSchema.put("properties", new JSONObject());
                    
        // Para cada vertex field  
        for (String field : vertex.getFields()){                   
            JSONObject fieldProperties = new JSONObject();
            fieldProperties.put("type", "string");
            fieldProperties.put("$id", field);
            //fieldProperties.put("....", "....");

            jsonSchema.getJSONObject("properties").put(field, fieldProperties);
        }  
        
        // Criando a lista de campos requeridos do json-schema
        JSONArray requiredFields = new JSONArray();
        
        // Adicionando todos os campos do vertex como campos requeridos.
        for (String field : vertex.getFields()){
            requiredFields.put(field);
        }   
        
        jsonSchema.put("required", requiredFields);
        
        return jsonSchema;
    }
           
    
    //
    // CODIGOs COPIADOS DA CLASSE COMMANDGENERATOR !!!!
    //
    
    // Recupera todos os vértices folhas.
    // Vértice Folha = vertexs que NÃO possuem arestas de entrada.
    private static ArrayList<TableVertex> getVertexsWithZeroInDegree(DirectedAcyclicGraph<TableVertex, RelationshipEdge> g) {
        ArrayList<TableVertex> vertexs_with_zero_inDegree = new ArrayList<>();
        // Percorrendo conjunto de vértices do graph 'g'
        Iterator<TableVertex> vertexIterator = g.vertexSet().iterator();
        while (vertexIterator.hasNext()) {
            TableVertex vertex = vertexIterator.next();
            // Recuperando apenas vértices sem arestas de entrada (vértices folha).
            if (g.inDegreeOf(vertex) == 0) {
                vertexs_with_zero_inDegree.add(vertex);
            }
        }

        return vertexs_with_zero_inDegree;
    }
    
    // Recupera o vertex raiz. Esse vertex representa a entidade alvo da transformação.
    private TableVertex getRootVertex(DirectedAcyclicGraph<TableVertex, RelationshipEdge> g) {
        TableVertex root = null;
        // Percorrendo conjunto de vértices do graph 'g'
        Iterator<TableVertex> vertexIterator = g.vertexSet().iterator();
        while (vertexIterator.hasNext()) {
            TableVertex vertex = vertexIterator.next();
            // Recuperando apenas o vértice sem arestas de saída. Esse vertice é raiz do grafo.
            if (g.outDegreeOf(vertex) == 0) {
                root = vertex;
            }
        }
        return root;
    }
    
}