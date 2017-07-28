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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Base class for custom protocol hash encoded {@link Stringifier}s.
 */
public abstract class ProtocolHashStringifier implements HashEncodingArtefact, Stringifier
{
    /**
     * 
     */
    private static final long serialVersionUID = 6471653470842760043L;

    private NodeRefHasher nodeRefHasher = NodeRefRadixHasher.RADIX_36_HASHER;

    private PathHasher classpathHasher;
    
    private PathHasher repositoryPathHasher;

    private Stringifier referenceDispatcher;

    public ProtocolHashStringifier(HashStore classpathHashStore, Stringifier referenceDispatcher)
    {
        this.classpathHasher = new StoredPathHasher(classpathHashStore);
        this.repositoryPathHasher=new StoredPathHasher(classpathHashStore);
        this.referenceDispatcher = referenceDispatcher;
    }

    protected String dispatchStringifyReference(Reference reference)
    {
        return referenceDispatcher.stringify(reference);
    }

    @Override
    public String stringify(Resource resource) throws ReferenceEncodingException
    {
        return resource.stringify(this);
    }

    @Override
    public String stringifyResource(Resource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown resource type " + resource);
    }

    @Override
    public String stringifyResource(RepositoryResource resource) throws ReferenceEncodingException
    {
        RepositoryLocation location = resource.getLocation();
        return location.stringify(this);

    }

    @Override
    public String stringifyResource(ClasspathResource resource) throws ReferenceEncodingException
    {
        String cp = resource.getClasspath();
        Pair<String, String> hash = classpathHasher.hash(cp);
        final String hashed = hash.getFirst();
        final String nonHashed = hash.getSecond();
        if (nonHashed == null)
        {
            return HASHED_CLASSPATH_RESOUCE_CODE + "-" + hashed;
        }
        else if (hashed == null)
        {
            return CLASSPATH_RESOUCE_CODE + "-" + nonHashed;
        }
        else
        {
            return MIXED_CLASSPATH_RESOUCE_CODE + "-" + hashed + "-" + nonHashed;
        }

    }

    @Override
    public String stringify(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        return repositoryLocation.stringify(this);
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown repository location  " + repositoryLocation);
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryNodeRef repositoryNodeRef) throws ReferenceEncodingException
    {
        NodeRef nodeRef = repositoryNodeRef.getNodeRef();
        Pair<String, String> hash = nodeRefHasher.hash(nodeRef);
        return REPOSITORY_NODEREF_RESOURCE_CODE + "-" + hash.getFirst() + hash.getSecond();
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryPath repositoryPath) throws ReferenceEncodingException
    {
        String cp = repositoryPath.getPath();
        Pair<String, String> hash = repositoryPathHasher.hash(cp);
        final String hashed = hash.getFirst();
        final String nonHashed = hash.getSecond();
        if (nonHashed == null)
        {
            return HASHED_REPOSITORY_PATH_CODE + "-" + hashed;
        }
        else if (hashed == null)
        {
            return REPOSITORY_PATH_CODE + "-" + nonHashed;
        }
        else
        {
            return MIXED_REPOSITORY_PATH_CODE + "-" + hashed + "-" + nonHashed;
        }
    }

    // parameters

    @Override
    public String stringify(List<Parameter> parameters) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid stringifier cotext " + parameters);
    }

    @Override
    public String stringify(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid stringifier cotext " + parameter);
    }

    @Override
    public String stringifyParameter(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid stringifier cotext " + parameter);
    }

    @Override
    public String stringifyParameter(ResourceParameter resourceParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid stringifier cotext " + resourceParameter);
    }

    @Override
    public String stringifyParameter(StringParameter stringParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid stringifier cotext " + stringParameter);
    }

    @Override
    public String stringifyParameter(ReferenceParameter referenceParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid stringifier cotext " + referenceParameter);
    }

}
