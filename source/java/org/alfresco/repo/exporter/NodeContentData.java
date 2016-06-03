package org.alfresco.repo.exporter;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * ContentData with associated NodeRef
 */
public class NodeContentData extends ContentData
{
    private static final long serialVersionUID = -455291250695108108L;
    private NodeRef nodeRef;
    
    /**
     * Construct
     */
    public NodeContentData(NodeRef nodeRef, ContentData contentData)
    {
        super(contentData.getContentUrl(), contentData.getMimetype(), contentData.getSize(),
                contentData.getEncoding(), contentData.getLocale());
        this.nodeRef = nodeRef;
    }

    /**
     * @return  noderef
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
}
