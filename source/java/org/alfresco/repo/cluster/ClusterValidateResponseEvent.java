/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cluster;

/**
 * 
 * @since Odin
 *
 */
public class ClusterValidateResponseEvent extends ClusterMessageEvent
{
    private static final long serialVersionUID = -813956714769487998L;
    
    private String address;
    private boolean ticketValid;

    public ClusterValidateResponseEvent(ClusterChecker clusterChecker, String address, String sourceId, String targetId, boolean ticketValid)
    {
        super(clusterChecker, sourceId, targetId);
        this.address = address;
        this.ticketValid = ticketValid;
    }

    public String getAddress()
    {
        return address;
    }

    public boolean isTicketValid()
    {
        return ticketValid;
    }

}
