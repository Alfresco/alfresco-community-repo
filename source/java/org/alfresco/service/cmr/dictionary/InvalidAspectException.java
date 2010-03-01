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
package org.alfresco.service.cmr.dictionary;

import org.alfresco.service.namespace.QName;

/**
 * Thrown when a reference to an <b>aspect</b> is incorrect.
 * 
 * @author Derek Hulley
 */
public class InvalidAspectException extends InvalidClassException
{
    private static final long serialVersionUID = 3257290240330051893L;

    public InvalidAspectException(QName aspectName)
    {
        super(null, aspectName);
    }

    public InvalidAspectException(String msg, QName aspectName)
    {
        super(msg, aspectName);
    }

    /**
     * @return Returns the offending aspect name
     */
    public QName getAspectName()
    {
        return getClassName();
    }
}
