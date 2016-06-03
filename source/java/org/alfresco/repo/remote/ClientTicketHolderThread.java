package org.alfresco.repo.remote;
 
/**
 * A Ticket holder that holds a ticket per thread.
 * @author britt
 */
public class ClientTicketHolderThread implements ClientTicketHolder 
{
    /**
     * The Thread Local storage for tickets.
     */
    private ThreadLocal<String> fTicket;
    
    public ClientTicketHolderThread()
    {
        fTicket = new ThreadLocal<String>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.remote.ClientTicketHolder#getTicket()
     */
    public String getTicket() 
    {
        return fTicket.get();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.remote.ClientTicketHolder#setTicket(java.lang.String)
     */
    public void setTicket(String ticket) 
    {
        fTicket.set(ticket);
    }
}
