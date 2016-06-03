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

import org.alfresco.util.Pair;
/**
 * Custom stringifier for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link VirtualProtocol}.
 */
public class VirtualHashStringifier extends ProtocolHashStringifier
{
    private static final long serialVersionUID = -252596166306653635L;

    private NumericPathHasher numericPathHasher = new NumericPathHasher();

    public VirtualHashStringifier(HashStore classpathHashStore, Stringifier referenceDispatcher)
    {
        super(classpathHashStore,
              referenceDispatcher);
    }

    /**
     * 
     */

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        final Protocol protocol = reference.getProtocol();
        if (!Protocols.VIRTUAL.protocol.equals(protocol))
        {
            throw new ReferenceEncodingException("Unsupported protocol " + protocol + "."
                        + Protocols.VIRTUAL.protocol.name + " exoected ");
        }

        return VIRTUAL_PROTOCOL_CODE + "-" + stringifyVirtualReference(reference);
    }

    protected String stringifyVirtualReference(Reference reference)
    {
        Resource resource = reference.getResource();
        String resourceString = resource.stringify(this);

        List<Parameter> parameters = reference.getParameters();

        ResourceParameter actualNodeParameter = (ResourceParameter) parameters
                    .get(VirtualProtocol.ACTUAL_NODE_LOCATION_PARAM_INDEX);
        Resource actualNodeResource = actualNodeParameter.getValue();
        String actualNodeResourceString = actualNodeResource.stringify(this);

        StringParameter templatePathParameter;
        templatePathParameter = (StringParameter) parameters.get(VirtualProtocol.TEMPLATE_PATH_PARAM_INDEX);
        String pathString = templatePathParameter.getValue();

        Pair<String, String> pathHash = numericPathHasher.hash(pathString);

        StringBuilder stringifiedPath = new StringBuilder();
        String hashed = pathHash.getFirst();
        String nonHashed = pathHash.getSecond();

        if (nonHashed == null)
        {
            stringifiedPath.append(HASHED_NUMERIC_PATH_CODE);
            stringifiedPath.append(hashed);
        }
        else if (hashed == null)
        {
            if (nonHashed.isEmpty())
            {
                stringifiedPath.append(NUMERIC_ROOT_PATH_CODE);
            }
            else
            {
                stringifiedPath.append(NUMERIC_PATH_CODE);
                stringifiedPath.append(nonHashed);
            }
        }
        else
        {
            stringifiedPath.append(MIXED_NUMERIC_PATH_CODE);
            stringifiedPath.append(hashed);
            stringifiedPath.append("-");
            stringifiedPath.append(nonHashed);
        }

//        String delimitedPathString;
//        if ("/".equals(pathString.trim()))
//        {
//            delimitedPathString = "";
//        }
//        else
//        {
//            delimitedPathString = pathString.replace('/',
//                                                     '-');
//        }

        String parametersString = actualNodeResourceString +"-"+ stringifiedPath.toString();

        return resourceString + "-" + parametersString;
    }

}
