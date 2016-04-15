
package org.alfresco.repo.virtual.ref;

import java.util.HashMap;
import java.util.Map;

/**
 * Common {@link Reference} protocols.
 */
public enum Protocols
{
    NODE(new NodeProtocol()), VIRTUAL(new VirtualProtocol()), VANILLA(new VanillaProtocol());

    private static volatile Map<String, Protocol> protocolsMap;

    public static synchronized Protocol fromName(String name)
    {
        return protocolsMap.get(name);
    }

    private synchronized static void register(Protocol protocol)
    {
        if (protocolsMap == null)
        {
            protocolsMap = new HashMap<>();
        }
        protocolsMap.put(protocol.name,
                         protocol);
    }

    public final Protocol protocol;

    Protocols(Protocol protocol)
    {
        this.protocol = protocol;
        register(protocol);
    }
}
