/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dreamoval.motech.imp.util;

import com.dreamoval.motech.core.manager.CoreManager;
import com.dreamoval.motech.core.model.IncMessageSessionStatus;
import com.dreamoval.motech.core.service.MotechContext;
import com.dreamoval.motech.model.dao.imp.IncomingMessageFormDAO;
import com.dreamoval.motech.model.dao.imp.IncomingMessageSessionDAO;
import com.dreamoval.motech.model.imp.IncMessageFormParameterStatus;
import com.dreamoval.motech.model.imp.IncMessageFormStatus;
import com.dreamoval.motech.core.model.IncMessageResponseStatus;
import com.dreamoval.motech.core.model.IncMessageStatus;
import com.dreamoval.motech.core.model.IncomingMessage;
import com.dreamoval.motech.model.imp.IncomingMessageForm;
import com.dreamoval.motech.model.imp.IncomingMessageFormDefinition;
import com.dreamoval.motech.model.imp.IncomingMessageFormParameter;
import com.dreamoval.motech.core.model.IncomingMessageResponse;
import com.dreamoval.motech.core.model.IncomingMessageSession;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;

/**
 * Handles construction and processing of a new IncomingMessageForm
 *
 * @author Kofi A. Asamoah (yoofi@dreamoval.com)
 *  Date : Dec 5, 2009
 */
public class FormCommandAction implements CommandAction {

    private CoreManager coreManager;
    private IncomingMessageParser parser;
    private IncomingMessageFormValidator formValidator;
    private static Logger logger = Logger.getLogger(FormCommandAction.class);

    /**
     * 
     * @see CommandAction.execute
     */
    public synchronized IncomingMessageResponse execute(IncomingMessage message, String requesterPhone) {
        MotechContext context = coreManager.createMotechContext();

        logger.info("Initializing session");
        IncomingMessageSession imSession = initializeSession(message, requesterPhone, context);

        logger.info("Generating form");
        IncomingMessageForm form = initializeForm(message, imSession.getFormCode(), context);

        logger.info("Validating form");
        formValidator.validate(form);
        message.setIncomingMessageForm(form);

        logger.info("Preparing response");
        IncomingMessageResponse response = prepareResponse(message);

        logger.info("Saving request");
        message.setIncomingMessageResponse(response);
        message.setMessageStatus(IncMessageStatus.PROCESSED);
        message.setLastModified(new Date());

        imSession.setDateEnded(new Date());
        imSession.setMessageSessionStatus(IncMessageSessionStatus.ENDED);

        IncomingMessageSessionDAO sessionDao = coreManager.createIncomingMessageSessionDAO(context);
        Transaction tx = (Transaction) sessionDao.getDBSession().getTransaction();
        tx.begin();
        sessionDao.save(imSession);
        tx.commit();

        return response;
    }

    /**
     * Initializes a request session conversation
     * @param message Incoming message
     * @param requesterPhone phone number of requester
     * @param context the context of the request
     * @return the initialized session
     */
    public synchronized IncomingMessageSession initializeSession(IncomingMessage message, String requesterPhone, MotechContext context) {
        String formCode = parser.getFormCode(message.getContent());

        IncomingMessageSession imSession = coreManager.createIncomingMessageSession();
        imSession.setFormCode(formCode);
        imSession.setRequesterPhone(requesterPhone);
        imSession.setMessageSessionStatus(IncMessageSessionStatus.STARTED);
        imSession.setDateStarted(new Date());
        imSession.setLastActivity(new Date());
        imSession.getIncomingMessages().add(message);

        IncomingMessageSessionDAO sessionDao = coreManager.createIncomingMessageSessionDAO(context);
        Transaction tx = (Transaction) sessionDao.getDBSession().getTransaction();
        tx.begin();
        sessionDao.save(imSession);
        tx.commit();

        return imSession;
    }

    /**
     * Initializes a request form
     * @param message the request message
     * @param formCode the type of form
     * @param context the context of the request
     * @return
     */
    public synchronized IncomingMessageForm initializeForm(IncomingMessage message, String formCode, MotechContext context) {
        IncomingMessageFormDefinition formDefn = coreManager.createIncomingMessageFormDefinitionDAO(context).getByCode(formCode);

        IncomingMessageForm form = coreManager.createIncomingMessageForm();
        form.setIncomingMsgFormDefinition(formDefn);
        form.setMessageFormStatus(IncMessageFormStatus.NEW);
        form.setDateCreated(new Date());
        form.setIncomingMsgFormParameters(new ArrayList<IncomingMessageFormParameter>());
        form.getIncomingMsgFormParameters().addAll(parser.getParams(message.getContent()));

        IncomingMessageFormDAO formDao = coreManager.createIncomingMessageFormDAO(context);
        Transaction tx = (Transaction) formDao.getDBSession().getTransaction();
        tx.begin();
        formDao.save(form);
        tx.commit();

        return form;
    }

    /**
     * Prepares a response to a request message
     * @param message the message to respond to
     * @return the response to the message
     */
    public synchronized IncomingMessageResponse prepareResponse(IncomingMessage message) {
        IncomingMessageForm form = message.getIncomingMessageForm();

        IncomingMessageResponse response = coreManager.createIncomingMessageResponse();
        response.setDateCreated(new Date());
        response.setIncomingMessage(message);

        if (form == null) {
            response.setContent("Invalid request");
            return response;
        }

        if (form.getMessageFormStatus().equals(IncMessageFormStatus.VALID)) {
            response.setMessageResponseStatus(IncMessageResponseStatus.SAVED);
            response.setContent("Saved");
        } else {
            String responseText = "Errors:";
            for (IncomingMessageFormParameter inParam : form.getIncomingMsgFormParameters()) {
                if (inParam.getMessageFormParamStatus().equals(IncMessageFormParameterStatus.INVALID)) {
                    responseText += '\n'+inParam.getErrText();
                }
            }
            response.setMessageResponseStatus(IncMessageResponseStatus.SAVED);
            response.setContent(responseText);
        }
        return response;
    }

    /**
     * @return the coreManager
     */
    public CoreManager getCoreManager() {
        return coreManager;
    }

    /**
     * @param coreManager the coreManager to set
     */
    public void setCoreManager(CoreManager coreManager) {
        this.coreManager = coreManager;
    }

    /**
     * @return the parser
     */
    public IncomingMessageParser getParser() {
        return parser;
    }

    /**
     * @param parser the parser to set
     */
    public void setParser(IncomingMessageParser parser) {
        this.parser = parser;
    }

    /**
     * @return the formValidator
     */
    public IncomingMessageFormValidator getFormValidator() {
        return formValidator;
    }

    /**
     * @param formValidator the formValidator to set
     */
    public void setFormValidator(IncomingMessageFormValidator formValidator) {
        this.formValidator = formValidator;
    }
}