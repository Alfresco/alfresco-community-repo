/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.repository;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.namespace.QName;


/**
 * Thrown when a child node <b>cm:name</b> property  violates the data dictionary
 * <b>duplicate</b> child association constraint.
 * 
 * @author Derek Hulley
 */
public class DuplicateChildNodeNameException extends RuntimeException
{
    private static final long serialVersionUID = 5143099335847200453L;

    private static final String ERR_DUPLICATE_NAME = "system.err.duplicate_name";
    
    private NodeRef parentNodeRef;
    private QName assocTypeQName;
    private String name;
    
    public DuplicateChildNodeNameException(NodeRef parentNodeRef, QName assocTypeQName, String name)
    {
        super(I18NUtil.getMessage(ERR_DUPLICATE_NAME, name));
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
