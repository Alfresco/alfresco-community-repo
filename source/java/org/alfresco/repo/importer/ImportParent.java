/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
