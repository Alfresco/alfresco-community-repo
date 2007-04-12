/**
 * 
 */
package org.alfresco.repo.remote;

/**
 * Remote client utility to hold an authentication ticket.
 * @author britt
 */
public class ClientTicketHolderGlobal implements ClientTicketHolder 
{
    /**
     * Thread local tickets.
     */
    private String fTicket;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.remote.ClientTicketHolder#setTicket(java.lang.String)
     */
    public void setTicket(String ticket)
    {
        fTicket = ticket;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.remote.ClientTicketHolder#getTicket()
     */
    public String getTicket()
    {
        return fTicket;
    }
}
