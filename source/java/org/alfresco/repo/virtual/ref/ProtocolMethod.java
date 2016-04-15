
package org.alfresco.repo.virtual.ref;

/**
 * A double-dispatched method abstraction.<br>
 * The method execution is dispatched based on concrete protocol type.<br>
 * Implementor encapsulate high-level reference protocol based semantic
 * functionality.
 * 
 * @param <R> method return type
 * @see Reference#execute(ProtocolMethod)
 * @author Bogdan Horje
 */
public interface ProtocolMethod<R>
{
    R execute(VanillaProtocol vanillaProtocol, Reference reference) throws ProtocolMethodException;

    R execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException;

    R execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException;

    R execute(Protocol protocol, Reference reference) throws ProtocolMethodException;
}
