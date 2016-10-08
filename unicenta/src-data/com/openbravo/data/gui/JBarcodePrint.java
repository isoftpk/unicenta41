//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2013 uniCenta & previous Openbravo POS works
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

package com.openbravo.data.gui;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.printer.DevicePrinter;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.printer.escpos.DevicePrinterPlain;
import com.openbravo.pos.printer.escpos.PrinterWritter;
import com.openbravo.pos.printer.escpos.PrinterWritterArgox;
import com.openbravo.pos.printer.escpos.UnicodeTranslator;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.LayoutStyle.ComponentPlacement;

public class JBarcodePrint extends JPanel implements EditorRecord {
    
    private static final byte[] NEW_LINE = {0x0A}; // Print and carriage return
	
    private ExecutorService exec;
    
    
    PrinterWritterArgox bpw;
	UnicodeTranslator trans;
	private AppConfig props;
	
    public JBarcodePrint(DirtyManager dirty) throws TicketPrinterException {

    	props =  new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
    	props.load();
    	
    	
    	initComponents();

    	m_jBarcode.getDocument().addDocumentListener(dirty);
    	
    	
    }
 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBarcodePrint = new javax.swing.JPanel();
        m_jBarcode = new javax.swing.JTextField();
        m_jPriceSell = new javax.swing.JTextField();
        m_jPrintQty = new javax.swing.JTextField();
        jbtnPrint = new javax.swing.JButton();
        
        //jbtnDelete = new javax.swing.JButton();
        //jSeparator1 = new javax.swing.JSeparator();
        //jbtnSave = new javax.swing.JButton();
        
        
        pnlBarcodePrint.setBorder(javax.swing.BorderFactory.createTitledBorder(AppLocal.getIntString("label.barcodeprint"))); // NOI18N
//        System.out.println(AppLocal.getIntString("label.barcodeprint"));
//        pnlBarcodePrint.setBorder(javax.swing.BorderFactory.createTitledBorder("Barcode Print")); // NOI18N
        pnlBarcodePrint.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        
        m_jBarcode.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPrintQty.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPrintQty.setText("1");
        
        jbtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ccard.png"))); // NOI18N
        jbtnPrint.setToolTipText("Print Barcode");
        
        javax.swing.GroupLayout grpLayoutBarcodePrint = new javax.swing.GroupLayout(pnlBarcodePrint);
        pnlBarcodePrint.setLayout(grpLayoutBarcodePrint);
        grpLayoutBarcodePrint.setHorizontalGroup(
        	grpLayoutBarcodePrint.createParallelGroup(Alignment.LEADING)
        		.addGroup(grpLayoutBarcodePrint.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(grpLayoutBarcodePrint.createParallelGroup(Alignment.LEADING)
        				.addComponent(m_jBarcode, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
        				.addComponent(m_jPrintQty, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
        				.addComponent(jbtnPrint, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap(61, Short.MAX_VALUE))
        );
        grpLayoutBarcodePrint.setVerticalGroup(
        	grpLayoutBarcodePrint.createParallelGroup(Alignment.LEADING)
        		.addGroup(grpLayoutBarcodePrint.createSequentialGroup()
        			.addGap(1)
        			.addComponent(m_jBarcode, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(m_jPrintQty, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jbtnPrint, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(115, Short.MAX_VALUE))
        );

        
        //setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(Alignment.LEADING, layout.createSequentialGroup()
        			.addComponent(pnlBarcodePrint, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(280, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(Alignment.LEADING, layout.createSequentialGroup()
        			.addComponent(pnlBarcodePrint, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
        			//.addContainerGap(183, Short.MAX_VALUE))
        			)
        );
        this.setLayout(layout);

            jbtnPrint.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jbtnNewActionPerformed(evt);
                }
            });

    }// </editor-fold>//GEN-END:initComponents

    private void jbtnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnNewActionPerformed
        // Add your handling code here:
    	String sComport = null; 
        try {
            //m_bd.actionInsert();
        	//JOptionPane.showMessageDialog(JBarcodePrint.this,m_jBarcode.getText(),"Information",JOptionPane.OK_OPTION);
        	sComport = props.getProperty("machine.printer.2");
        	//System.out.println(props.getProperty("machine.printer.2"));
        	//System.out.println(sComport.substring(0,sComport.indexOf(":")));
        	
        	if ("argox".equals(sComport.substring(0,sComport.indexOf(":"))))	{
        		printBarcode(sComport);
        	}
        	else   	{
        		JOptionPane.showMessageDialog(JBarcodePrint.this,"Please setup argox printer in peripherals under 'Barcode Printer'","Information",JOptionPane.OK_OPTION);
        	}
        	
        } catch (Exception eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nonew"), eD);
            msg.show(this);
        }	
    }//GEN-LAST:event_jbtnNewActionPerformed
    
    
    @Override
    public void writeValueEdit(Object value) {

        //reportlock = true;
        Object[] myprod = (Object[]) value;
        //m_jTitle.setText(Formats.STRING.formatValue(myprod[1]) + " - " + Formats.STRING.formatValue(myprod[3]));
        //m_id = myprod[0];
        //m_jRef.setText(Formats.STRING.formatValue(myprod[1]));
        m_jBarcode.setText(Formats.STRING.formatValue(myprod[2]));
        m_jPriceSell.setText(Formats.CURRENCY.formatValue(myprod[6]));
    }
    
    public Component getComponent() {
        return this;
    }
    
// Variables declaration - do not modify//GEN-BEGIN:variables
//    private javax.swing.JSeparator jSeparator1;
//    private javax.swing.JButton jbtnDelete;
//    private javax.swing.JButton jbtnSave;
// End of variables declaration//GEN-END:variables
    
    private javax.swing.JPanel pnlBarcodePrint;
    private javax.swing.JTextField m_jBarcode;
    private javax.swing.JTextField m_jPriceSell;
    private javax.swing.JTextField m_jPrintQty;
    private javax.swing.JButton jbtnPrint;
    

	@Override
	public Object createValue() throws BasicException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeValueEOF() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeValueInsert() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeValueDelete(Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	
	
	public void printBarcode(String sComport) {
    	
                exec = Executors.newSingleThreadExecutor();
        
    	try {
                                
		bpw = new PrinterWritterArgox(sComport.substring(sComport.lastIndexOf(",")+1));
			
	    	bpw.write("N");
	    	bpw.write(NEW_LINE);
//	    	bpw.write("A10,10,0,3,2,2,N,\"TPOM\"");
//	    	bpw.write(NEW_LINE);
//	    	bpw.write("B320,200,0,1,2,1,50,B,\""+ m_jBarcode.getText() + "\"");
//	    	bpw.write(NEW_LINE);

//                                bpw.write("LO280,10,260,2");
//	    	bpw.write(NEW_LINE);	    	
//	    	bpw.write("LO280,300,260,2");
//	    	bpw.write(NEW_LINE);	    	
//	    	bpw.write("A260,30,0,c,1,1,N,\"That Place on Main\"");
//	    	bpw.write(NEW_LINE);	    	
//	    	bpw.write("A300,90,0,3,2,2,N,\"" + m_jPriceSell.getText() + "\"");
//	    	bpw.write(NEW_LINE);
//	    	bpw.write("B300,160,0,1,2,1,90,B,\""+ m_jBarcode.getText() + "\"");
//	    	bpw.write(NEW_LINE);
//	    	bpw.write("P" + m_jPrintQty.getText());
//	    	bpw.write(NEW_LINE);

	    	bpw.write("LO280,10,260,2");
	    	bpw.write(NEW_LINE);	    	
	    	bpw.write("LO280,300,260,2");
	    	bpw.write(NEW_LINE);	    	
	    	bpw.write("A90,30,0,c,1,1,N,\"That Place on Main\"");
	    	bpw.write(NEW_LINE);	    	
	    	bpw.write("A130,90,0,3,2,2,N,\"" + m_jPriceSell.getText() + "\"");
	    	bpw.write(NEW_LINE);
	    	bpw.write("B130,160,0,1,2,1,90,B,\""+ m_jBarcode.getText() + "\"");
	    	bpw.write(NEW_LINE);
	    	bpw.write("P" + m_jPrintQty.getText());
	    	bpw.write(NEW_LINE);
                
                
	    	bpw.close();
	    	
		} catch (TicketPrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
            

	}
	

    public void write(final byte[] data) {
        exec.execute(new Runnable() {
            @Override
            public void run() {
				bpw.internalWrite(data);
            }
        });
    }
    
    public void flush() {
        exec.execute(new Runnable() {
            @Override
            public void run() {
            	bpw.internalFlush();
            }
        });
    }
    
    public void close() {
        exec.execute(new Runnable() {
            @Override
            public void run() {
            	bpw.internalClose();
            }
        });
        exec.shutdown();
    }
    
    
}