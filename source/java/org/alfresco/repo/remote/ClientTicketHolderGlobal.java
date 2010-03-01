/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
