package org.alfresco.repo.remote;

/**
 * Interface for Authentication ticket caching.
 * @author britt
 */
public interface ClientTicketHolder 
{
    /**
     * Set the ticket.
     */
    public void setTicket(String ticket);
    
    /**
     * Get the ticket.
     */
    public String getTicket();
}
