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

package org.alfresco.repo.virtual.bundle;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ref.AbstractProtocolMethod;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.ReferenceEncodingException;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

public class GetPathMethod extends AbstractProtocolMethod<Path>
{
    private VirtualStore virtualStore;

    private ActualEnvironment environment;

    public GetPathMethod(VirtualStore virtualStore, ActualEnvironment actualEnvironment)
    {
        super();
        this.virtualStore = virtualStore;
        this.environment = actualEnvironment;
    }

    @Override
    public Path execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        try
        {

            NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(environment));

            Path path = null;
            if (actualNodeRef == null)
            {
                // Although not a feature yet, pure-virtual-references should
                // use an empty path as root since pure-virtual-references have
                // no actual peer to use.
                path = new Path();
            }
            else
            {
                path = environment.getPath(actualNodeRef);
            }
            Path virtualPath = virtualStore.getPath(reference);
            return path.append(virtualPath);
        }
        catch (ReferenceEncodingException e)
        {
            throw new ProtocolMethodException(e);
        }
    }

    @Override
    public Path execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        Reference parent = protocol.getVirtualParentReference(reference);
        NodeRef nodeRef = protocol.getNodeRef(reference);
        Path nodeRefPath = environment.getPath(nodeRef);
        Path parentPath = parent.execute(this);
        parentPath.append(nodeRefPath.last());
        return parentPath;
    }
}
