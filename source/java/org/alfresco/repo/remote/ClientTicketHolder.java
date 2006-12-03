/**
 * 
 */
package org.alfresco.repo.remote;

/**
 * Remote client utility to hold an authentication ticket.
 * @author britt
 */
public class ClientTicketHolder 
{
    /**
     * Thread local tickets.
     */
    private static String fTicket;
    
    /**
     * Set the ticket.
     */
    public static void SetTicket(String ticket)
    {
        fTicket = ticket;
    }
    
    /**
     * Get the ticket.
     */
    public static String GetTicket()
    {
        return fTicket;
    }
}
