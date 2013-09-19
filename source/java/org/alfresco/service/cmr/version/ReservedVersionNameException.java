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
package org.alfresco.service.cmr.version;

import java.text.MessageFormat;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public class ReservedVersionNameException extends RuntimeException
{
    /**
     * Serial verison UID
     */
    private static final long serialVersionUID = 3690478030330015795L;

    /**
     * Error message
     */
    private static final String MESSAGE = "The version property name {0} clashes with a reserved verison property name.";
    
    /**
     * Constructor
     * 
     * @param propertyName  the name of the property that clashes with
     *                      a reserved property name
     */
    public ReservedVersionNameException(String propertyName)
    {
        super(MessageFormat.format(MESSAGE, new Object[]{propertyName}));
    }
}
