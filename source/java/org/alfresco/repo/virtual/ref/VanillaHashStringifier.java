
package org.alfresco.repo.virtual.ref;
/**
 * Custom stringifier for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link VanillaProtocol}.
 */
public class VanillaHashStringifier extends VirtualHashStringifier
{
    private static final long serialVersionUID = -2087786593789426927L;

    public VanillaHashStringifier(HashStore classpathHashStore, Stringifier referenceDispatcher)
    {
        super(classpathHashStore,
              referenceDispatcher);
    }

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        final Protocol protocol = reference.getProtocol();
        if (!Protocols.VANILLA.protocol.equals(protocol))
        {
            throw new ReferenceEncodingException("Unsupported protocol " + protocol + "."
                        + Protocols.VIRTUAL.protocol.name + " exoected ");
        }

        String virtualString = stringifyVirtualReference(reference);

        ResourceParameter vanillaTemplateParam = (ResourceParameter) reference
                    .getParameters()
                        .get(VanillaProtocol.VANILLA_TEMPLATE_PARAM_INDEX);

        String vanillaString = stringify(vanillaTemplateParam.getValue());

        return VANILLA_PROTOCOL_CODE + "-" + virtualString + "-" + vanillaString;
    }
}
