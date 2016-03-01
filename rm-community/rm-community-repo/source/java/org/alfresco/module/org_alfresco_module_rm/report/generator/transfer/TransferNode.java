 
package org.alfresco.module.org_alfresco_module_rm.report.generator.transfer;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Transfer node class
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class TransferNode
{
    /** Transfer node reference */
    private NodeRef nodeRef;

    /** Transfer node properties */
    private Map<String, Serializable> properties;

    /**
     * @param nodeRef
     * @param properties
     */
    public TransferNode(NodeRef nodeRef, Map<String, Serializable> properties)
    {
        this.nodeRef = nodeRef;
        this.properties = properties;
    }

    /**
     * @return transfer node reference
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    /**
     * @return transfer node properties
     */
    public Map<String, Serializable> getProperties()
    {
        return this.properties;
    }
}
