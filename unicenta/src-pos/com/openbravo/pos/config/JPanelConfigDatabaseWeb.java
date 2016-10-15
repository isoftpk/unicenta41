//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2016 uniCenta
//    http://www.unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.config;

import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DriverWrapper;
import com.openbravo.pos.util.AltEncrypter;
import com.openbravo.pos.util.DirectoryEvent;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * @author Jack Gerrard
 * @author adrianromero
 */
public class JPanelConfigDatabaseWeb extends javax.swing.JPanel implements PanelConfig {
    
    private final DirtyManager dirty = new DirtyManager();
    
    /** Creates new form JPanelConfigDatabase */
    public JPanelConfigDatabaseWeb() {
        
        initComponents();
        
        jtxtDbWebDriverLib.getDocument().addDocumentListener(dirty);
        jtxtDbWebDriver.getDocument().addDocumentListener(dirty);
        jbtnDbWebDriverLib.addActionListener(new DirectoryEvent(jtxtDbWebDriverLib));
        jcboDBDriver.addActionListener(dirty);
        jcboDBDriver.addItem("MySQL");
//        jcboDBDriver.addItem("PostgreSQL");
        
        jcboDBDriver.setSelectedIndex(0);
        
        webEnable.addActionListener(dirty);        
        
// primary DB        
        jtxtDbWebName.getDocument().addDocumentListener(dirty);
        jtxtDbWebURL.getDocument().addDocumentListener(dirty);
        jtxtDbWebIP.getDocument().addDocumentListener(dirty);
        jtxtDbWebPort.getDocument().addDocumentListener(dirty);        
        jtxtDbWebPassword.getDocument().addDocumentListener(dirty);
        jtxtDbWebUser.getDocument().addDocumentListener(dirty);        

// secondary DB - NONE

    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {
        
        webEnable.setSelected(Boolean.parseBoolean(config.getProperty("dbweb.enable")));                

        jcboDBDriver.setSelectedItem(config.getProperty("dbweb.engine"));
        jtxtDbWebDriverLib.setText(config.getProperty("dbweb.driverlib"));
        jtxtDbWebDriver.setText(config.getProperty("dbweb.driver"));

// primary DB              
        jtxtDbWebName.setText(config.getProperty("dbweb.name"));
        jtxtDbWebIP.setText(config.getProperty("dbweb.ip"));
        jtxtDbWebPort.setText(config.getProperty("dbweb.port"));        
        jtxtDbWebURL.setText(config.getProperty("dbweb.URL"));
        String sDBUser = config.getProperty("dbweb.user");
        String sDBPassword = config.getProperty("dbweb.password");        
        if (sDBUser != null && sDBPassword != null && sDBPassword.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser);
            sDBPassword = cypher.decrypt(sDBPassword.substring(6));
        }        
        jtxtDbWebUser.setText(sDBUser);
        jtxtDbWebPassword.setText(sDBPassword);   

// secondary DB - NONE

        dirty.setDirty(false);
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

// multi-db        
        config.setProperty("dbweb.enable",Boolean.toString(webEnable.isSelected()));
        
        config.setProperty("dbweb.engine", comboValue(jcboDBDriver.getSelectedItem()));
        config.setProperty("dbweb.driverlib", jtxtDbWebDriverLib.getText());
        config.setProperty("dbweb.driver", jtxtDbWebDriver.getText());

// primary DB
        config.setProperty("dbweb.name", jtxtDbWebName.getText());
        config.setProperty("dbweb.ip", jtxtDbWebIP.getText());
        config.setProperty("dbweb.port", jtxtDbWebPort.getText());        
        config.setProperty("dbweb.URL", jtxtDbWebURL.getText());
        config.setProperty("dbweb.user", jtxtDbWebUser.getText());
        AltEncrypter cypher = new AltEncrypter("cypherkey" + jtxtDbWebUser.getText());       
        config.setProperty("dbweb.password", "crypt:" + 
                cypher.encrypt(new String(jtxtDbWebPassword.getPassword())));

// secondary DB - NONE

        dirty.setDirty(false);
    }

    private String comboValue(Object value) {
        return value == null ? "" : value.toString();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        webPopOver1 = new com.alee.extended.window.WebPopOver();
        jLblDbWeb = new javax.swing.JLabel();
        jcboDBDriver = new javax.swing.JComboBox();
        jLblDbWebDriverLib = new javax.swing.JLabel();
        jtxtDbWebDriverLib = new javax.swing.JTextField();
        jbtnDbWebDriverLib = new javax.swing.JButton();
        jLblDbWebDriver = new javax.swing.JLabel();
        jtxtDbWebDriver = new javax.swing.JTextField();
        jLblDbWebURL = new javax.swing.JLabel();
        jtxtDbWebURL = new javax.swing.JTextField();
        jLblDbWebUser = new javax.swing.JLabel();
        jtxtDbWebUser = new javax.swing.JTextField();
        jLblDbWebPass = new javax.swing.JLabel();
        jtxtDbWebPassword = new javax.swing.JPasswordField();
        jBtnWebTest = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLblDbWebName = new javax.swing.JLabel();
        jtxtDbWebName = new javax.swing.JTextField();
        LblMultiDbWeb = new com.alee.laf.label.WebLabel();
        webEnable = new com.alee.extended.button.WebSwitch();
        jLblDbWebIP = new javax.swing.JLabel();
        jtxtDbWebIP = new javax.swing.JTextField();
        jLblDbWebPort = new javax.swing.JLabel();
        jtxtDbWebPort = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setPreferredSize(new java.awt.Dimension(900, 500));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLblDbWeb.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLblDbWeb.setText(bundle.getString("label.DatabaseWeb")); // NOI18N
        jLblDbWeb.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWeb, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, -1, -1));

        jcboDBDriver.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboDBDriver.setPreferredSize(new java.awt.Dimension(150, 30));
        jcboDBDriver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboDBDriverActionPerformed(evt);
            }
        });
        add(jcboDBDriver, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 230, -1, -1));

        jLblDbWebDriverLib.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebDriverLib.setText(AppLocal.getIntString("label.dbwebdriverlib")); // NOI18N
        jLblDbWebDriverLib.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebDriverLib, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, -1, 25));

        jtxtDbWebDriverLib.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebDriverLib.setPreferredSize(new java.awt.Dimension(500, 30));
        add(jtxtDbWebDriverLib, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 190, -1, -1));

        jbtnDbWebDriverLib.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        jbtnDbWebDriverLib.setText("  ");
        jbtnDbWebDriverLib.setToolTipText("");
        jbtnDbWebDriverLib.setMaximumSize(new java.awt.Dimension(64, 32));
        jbtnDbWebDriverLib.setMinimumSize(new java.awt.Dimension(64, 32));
        jbtnDbWebDriverLib.setPreferredSize(new java.awt.Dimension(80, 45));
        add(jbtnDbWebDriverLib, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 180, -1, -1));

        jLblDbWebDriver.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebDriver.setText(AppLocal.getIntString("label.DbWebDriver")); // NOI18N
        jLblDbWebDriver.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebDriver, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 230, -1, -1));

        jtxtDbWebDriver.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebDriver.setPreferredSize(new java.awt.Dimension(150, 30));
        jtxtDbWebDriver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtDbWebDriverActionPerformed(evt);
            }
        });
        add(jtxtDbWebDriver, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 230, 180, -1));

        jLblDbWebURL.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebURL.setText(AppLocal.getIntString("label.DbWebURL")); // NOI18N
        jLblDbWebURL.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebURL, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 343, -1, -1));

        jtxtDbWebURL.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebURL.setPreferredSize(new java.awt.Dimension(275, 30));
        add(jtxtDbWebURL, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 343, 470, -1));

        jLblDbWebUser.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebUser.setText(AppLocal.getIntString("label.DbWebUser")); // NOI18N
        jLblDbWebUser.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, -1, -1));

        jtxtDbWebUser.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebUser.setPreferredSize(new java.awt.Dimension(150, 30));
        add(jtxtDbWebUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 380, -1, -1));

        jLblDbWebPass.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebPass.setText(AppLocal.getIntString("label.DbWebPassword")); // NOI18N
        jLblDbWebPass.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 417, -1, -1));

        jtxtDbWebPassword.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebPassword.setPreferredSize(new java.awt.Dimension(150, 30));
        add(jtxtDbWebPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 417, -1, -1));

        jBtnWebTest.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnWebTest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/database.png"))); // NOI18N
        jBtnWebTest.setText(bundle.getString("button.test")); // NOI18N
        jBtnWebTest.setActionCommand(bundle.getString("Button.Test")); // NOI18N
        jBtnWebTest.setPreferredSize(new java.awt.Dimension(110, 45));
        jBtnWebTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnWebTestActionPerformed(evt);
            }
        });
        add(jBtnWebTest, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 455, -1, -1));
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 137, 880, -1));

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/uniCenta_logo_vert_100.png"))); // NOI18N
        jLabel5.setText(bundle.getString("message.DBWebDefault")); // NOI18N
        jLabel5.setToolTipText("");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel5.setPreferredSize(new java.awt.Dimension(889, 120));
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 880, -1));

        jLblDbWebName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebName.setText(AppLocal.getIntString("label.DbWebName")); // NOI18N
        jLblDbWebName.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebName, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 269, -1, -1));

        jtxtDbWebName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebName.setPreferredSize(new java.awt.Dimension(275, 30));
        add(jtxtDbWebName, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 269, -1, -1));

        LblMultiDbWeb.setText(AppLocal.getIntString("label.dbweb")); // NOI18N
        LblMultiDbWeb.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        LblMultiDbWeb.setPreferredSize(new java.awt.Dimension(125, 30));
        add(LblMultiDbWeb, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, -1, -1));

        webEnable.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webEnable.setPreferredSize(new java.awt.Dimension(80, 30));
        webEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webEnableActionPerformed(evt);
            }
        });
        add(webEnable, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 150, -1, -1));

        jLblDbWebIP.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebIP.setText(AppLocal.getIntString("label.DbWebIP")); // NOI18N
        jLblDbWebIP.setPreferredSize(new java.awt.Dimension(125, 30));
        add(jLblDbWebIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 306, -1, -1));

        jtxtDbWebIP.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebIP.setPreferredSize(new java.awt.Dimension(135, 30));
        add(jtxtDbWebIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(139, 306, -1, -1));

        jLblDbWebPort.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbWebPort.setText(AppLocal.getIntString("label.DbWebPort")); // NOI18N
        jLblDbWebPort.setPreferredSize(new java.awt.Dimension(50, 30));
        add(jLblDbWebPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(288, 306, -1, -1));

        jtxtDbWebPort.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbWebPort.setPreferredSize(new java.awt.Dimension(70, 30));
        add(jtxtDbWebPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(344, 306, 70, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void jtxtDbWebDriverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtDbWebDriverActionPerformed

    }//GEN-LAST:event_jtxtDbWebDriverActionPerformed

    private void jcboDBDriverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboDBDriverActionPerformed

        String dirname = System.getProperty("dirname.path");
        dirname = dirname == null ? "./" : dirname;
           
        
        if ("PostgreSQL".equals(jcboDBDriver.getSelectedItem())) {
            jtxtDbWebDriverLib.setText(new File(new File(dirname), "lib/postgresql-9.4-1208.jdbc4.jar").getAbsolutePath());
            jtxtDbWebDriver.setText("org.postgresql.Driver");
            jtxtDbWebURL.setText("jdbc:postgresql://localhost:5432/unicentaopos");            
        } else {
            jtxtDbWebDriverLib.setText(new File(new File(dirname), "lib/mysql-connector-java-5.1.34-bin.jar").getAbsolutePath());
            jtxtDbWebDriver.setText("com.mysql.jdbc.Driver");
            jtxtDbWebURL.setText("jdbc:mysql://localhost:3306/unicentaopos");
        }    
    }//GEN-LAST:event_jcboDBDriverActionPerformed

    private void jBtnWebTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnWebTestActionPerformed
        try {
            String driverlib = jtxtDbWebDriverLib.getText();
            String driver = jtxtDbWebDriver.getText();
            String url = jtxtDbWebURL.getText();
            String user = jtxtDbWebUser.getText();
            String password = new String(jtxtDbWebPassword.getPassword());

            ClassLoader cloader = new URLClassLoader(new URL[]{new File(driverlib).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(driver, true, cloader).newInstance()));

            Session session =  new Session(url, user, password);
            Connection connection = session.getConnection();
            boolean isValid;
            isValid = (connection == null) ? false : connection.isValid(1000);

            if (isValid) {
                JOptionPane.showMessageDialog(this, 
                        AppLocal.getIntString("message.databasesuccess"), 
                        "Connection Test", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JMessageDialog.showMessage(this, 
                        new MessageInf(MessageInf.SGN_WARNING, "Connection Error"));
            }
        } catch (InstantiationException | IllegalAccessException | MalformedURLException | ClassNotFoundException e) {
            JMessageDialog.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, 
                            AppLocal.getIntString("message.databasedrivererror"), e));
        } catch (SQLException e) {
            JMessageDialog.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, 
                            AppLocal.getIntString("message.databaseconnectionerror"), e));
        } catch (Exception e) {
            JMessageDialog.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, "Unknown exception", e));
        }
    }//GEN-LAST:event_jBtnWebTestActionPerformed

    private void webEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webEnableActionPerformed
        if (webEnable.isSelected()) {
            jLblDbWebName.setEnabled(true);
            jtxtDbWebName.setEnabled(true);
            jLblDbWebIP.setEnabled(true);
            jtxtDbWebIP.setEnabled(true);  
            jLblDbWebPort.setEnabled(true);
            jtxtDbWebPort.setEnabled(true);            
            jLblDbWebURL.setEnabled(true);
            jtxtDbWebURL.setEnabled(true);            
            jLblDbWebUser.setEnabled(true);
            jtxtDbWebUser.setEnabled(true);
            jLblDbWebPass.setEnabled(true);
            jtxtDbWebPassword.setEnabled(true);
            jBtnWebTest.setEnabled(true);

        } else {
            jLblDbWebName.setEnabled(false);
            jtxtDbWebName.setEnabled(false);
            jLblDbWebIP.setEnabled(false);
            jtxtDbWebIP.setEnabled(false);  
            jLblDbWebPort.setEnabled(false);
            jtxtDbWebPort.setEnabled(false);            
            jLblDbWebURL.setEnabled(false);
            jtxtDbWebURL.setEnabled(false);            
            jLblDbWebUser.setEnabled(false);
            jtxtDbWebUser.setEnabled(false);
            jLblDbWebPass.setEnabled(false);
            jtxtDbWebPassword.setEnabled(false);
            jBtnWebTest.setEnabled(false);
        }

    }//GEN-LAST:event_webEnableActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.alee.laf.label.WebLabel LblMultiDbWeb;
    private javax.swing.JButton jBtnWebTest;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLblDbWeb;
    private javax.swing.JLabel jLblDbWebDriver;
    private javax.swing.JLabel jLblDbWebDriverLib;
    private javax.swing.JLabel jLblDbWebIP;
    private javax.swing.JLabel jLblDbWebName;
    private javax.swing.JLabel jLblDbWebPass;
    private javax.swing.JLabel jLblDbWebPort;
    private javax.swing.JLabel jLblDbWebURL;
    private javax.swing.JLabel jLblDbWebUser;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton jbtnDbWebDriverLib;
    private javax.swing.JComboBox jcboDBDriver;
    private javax.swing.JTextField jtxtDbWebDriver;
    private javax.swing.JTextField jtxtDbWebDriverLib;
    private javax.swing.JTextField jtxtDbWebIP;
    private javax.swing.JTextField jtxtDbWebName;
    private javax.swing.JPasswordField jtxtDbWebPassword;
    private javax.swing.JTextField jtxtDbWebPort;
    private javax.swing.JTextField jtxtDbWebURL;
    private javax.swing.JTextField jtxtDbWebUser;
    private com.alee.extended.button.WebSwitch webEnable;
    private com.alee.extended.window.WebPopOver webPopOver1;
    // End of variables declaration//GEN-END:variables

     
}
