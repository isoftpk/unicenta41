/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.ticket.CategoryInfo;

/**
 *
 * @author PK
 */
public class DataLogicWeb extends BeanFactoryDataWeb {

    private SentenceFind m_getProductByName;    
    
    protected Session s;

    public DataLogicWeb() {
    }
    
     /**
     *
     * @param s
     */
    @Override
    public void init(Session s){
        this.s = s;
        
        m_getProductByName = new PreparedSentence(s, "SELECT reference FROM pr_product WHERE id_product = ?", SerializerWriteString.INSTANCE, SerializerReadString.INSTANCE);        
    }
    
    /**
     *
     * @return
     */
        public final SentenceList getProuctsList() {
        return new StaticSentence(s
            , "SELECT reference FROM pr_product WHERE id_product = ?"
            , null
            , CategoryInfo.getSerializerRead());
    }

    
        
    /**
     *
     * @return
     * @throws BasicException
     */
    public final String findProductNameById() throws BasicException {
        
        return (String) m_getProductByName.find("2");
    }
     
        
        
}
