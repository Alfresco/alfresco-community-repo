/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.auth;


/**
 * {@link WebScriptCrednetials} class for holding Alfresco tickets.
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
