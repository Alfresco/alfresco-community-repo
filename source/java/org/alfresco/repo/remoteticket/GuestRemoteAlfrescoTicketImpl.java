package org.alfresco.repo.remoteticket;

import org.alfresco.util.Pair;

/**
 * An implementation of {@link org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketInfo} which authenticates
 *  as the Guest user
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class GuestRemoteAlfrescoTicketImpl extends AbstractRemoteAlfrescoTicketImpl
{
    private static final String GUEST_USERNAME = "guest";
    private static final String GUEST_URL_PARAM = "guest=true";
    
    public GuestRemoteAlfrescoTicketImpl() {}

    /**
     * Returns the Ticket as a URL Parameter fragment, of the form
     *  "guest=true"
     */
    public String getAsUrlParameters()
    {
        return GUEST_URL_PARAM;
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
        return new Pair<String,String>(GUEST_USERNAME, "");
    }
}
