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
