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

import java.util.Arrays;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

public class NodeProtocol extends Protocol
{
    /**
     * 
     */
    private static final long serialVersionUID = -6120481299842983600L;

    public static Reference newReference(NodeRef nodeRef, Reference parentReference)
    {
        return new Reference(DEFAULT_ENCODING,
                             Protocols.NODE.protocol,
                             new RepositoryResource(new RepositoryNodeRef(nodeRef)),
                             Arrays.asList(new ReferenceParameter(parentReference)));
    }

    public static Reference newReference(Encoding encoding, Resource actualNodeResource, Reference parentReference)
    {
        return new Reference(DEFAULT_ENCODING,
                             Protocols.NODE.protocol,
                             actualNodeResource,
                             Arrays.asList(new ReferenceParameter(parentReference)));
    }

    public NodeProtocol()
    {
        super("node");
    }

    public NodeRef getNodeRef(Reference reference)
    {
        // TODO: use a resource processor for node ref extraction
        RepositoryResource repositoryResource = (RepositoryResource) reference.getResource();
        RepositoryNodeRef reposioryNodeRef = (RepositoryNodeRef) repositoryResource.getLocation();
        return reposioryNodeRef.getNodeRef();
    }

    public Reference getVirtualParentReference(Reference reference)
    {
        return ((ReferenceParameter) reference.getParameters().get(0)).getValue();
    }

    @Override
    public <R> R dispatch(ProtocolMethod<R> method, Reference reference) throws ProtocolMethodException
    {
        return method.execute(this,
                              reference);
    }

    @Override
    public Reference propagateNodeRefMutations(NodeRef mutatedNodeRef, Reference reference)
    {
        StoreRef storeRef = mutatedNodeRef.getStoreRef();
        String storeId = storeRef.getIdentifier();
        String protocol = storeRef.getProtocol();

        if (Version2Model.STORE_ID.equals(storeId) || VersionModel.STORE_ID.equals(storeId)
                    || VersionBaseModel.STORE_PROTOCOL.equals(protocol))
        {
            Resource resource = reference.getResource();
            if (resource instanceof RepositoryResource)
            {
                RepositoryResource repositoryResource = (RepositoryResource) resource;
                RepositoryLocation location = repositoryResource.getLocation();
                if (location instanceof RepositoryNodeRef)
                {
                    RepositoryNodeRef repositoryNodeRef = (RepositoryNodeRef) location;
                    NodeRef nodeRef = repositoryNodeRef.getNodeRef();
                    NodeRef nodeRefPropagation = new NodeRef(mutatedNodeRef.getStoreRef(),
                                                             nodeRef.getId());
                    Resource resourcePropagation = new RepositoryResource(new RepositoryNodeRef(nodeRefPropagation));

                    return new Reference(reference.getEncoding(),
                                         reference.getProtocol(),
                                         resourcePropagation,
                                         reference.getParameters());
                }
            }
        }

        // default branch

        return reference;
    }

}
