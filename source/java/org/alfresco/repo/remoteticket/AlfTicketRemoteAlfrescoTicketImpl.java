package org.alfresco.repo.remoteticket;

import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.util.Pair;

/**
 * An implementation of {@link org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketInfo} which works
 *  with the regular Alfresco alf_ticket ticket system
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class AlfTicketRemoteAlfrescoTicketImpl extends AbstractRemoteAlfrescoTicketImpl
{
    private static final String TICKET_USERNAME = Authorization.TICKET_USERID;
    private static final String TICKET_URL_PARAM = "alf_ticket";
    
    private final String ticket;

    public AlfTicketRemoteAlfrescoTicketImpl(String ticket)
    {
        this.ticket = ticket;
    }

    /**
     * Returns the Ticket as a URL Parameter fragment, of the form
     *  "alf_ticket=XXXX"
     */
    public String getAsUrlParameters()
    {
        return TICKET_URL_PARAM + "=" + ticket;
    }
    
    /**
     * Returns the Ticket as a URL Escaped Parameter fragment, which is the
     *  same as the un-escaped due to the format of Alfresco Tickets
     */
    public String getAsEscapedUrlParameters()
    {
        return getAsUrlParameters();
    }
    
    /**
     * Returns the Ticket in the form of a pseudo username and password. 
     * The Username is a special ticket identifier, and the password
     *  is the ticket
     */
    public Pair<String,String> getAsUsernameAndPassword()
    {
        return new Pair<String,String>(TICKET_USERNAME, ticket);
    }
    
    public String toString()
    {
        return "Remote Alfresco Ticket: " + ticket;
    }
}
