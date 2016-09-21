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
//    CSV Import Panel added by JDL - February 2013
//    Additonal library required - javacsv

package com.openbravo.pos.imports;

import com.csvreader.CsvReader;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang.StringUtils;
import com.alee.laf.optionpane.WebOptionPane;


/**
 * Graphical User Interface and code for importing data from a CSV file allowing
 * adding or updating many products quickly and easily.
 *
 */
public class JPanelCSVImport extends JPanel implements JPanelView {

    private ArrayList<String> Headers = new ArrayList<>();
    private Session s;
    private Connection con;
    private String csvFileName;
    private Double dOriginalRate;
    private String dCategory;
    private String csvMessage = "";
    private CsvReader products;
    private double oldSellPrice = 0;
    private double oldBuyPrice = 0;
    private int currentRecord;
    private int rowCount = 0;
    private String last_folder;
    private File config_file;
    private static String category_disable_text = "[ USE DEFAULT CATEGORY ]";
    private static String reject_bad_categories_text = "[ REJECT ITEMS WITH BAD CATEGORIES ]";
    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;

    /**
     *
     */
    protected SaveProvider spr;
    private String productReference;
    private String productBarcode;
    private String productBarcodetype;    
    private String productName;
    private String Category;
    private Double productBuyPrice;
    private Double productSellPrice;
    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    private SentenceList taxcatsent;
    private ComboBoxValModel taxcatmodel;
    private SentenceList taxsent;
    private TaxesLogic taxeslogic;
    private DocumentListener documentListener;
    private HashMap cat_list = new HashMap();
    private ArrayList badCategories = new ArrayList();
    private ProductInfoExt prodInfo;
    private String recordType = null;
    private int newRecords = 0;
    private int invalidRecords = 0;
    private int priceUpdates = 0;
    private int missingData = 0;
    private int noChanges = 0;
    private int badPrice = 0;
    private double dTaxRate;

    /**
     * Constructs a new JPanelCSVImport object
     *
     * @param oApp AppView
     */
    public JPanelCSVImport(AppView oApp) {
        this(oApp.getProperties());
    }

    /**
     * Constructs a new JPanelCSVImport object
     *
     * @param props AppProperties
     */
    @SuppressWarnings("empty-statement")
    public JPanelCSVImport(AppProperties props) {
        initComponents();

        try {
            s = AppViewConnection.createSession(props);
            con = s.getConnection();
        } catch (BasicException | SQLException e) {;
        }

        m_dlSales = new DataLogicSales();
        m_dlSales.init(s);

        m_dlSystem = new DataLogicSystem();
        m_dlSystem.init(s);


        spr = new SaveProvider(
                m_dlSales.getProductCatUpdate(),
                m_dlSales.getProductCatInsert(),
                m_dlSales.getProductCatDelete());

        last_folder = props.getProperty("CSV.last_folder");
        config_file = props.getConfigFile();

        jFileName.getDocument().addDocumentListener(documentListener);

        documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                jHeaderRead.setEnabled(true);
            }

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                if (!"".equals(jFileName.getText().trim())) {
                    jHeaderRead.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                if (jFileName.getText().trim().equals("")) {
                    jHeaderRead.setEnabled(false);
                }
            }
        };
        jFileName.getDocument().addDocumentListener(documentListener);

    }

    /**
     * Reads the headers from the CSV file and initializes subsequent form
     * fields. This function first reads the headers from the CSVFileName file,
     * then puts them into the header combo boxes and enables the other form
     * inputs.
     *
     * @todo Simplify this method by stripping the file reading and writing
     * functionality out into it's own class. Also make the enabling fields
     * section into it's own function and return the 'Headers' to the calling
     * function to be added there.
     *
     * @param CSVFileName Name of the file (including the path) to open and read
     * CSV data from
     * @throws IOException If there is an issue reading the CSV file
     */
    private void GetheadersFromFile(String CSVFileName) throws IOException {

        File f = new File(CSVFileName);
        if (f.exists()) {
            products = new CsvReader(CSVFileName);
            products.setDelimiter(((String) jComboSeparator.getSelectedItem()).charAt(0));
            products.readHeaders();
                          
            if (products.getHeaderCount() < 5) {
                JOptionPane.showMessageDialog(null,
                        "Insufficient headers found in file",
                        "Invalid Header Count.",
                        JOptionPane.WARNING_MESSAGE);
                products.close();
                return;
            }
            rowCount = 0;
            int i = 0;
            Headers.clear();
            Headers.add("");
            jComboName.addItem("");
            jComboReference.addItem("");
            jComboBarcode.addItem("");
            jComboBuy.addItem("");
            jComboSell.addItem("");
            jComboCategory.addItem("");

            while (i < products.getHeaderCount()) {
                jComboName.addItem(products.getHeader(i));
                jComboReference.addItem(products.getHeader(i));
                jComboBarcode.addItem(products.getHeader(i));
                jComboBuy.addItem(products.getHeader(i));
                jComboSell.addItem(products.getHeader(i));
                jComboCategory.addItem(products.getHeader(i));
                Headers.add(products.getHeader(i));
                ++i;
            }

            enableCheckBoxes();

            while (products.readRecord()) {
                ++rowCount;
            }

            jTextRecords.setText(Long.toString(rowCount));
                      
            products.close();

        } else {
            JOptionPane.showMessageDialog(null, "Unable to locate "
                    + CSVFileName,
                    "File not found",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Enables all the selection options on the for to allow the user to
     * interact with the routine.
     *
     */
    private void enableCheckBoxes() {
        jHeaderRead.setEnabled(false);
        jImport.setEnabled(false);
        jbtnReset.setEnabled(true);        
        jComboReference.setEnabled(true);
        jComboName.setEnabled(true);
        jComboBarcode.setEnabled(true);
        jComboBuy.setEnabled(true);
        jComboSell.setEnabled(true);
        jComboCategory.setEnabled(true);
        jComboDefaultCategory.setEnabled(true);
        jComboTax.setEnabled(true);
        jCheckInCatalogue.setEnabled(true);
        jCheckSellIncTax.setEnabled(true);
    }

    /**
     * Imports the CVS File using specifications from the form.
     *
     * @param CSVFileName Name of the file (including path) to import.
     * @throws IOException If there are file reading issues.
     */
    private void ImportCsvFile(String CSVFileName) throws IOException {

        File f = new File(CSVFileName);
        if (f.exists()) {


            // Read file
            products = new CsvReader(CSVFileName);
            products.setDelimiter(((String) jComboSeparator.getSelectedItem()).charAt(0));
            products.readHeaders();

            currentRecord = 0;
            
            while (products.readRecord()) {
                productReference = products.get((String) jComboReference.getSelectedItem());
                productName = products.get((String) jComboName.getSelectedItem());
                productBarcode = products.get((String) jComboBarcode.getSelectedItem());
                String BuyPrice = products.get((String) jComboBuy.getSelectedItem());
                String SellPrice = products.get((String) jComboSell.getSelectedItem());
                Category = products.get((String) jComboCategory.getSelectedItem());
                currentRecord++;

                BuyPrice = StringUtils.replaceChars(BuyPrice, "$", ""); 
                SellPrice = StringUtils.replaceChars(SellPrice, "$", "");

                dCategory = getCategory();

                if ("Bad Category".equals(dCategory)) {
                    csvMessage = "Bad category details";
                } else {
                    csvMessage = "Missing data or Invalid number";
                };

                if (validateNumber(BuyPrice)) {
                    productBuyPrice = Double.parseDouble(BuyPrice);
                } else {
                    productBuyPrice = null;
                }

                if (validateNumber(SellPrice)) {
                    productSellPrice = getSellPrice(SellPrice);
                } else {
                    productSellPrice = null;
                }

                if ("".equals(productReference)
                        | "".equals(productName)
                        | "".equals(productBarcode)
                        | "".equals(BuyPrice)
                        | "".equals(SellPrice)
                        | productBuyPrice == null
                        | productSellPrice == null
                        | "Bad Category".equals(dCategory)) {
                    if (productBuyPrice == null | productSellPrice == null) {
                        badPrice++;
                    } else {
                        missingData++;
                    }
                    createCSVEntry(csvMessage, null, null);
                } else {

                    recordType = getRecord();
                    switch (recordType) {
                        case "new":
                            createProduct("new");
                            newRecords++;
                            createCSVEntry("New product", null, null);
                            break;
                        case "name error":
                        case "barcode error":
                        case "reference error":
                        case "Duplicate Reference found.":
                        case "Duplicate Barcode found.":
                        case "Duplicate Description found.":
                        case "Exception":
                            invalidRecords++;
                            createCSVEntry(recordType, null, null);
                            break;
                        default:
                            updateRecord(recordType);
                            break;
                    }

                }

            }
        } else {
            JOptionPane.showMessageDialog(null, "Unable to locate " 
                + CSVFileName, "File not found", JOptionPane.WARNING_MESSAGE);
        }

        jTextNew.setText(Integer.toString(newRecords));
        jTextUpdate.setText(Integer.toString(priceUpdates));
        jTextInvalid.setText(Integer.toString(invalidRecords));
        jTextMissing.setText(Integer.toString(missingData));
        jTextNoChange.setText(Integer.toString(noChanges));
        jTextBadPrice.setText(Integer.toString(badPrice));
        jTextBadCats.setText(Integer.toString(badCategories.size()));
    }
    

    /**
     * Tests
     * <code>testString</code> for validity as a number
     *
     * @param testString the string to be checked
     * @return <code>True<code> if a real number <code>False<code> if not
     */
    private Boolean validateNumber(String testString) {
        try {
            Double res = Double.parseDouble(testString);
            return (true);
        } catch (NumberFormatException e) {
            return (false);
        }
    }

    /*
     * Get the category to be used for the new product
     * returns category id as string
     */
    private String getCategory() {
        // get the category to be used for the product
        if (jComboCategory.getSelectedItem() != category_disable_text) {
            // get the category ID of the catergory passed
            String cat = (String) cat_list.get(Category);
            // only if we have a valid category 
            if (cat != null) {
                return (cat);
            }
        }

        if (!badCategories.contains(Category)) {
            badCategories.add(Category.trim()); 
        }
        return ((jComboDefaultCategory.getSelectedItem() 
                == reject_bad_categories_text) ? "Bad Category" 
                : (String) cat_list.get(m_CategoryModel.getSelectedText()));

    }

    /**
     * Adjusts the sell price for included taxes if needed and converted to
     * <code>double</code>
     *
     * @param pSellPrice sell price to be converted
     * @return sell price after adjustment for included taxes and converted to <code>double</double>
     */
    private Double getSellPrice(String pSellPrice) {

        dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
        if (jCheckSellIncTax.isSelected()) {
            return ((Double.parseDouble(pSellPrice)) / (1 + dTaxRate));
        } else {
            return (Double.parseDouble(pSellPrice));
        }
    }

    /**
     * Updated the record in the database with the new prices and category if
     * needed.
     *
     * @param pID Unique product id of the record to be updated It then creates
     * an updated record for the product, subject to the prices be different
     *
     */
    private void updateRecord(String pID) {
        prodInfo = new ProductInfoExt();
        try {
            prodInfo = m_dlSales.getProductInfo(pID);
            dOriginalRate = taxeslogic.getTaxRate(prodInfo.getTaxCategoryID());
            dCategory = (String) cat_list.get(prodInfo.getCategoryID());
            oldBuyPrice = prodInfo.getPriceBuy();
            oldSellPrice = prodInfo.getPriceSell();
            productSellPrice = productSellPrice * (1 + dOriginalRate);
            if ((oldBuyPrice != productBuyPrice) || (oldSellPrice != productSellPrice)) {
                createCSVEntry("Updated Price Details", oldBuyPrice, oldSellPrice * (1 + dOriginalRate));
                createProduct("update");
                priceUpdates++;
            } else {
                noChanges++;
            }
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the title of the current panel
     *
     * @return The name of the panel
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CSVImport");
    }

    /**
     * Returns this object
     * @return 
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     * Loads Tax and category data into their combo boxes.
     * @throws com.openbravo.basic.BasicException
     */
    @Override
    public void activate() throws BasicException {
        // Get tax details and logic
        taxsent = m_dlSales.getTaxList();  //get details taxes table
        taxeslogic = new TaxesLogic(taxsent.list());
        taxcatsent = m_dlSales.getTaxCategoriesList();
        taxcatmodel = new ComboBoxValModel(taxcatsent.list());
        jComboTax.setModel(taxcatmodel);

        // Get categories list
        m_sentcat = m_dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
        m_CategoryModel.add(reject_bad_categories_text);
        jComboDefaultCategory.setModel(m_CategoryModel);

        // Build the cat_list for later use
        cat_list = new HashMap<>();
        for (Object category : m_sentcat.list()) {
            m_CategoryModel.setSelectedItem(category);
            cat_list.put(category.toString(), m_CategoryModel.getSelectedKey().toString());
        }

        // reset the selected to the first in the list
        m_CategoryModel.setSelectedItem(null);
        taxcatmodel.setSelectedFirst();

        // Set the column delimiter
        jComboSeparator.removeAllItems();
        jComboSeparator.addItem(",");
        jComboSeparator.addItem(";");
        jComboSeparator.addItem("~");
        jComboSeparator.addItem("^");

    }

    /**
     * Resets all the form fields
     */
    public void resetFields() {
        // Clear the form
        jComboReference.removeAllItems();
        jComboReference.setEnabled(false);
        jComboName.removeAllItems();
        jComboName.setEnabled(false);
        jComboBarcode.removeAllItems();
        jComboBarcode.setEnabled(false);
        jComboBuy.removeAllItems();
        jComboBuy.setEnabled(false);
        jComboSell.removeAllItems();
        jComboSell.setEnabled(false);
        jComboCategory.removeAllItems();
        jComboCategory.setEnabled(false);
        jComboDefaultCategory.setEnabled(false);
        jComboTax.setEnabled(false);
        jImport.setEnabled(false);
        jbtnReset.setEnabled(true);        
        jHeaderRead.setEnabled(false);
        jCheckInCatalogue.setSelected(false);
        jCheckInCatalogue.setEnabled(false);
        jCheckSellIncTax.setSelected(false);
        jCheckSellIncTax.setEnabled(false);
        jFileName.setText(null);
        csvFileName = "";
        jTextNew.setText("");
        jTextUpdate.setText("");
        jTextInvalid.setText("");
        jTextMissing.setText("");
        jTextNoChange.setText("");
        jTextRecords.setText("");
        jTextBadPrice.setText("");
        Headers.clear();
    }

    /**
     * Checks the field mappings to ensure all compulsory fields have been
     * completed to allow import to proceed
     */
    public void checkFieldMapping() {
        if (jComboReference.getSelectedItem() != "" & jComboName.getSelectedItem() != "" & jComboBarcode.getSelectedItem() != ""
                & jComboBuy.getSelectedItem() != "" & jComboSell.getSelectedItem() != "" & jComboCategory.getSelectedItem() != ""
                & m_CategoryModel.getSelectedText() != null) {
            jImport.setEnabled(true);
            jbtnReset.setEnabled(true);            
        } else {
            jImport.setEnabled(false);
            jbtnReset.setEnabled(false);             
        }
    }

    /**
     * Deactivates and resets all form fields.
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        resetFields();
        return (true);
    }

    /**
     *
     * @param pType
     */
    public void createProduct(String pType) {

        Object[] myprod = new Object[31];
        myprod[0] = UUID.randomUUID().toString();                               // ID string
        myprod[1] = productReference;                                           // Reference string
        myprod[2] = productBarcode;                                             // Barcode String     
        myprod[3] = null;                                                       // Barcode Type
        myprod[4] = productName;                                                // Name string        
        myprod[5] = productBuyPrice;                                            // Buy price double
        myprod[6] = productSellPrice;                                           // Sell price double        
        myprod[7] = dCategory;                                                  // Category string
        myprod[8] = taxcatmodel.getSelectedKey();                               // Tax string
        myprod[9] = null;                                                       // Attributeset string
        myprod[10] = 0.0;                                                       // Stock cost double
        myprod[11] = 0.0;                                                       // Stock volume double
        myprod[12] = null;                                                      // Image        
        myprod[13] = false;                                                     // IScomment flag (Attribute modifier)
        myprod[14] = false;                                                     // ISscale flag
        myprod[15] = false;                                                     // IsConstant flag
        myprod[16] = false;                                                      // PrintKB flag
        myprod[17] = false;                                                      // SendStatus flag        
        myprod[18] = false;                                                     // isService flag
        myprod[19] = null;                                                      // Attributes
        myprod[20] = "<html><center>" + productName;                            // Setup Display button    
        myprod[21] = false;                                                     // isVariable price flag
        myprod[22] = false;                                                     // Compulsory Att flag
        myprod[23] = "<html><center><h4>" + productName;                        // Text tip string
        myprod[24] = false;                                                     // Warranty flag
        myprod[25] = 0.0;                                                       // Stock Units
        myprod[26] = "1";                                                       // Printer         
        myprod[27] = "0";                                                       // Supplier
        myprod[28] = "0";                                                       // UOM
        myprod[29] = jCheckInCatalogue.isSelected();                            // In catalog flag
        myprod[30] = null;                                                      // catalog order                
        
        try {
            if ("new".equals(pType)) {
                spr.insertData(myprod);

            } else {
                spr.updateData(myprod);
            }
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param csvError
     * @param PreviousBuy
     * @param previousSell
     */
    public void createCSVEntry(String csvError, Double PreviousBuy, Double previousSell) {

        Object[] myprod = new Object[11];
        myprod[0] = UUID.randomUUID().toString();                               // ID string
        myprod[1] = Integer.toString(currentRecord);                            // Record number
        myprod[2] = csvError;                                                   // Error description
        myprod[3] = productReference;                                           // Reference string
        myprod[4] = productBarcode;                                             // Barcode String        
        myprod[5] = productName;                                                // Name string        
        myprod[6] = productBuyPrice;                                            // Buy price
        myprod[7] = productSellPrice;                                           // Sell price
        myprod[8] = PreviousBuy;                                                // Previous Buy price double
        myprod[9] = previousSell;                                               // Previous Sell price double
        myprod[10] = Category;
        try {
            m_dlSystem.execAddCSVEntry(myprod);
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public String getRecord() {
        // Get record type using using DataLogicSystem
        Object[] myprod = new Object[3];
        myprod[0] = productReference;
        myprod[1] = productBarcode;
        myprod[2] = productName;
        try {
            return (m_dlSystem.getProductRecordType(myprod));
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Exception";
    }
 
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooserPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jFileName = new javax.swing.JTextField();
        jbtnDbDriverLib = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jComboReference = new javax.swing.JComboBox();
        jComboBarcode = new javax.swing.JComboBox();
        jComboName = new javax.swing.JComboBox();
        jComboBuy = new javax.swing.JComboBox();
        jComboSell = new javax.swing.JComboBox();
        jComboDefaultCategory = new javax.swing.JComboBox();
        jComboTax = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jCheckInCatalogue = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jCheckSellIncTax = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jComboCategory = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        jImport = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jbtnReset = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jHeaderRead = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextUpdates = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTextRecords = new javax.swing.JTextField();
        jTextNew = new javax.swing.JTextField();
        jTextInvalid = new javax.swing.JTextField();
        jTextUpdate = new javax.swing.JTextField();
        jTextMissing = new javax.swing.JTextField();
        jTextBadPrice = new javax.swing.JTextField();
        jTextNoChange = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextBadCats = new javax.swing.JTextField();
        jComboSeparator = new javax.swing.JComboBox();
        jProgressBar1 = new javax.swing.JProgressBar();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(750, 500));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.csvfile")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        jFileName.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jFileName.setPreferredSize(new java.awt.Dimension(400, 30));
        jFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileNameActionPerformed(evt);
            }
        });

        jbtnDbDriverLib.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        jbtnDbDriverLib.setMaximumSize(new java.awt.Dimension(64, 32));
        jbtnDbDriverLib.setMinimumSize(new java.awt.Dimension(64, 32));
        jbtnDbDriverLib.setPreferredSize(new java.awt.Dimension(80, 45));
        jbtnDbDriverLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnDbDriverLibActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFileChooserPanelLayout = new javax.swing.GroupLayout(jFileChooserPanel);
        jFileChooserPanel.setLayout(jFileChooserPanelLayout);
        jFileChooserPanelLayout.setHorizontalGroup(
            jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFileChooserPanelLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbtnDbDriverLib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120))
        );
        jFileChooserPanelLayout.setVerticalGroup(
            jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFileChooserPanelLayout.createSequentialGroup()
                .addGroup(jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addComponent(jbtnDbDriverLib, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(430, 400));

        jComboReference.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboReference.setEnabled(false);
        jComboReference.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboReference.setOpaque(false);
        jComboReference.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboReference.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboReferenceItemStateChanged(evt);
            }
        });
        jComboReference.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboReferenceFocusGained(evt);
            }
        });

        jComboBarcode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboBarcode.setEnabled(false);
        jComboBarcode.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboBarcode.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboBarcode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBarcodeItemStateChanged(evt);
            }
        });
        jComboBarcode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBarcodeFocusGained(evt);
            }
        });

        jComboName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboName.setEnabled(false);
        jComboName.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboName.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboNameItemStateChanged(evt);
            }
        });
        jComboName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboNameFocusGained(evt);
            }
        });

        jComboBuy.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboBuy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jComboBuy.setSelectedIndex(-1);
        jComboBuy.setEnabled(false);
        jComboBuy.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboBuy.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboBuy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBuyItemStateChanged(evt);
            }
        });
        jComboBuy.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBuyFocusGained(evt);
            }
        });

        jComboSell.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboSell.setEnabled(false);
        jComboSell.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboSell.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboSell.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboSellItemStateChanged(evt);
            }
        });
        jComboSell.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboSellFocusGained(evt);
            }
        });

        jComboDefaultCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboDefaultCategory.setEnabled(false);
        jComboDefaultCategory.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboDefaultCategory.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboDefaultCategory.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboDefaulrCategoryItemStateChanged(evt);
            }
        });
        jComboDefaultCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboDefaultCategoryActionPerformed(evt);
            }
        });

        jComboTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboTax.setEnabled(false);
        jComboTax.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(bundle.getString("label.prodref")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(bundle.getString("label.prodbarcode")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(bundle.getString("label.prodname")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText(bundle.getString("label.prodpricebuy")); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText(bundle.getString("label.prodcategory")); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(bundle.getString("label.prodtaxcode")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(100, 30));

        jCheckInCatalogue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCheckInCatalogue.setEnabled(false);
        jCheckInCatalogue.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(bundle.getString("label.prodincatalog")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));

        jCheckSellIncTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCheckSellIncTax.setEnabled(false);
        jCheckSellIncTax.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText(bundle.getString("label.csvsellingintax")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(200, 30));

        jComboCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboCategory.setEnabled(false);
        jComboCategory.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboCategory.setName(""); // NOI18N
        jComboCategory.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboCategory.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboCategoryItemStateChanged(evt);
            }
        });
        jComboCategory.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboCategoryFocusGained(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(bundle.getString("label.prodpricesell")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(100, 30));

        jImport.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jImport.setText(bundle.getString("label.csvimpostbtn")); // NOI18N
        jImport.setEnabled(false);
        jImport.setPreferredSize(new java.awt.Dimension(110, 45));
        jImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImportActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(bundle.getString("label.proddefaultcategory")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        jbtnReset.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnReset.setText(bundle.getString("button.reset")); // NOI18N
        jbtnReset.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboDefaultCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckInCatalogue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(59, 59, 59)
                        .addComponent(jCheckSellIncTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jbtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(133, 133, 133))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboDefaultCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckSellIncTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckInCatalogue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(67, 67, 67))
        );

        jLabel17.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel17.setText("Import Version V2.2");

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(bundle.getString("label.csvdelimit")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(100, 30));

        jHeaderRead.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jHeaderRead.setText(bundle.getString("label.csvread")); // NOI18N
        jHeaderRead.setEnabled(false);
        jHeaderRead.setPreferredSize(new java.awt.Dimension(110, 45));
        jHeaderRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHeaderReadActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true), bundle.getString("title.CSVImport"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(bundle.getString("label.csvrecordsfound")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText(bundle.getString("label.csvnewproducts")); // NOI18N
        jLabel14.setMaximumSize(new java.awt.Dimension(77, 14));
        jLabel14.setMinimumSize(new java.awt.Dimension(77, 14));
        jLabel14.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText(bundle.getString("label.cvsinvalid")); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(150, 30));

        jTextUpdates.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextUpdates.setText(bundle.getString("label.csvpriceupdated")); // NOI18N
        jTextUpdates.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(bundle.getString("label.csvmissing")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText(bundle.getString("label.csvbad")); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText(bundle.getString("label.cvsnotchanged")); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(150, 30));

        jTextRecords.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextRecords.setForeground(new java.awt.Color(102, 102, 102));
        jTextRecords.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextRecords.setBorder(null);
        jTextRecords.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextRecords.setEnabled(false);
        jTextRecords.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextNew.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextNew.setForeground(new java.awt.Color(102, 102, 102));
        jTextNew.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextNew.setBorder(null);
        jTextNew.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextNew.setEnabled(false);
        jTextNew.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextInvalid.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextInvalid.setForeground(new java.awt.Color(102, 102, 102));
        jTextInvalid.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextInvalid.setBorder(null);
        jTextInvalid.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextInvalid.setEnabled(false);
        jTextInvalid.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextUpdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextUpdate.setForeground(new java.awt.Color(102, 102, 102));
        jTextUpdate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextUpdate.setBorder(null);
        jTextUpdate.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextUpdate.setEnabled(false);
        jTextUpdate.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextMissing.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextMissing.setForeground(new java.awt.Color(102, 102, 102));
        jTextMissing.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextMissing.setBorder(null);
        jTextMissing.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextMissing.setEnabled(false);
        jTextMissing.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextBadPrice.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextBadPrice.setForeground(new java.awt.Color(255, 0, 204));
        jTextBadPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBadPrice.setBorder(null);
        jTextBadPrice.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextBadPrice.setEnabled(false);
        jTextBadPrice.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextNoChange.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextNoChange.setForeground(new java.awt.Color(102, 102, 102));
        jTextNoChange.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextNoChange.setBorder(null);
        jTextNoChange.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextNoChange.setEnabled(false);
        jTextNoChange.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(bundle.getString("label.cvsbadcats")); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(150, 30));

        jTextBadCats.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextBadCats.setForeground(new java.awt.Color(255, 0, 204));
        jTextBadCats.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBadCats.setBorder(null);
        jTextBadCats.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextBadCats.setEnabled(false);
        jTextBadCats.setPreferredSize(new java.awt.Dimension(100, 30));
        jTextBadCats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextBadCatsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextUpdates, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextBadCats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextBadPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextNoChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextUpdates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBadPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextNoChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBadCats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jComboSeparator.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jComboSeparator.setPreferredSize(new java.awt.Dimension(50, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 637, Short.MAX_VALUE)
                        .addComponent(jLabel17))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(138, 138, 138)
                                        .addComponent(jHeaderRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(4, 4, 4)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jHeaderRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jHeaderReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jHeaderReadActionPerformed
        try {
            GetheadersFromFile(jFileName.getText());
        } catch (IOException ex) {
            Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jHeaderReadActionPerformed

    private void jImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImportActionPerformed

        if(WebOptionPane.showConfirmDialog (null, "Continue Import?", 
            "Confirm", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){         
     
                jImport.setEnabled(false);

            try {
                ImportCsvFile(jFileName.getText());
            } catch (IOException ex) {
                Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
            }
            jImport.setText("Completed");
            jbtnReset.setEnabled(true);             
        }
        
        WebOptionPane.showMessageDialog(null, "Import Complete", "Completed",
                WebOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jImportActionPerformed

    private void jFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileNameActionPerformed
        jImport.setEnabled(false);
        jHeaderRead.setEnabled(true);
    }//GEN-LAST:event_jFileNameActionPerformed

    private void jbtnDbDriverLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnDbDriverLibActionPerformed
        resetFields();


        JFileChooser chooser = new JFileChooser(last_folder == null ? "C:\\" : last_folder);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("csv files", "csv");
        chooser.setFileFilter(filter);
        chooser.showOpenDialog(null);
        File csvFile = chooser.getSelectedFile();
      
        if (csvFile == null) {
            return;
        }

        File current_folder = chooser.getCurrentDirectory();

        if (last_folder == null || !last_folder.equals(current_folder.getAbsolutePath())) {
            AppConfig CSVConfig = new AppConfig(config_file);
            CSVConfig.load();
            CSVConfig.setProperty("CSV.last_folder", current_folder.getAbsolutePath());
            last_folder = current_folder.getAbsolutePath();
            try {
                CSVConfig.save();
            } catch (IOException ex) {
                Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String csv = csvFile.getName();
        if (!(csv.trim().equals(""))) {
            csvFileName = csvFile.getAbsolutePath();
            jFileName.setText(csvFileName);
        }
    }//GEN-LAST:event_jbtnDbDriverLibActionPerformed

    private void jComboCategoryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboCategoryFocusGained
        jComboCategory.removeAllItems();
        int i = 1;
        jComboCategory.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboBarcode.getSelectedItem()) & (Headers.get(i) != jComboReference.getSelectedItem())
                & (Headers.get(i) != jComboName.getSelectedItem()) & (Headers.get(i) != jComboBuy.getSelectedItem())
                & (Headers.get(i) != jComboSell.getSelectedItem())) {
                jComboCategory.addItem(Headers.get(i));
            }
            ++i;
        }
        jComboCategory.addItem(category_disable_text);
    }//GEN-LAST:event_jComboCategoryFocusGained

    private void jComboCategoryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboCategoryItemStateChanged

        try {
            if (jComboCategory.getSelectedItem() == "[ USE DEFAULT CATEGORY ]") {
                m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
                jComboDefaultCategory.setModel(m_CategoryModel);
            } else {
                m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
                m_CategoryModel.add(reject_bad_categories_text);
                jComboDefaultCategory.setModel(m_CategoryModel);
            }
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCSVImport.class.getName()).log(Level.SEVERE, null, ex);
        }
        checkFieldMapping();
    }//GEN-LAST:event_jComboCategoryItemStateChanged

    private void jComboDefaultCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboDefaultCategoryActionPerformed
        checkFieldMapping();
    }//GEN-LAST:event_jComboDefaultCategoryActionPerformed

    private void jComboDefaulrCategoryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboDefaulrCategoryItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboDefaulrCategoryItemStateChanged

    private void jComboSellFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboSellFocusGained
        jComboSell.removeAllItems();
        int i = 1;
        jComboSell.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem()) 
                    & (Headers.get(i) != jComboReference.getSelectedItem()) 
                    & (Headers.get(i) != jComboName.getSelectedItem()) 
                    & (Headers.get(i) != jComboBuy.getSelectedItem()) 
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())) {

                jComboSell.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboSellFocusGained

    private void jComboSellItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboSellItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboSellItemStateChanged

    private void jComboBuyFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBuyFocusGained
        jComboBuy.removeAllItems();
        int i = 1;
        jComboBuy.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem()) 
                    & (Headers.get(i) != jComboReference.getSelectedItem()) 
                    & (Headers.get(i) != jComboName.getSelectedItem()) 
                    & (Headers.get(i) != jComboBarcode.getSelectedItem()) 
                    & (Headers.get(i) != jComboSell.getSelectedItem())) {
                jComboBuy.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboBuyFocusGained

    private void jComboBuyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBuyItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboBuyItemStateChanged

    private void jComboNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboNameFocusGained
        jComboName.removeAllItems();
        int i = 1;
        jComboName.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem()) 
                    & (Headers.get(i) != jComboReference.getSelectedItem()) 
                    & (Headers.get(i) != jComboBarcode.getSelectedItem()) 
                    & (Headers.get(i) != jComboBuy.getSelectedItem()) 
                    & (Headers.get(i) != jComboSell.getSelectedItem())) {
                jComboName.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboNameFocusGained

    private void jComboNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboNameItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboNameItemStateChanged

    private void jComboBarcodeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBarcodeFocusGained
        jComboBarcode.removeAllItems();
        int i = 1;
        jComboBarcode.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem()) 
                    & (Headers.get(i) != jComboReference.getSelectedItem()) 
                    & (Headers.get(i) != jComboName.getSelectedItem()) 
                    & (Headers.get(i) != jComboBuy.getSelectedItem()) 
                    & (Headers.get(i) != jComboSell.getSelectedItem())) {
                jComboBarcode.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboBarcodeFocusGained

    private void jComboBarcodeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBarcodeItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboBarcodeItemStateChanged

    private void jComboReferenceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboReferenceFocusGained
        jComboReference.removeAllItems();
        int i = 1;
        jComboReference.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem()) 
                    & (Headers.get(i) != jComboBarcode.getSelectedItem()) 
                    & (Headers.get(i) != jComboName.getSelectedItem()) 
                    & (Headers.get(i) != jComboBuy.getSelectedItem()) 
                    & (Headers.get(i) != jComboSell.getSelectedItem())) {
                jComboReference.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboReferenceFocusGained

    private void jComboReferenceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboReferenceItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboReferenceItemStateChanged

    private void jTextBadCatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextBadCatsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextBadCatsActionPerformed

    private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed
       resetFields();
    }//GEN-LAST:event_jbtnResetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckInCatalogue;
    private javax.swing.JCheckBox jCheckSellIncTax;
    private javax.swing.JComboBox jComboBarcode;
    private javax.swing.JComboBox jComboBuy;
    private javax.swing.JComboBox jComboCategory;
    private javax.swing.JComboBox jComboDefaultCategory;
    private javax.swing.JComboBox jComboName;
    private javax.swing.JComboBox jComboReference;
    private javax.swing.JComboBox jComboSell;
    private javax.swing.JComboBox jComboSeparator;
    private javax.swing.JComboBox jComboTax;
    private javax.swing.JPanel jFileChooserPanel;
    private javax.swing.JTextField jFileName;
    private javax.swing.JButton jHeaderRead;
    private javax.swing.JButton jImport;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextField jTextBadCats;
    private javax.swing.JTextField jTextBadPrice;
    private javax.swing.JTextField jTextInvalid;
    private javax.swing.JTextField jTextMissing;
    private javax.swing.JTextField jTextNew;
    private javax.swing.JTextField jTextNoChange;
    private javax.swing.JTextField jTextRecords;
    private javax.swing.JTextField jTextUpdate;
    private javax.swing.JLabel jTextUpdates;
    private javax.swing.JButton jbtnDbDriverLib;
    private javax.swing.JButton jbtnReset;
    // End of variables declaration//GEN-END:variables
}
