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

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.transaction.DoNotRetryException;
import org.alfresco.service.namespace.QName;


/**
 * Thrown when a child node <b>cm:name</b> property  violates the data dictionary
 * <b>duplicate</b> child association constraint.
 * <p/>
 * Note that this exception may be triggered by database constraints but must
 * still NOT trigger transaction retries.
 * 
 * @author Derek Hulley
 */
public class DuplicateChildNodeNameException extends RuntimeException implements DoNotRetryException
{
    private static final long serialVersionUID = 5143099335847200453L;

    private static final String ERR_DUPLICATE_NAME = "system.err.duplicate_name";
    
    private NodeRef parentNodeRef;
    private QName assocTypeQName;
    private String name;
    
    public DuplicateChildNodeNameException(NodeRef parentNodeRef, QName assocTypeQName, String name, Throwable e)
    {
        super(I18NUtil.getMessage(ERR_DUPLICATE_NAME, name), e);
        this.parentNodeRef = parentNodeRef;
        this.assocTypeQName = assocTypeQName;
        this.name = name;
    }

    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }

    public QName getAssocTypeQName()
    {
        return assocTypeQName;
    }

    public String getName()
    {
        return name;
    }
}
