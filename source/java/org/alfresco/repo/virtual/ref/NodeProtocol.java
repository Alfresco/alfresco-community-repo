
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
