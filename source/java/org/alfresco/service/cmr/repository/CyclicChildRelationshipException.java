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
 * Thrown when a cyclic parent-child relationship is detected.
 * 
 * @author Derek Hulley
 */
public class CyclicChildRelationshipException extends RuntimeException
{
    private static final long serialVersionUID = 3545794381924874036L;

    private ChildAssociationRef assocRef;
    
    public CyclicChildRelationshipException(String msg, ChildAssociationRef assocRef)
    {
        super(msg);
        this.assocRef = assocRef;
    }

    public ChildAssociationRef getAssocRef()
    {
        return assocRef;
    }
}
