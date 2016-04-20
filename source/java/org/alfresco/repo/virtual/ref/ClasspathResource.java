
package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;

/**
 * Identifies content from a classpath
 */
public class ClasspathResource implements Resource
{

    private String classpath;

    public ClasspathResource(String classpath)
    {
        this.classpath = classpath;
    }

    public String getClasspath()
    {
        return classpath;
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyResource(this);
    }

    @Override
    public int hashCode()
    {
        return classpath != null ? classpath.hashCode() : 0;
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
        else if (!(obj instanceof ClasspathResource))
        {
            return false;
        }

        ClasspathResource other = (ClasspathResource) obj;

        if (classpath == null)
        {
            return other.classpath == null;
        }
        else
        {
            return this.classpath.equals(other.classpath);
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
        return environment.openContentStream(classpath);
    }
}
