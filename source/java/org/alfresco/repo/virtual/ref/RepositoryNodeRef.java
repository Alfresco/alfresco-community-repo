
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
