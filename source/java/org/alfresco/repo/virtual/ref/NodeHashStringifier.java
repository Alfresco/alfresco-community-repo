
package org.alfresco.repo.virtual.ref;

import java.util.List;
/**
 * Custom stringifier for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link NodeProtocol}.
 */
public class NodeHashStringifier extends ProtocolHashStringifier
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public NodeHashStringifier(HashStore classpathHashStore, Stringifier referenceDispatcher)
    {
        super(classpathHashStore,
              referenceDispatcher);
    }

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        final Protocol protocol = reference.getProtocol();
        if (!Protocols.NODE.protocol.equals(protocol))
        {
            throw new ReferenceEncodingException("Unsupported protocol " + protocol);
        }

        Resource resource = reference.getResource();

        String resourceString = stringify(resource);

        List<Parameter> parameters = reference.getParameters();

        ReferenceParameter referenceParameter = (ReferenceParameter) parameters.get(0);
        
        Reference parentReference = referenceParameter.getValue();
        
        String parametersString = dispatchStringifyReference(parentReference);

        return NODE_PROTOCOL_CODE + "-" + resourceString + "-" + parametersString;

    }

  
}
