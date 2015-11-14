/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.template;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class FilingData
{
    private NodeRef filingNodeRef;

    private QName assocTypeQName;

    private QName assocQName;

    private QName nodeTypeQName;

    private Set<QName> aspects;

    private Map<QName, Serializable> properties;

    public FilingData(NodeRef filingNodeRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName,
                Set<QName> aspects, Map<QName, Serializable> properties)
    {
        super();
        this.filingNodeRef = filingNodeRef;
        this.assocTypeQName = assocTypeQName;
        this.assocQName = assocQName;
        this.nodeTypeQName = nodeTypeQName;
        this.aspects = aspects;
        this.properties = properties;
    }

    public Set<QName> getAspects()
    {
        return aspects;
    }

    public NodeRef getFilingNodeRef()
    {
        return filingNodeRef;
    }

    public QName getAssocTypeQName()
    {
        return assocTypeQName;
    }

    public QName getAssocQName()
    {
        return assocQName;
    }

    public QName getNodeTypeQName()
    {
        return nodeTypeQName;
    }

    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }

}
