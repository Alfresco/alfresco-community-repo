
package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;

/**
 * Identifies content from {@link RepositoryLocation}
 */
public class RepositoryResource implements Resource
{
    private RepositoryLocation location;

    public RepositoryResource(RepositoryLocation aLocation)
    {
        this.location = aLocation;
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyResource(this);
    }

    public RepositoryLocation getLocation()
    {
        return location;
    }

    @Override
    public int hashCode()
    {
        return location != null ? location.hashCode() : 0;
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
        else if (!(obj instanceof RepositoryResource))
        {
            return false;
        }

        RepositoryResource other = (RepositoryResource) obj;

        if (location == null)
        {
            return other.location == null;
        }
        else
        {
            return this.location.equals(other.location);
        }
    }

    @Override
    public <R> R processWith(ResourceProcessor<R> processor) throws ResourceProcessingError
    {
        return processor.process(this);
    }

    @Override
    public InputStream asStream(ActualEnvironment environment) throws ActualEnvironmentException
    {
        return location.openContentStream(environment);
    }

}
