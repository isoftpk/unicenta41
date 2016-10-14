/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.forms;

import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.ticket.CategoryInfo;

/**
 *
 * @author pk
 */
public class DataLogicWeb extends BeanFactoryDataSingle {
    
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
    }
    
        /**
     *
     * @return
     */
        public final SentenceList getCategoriesList() {
        return new StaticSentence(s
            , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER "                    
                + "FROM categories "
                + "ORDER BY NAME"
            , null
            , CategoryInfo.getSerializerRead());
    }
}
