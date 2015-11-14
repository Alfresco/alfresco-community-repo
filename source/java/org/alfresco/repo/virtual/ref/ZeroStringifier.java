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

public class ZeroStringifier implements Stringifier, ZeroEncoding
{

    private static final long serialVersionUID = 6777894566062875199L;

    private int getProtocolEncoding(Protocol protocol) throws ReferenceEncodingException
    {
        String protocolStr = protocol.toString();
        if (protocolStr.equals("vanilla"))
        {
            return VANILLA_PROTOCOL_CODE;
        }
        else if (protocolStr.equals("virtual"))
        {
            return VIRTUAL_PROTOCOL_CODE;
        }
        else if (protocolStr.equals("node"))
        {
            return NODE_PROTOCOL_CODE;
        }
        else 
        {
            throw new ReferenceEncodingException("Invalid protocol: " + protocolStr);
        }
    }

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        String resource = stringify(reference.getResource());
        int protocolResourceEncoding = this.getProtocolEncoding(reference.getProtocol());
        protocolResourceEncoding += Integer.parseInt(resource.substring(0,
                                                                        1));
        return protocolResourceEncoding + DELIMITER + resource.substring(2) + stringify(reference.getParameters());
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
        String resourceLocation = stringify(resource.getLocation());
        int locationCode = Integer.parseInt(resourceLocation.substring(0,
                                                                       1));
        locationCode += REPOSITORY_RESOURCE_CODE;
        return locationCode + DELIMITER + resourceLocation.substring(2);
    }

    @Override
    public String stringifyResource(ClasspathResource resource)
    {
        return CLASSPATH_RESOURCE_CODE + DELIMITER + resource.getClasspath();
    }

    @Override
    public String stringify(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        return repositoryLocation.stringify(this);
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid location: " + repositoryLocation.getClass());
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryNodeRef repositoryNodeRef) throws ReferenceEncodingException
    {
        NodeRef nodeRef = repositoryNodeRef.getNodeRef();
        return NODE_CODE + DELIMITER + nodeRef.getId();
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryPath repositoryPath) throws ReferenceEncodingException
    {
        return PATH_CODE + DELIMITER + repositoryPath.getPath();
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
        return stringify(resourceParameter.getValue());
    }

    @Override
    public String stringifyParameter(StringParameter stringParameter) throws ReferenceEncodingException
    {
        return STRING_PARAMETER + DELIMITER + stringParameter.getValue();
    }

    @Override
    public String stringifyParameter(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Invalid parameter: " + parameter.getClass());
    }

    @Override
    public String stringifyParameter(ReferenceParameter parameter) throws ReferenceEncodingException
    {
        return REFERENCE_PARAMETER + DELIMITER + stringify(parameter.getValue()) + DELIMITER + REFERENCE_DELIMITER;
    }

}
