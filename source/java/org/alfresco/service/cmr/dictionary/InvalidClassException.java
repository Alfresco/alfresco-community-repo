/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;

/**
 * Thrown when an operation cannot be performed because the dictionary class 
 * reference does not exist.
 * 
 */
public class InvalidClassException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3256722870754293558L;

    private QName className;
    
    public InvalidClassException(QName className)
    {
        this(null, className);
    }

    public InvalidClassException(String msg, QName className)
    {
        super(msg);
        this.className = className;
    }

    /**
     * @return Returns the offending class name
     */
    public QName getClassName()
    {
        return className;
    }
}
