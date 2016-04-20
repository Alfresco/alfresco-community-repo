
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
