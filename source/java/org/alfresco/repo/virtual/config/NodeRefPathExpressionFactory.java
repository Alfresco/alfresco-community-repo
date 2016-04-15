
package org.alfresco.repo.virtual.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Spring context {@link NodeRefPathExpression} factory bean.<br>
 * It creates {@link NodeRefPathExpression} instances configured with the
 * spring context defined {@link NodeRefResolver} and the given set of spring
 * configured {@link NodeRefContext}s.
 */
public class NodeRefPathExpressionFactory 
{
    private Map<String, NodeRefContext> contextsMap = Collections.emptyMap();

    private NodeRefResolver resolver;

    public void setResolver(NodeRefResolver resolver)
    {
        this.resolver = resolver;
    }

    public void setNodeRefContexts(Set<NodeRefContext> nodeRefContexts)
    {
        this.contextsMap = new HashMap<String, NodeRefContext>();

        for (NodeRefContext nodeRefContext : nodeRefContexts)
        {
            this.contextsMap.put(nodeRefContext.getContextName(),
                                 nodeRefContext);
        }
    }

    public NodeRefPathExpression createInstance()
    {
        return new NodeRefPathExpression(resolver,
                                           this.contextsMap);
    }

}
