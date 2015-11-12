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

package org.alfresco.repo.virtual.ref;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Converts the object to a string representation according to
 * {@link Encodings#PLAIN} encoding definition.
 */
public class PlainStringifier implements Stringifier, PlainEncoding
{
    /**
     * 
     */
    private static final long serialVersionUID = -8169416257716384803L;

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        return reference.getProtocol() + DELIMITER + stringify(reference.getResource())
                    + stringify(reference.getParameters());
    }

    @Override
    public String stringify(Resource resource) throws ReferenceEncodingException
    {
        return resource.stringify(this);
    }

    @Override
    public String stringifyResource(Resource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid reference " + resource.getClass());
    }

    @Override
    public String stringifyResource(RepositoryResource resource) throws ReferenceEncodingException
    {
        return REPOSITORY + DELIMITER + stringify(resource.getLocation());
    }

    @Override
    public String stringifyResource(ClasspathResource resource)
    {
        return CLASSPATH + DELIMITER + resource.getClasspath();
    }

    @Override
    public String stringify(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        return repositoryLocation.stringify(this);
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid location " + repositoryLocation.getClass());
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryNodeRef repositoryNodeRef) throws ReferenceEncodingException
    {
        NodeRef nodeRef = repositoryNodeRef.getNodeRef();
        StoreRef storeRef = nodeRef.getStoreRef();

        return NODE + DELIMITER + storeRef.getProtocol() + DELIMITER + storeRef.getIdentifier() + DELIMITER
                    + nodeRef.getId();
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryPath repositoryPath) throws ReferenceEncodingException
    {
        return PATH + DELIMITER + repositoryPath.getPath();
    }

    @Override
    public String stringify(List<Parameter> parameters) throws ReferenceEncodingException
    {
        StringBuilder parametersBuilder = new StringBuilder();
        for (Parameter parameter : parameters)
        {
            parametersBuilder.append(DELIMITER);
            parametersBuilder.append(stringify(parameter));
        }
        return parametersBuilder.toString();
    }

    @Override
    public String stringify(Parameter parameter) throws ReferenceEncodingException
    {
        return parameter.stringify(this);
    }

    @Override
    public String stringifyParameter(ResourceParameter resourceParameter) throws ReferenceEncodingException
    {
        return RESOURCE_PARAMETER + DELIMITER + stringify(resourceParameter.getValue());
    }

    @Override
    public String stringifyParameter(StringParameter stringParameter) throws ReferenceEncodingException
    {
        return STRING_PARAMETER + DELIMITER + stringParameter.getValue();
    }

    @Override
    public String stringifyParameter(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid parameter " + parameter.getClass());
    }

    @Override
    public String stringifyParameter(ReferenceParameter parameter) throws ReferenceEncodingException
    {
        return REFERENCE_PARAMETER + DELIMITER + stringify(parameter.getValue()) + DELIMITER + REFERENCE_DELIMITER;
    }

}
