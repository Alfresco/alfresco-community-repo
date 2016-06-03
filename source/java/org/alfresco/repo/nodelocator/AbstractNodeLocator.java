
package org.alfresco.repo.nodelocator;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.action.ParameterDefinition;

/**
 * Base class for all {@link NodeLocator} implementations. 
 * <p>Extending this class with automatically register the node locator with the NodeLocatorService.</p>
 * 
 * @author Nick Smith
 * @since 4.0
 */
public abstract class AbstractNodeLocator implements NodeLocator
{
    public void setNodeLocatorService(NodeLocatorService nodeLocatorService)
    {
        nodeLocatorService.register(getName(), this);
    }
    
    /**
    * {@inheritDoc}
    */
    public List<ParameterDefinition> getParameterDefinitions()
    {
        return Collections.emptyList();
    }
    
    public abstract String getName();
}
