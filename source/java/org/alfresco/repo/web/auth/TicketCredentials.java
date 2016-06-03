package org.alfresco.repo.web.auth;


/**
 * {@link WebCredentials} class for holding Alfresco tickets.
 *
 * @author Alex Miller
 */
public class TicketCredentials implements WebCredentials
{
    private static final long serialVersionUID = -8255499275655719748L;

    private String ticket;

    public TicketCredentials(String ticket)
    {
        this.ticket = ticket;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.ticket == null) ? 0 : this.ticket.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        TicketCredentials other = (TicketCredentials) obj;
        if (this.ticket == null)
        {
            if (other.ticket != null) { return false; }
        }
        else if (!this.ticket.equals(other.ticket)) { return false; }
        return true;
    }
}
