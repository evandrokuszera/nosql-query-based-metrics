/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.gui;

import dag.persistence.ConversionProcessJson;
import dag.persistence.JSONPersistence;
import metrics.Metrics;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import java.awt.Dialog;
import java.io.File;
import java.sql.SQLException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import jdbc_connection.GenericConnection;
import jdbc_connection.PostgresConnection;
import dag.nosql_schema.ConversionProcess;
import dag.nosql_schema.NoSQLSchema;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONObject;
import dag.persistence.DAGJson;
import dag.persistence.NoSQLSchemaJson;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evandro
 */
public class MainConversionJFrame extends javax.swing.JFrame {
    private GenericConnection connection;
    private ConversionProcess conversionProcess;
    private DefaultTableModel modelSchemaTable = new DefaultTableModel();
    private DefaultTableModel modelQueriesTable = new DefaultTableModel();
    
    public static String filePath = "D:\\";

    /**
     * Creates new form MainConversionJFrame
     */
    public MainConversionJFrame() {
        initComponents();
        
        this.btnStructuralMetrics.setVisible(false);
        
        conversionProcess = new ConversionProcess();
        
        modelSchemaTable = (DefaultTableModel) this.tblSchemas.getModel();
        modelQueriesTable = (DefaultTableModel) this.tblQueries.getModel();
        
        // Largura padrão das colunas
        tblQueries.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblQueries.getColumnModel().getColumn(1).setPreferredWidth(535);
        // Desligar o autoresize, caso contrario não funciona a configuração do tamanho das colunas.
        tblQueries.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        loadDefaultValuesToTest();
    }  
    
    private void loadDefaultValuesToTest(){
        this.txtDatabase.setText("ds2_10mb");
        this.txtNome.setText("Identification name of the RDB to NoSQL Document process");
        this.txtServer.setText("localhost");
        this.txtUser.setText("postgres");
        this.cmbDriver.setSelectedIndex(0);
        this.pasPassword.setText("123456");
    }
    
    private void clearControls(){
        this.txtDatabase.setText("");
        this.txtNome.setText("");
        this.txtServer.setText("");
        this.txtUser.setText("");
        this.cmbDriver.setSelectedIndex(-1);
        this.pasPassword.setText("");
        
        // Limpa os dados da tabela.
        while (modelSchemaTable.getRowCount() > 0) modelSchemaTable.removeRow(0);
        while (modelQueriesTable.getRowCount() > 0) modelQueriesTable.removeRow(0);
    }
    
    private void writeControls(){
        this.txtDatabase.setText(conversionProcess.getDatabase());
        this.txtNome.setText(conversionProcess.getName());
        this.txtServer.setText(conversionProcess.getServer());
        this.txtUser.setText(conversionProcess.getUser());
        this.cmbDriver.setSelectedItem(conversionProcess.getDriver());
        this.pasPassword.setText(conversionProcess.getPassword());
        loadSchemaTable();
        loadQueriesTable();
    }
    
    private void readControls(){
        conversionProcess.setDatabase(this.txtDatabase.getText());
        conversionProcess.setName(this.txtNome.getText());
        conversionProcess.setServer(this.txtServer.getText());
        conversionProcess.setUser(this.txtUser.getText());
        conversionProcess.setDriver(this.cmbDriver.getSelectedItem().toString());
        conversionProcess.setPassword(this.pasPassword.getText());
    }
    
    private void loadSchemaTable(){
        // Limpa os dados da tabela.
        while (modelSchemaTable.getRowCount() > 0) modelSchemaTable.removeRow(0);
        
        for (NoSQLSchema schema : conversionProcess.getSchemas()){
            // Define o objeto metrics para o schema corrente.
            Metrics metrics = new Metrics(schema);
            
            Object[] row = new Object[8];
            row[0] = schema.getName();
            row[1] = metrics.size().getNumberOfCollectionsInSchema();
            row[2] = metrics.size().getNumberOfDocumentsInSchema();
            row[3] = metrics.size().getNumberOfArraysOfDocumentsInSchema();
            row[4] = "N/D"; // não implementei.
            row[5] = metrics.size().getNumberOfAtomicAttributesInSchema();
            row[6] = metrics.size().getSchemaSize(false);
            row[7] = metrics.depth().getDepthOfSchema();
                        
            modelSchemaTable.addRow(row);
        }
    }
    
    private void loadQueriesTable(){
        // Limpa os dados da tabela.
        while (modelQueriesTable.getRowCount() > 0) modelQueriesTable.removeRow(0);
        
        // Define o objeto metrics para usar o método getPaths.
        Metrics metrics = new Metrics(null);
        
        int count = 1;
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> query : conversionProcess.getQueries()){
            
            Object[] row = new Object[2];
            row[0] = count++;
            row[1] = metrics.path().getPaths(query);
                                    
            modelQueriesTable.addRow(row);
        }
    }
    
    public static String openFileChooser(){
        return openFileChooser(MainConversionJFrame.filePath);
    }
    
    public static String openFileChooser(String filePath){
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
        MainConversionJFrame.filePath = filePath;
        chooser.setCurrentDirectory(new File(MainConversionJFrame.filePath));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            MainConversionJFrame.filePath = chooser.getCurrentDirectory().getAbsolutePath();
            String filename = chooser.getSelectedFile().getAbsolutePath();
            if (! filename.toLowerCase().contains(".json")){
                filename += ".json";
            }
            return filename;
        }
        return null;
    }
    
    public static String saveFileChooser(){
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
        chooser.setCurrentDirectory(new File(MainConversionJFrame.filePath));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            MainConversionJFrame.filePath = chooser.getCurrentDirectory().getAbsolutePath();
            String filename = chooser.getSelectedFile().getAbsolutePath();
            if (! filename.toLowerCase().contains(".json")){
                filename += ".json";
            }
            return filename;
        }     
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtNome = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSchemas = new javax.swing.JTable();
        btnAddSchema = new javax.swing.JButton();
        btnRemoveSchema = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        btnQueryMetrics = new javax.swing.JButton();
        btnStructuralMetrics = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblQueries = new javax.swing.JTable();
        btnAddQuery = new javax.swing.JButton();
        btnRemoveQuery = new javax.swing.JButton();
        btnExportQuery = new javax.swing.JButton();
        btnImportQuery = new javax.swing.JButton();
        btnViewQuery = new javax.swing.JButton();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cmbDriver = new javax.swing.JComboBox<>();
        btnConnectionTest = new javax.swing.JButton();
        txtDatabase = new javax.swing.JTextField();
        pasPassword = new javax.swing.JPasswordField();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtServer = new javax.swing.JTextField();
        txtUser = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu = new javax.swing.JMenu();
        mniNew = new javax.swing.JMenuItem();
        mniLoad = new javax.swing.JMenuItem();
        mniSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mniExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        mniCAiSE_article = new javax.swing.JMenuItem();
        mniCAiSE_demo = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("QBMetrics - RDB to NoSQL Conversion Tool");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Conversion Process Name:"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtNome, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tblSchemas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Collections", "Docs", "Doc Arrays", "Arrays", "Atomic", "Size", "MaxDepth"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSchemas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSchemasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblSchemas);

        btnAddSchema.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adicionar.png"))); // NOI18N
        btnAddSchema.setText("Add");
        btnAddSchema.setMaximumSize(new java.awt.Dimension(91, 25));
        btnAddSchema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSchemaActionPerformed(evt);
            }
        });

        btnRemoveSchema.setIcon(new javax.swing.ImageIcon(getClass().getResource("/remover.png"))); // NOI18N
        btnRemoveSchema.setText("Remove");
        btnRemoveSchema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveSchemaActionPerformed(evt);
            }
        });

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exportar.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.setMaximumSize(new java.awt.Dimension(91, 25));
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/importar.png"))); // NOI18N
        btnImport.setText("Import");
        btnImport.setMaximumSize(new java.awt.Dimension(91, 25));
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/visualizar.png"))); // NOI18N
        btnView.setText("View");
        btnView.setMaximumSize(new java.awt.Dimension(91, 25));
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 594, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnRemoveSchema, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .addComponent(btnExport, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .addComponent(btnImport, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .addComponent(btnAddSchema, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .addComponent(btnView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAddSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveSchema)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnAddSchema, btnExport, btnImport, btnRemoveSchema, btnView});

        jTabbedPane1.addTab("NoSQL Schemas as DAGs", jPanel2);

        btnQueryMetrics.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qmetrics.png"))); // NOI18N
        btnQueryMetrics.setText("Query Metrics");
        btnQueryMetrics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueryMetricsActionPerformed(evt);
            }
        });

        btnStructuralMetrics.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smetrics.png"))); // NOI18N
        btnStructuralMetrics.setText("SMetrics");
        btnStructuralMetrics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStructuralMetricsActionPerformed(evt);
            }
        });

        tblQueries.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Paths"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblQueries);
        if (tblQueries.getColumnModel().getColumnCount() > 0) {
            tblQueries.getColumnModel().getColumn(0).setResizable(false);
        }

        btnAddQuery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adicionar.png"))); // NOI18N
        btnAddQuery.setText("Add");
        btnAddQuery.setMaximumSize(new java.awt.Dimension(91, 25));
        btnAddQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddQueryActionPerformed(evt);
            }
        });

        btnRemoveQuery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/remover.png"))); // NOI18N
        btnRemoveQuery.setText("Remove");
        btnRemoveQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveQueryActionPerformed(evt);
            }
        });

        btnExportQuery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exportar.png"))); // NOI18N
        btnExportQuery.setText("Export");
        btnExportQuery.setMaximumSize(new java.awt.Dimension(91, 25));
        btnExportQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportQueryActionPerformed(evt);
            }
        });

        btnImportQuery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/importar.png"))); // NOI18N
        btnImportQuery.setText("Import");
        btnImportQuery.setMaximumSize(new java.awt.Dimension(91, 25));
        btnImportQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportQueryActionPerformed(evt);
            }
        });

        btnViewQuery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/visualizar.png"))); // NOI18N
        btnViewQuery.setText("View");
        btnViewQuery.setMaximumSize(new java.awt.Dimension(91, 25));
        btnViewQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewQueryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRemoveQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAddQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(btnExportQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnImportQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnViewQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAddQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImportQuery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnViewQuery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Queries as DAGs", jPanel3);

        jLabel2.setText("Driver:");

        cmbDriver.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Postgres", "MySQL" }));

        btnConnectionTest.setText("Connection Test");
        btnConnectionTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectionTestActionPerformed(evt);
            }
        });

        pasPassword.setText("jPasswordField1");

        jLabel6.setText("Database:");

        jLabel5.setText("Password:");

        jLabel4.setText("User:");

        jLabel3.setText("Server:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(cmbDriver, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtUser, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                            .addComponent(txtServer, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDatabase))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pasPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnConnectionTest, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {pasPassword, txtUser});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(txtDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(pasPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)))
                    .addComponent(btnConnectionTest, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {pasPassword, txtUser});

        jTabbedPane3.addTab("RDB Source", jPanel4);

        jMenu.setText("File");

        mniNew.setText("New");
        mniNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniNewActionPerformed(evt);
            }
        });
        jMenu.add(mniNew);

        mniLoad.setText("Load");
        mniLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLoadActionPerformed(evt);
            }
        });
        jMenu.add(mniLoad);

        mniSave.setText("Save");
        mniSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSaveActionPerformed(evt);
            }
        });
        jMenu.add(mniSave);
        jMenu.add(jSeparator1);

        mniExit.setText("Exit");
        mniExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExitActionPerformed(evt);
            }
        });
        jMenu.add(mniExit);

        jMenuBar1.add(jMenu);

        jMenu2.setText("Help");

        jMenuItem5.setText("About");
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu1.setForeground(new java.awt.Color(255, 0, 51));
        jMenu1.setText("Demo");

        mniCAiSE_article.setText("CAiSE main article");
        mniCAiSE_article.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniCAiSE_articleActionPerformed(evt);
            }
        });
        jMenu1.add(mniCAiSE_article);

        mniCAiSE_demo.setText("CAiSE demo tool");
        mniCAiSE_demo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniCAiSE_demoActionPerformed(evt);
            }
        });
        jMenu1.add(mniCAiSE_demo);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnQueryMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnStructuralMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnQueryMetrics)
                    .addComponent(btnStructuralMetrics))
                .addGap(5, 5, 5))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void mniLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadActionPerformed
        String filename = openFileChooser();
        if (filename!=null){
            JSONObject jsonConversionProcess = JSONPersistence.loadJSONfromFile(filename);
            this.conversionProcess = ConversionProcessJson.fromJSON(jsonConversionProcess);
            writeControls();
        }
    }//GEN-LAST:event_mniLoadActionPerformed

    private void mniExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_mniExitActionPerformed

    private void mniNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniNewActionPerformed
        clearControls();
        conversionProcess = new ConversionProcess();
        modelSchemaTable = (DefaultTableModel) this.tblSchemas.getModel();
    }//GEN-LAST:event_mniNewActionPerformed

    private void btnAddSchemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSchemaActionPerformed
        if (connection != null){
            SchemaJDialog schemaDialog = new SchemaJDialog(this, true);
            schemaDialog.setNosqlSchema(new NoSQLSchema(""));
            schemaDialog.setRdbConnection(connection);
            schemaDialog.setVisible(true);
            if (!schemaDialog.isCancel()){
                conversionProcess.getSchemas().add(schemaDialog.getNosqlSchema());
                loadSchemaTable();
            }
        } else {
            btnConnectionTestActionPerformed(null); // conecta no RDB
            JOptionPane.showMessageDialog(this, "Please, press 'Connection Test' button before add new NoSQL schema.");
        }
    }//GEN-LAST:event_btnAddSchemaActionPerformed

    private void btnAddQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddQueryActionPerformed
        if (connection != null) {
            DAGCreatorDialog dagCreatorDialog = new DAGCreatorDialog(null, true);
            dagCreatorDialog.setRdbConnection(connection);
            dagCreatorDialog.setVisible(true);

            if (!dagCreatorDialog.isCancel()) {
                this.conversionProcess.getQueries().add(dagCreatorDialog.getGraph());
                loadQueriesTable();
            }
        } else {
            btnConnectionTestActionPerformed(null); // conecta no RDB
            JOptionPane.showMessageDialog(this, "Please, press 'Connection Test' button before add new NoSQL schema.");
        }
    }//GEN-LAST:event_btnAddQueryActionPerformed

    private void btnConnectionTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectionTestActionPerformed
        this.connection = new PostgresConnection(this.txtUser.getText(), this.pasPassword.getText(), this.txtServer.getText(), this.txtDatabase.getText());
        this.connection.openConnection();
        try {
            if (!this.connection.getConnection().isClosed()){
                JOptionPane.showMessageDialog(this, "RDB connection established with success!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error in connecting to RDB database:"+ex);
        }
        this.connection.closeConnection();
    }//GEN-LAST:event_btnConnectionTestActionPerformed

    private void mniSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveActionPerformed
        String filename = saveFileChooser();
        if (filename!=null){
            readControls();
            JSONObject jsonConversionProcess = ConversionProcessJson.toJSON(conversionProcess);
            JSONPersistence.saveJSONtoFile(jsonConversionProcess, filename);
        }
    }//GEN-LAST:event_mniSaveActionPerformed

    private void tblSchemasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSchemasMouseClicked
        if (tblSchemas.getSelectedRow() != -1 && evt.getClickCount() > 1){
            if (connection == null){
                btnConnectionTestActionPerformed(null); // conecta no RDB
            } else {
            
                NoSQLSchema selectNoSQLSchema = conversionProcess.getSchemas().get(tblSchemas.getSelectedRow());

                SchemaJDialog schemaDialog = new SchemaJDialog(this, true);
                schemaDialog.setNosqlSchema(selectNoSQLSchema);
                schemaDialog.setRdbConnection(connection);
                schemaDialog.setVisible(true);
                if (!schemaDialog.isCancel()){
                    conversionProcess.getSchemas().set(tblSchemas.getSelectedRow(), schemaDialog.getNosqlSchema());                       
                    loadSchemaTable();
                }
            }
        }
    }//GEN-LAST:event_tblSchemasMouseClicked

    private void btnRemoveSchemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSchemaActionPerformed
        if (tblSchemas.getSelectedRow() != -1){
            while (tblSchemas.getSelectedRows().length > 0){
                this.conversionProcess.getSchemas().remove(tblSchemas.getSelectedRows()[0]);
                this.modelSchemaTable.removeRow(tblSchemas.getSelectedRows()[0]);                
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please, select one or more schemas for remove.");
        }
    }//GEN-LAST:event_btnRemoveSchemaActionPerformed

    private void btnStructuralMetricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStructuralMetricsActionPerformed
        if (tblSchemas.getSelectedRow() != -1){
            ShowMetrics showMetrics = new ShowMetrics();            
            showMetrics.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            showMetrics.setNoSQLSchema(this.conversionProcess.getSchemas().get(tblSchemas.getSelectedRow()));
            showMetrics.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please, you should select one NoSQL schema to view the metrics.");
        }
    }//GEN-LAST:event_btnStructuralMetricsActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        if (tblSchemas.getSelectedRow() != -1){
            String filename = saveFileChooser();
            if (filename!=null){
                JSONObject obj = NoSQLSchemaJson.toJSON(this.conversionProcess.getSchemas().get(tblSchemas.getSelectedRow()));
                JSONPersistence.saveJSONtoFile(obj, filename);
            }
        }
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        String filename = openFileChooser();
        if (filename != null){
            JSONObject obj = JSONPersistence.loadJSONfromFile(filename);
            conversionProcess.getSchemas().add(NoSQLSchemaJson.fromJSON(obj));
            this.loadSchemaTable();
        }
    }//GEN-LAST:event_btnImportActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        if (tblSchemas.getSelectedRow() != -1){
            DAGVisualization dialog = new DAGVisualization(null, false);
            dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            dialog.loadGraph(conversionProcess.getSchemas().get(tblSchemas.getSelectedRow()).getDAGSchema(), 0);
            dialog.setTitle(conversionProcess.getSchemas().get(tblSchemas.getSelectedRow()).getName());
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please, select one schema to view the graph.");
        }
    }//GEN-LAST:event_btnViewActionPerformed

    private void btnRemoveQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveQueryActionPerformed
        if (tblQueries.getSelectedRow() != -1){
            while (tblQueries.getSelectedRows().length > 0){
                this.conversionProcess.getQueries().remove(tblQueries.getSelectedRows()[0]);
                this.modelQueriesTable.removeRow(tblQueries.getSelectedRows()[0]);                
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please, select one or more queries for remove.");
        }
    }//GEN-LAST:event_btnRemoveQueryActionPerformed

    private void btnExportQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportQueryActionPerformed
        if (tblQueries.getSelectedRow() != -1){
            String filename = saveFileChooser();
            if (filename!=null){
                String queryId = "Query " + (tblQueries.getSelectedRow() + 1);
                JSONObject obj = DAGJson.toJSON(this.conversionProcess.getQueries().get(tblQueries.getSelectedRow()),queryId);
                JSONPersistence.saveJSONtoFile(obj, filename);
            }
        }
    }//GEN-LAST:event_btnExportQueryActionPerformed

    private void btnImportQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportQueryActionPerformed
        String filename = openFileChooser();
        if (filename != null){
            JSONObject obj = JSONPersistence.loadJSONfromFile(filename);
            conversionProcess.getQueries().add(DAGJson.fromJSON(obj));
            this.loadQueriesTable();
        }
    }//GEN-LAST:event_btnImportQueryActionPerformed

    private void btnViewQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewQueryActionPerformed
        if (tblQueries.getSelectedRow() != -1){
            DAGVisualization dialog = new DAGVisualization(null, false);
            dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            dialog.loadGraph(conversionProcess.getQueries().get(tblQueries.getSelectedRow()), 0);
            dialog.setTitle("Query " + tblQueries.getSelectedRow() + 1);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please, select one schema to view the graph.");
        }
    }//GEN-LAST:event_btnViewQueryActionPerformed

    private void btnQueryMetricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueryMetricsActionPerformed
        ShowQueryMetricsJFrame showMetrics = new ShowQueryMetricsJFrame();            
        showMetrics.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        showMetrics.setConversionProcess(this.conversionProcess);
        showMetrics.setVisible(true);
    }//GEN-LAST:event_btnQueryMetricsActionPerformed

    private void mniCAiSE_articleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCAiSE_articleActionPerformed
        String msg = "The QBMetrics tool is used to convert RDB to NoSQL document model.\n";
        msg += "\n";
        msg += "This tool aims to create one or more NoSQL schemas from an input RDB.\n";
        msg += "The term 'NoSQL schema' is used to represent the format of the documents in the collections that will be created.\n";
        msg += "\n";
        msg += "You clicked on the 'CAiSE main article' option.\n";
        msg += "This option loads a set of schemas and queries used in the experiments of the article submitted to CAiSE20:\n";
        msg += "'Query-based metrics for evaluating and comparing document schemas'\n";
        msg += "\n";
        msg += "Note: to add or edit schemas and queries, an input RDB is required.\n";
        msg += "In this way, you will only be able to view and calculate the metrics on the schemas and queries of the CAiSE20 article.\n";
        msg += "The RDB used in the experiments can be found here: https://linux.dell.com/dvdstore/\n";
        
        JOptionPane.showMessageDialog(this, msg, "CAiSE20 Main Article", JOptionPane.INFORMATION_MESSAGE);
        
        try {
            File f = new File(this.getClass().getResource("/caise20/main_article_conversion_process.json").toURI());
            JSONObject jsonConversionProcess = JSONPersistence.loadJSONfromFile(f.getAbsolutePath());
            this.conversionProcess = ConversionProcessJson.fromJSON(jsonConversionProcess);
            writeControls();
        } catch (URISyntaxException ex) {
            JOptionPane.showMessageDialog(this, "Error opening the 'main_article_conversion_process.json' file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mniCAiSE_articleActionPerformed

    private void mniCAiSE_demoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCAiSE_demoActionPerformed
        String msg = "The QBMetrics tool is used to convert RDB to NoSQL document model.\n";
        msg += "\n";
        msg += "This tool aims to create one or more NoSQL schemas from an input RDB.\n";
        msg += "The term 'NoSQL schema' is used to represent the format of the documents in the collections that will be created.\n";
        msg += "\n";
        msg += "You clicked on the 'CAiSE demo' option.\n";
        msg += "This option loads a set of schemas and queries used in the experiments of the demo article submitted to CAiSE20 Forum:\n";
        msg += "'QBMetrics: a tool for evaluating and comparing document schemas'\n";
        msg += "\n";
        msg += "Note: to add or edit schemas and queries, an input RDB is required.\n";
        msg += "In this way, you will only be able to view and calculate the metrics on the schemas and queries of the CAiSE20 Forum article.\n";
        msg += "The RDB used in the experiments can be found here: https://linux.dell.com/dvdstore/\n";
        
        JOptionPane.showMessageDialog(this, msg, "CAiSE20 Demo Article", JOptionPane.INFORMATION_MESSAGE);
        
        try {
            File f = new File(this.getClass().getResource("/caise20/demo_article_conversion_process.json").toURI());
            JSONObject jsonConversionProcess = JSONPersistence.loadJSONfromFile(f.getAbsolutePath());
            this.conversionProcess = ConversionProcessJson.fromJSON(jsonConversionProcess);
            writeControls();
        } catch (URISyntaxException ex) {
            JOptionPane.showMessageDialog(this, "Error opening the 'demo_article_conversion_process.json' file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mniCAiSE_demoActionPerformed

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
                //if ("Nimbus".equals(info.getName())) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainConversionJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainConversionJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainConversionJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainConversionJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainConversionJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddQuery;
    private javax.swing.JButton btnAddSchema;
    private javax.swing.JButton btnConnectionTest;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnExportQuery;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnImportQuery;
    private javax.swing.JButton btnQueryMetrics;
    private javax.swing.JButton btnRemoveQuery;
    private javax.swing.JButton btnRemoveSchema;
    private javax.swing.JButton btnStructuralMetrics;
    private javax.swing.JButton btnView;
    private javax.swing.JButton btnViewQuery;
    private javax.swing.JComboBox<String> cmbDriver;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JMenuItem mniCAiSE_article;
    private javax.swing.JMenuItem mniCAiSE_demo;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniLoad;
    private javax.swing.JMenuItem mniNew;
    private javax.swing.JMenuItem mniSave;
    private javax.swing.JPasswordField pasPassword;
    private javax.swing.JTable tblQueries;
    private javax.swing.JTable tblSchemas;
    private javax.swing.JTextField txtDatabase;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtServer;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables
}
