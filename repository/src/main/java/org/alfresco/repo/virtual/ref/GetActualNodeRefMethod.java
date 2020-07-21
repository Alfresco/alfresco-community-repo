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

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Returns the actual {@link NodeRef} of a given reference.<br>
 * Actual {@link NodeRef} are node references of content elements found in the
 * Alfresco repository that are subjected to a virtualization process.
 * 
 * @author Bogdan Horje
 */
public class GetActualNodeRefMethod extends AbstractProtocolMethod<NodeRef>
{
    private ActualEnvironment environment;

    public GetActualNodeRefMethod(ActualEnvironment environment)
    {
        super();
        this.environment = environment;
    }

    @Override
    public NodeRef execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        try
        {
            RepositoryLocation nodeRefLocation = virtualProtocol.getActualNodeLocation(reference);
            return nodeRefLocation.asNodeRef(environment);
        }
        catch (ActualEnvironmentException e)
        {
            throw new ProtocolMethodException(e);
        }
    }

    @Override
    public NodeRef execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        return protocol.getNodeRef(reference);
    }
}
