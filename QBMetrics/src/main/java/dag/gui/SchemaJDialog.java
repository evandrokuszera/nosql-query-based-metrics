/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.gui;

import metrics.Metrics;
import dag.model.RelationshipEdge;
import dag.model.TableVertex;
import dag.utils.GraphUtils;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jdbc_connection.GenericConnection;
import dag.nosql_schema.NoSQLSchema;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONObject;
import dag.persistence.DAGJson;
import dag.persistence.JSONPersistence;

/**
 *
 * @author Evandro
 */
public class SchemaJDialog extends javax.swing.JDialog {
    private NoSQLSchema nosqlSchema; 
    private GenericConnection rdbConnection; //= new PostgresConnection("postgres", "123456", "localhost", "ds2_10mb");
    private DefaultTableModel modelTable = new DefaultTableModel();
    private boolean cancel = true;
    
    /**
     * Creates new form SchemaJDialog
     */
    public SchemaJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        modelTable = (DefaultTableModel) this.tblCollections.getModel();        
        
        tblCollections.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblCollections.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblCollections.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblCollections.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblCollections.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblCollections.getColumnModel().getColumn(5).setPreferredWidth(150);
        tblCollections.getColumnModel().getColumn(6).setPreferredWidth(150);
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setRdbConnection(GenericConnection rdbConnection) {
        this.rdbConnection = rdbConnection;
    }
        
    public void setNosqlSchema(NoSQLSchema nosqlSchema) {
        this.nosqlSchema = nosqlSchema;
        this.txtSchemaName.setText(this.nosqlSchema.getName());
        this.txtSchemaNameKeyReleased(null);
        this.loadCollections();
    }

    public NoSQLSchema getNosqlSchema() {
        return nosqlSchema;
    }
    
    private void loadCollections(){        
        // Remove todas as entradas da tabela tblCollections.
        while (tblCollections.getRowCount() > 0) modelTable.removeRow(0);
        
        // Objeto metrics usado aqui para carregar as métricas da coleção na tblCollections.
        Metrics metrics = new Metrics(nosqlSchema);
        
        // Carrega tabela tblCollection com as collections (DAGs) do nosqlSchema.
        for (DirectedAcyclicGraph<TableVertex, RelationshipEdge> dag : this.nosqlSchema.getEntities()){
            String collection = GraphUtils.getRootVertex(dag).getName();
            
            Object[] row = new Object[7];
            row[0] = collection;
            row[1] = metrics.size().getNumberOfDocuments(collection);
            row[2] = metrics.size().getNumberOfArraysOfDocuments(collection);
            row[3] = "N/D"; // Ainda não implementei isso, nem sei se será útil.
            row[4] = metrics.size().getNumberOfAtomicAttributes(collection);
            row[5] = metrics.size().getCollectionSize(collection, false);
            row[6] = metrics.depth().getDepthOfCollection(collection);
            
            modelTable.addRow(row);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSchemaName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCollections = new javax.swing.JTable();
        btnAddCollection = new javax.swing.JButton();
        btnRemoveCollection = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();
        btnViewCollection = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("NoSQL Schema");

        btnOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ok.png"))); // NOI18N
        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cancelar.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jTabbedPane1.setFocusable(false);

        jLabel1.setText("Name:");

        txtSchemaName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSchemaNameKeyReleased(evt);
            }
        });

        jLabel2.setText("Collections:");

        tblCollections.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Collection", "Docs", "Doc Arrays", "Arrays", "Atomic", "Size", "Depth"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCollections.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCollectionsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblCollections);

        btnAddCollection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adicionar.png"))); // NOI18N
        btnAddCollection.setText("Add");
        btnAddCollection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCollectionActionPerformed(evt);
            }
        });

        btnRemoveCollection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/remover.png"))); // NOI18N
        btnRemoveCollection.setText("Remove");
        btnRemoveCollection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveCollectionActionPerformed(evt);
            }
        });

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exportar.png"))); // NOI18N
        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/importar.png"))); // NOI18N
        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        btnViewCollection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/visualizar.png"))); // NOI18N
        btnViewCollection.setText("View");
        btnViewCollection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewCollectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtSchemaName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAddCollection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRemoveCollection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnViewCollection, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnExport, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAddCollection, btnRemoveCollection, btnViewCollection});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSchemaName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddCollection)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveCollection)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnViewCollection))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(0, 5, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 589, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel)
                .addGap(8, 8, 8))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancel, btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        if (txtSchemaName.getText().length()==0){
            JOptionPane.showMessageDialog(this, "Please, you should enter a schema name.", "Requeried Field", JOptionPane.ERROR_MESSAGE);
        } else if (this.modelTable.getRowCount() == 0){
            JOptionPane.showMessageDialog(this, "Please, you should add at leat one collection in the schema.", "Requeried Field", JOptionPane.ERROR_MESSAGE);
        } else {
            this.nosqlSchema.setName(txtSchemaName.getText());
            this.cancel = false;
            this.dispose();            
        }
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.cancel = true;
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnAddCollectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCollectionActionPerformed
        DAGCreatorDialog dagCreatorDialog = new DAGCreatorDialog(null, true);
        dagCreatorDialog.setRdbConnection(rdbConnection);
        dagCreatorDialog.setVisible(true);
        
        if (!dagCreatorDialog.isCancel()){
            this.nosqlSchema.getEntities().add(dagCreatorDialog.getGraph());            
            this.loadCollections();
        }
    }//GEN-LAST:event_btnAddCollectionActionPerformed

    private void btnRemoveCollectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveCollectionActionPerformed
        if (tblCollections.getSelectedRow() != -1){
            while (tblCollections.getSelectedRows().length > 0){
                this.nosqlSchema.getEntities().remove(tblCollections.getSelectedRows()[0]);
                this.modelTable.removeRow(tblCollections.getSelectedRows()[0]);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please, select one or more collection for remove.");
        }
    }//GEN-LAST:event_btnRemoveCollectionActionPerformed

    private void btnViewCollectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewCollectionActionPerformed
        if (tblCollections.getSelectedRow() != -1){
            DAGVisualization dialog = new DAGVisualization(null, false);
            dialog.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
            dialog.loadGraph(this.nosqlSchema.getEntities().get(tblCollections.getSelectedRow()), 0);
            dialog.setTitle(this.nosqlSchema.getEntityName(tblCollections.getSelectedRow()));
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please, select one collection to view the graph.");
        }       
    }//GEN-LAST:event_btnViewCollectionActionPerformed

    private void tblCollectionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCollectionsMouseClicked
        if (evt.getClickCount() > 1){
            JOptionPane.showMessageDialog(this, "Sorry, but in this version is not possible to edit a collection.", "Functionality not available",JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_tblCollectionsMouseClicked

    private void txtSchemaNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSchemaNameKeyReleased
        this.setTitle("NoSQL Schema - ["+this.txtSchemaName.getText()+"]");
    }//GEN-LAST:event_txtSchemaNameKeyReleased

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        if (tblCollections.getSelectedRow() != -1){  
            
            
            String filename = MainConversionJFrame.saveFileChooser();
            if (filename!=null){
                JSONObject obj = DAGJson.toJSON(
                    this.nosqlSchema.getEntities().get(tblCollections.getSelectedRow()), 
                    (String) modelTable.getValueAt(tblCollections.getSelectedRow(), 0));
                
                JSONPersistence.saveJSONtoFile(obj, filename);
            }
        }
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        
        String filename = MainConversionJFrame.openFileChooser();
        if (filename != null){
            JSONObject obj = JSONPersistence.loadJSONfromFile(filename);
            nosqlSchema.getEntities().add(DAGJson.fromJSON(obj));
            this.loadCollections();
        }
    }//GEN-LAST:event_btnImportActionPerformed

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
            java.util.logging.Logger.getLogger(SchemaJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SchemaJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SchemaJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SchemaJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SchemaJDialog dialog = new SchemaJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAddCollection;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnRemoveCollection;
    private javax.swing.JButton btnViewCollection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable tblCollections;
    private javax.swing.JTextField txtSchemaName;
    // End of variables declaration//GEN-END:variables
}