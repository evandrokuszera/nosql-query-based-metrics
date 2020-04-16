/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.gui;

import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.nosql_schema.NoSQLSchema;
import dag.utils.GraphUtils;
import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import jdbc_connection.GenericConnection;
import metrics.Metrics;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author Evandro
 */
public class DAGCreatorDialog extends javax.swing.JDialog {

    private DirectedAcyclicGraph<TableVertex, RelationshipEdge> graph;
    //private PostgresConnection rdbConnection;
    private GenericConnection rdbConnection;
    private DatabaseMetaData rdb_metadata;
    private DefaultListModel<String> lstModel_RDBTables = new DefaultListModel<>();
    private DefaultListModel lstModel_DAGVertexs = new DefaultListModel();
    private DefaultTableModel tblModel_DAGEdges = new DefaultTableModel();
    private ArrayList<RelationshipEdge> edgesArray = new ArrayList<>(); // estou usando este array para controlar o processo de exclusão das arestas no tabela
    private boolean cancel = true;
    private DefaultTableModel tblCollectionMetricsModelTable = new DefaultTableModel();

    /**
     * Creates new form DAGCreationDialog
     */
    public DAGCreatorDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        graph = new DirectedAcyclicGraph<>(RelationshipEdge.class);

        lstRDBTables.setModel(lstModel_RDBTables);
        lstDAGVertexs.setModel(lstModel_DAGVertexs);
        tblModel_DAGEdges = (DefaultTableModel) this.tblEdges.getModel();
        tblCollectionMetricsModelTable = (DefaultTableModel) this.tblCollectionMetric.getModel();

        // Largura padrão das colunas
        tblEdges.getColumnModel().getColumn(0).setPreferredWidth(200);
        tblEdges.getColumnModel().getColumn(1).setPreferredWidth(35);
        tblEdges.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblEdges.getColumnModel().getColumn(3).setPreferredWidth(40);

        // Desligar o autoresize, caso contrario não funciona a configuração do tamanho das colunas.
        tblEdges.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Configurando o alinhamento da coluna e cabeçalho da coluna 2 para centralizado.
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        tblEdges.getColumnModel().getColumn(1).setCellRenderer(centralizado);
        tblEdges.getColumnModel().getColumn(1).setHeaderRenderer(centralizado);
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setRdbConnection(GenericConnection rdbConnection) {
        this.rdbConnection = rdbConnection;
        this.loadVerticesFromRDB();
    }

    public DirectedAcyclicGraph<TableVertex, RelationshipEdge> getGraph() {
        return graph;
    }

    private void loadVerticesFromRDB() {
        rdbConnection.openConnection();

        try {
            rdb_metadata = rdbConnection.getConnection().getMetaData();
            loadRDBTablesInList();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex);
        }
    }

    private void loadCmbSourceVertexs() {
        cmbSourceVertex.removeAllItems();

        for (int i = 0; i < lstModel_DAGVertexs.size(); i++) {
            cmbSourceVertex.addItem(lstModel_DAGVertexs.get(i));
        }

    }

    private void loadCmbTargetVertexs() {
        ArrayList<String> related_tables = new ArrayList<>();

        cmbTargetVertex.removeAllItems();

        // Se o usuário quer carregar todos os vértices do DAG no comboTargetVertex, então...
        if (this.chkAllowAllVertices.isSelected()) {
            for (int i = 0; i < lstModel_DAGVertexs.size(); i++) {
                cmbTargetVertex.addItem(lstModel_DAGVertexs.get(i));
            }
            return;
        }

        if (cmbSourceVertex.getSelectedIndex() != -1) {

            // 1 - Monta uma lista de Tabelas Relacionadas ao Vertex.
            String source_table_name = ((TableVertex) cmbSourceVertex.getSelectedItem()).getName();

            try {
                ResultSet rs;

                rs = rdb_metadata.getExportedKeys("", "", source_table_name);
                while (rs.next()) {
                    related_tables.add(rs.getString("FKTABLE_NAME"));
                }

                rs = rdb_metadata.getImportedKeys("", "", source_table_name);
                while (rs.next()) {
                    related_tables.add(rs.getString("PKTABLE_NAME"));
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex);
            }

            // 2 - Monta uma lista de target vertex Relacionados ao source vertex.   
            for (int i = 0; i < lstModel_DAGVertexs.size(); i++) {

                TableVertex tableVertex = (TableVertex) lstModel_DAGVertexs.get(i);

                if (related_tables.contains(tableVertex.getName())) {
                    cmbTargetVertex.addItem(tableVertex);
                }

            }
        }
    }

    // Cria TableVertex com base na table_name.
    // Os campos da table_name são extraídos dos metadados do RDB.
    protected TableVertex createVertexFromRDBMetadata(String table_name) throws SQLException {
        ResultSet rsPrimaryKeys = rdb_metadata.getPrimaryKeys("", "", table_name);

        // Obs.: estou considerando somente PKs simples. Suporte a PKs compostas não foi implementado.
        // Recuperando a chave primária da table_name.
        String primaryKeyName = "";
        while (rsPrimaryKeys.next()) {
            primaryKeyName = rsPrimaryKeys.getString("COLUMN_NAME");
        }
        rsPrimaryKeys.close();

        // Criando novo vertex com base nos dados da tabela RDB.
        TableVertex newTableVertex = new TableVertex(table_name, table_name, primaryKeyName);
        // Adicionando os campos do novo vertex com base nos metadados da tabela RDB.
        ResultSet rsTableFields = rdb_metadata.getColumns("", "", table_name, "");
        while (rsTableFields.next()) {
            newTableVertex.getFields().add(rsTableFields.getString("COLUMN_NAME"));
        }
        rsTableFields.close();

        return newTableVertex;
    }

    private RelationshipEdge createEdgeFromRDBMetadata(String source, String target) {
        RelationshipEdge edge = null;
        try {
            ResultSet rs1 = rdb_metadata.getCrossReference("", "", target, "", "", source);
            while (rs1.next()) {
                edge = new RelationshipEdge("many_embedded", rs1.getString("PKTABLE_NAME"), rs1.getString("FKTABLE_NAME"), rs1.getString("PKCOLUMN_NAME"), rs1.getString("FKCOLUMN_NAME"));
            }
            rs1.close();

            ResultSet rs2 = rdb_metadata.getCrossReference("", "", source, "", "", target);
            while (rs2.next()) {
                edge = new RelationshipEdge("one_embedded", rs2.getString("PKTABLE_NAME"), rs2.getString("FKTABLE_NAME"), rs2.getString("PKCOLUMN_NAME"), rs2.getString("FKCOLUMN_NAME"));
            }
            rs2.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex);
        }
        return edge;
    }

    private void loadRDBTablesInList() throws SQLException {
        lstModel_RDBTables.clear();

        String[] types = {"TABLE"};
        ResultSet rs = rdb_metadata.getTables("", "", "", types);
        while (rs.next()) {
            lstModel_RDBTables.addElement(rs.getString("TABLE_NAME"));
        }
        rs.close();
    }

    private void clearAllComponents() {
        this.lstModel_DAGVertexs.removeAllElements();
        this.cmbSourceVertex.removeAllItems();
        this.cmbTargetVertex.removeAllItems();
        while (this.tblEdges.getRowCount() > 0) this.tblModel_DAGEdges.removeRow(0);
        edgesArray.clear();
        while (tblCollectionMetric.getRowCount() > 0) tblCollectionMetricsModelTable.removeRow(0);
    }
    
    // Este método carrega as métricas estruturais da coleção na tabela para visualização
    // Para tal, é necessário criar um objeto NoSQLSchema temporário apenas para invocar as métricas.
    // Esse objeto NoSQLSchema deve ser destruído, não repassado para as demais telas da aplicação.
    private void loadCollectionMetrics(){        
        // Remove todas as entradas da tabela tblCollections.
        while (tblCollectionMetric.getRowCount() > 0) tblCollectionMetricsModelTable.removeRow(0);
        
        NoSQLSchema schemaTemporario = new NoSQLSchema("Temporário, usado apenas para invocar as métricas");
        schemaTemporario.getEntities().add(graph); // adiciono o graph que está sendo criado, que representa uma coleção do esquema.
        
        // Objeto metrics usado aqui para carregar as métricas da coleção na tblCollections.
        Metrics metrics = new Metrics(schemaTemporario);
        
        // Carrega tabela tblCollection com a coleção que está sendo criada.
        String collection = GraphUtils.getRootVertex(graph).getName();

        Object[] row = new Object[7];
        row[0] = collection;
        row[1] = metrics.size().getNumberOfDocuments(collection);
        row[2] = metrics.size().getNumberOfArraysOfDocuments(collection);
        row[3] = "N/D"; // Ainda não implementei isso, nem sei se será útil.
        row[4] = metrics.size().getNumberOfAtomicAttributes(collection);
        row[5] = metrics.size().getCollectionSize(collection, false);
        row[6] = metrics.depth().getDepthOfCollection(collection);
        tblCollectionMetricsModelTable.addRow(row);
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstRDBTables = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstDAGVertexs = new javax.swing.JList<>();
        btnAddVertex = new javax.swing.JButton();
        btnAddAllVertex = new javax.swing.JButton();
        btnRemVertex = new javax.swing.JButton();
        btnRemAllVertex = new javax.swing.JButton();
        btnRemAllVertex1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        cmbSourceVertex = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cmbTargetVertex = new javax.swing.JComboBox();
        btnAddEdge = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblEdges = new javax.swing.JTable();
        chkAllowAllVertices = new javax.swing.JCheckBox();
        btnExportDAG = new javax.swing.JButton();
        btnVisualizingDAG = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblCollectionMetric = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DAG Creator");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Step 1: Add vertices (tables)"));

        lstRDBTables.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lstRDBTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstRDBTablesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstRDBTables);

        jLabel2.setText("RDB Tables:");

        jLabel3.setText("DAG Vertexs:");

        lstDAGVertexs.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(lstDAGVertexs);

        btnAddVertex.setText("Add ->");
        btnAddVertex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddVertexActionPerformed(evt);
            }
        });

        btnAddAllVertex.setText("Add All ->");
        btnAddAllVertex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAllVertexActionPerformed(evt);
            }
        });

        btnRemVertex.setText("<- Rem");
        btnRemVertex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemVertexActionPerformed(evt);
            }
        });

        btnRemAllVertex.setText("<- Rem All");
        btnRemAllVertex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemAllVertexActionPerformed(evt);
            }
        });

        btnRemAllVertex1.setForeground(new java.awt.Color(0, 51, 255));
        btnRemAllVertex1.setText("Clear DAG");
        btnRemAllVertex1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemAllVertex1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnAddAllVertex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnRemAllVertex, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRemAllVertex1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnAddVertex, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemVertex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAddAllVertex, btnAddVertex, btnRemAllVertex, btnRemAllVertex1, btnRemVertex});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAddVertex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddAllVertex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemVertex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemAllVertex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemAllVertex1))))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Step 2: Add edges (relationships)"));

        jLabel4.setText("Source Vertex:");

        cmbSourceVertex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSourceVertexActionPerformed(evt);
            }
        });

        jLabel5.setText("Target Vertex:");

        btnAddEdge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adicionar.png"))); // NOI18N
        btnAddEdge.setText("Add");
        btnAddEdge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddEdgeActionPerformed(evt);
            }
        });

        tblEdges.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Source", "-->", "Target", "Rem"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblEdges.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblEdgesMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblEdges);
        if (tblEdges.getColumnModel().getColumnCount() > 0) {
            tblEdges.getColumnModel().getColumn(0).setPreferredWidth(240);
            tblEdges.getColumnModel().getColumn(1).setPreferredWidth(40);
            tblEdges.getColumnModel().getColumn(2).setPreferredWidth(240);
            tblEdges.getColumnModel().getColumn(3).setPreferredWidth(40);
        }

        chkAllowAllVertices.setText("Allow all vertices");

        btnExportDAG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exportar.png"))); // NOI18N
        btnExportDAG.setText("Export");
        btnExportDAG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportDAGActionPerformed(evt);
            }
        });

        btnVisualizingDAG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/visualizar.png"))); // NOI18N
        btnVisualizingDAG.setText("View");
        btnVisualizingDAG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVisualizingDAGActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(cmbSourceVertex, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(48, 48, 48)
                                .addComponent(chkAllowAllVertices))
                            .addComponent(cmbTargetVertex, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddEdge, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnExportDAG, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVisualizingDAG, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(chkAllowAllVertices))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbSourceVertex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTargetVertex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEdge))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnExportDAG)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnVisualizingDAG))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(115, 115, 115))
        );

        btnOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ok.png"))); // NOI18N
        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cancelar.png"))); // NOI18N
        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Step 3: Viewing collection metrics"));

        tblCollectionMetric.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Collection", "Docs", "Doc Arrays", "Arrays", "Atomic", "Size", "MaxDepth"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCollectionMetric.setAutoscrolls(false);
        tblCollectionMetric.setEnabled(false);
        tblCollectionMetric.setFocusable(false);
        tblCollectionMetric.setRowSelectionAllowed(false);
        tblCollectionMetric.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(tblCollectionMetric);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addGap(28, 28, 28))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancelar, btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancelar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lstRDBTablesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstRDBTablesMouseClicked
        if (evt.getClickCount() > 1) {
            btnAddVertexActionPerformed(null);
        }
    }//GEN-LAST:event_lstRDBTablesMouseClicked

    private void btnAddVertexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddVertexActionPerformed
        if (lstRDBTables.getSelectedIndex() != -1) {
            try {
                TableVertex vertex = createVertexFromRDBMetadata(lstModel_RDBTables.get(lstRDBTables.getSelectedIndex()));
                lstModel_DAGVertexs.addElement(vertex);
                this.graph.addVertex(vertex);
                loadCmbSourceVertexs();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex);
            }
        }
    }//GEN-LAST:event_btnAddVertexActionPerformed

    private void btnAddAllVertexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAllVertexActionPerformed
        for (int i = 0; i < lstModel_RDBTables.size(); i++) {
            try {
                TableVertex vertex = createVertexFromRDBMetadata(lstModel_RDBTables.get(i));
                lstModel_DAGVertexs.addElement(vertex);
                this.graph.addVertex(vertex);
                loadCmbSourceVertexs();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex);
            }

        }
    }//GEN-LAST:event_btnAddAllVertexActionPerformed

    private void btnRemVertexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemVertexActionPerformed
        if (lstDAGVertexs.getSelectedIndex() != -1) {
            // removendo vertex do grafo
            TableVertex vertex = (TableVertex) this.lstModel_DAGVertexs.getElementAt(lstDAGVertexs.getSelectedIndex());
            this.graph.removeVertex(vertex);
            // removendo vertex da lstDAGVertex.
            this.lstModel_DAGVertexs.removeElementAt(lstDAGVertexs.getSelectedIndex());
        }
    }//GEN-LAST:event_btnRemVertexActionPerformed

    private void btnRemAllVertexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemAllVertexActionPerformed
        while (this.lstModel_DAGVertexs.size() > 0) {
            // removendo vertex do grafo
            TableVertex vertex = (TableVertex) this.lstModel_DAGVertexs.getElementAt(0);
            this.graph.removeVertex(vertex);
            // removendo vertex da lstDAGVertex.
            this.lstModel_DAGVertexs.removeElementAt(0);
        }
    }//GEN-LAST:event_btnRemAllVertexActionPerformed

    private void btnRemAllVertex1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemAllVertex1ActionPerformed
        clearAllComponents();
        graph = new DirectedAcyclicGraph<>(RelationshipEdge.class);
    }//GEN-LAST:event_btnRemAllVertex1ActionPerformed

    private void btnExportDAGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportDAGActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JSON Files", "json");
        //chooser.setCurrentDirectory(new File("D:\\DADOS\\2. Doutorado - UFPR\\Letícia\\4. SimCaq\\Amostra de Dados Original - SIMCAQ"));
        chooser.setCurrentDirectory(new File(MainConversionJFrame.filePath));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String filename = chooser.getSelectedFile().getAbsolutePath();
            if (!filename.toLowerCase().contains(".json")) {
                filename += ".json";
            }
            GraphUtils.saveDagAsJsonObject(graph, GraphUtils.getRootVertex(graph).getTableName(), filename);
        }
    }//GEN-LAST:event_btnExportDAGActionPerformed

    private void btnVisualizingDAGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVisualizingDAGActionPerformed
        DAGVisualization dialog = new DAGVisualization(null, true);
        dialog.loadGraph(graph, 0);
        dialog.setTitle("TESTE");
        dialog.setVisible(true);
    }//GEN-LAST:event_btnVisualizingDAGActionPerformed

    private void cmbSourceVertexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSourceVertexActionPerformed
        if (cmbSourceVertex.getSelectedIndex() != -1) {
            loadCmbTargetVertexs();
        }
    }//GEN-LAST:event_cmbSourceVertexActionPerformed

    private void btnAddEdgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddEdgeActionPerformed

        if (cmbSourceVertex.getSelectedIndex() != -1 && cmbTargetVertex.getSelectedIndex() != -1) {
            // adição manual da aresta...
            if (chkAllowAllVertices.isSelected()) {
                
                TableVertex source = (TableVertex) cmbSourceVertex.getSelectedItem();
                TableVertex target = (TableVertex) cmbTargetVertex.getSelectedItem();                
                RelationshipEdge edge = new RelationshipEdge(source.getTableName(), target.getTableName(), "", "");
                graph.addEdge(source, target, edge);
                
                EdgeCreatorJDialog edgeDialog = new EdgeCreatorJDialog(null, true);
                edgeDialog.setRdbConnection(rdbConnection);
                edgeDialog.setEdge(edge);
                edgeDialog.setVisible(true);
                
                if (!edgeDialog.isCancel()){                    
                    edgesArray.add(edgeDialog.getEdge());
                    tblModel_DAGEdges.addRow(new Object[]{source.getName() + "(" + source.getId() + ")", "-->", target.getName() + "(" + target.getId() + ")", "Rem"});
                } else {
                    // remove aresta do graph.
                    graph.removeEdge(edge);
                }
            // adição automatica da aresta, ou seja, a aresta é criada com base nos metadados do RDB.
            } else {

                TableVertex source = (TableVertex) cmbSourceVertex.getSelectedItem();
                TableVertex target = (TableVertex) cmbTargetVertex.getSelectedItem();
                RelationshipEdge edge = createEdgeFromRDBMetadata(source.getTableName(), target.getTableName());

                graph.addEdge(source, target, edge);
                edgesArray.add(edge);
                tblModel_DAGEdges.addRow(new Object[]{source.getName() + "(" + source.getId() + ")", "-->", target.getName() + "(" + target.getId() + ")", "Rem"});
            }
            loadCollectionMetrics(); // carrega tabela com as métricas da coleção que está sendo criada.
        }
    }//GEN-LAST:event_btnAddEdgeActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        this.cancel = false;
        // validation 1
        if (graph.vertexSet().size()==0){
            JOptionPane.showMessageDialog(this, "You need to add at least one vertex to create the collection.","Error to create collection", JOptionPane.ERROR_MESSAGE);
            this.cancel = true;
        }
        // validation 2
        if (graph.vertexSet().size()>1){
            for (TableVertex table : graph.vertexSet()){
                if (graph.degreeOf(table) == 0){
                    JOptionPane.showMessageDialog(this, "All the vertices must be connected by edges to create the collection.","Error to create collection", JOptionPane.ERROR_MESSAGE);            
                    this.cancel = true;
                    break;
                }
            }
        }
        
        if (this.cancel==false)
            this.dispose();
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        this.cancel = true;
        this.dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void tblEdgesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblEdgesMouseClicked

        if (evt.getClickCount() > 1) {
            if (tblEdges.getSelectedRow() != -1) {
                if (tblEdges.getSelectedColumn() == 3) {
                    while (tblEdges.getSelectedRows().length > 0) {
                        graph.removeEdge(edgesArray.get(tblEdges.getSelectedRows()[0]));
                        edgesArray.remove(tblEdges.getSelectedRows()[0]);
                        tblModel_DAGEdges.removeRow(tblEdges.getSelectedRows()[0]);
                    }
                } else {

                    EdgeCreatorJDialog edgeDialog = new EdgeCreatorJDialog(null, true);
                    edgeDialog.setRdbConnection(rdbConnection);
                    edgeDialog.setEdge(edgesArray.get(tblEdges.getSelectedRows()[0]));
                    edgeDialog.setVisible(true);

                }
            }
        }

    }//GEN-LAST:event_tblEdgesMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DAGCreatorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DAGCreatorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DAGCreatorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DAGCreatorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DAGCreatorDialog dialog = new DAGCreatorDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAllVertex;
    private javax.swing.JButton btnAddEdge;
    private javax.swing.JButton btnAddVertex;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnExportDAG;
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnRemAllVertex;
    private javax.swing.JButton btnRemAllVertex1;
    private javax.swing.JButton btnRemVertex;
    private javax.swing.JButton btnVisualizingDAG;
    private javax.swing.JCheckBox chkAllowAllVertices;
    private javax.swing.JComboBox cmbSourceVertex;
    private javax.swing.JComboBox cmbTargetVertex;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JList<String> lstDAGVertexs;
    private javax.swing.JList<String> lstRDBTables;
    private javax.swing.JTable tblCollectionMetric;
    private javax.swing.JTable tblEdges;
    // End of variables declaration//GEN-END:variables
}
