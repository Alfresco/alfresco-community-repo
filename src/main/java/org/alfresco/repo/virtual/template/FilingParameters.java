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

package org.alfresco.repo.virtual.template;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.namespace.QName;

/**
 * Encapsulated node creation parameters needed to produce {@link FilingData}
 * used for node creation in virtual contexts using {@link FilingRule}s.
 */
public class FilingParameters
{
    private Reference parentRef;

    private QName assocTypeQName;

    private QName assocQName;

    private QName nodeTypeQName;

    private Map<QName, Serializable> properties;

    public FilingParameters(Reference parentReference)
    {
        this(parentReference,
             null,
             null,
             null,
             null);
    }

    public FilingParameters(Reference parentReference, QName assocTypeQName, QName assocQName, QName nodeTypeQName,
                Map<QName, Serializable> properties)
    {
        super();
        this.parentRef = parentReference;
        this.assocTypeQName = assocTypeQName;
        this.assocQName = assocQName;
        this.nodeTypeQName = nodeTypeQName;
        this.properties = properties;
    }

    public Reference getParentRef()
    {
        return this.parentRef;
    }

    public QName getAssocTypeQName()
    {
        return this.assocTypeQName;
    }

    public QName getAssocQName()
    {
        return this.assocQName;
    }

    public QName getNodeTypeQName()
    {
        return this.nodeTypeQName;
    }

    public Map<QName, Serializable> getProperties()
    {
        return this.properties;
    }

}
