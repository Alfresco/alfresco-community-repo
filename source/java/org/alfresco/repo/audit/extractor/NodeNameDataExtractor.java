package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.PropertyCheck;

/**
 * An extractor that pulls out the {@link ContentModel#PROP_NAME <b>cm:name</b>} property from a node.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class NodeNameDataExtractor extends AbstractDataExtractor
{
    private NodeService nodeService;
    
    /**
     * Set the service to get the property from
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "nodeService", nodeService);
    }

    /**
     * @return          Returns <tt>true</tt> if the data is a {@link NodeRef}
     */
    public boolean isSupported(Serializable data)
    {
        return (data != null && data instanceof NodeRef);
    }

    /**
     * Gets the <b>cm:name</b> property from the node
     */
    public Serializable extractData(Serializable in) throws Throwable
    {
        NodeRef nodeRef = (NodeRef) in;
        String nodeName = null;
        if (!nodeService.exists(nodeRef))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Extractor can't pull value from non-existent node: " + nodeRef);
            }
        }
        else
        {
            nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        }
        return nodeName;
    }
}
