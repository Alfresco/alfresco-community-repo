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
package org.alfresco.service.cmr.repository;

/**
 * Thrown when an operation cannot be performed because the<b>child association</b>
 * reference no longer exists.
 * 
 * @author Derek Hulley
 */
public class InvalidChildAssociationRefException extends RuntimeException
{
    private static final long serialVersionUID = -7493054268618534572L;

    private ChildAssociationRef childAssociationRef;
    
    public InvalidChildAssociationRefException(ChildAssociationRef childAssociationRef)
    {
        this(null, childAssociationRef);
    }

    public InvalidChildAssociationRefException(String msg, ChildAssociationRef childAssociationRef)
    {
        super(msg);
        this.childAssociationRef = childAssociationRef;
    }

    /**
     * @return Returns the offending child association reference
     */
    public ChildAssociationRef getChildAssociationRef()
    {
        return childAssociationRef;
    }
}
