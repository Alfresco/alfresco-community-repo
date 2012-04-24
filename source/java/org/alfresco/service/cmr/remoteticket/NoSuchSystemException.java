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
package org.alfresco.service.cmr.remoteticket;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown if a request is made, to work on 
 *  authentication for a Remote System, where the
 *  System is not known to the service.
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class NoSuchSystemException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 282472917033620185L;
    private String system; 

    public NoSuchSystemException(String system) 
    {
        super("No Remote System defined with ID '" + system + "'");
        this.system = system;
    }
    
    public String getSystem() 
    {
        return system;
    }
}