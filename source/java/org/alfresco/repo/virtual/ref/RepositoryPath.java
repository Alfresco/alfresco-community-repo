
package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.service.cmr.repository.NodeRef;

public class RepositoryPath implements RepositoryLocation
{
    private String path;

    public RepositoryPath(String aPath)
    {
        this.path = aPath;
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyRepositoryLocation(this);
    }

    @Override
    public int hashCode()
    {
        return this.path != null ? this.path.hashCode() : 0;
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
        else if (!(obj instanceof RepositoryPath))
        {
            return false;
        }

        RepositoryPath other = (RepositoryPath) obj;

        if (path == null)
        {
            return other.path == null;
        }
        else
        {
            return this.path.equals(other.path);
        }
    }

    @Override
    public InputStream openContentStream(ActualEnvironment environment) throws ActualEnvironmentException
    {
        throw new ActualEnvironmentException("Not implemented!");
    }

    @Override
    public NodeRef asNodeRef(ActualEnvironment environment) throws ActualEnvironmentException
    {
        throw new RuntimeException("Not implemented!");
    }
}
