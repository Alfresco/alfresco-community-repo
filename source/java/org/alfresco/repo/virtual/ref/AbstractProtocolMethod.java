
package org.alfresco.repo.virtual.ref;

/**
 * Handles protocol method dispatch. Not supported: base protocol
 * dispatches. The vanilla protocol can be handled by default
 * as the virtual protocol.
 *
 * @param <R>
 */
public abstract class AbstractProtocolMethod<R> implements ProtocolMethod<R>
{
    public static final String PATH_SEPARATOR = "/";

    @Override
    public R execute(VanillaProtocol vanillaProtocol, Reference reference) throws ProtocolMethodException
    {
        // cast to force virtual protocol execution

        return execute((VirtualProtocol) vanillaProtocol,
                       reference);
    }

    @Override
    public R execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        return throwUnsupportedProtocolException(virtualProtocol);
    }

    @Override
    public R execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        return throwUnsupportedProtocolException(protocol);
    }

    private R throwUnsupportedProtocolException(Protocol protocol) throws ProtocolMethodException
    {
        throw new ProtocolMethodException("Unsupported protocol " + protocol);
    }

    @Override
    public R execute(Protocol protocol, Reference reference) throws ProtocolMethodException
    {
        return throwUnsupportedProtocolException(protocol);
    }
}
