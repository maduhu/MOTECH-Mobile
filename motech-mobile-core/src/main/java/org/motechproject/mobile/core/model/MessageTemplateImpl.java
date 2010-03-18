package org.motechproject.mobile.core.model;

import java.util.Date;

/**
 * MessageTemplateImpl class is an implementation of MessageTemplate interface which is
 * mapped in hibernate.It provides properties to handle MessageTemplate operations
 *  Date : Sep 27, 2009
 * @author joseph Djomeda (joseph@dreamoval.com)
 */
public class MessageTemplateImpl extends MotechEntityImpl implements MessageTemplate {

    public MessageTemplateImpl() {
    }
    private NotificationType notificationType;
    private Language language;
    private Date dateCreated;
    private MessageType messageType;
    private String template;

    /**
     * @return the notification_type
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * @param notification_type the notification_type to set
     */
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return the date_created
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param date_created the date_created to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the message_type
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param message_type the message_type to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = System.getProperty("line.separator");

        if (this != null) {
            sb.append((this.getId() != null) ? "key=Id value=" + this.getId().toString() : "Id is null ");
            sb.append(newLine);
            sb.append((this.language != null) ? "key=Language.Id value=" + this.language.getId() : "Language is null ");
            sb.append(newLine);
            sb.append((this.notificationType != null) ? "key=NotificationType.Id value=" + this.notificationType.getId() : "NotificationType is null ");
            sb.append(newLine);
            sb.append((this.template != null) ? "key=template value=" + this.template : "template is null  ");
            sb.append(newLine);

            sb.append((this.dateCreated != null) ? "key=dateCreated value=" + this.dateCreated.toString() : "dateCreated is null ");
            sb.append(newLine);
            sb.append((this.messageType != null) ? "key=messageType value=" + this.messageType.toString() : "messageType is null ");
            sb.append(newLine);


            return sb.toString();

        } else {
            return "Object is null";
        }

    }
}