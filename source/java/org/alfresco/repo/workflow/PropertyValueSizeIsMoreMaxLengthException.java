/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.workflow;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;

public class PropertyValueSizeIsMoreMaxLengthException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5722742734237891185L;

    public PropertyValueSizeIsMoreMaxLengthException(QName name)
    {
        super("Property '" + name.getLocalName() + "' has size more than max value.");
    }
    
    public PropertyValueSizeIsMoreMaxLengthException(String name)
    {
        super("Property '" + name + "' has size more than max value.");
    }
}