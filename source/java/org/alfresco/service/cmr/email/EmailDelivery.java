package org.alfresco.service.cmr.email;

import java.io.Serializable;

/**
 * Delivery instructions for an email message.
 * @author mrogers
 *
 */
public class EmailDelivery implements Serializable
{
    private String recipient;
    private String from;
    private String auth;

    /**
     * New Email Delivery Instructions.  Who gets the message and who sent it.
     * Which may be different from the contents of the message.
     * @param recipient
     * @param from
     * @param auth - may be null if the email is not authenticated
     */
    public EmailDelivery(String recipient, String from, String auth)
    {
        this.recipient = recipient;
        this.from = from;
        this.auth = auth;
    }

    public String getRecipient()
    {
        return recipient;
    }
    
    public String getFrom()
    {
        return from;
    }
    public String getAuth()
    {
        return auth;
    }

}
