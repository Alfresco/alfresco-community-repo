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
package org.alfresco.repo.forms;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception used by the Form service when a form can not be found for the given item
 *
 * @author Gavin Cornwell
 */
public class FormNotFoundException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 688834574410335422L;

    public FormNotFoundException(Item item)
    {
        // TODO: replace strings with msg ids
        super("A form could not be found for item: " + item);
    }
    
    public FormNotFoundException(Item item, Throwable cause)
    {
        // TODO: replace strings with msg ids
        super("A form could not be found for item: " + item, cause);
    }
}
