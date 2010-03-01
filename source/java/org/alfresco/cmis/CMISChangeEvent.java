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
package org.alfresco.cmis;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class describes entry record for some <b>Change Log</b> descriptor.
 * 
 * @author Dmitry Velichkevich
 */
public interface CMISChangeEvent
{    
    /**
     * Gets the change type.
     * 
     * @return {@link CMISChangeType} <b>enum</b> value that determines the type of current <b>Change Event</b>
     */
    public CMISChangeType getChangeType();
    
    /**
     * Gets the change time.
     * 
     * @return {@link Date} value that represents time of current <b>Change Event</b>
     */
    public Date getChangeTime();
    
    /**
     * Gets the changed node (may no longer exist).
     * 
     * @return the changed node
     */
    public NodeRef getChangedNode();

    /**
     * Gets the object id.
     * 
     * @return the object id
     */
    public String getObjectId();
    
}
