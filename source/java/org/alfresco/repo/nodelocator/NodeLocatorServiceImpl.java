
package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the {@link NodeLocatorService} which is responsible for locating a 
 * {@link NodeRef} using a named lookup strategy.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class NodeLocatorServiceImpl implements NodeLocatorService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(NodeLocatorServiceImpl.class);
    
    private final Map<String, NodeLocator> locators = new HashMap<String, NodeLocator>();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef getNode(String locatorName, NodeRef source, Map<String, Serializable> params)
    {
        NodeLocator locator = locators.get(locatorName);
        
        if (locator == null)
        {
            String msg = "No NodeLocator is registered with name: " + locatorName;
            throw new IllegalArgumentException(msg);
        }
        
        NodeRef node = locator.getNode(source, params);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Node locator named '" + locatorName + "' found node: " + node);
        }
        
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(String locatorName, NodeLocator locator)
    {
        ParameterCheck.mandatory("locatorName", locatorName);
        ParameterCheck.mandatory("locator", locator);
        
        if (locators.containsKey(locatorName))
        {
            String msg = "Locator with name '" + locatorName + "' is already registered!";
            throw new IllegalArgumentException(msg);
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered node locator: " + locatorName);
        }
        
        locators.put(locatorName, locator);
    }

}
