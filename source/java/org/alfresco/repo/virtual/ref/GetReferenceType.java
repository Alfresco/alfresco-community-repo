
package org.alfresco.repo.virtual.ref;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Returns actual {@link QName} Type of a given reference <br>
 * indicated by the given protocol reference.
 */
public class GetReferenceType extends AbstractProtocolMethod<QName>
{
    private ActualEnvironment environment;

    public GetReferenceType(ActualEnvironment environment)
    {
        super();
        this.environment = environment;
    }

    @Override
    public QName execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        NodeRef nodeRef = protocol.getNodeRef(reference);

        return environment.getType(nodeRef);
    }

    @Override
    public QName execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        return ContentModel.TYPE_FOLDER;
    }

}
