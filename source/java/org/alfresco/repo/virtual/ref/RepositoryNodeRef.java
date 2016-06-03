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

package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.service.cmr.repository.NodeRef;

public class RepositoryNodeRef implements RepositoryLocation
{

    private NodeRef nodeRef;

    public RepositoryNodeRef(NodeRef aNodeRef)
    {
        this.nodeRef = aNodeRef;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyRepositoryLocation(this);
    }

    @Override
    public int hashCode()
    {
        return nodeRef != null ? nodeRef.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof RepositoryNodeRef))
        {
            return false;
        }

        RepositoryNodeRef other = (RepositoryNodeRef) obj;

        if (nodeRef == null)
        {
            return other.nodeRef == null;
        }
        else
        {
            return this.nodeRef.equals(other.nodeRef);
        }
    }

    @Override
    public InputStream openContentStream(ActualEnvironment environment) throws ActualEnvironmentException
    {
        return environment.openContentStream(nodeRef);
    }

    @Override
    public NodeRef asNodeRef(ActualEnvironment environment) throws ActualEnvironmentException
    {
        return getNodeRef();
    }

}
