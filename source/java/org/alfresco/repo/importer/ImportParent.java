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
package org.alfresco.repo.importer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Description of parent for node to import.
 * 
 * @author David Caruana
 *
 */
public interface ImportParent
{
    /**
     * @return  the parent ref
     */    
    public NodeRef getParentRef();
    
    /**
     * @return  the child association type
     */
    public QName getAssocType();

    /**
     * Re-set parent reference
     * 
     * @param parentRef  parent reference
     */
    public void setParentRef(NodeRef parentRef);
    
    /**
     * Re-set association type
     * 
     * @param  assocType  association type
     */
    public void setAssocType(QName assocType);
    
}
