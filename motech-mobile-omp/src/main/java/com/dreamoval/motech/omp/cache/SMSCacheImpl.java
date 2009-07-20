/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dreamoval.motech.omp.cache;

import com.dreamoval.motech.core.DataAccess.DAO.MessageDetailsDAO;
import com.dreamoval.motech.core.DataAccess.Domain.MessageDetails;

/**
 *
 * @author Yoofi
 */
public class SMSCacheImpl implements SMSCache {

    private MessageDetailsDAO messageDetailsDao;

    public boolean saveMessage(MessageDetails messageDetails) {
        this.messageDetailsDao.StoreMessage(messageDetails);
        return true;
    }

    public boolean updateMessage(MessageDetails messageDetails) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the messageDetailsDao
     */
    public MessageDetailsDAO getMessageDetailsDao() {
        return messageDetailsDao;
    }

    /**
     * @param messageDetailsDao the messageDetailsDao to set
     */
    public void setMessageDetailsDao(MessageDetailsDAO messageDetailsDao) {
        this.messageDetailsDao = messageDetailsDao;
    }

}
