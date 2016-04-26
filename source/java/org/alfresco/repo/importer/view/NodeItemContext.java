package org.alfresco.repo.importer.view;

import org.alfresco.service.namespace.QName;


/**
 * Represents Property Context
 * 
 * @author David Caruana
 *
 */
public class NodeItemContext extends ElementContext
{
    private NodeContext nodeContext;
    
    /**
     * Construct
     * 
     * @param elementName QName
     * @param nodeContext NodeContext
     */
    public NodeItemContext(QName elementName, NodeContext nodeContext)
    {
        super(elementName, nodeContext.getDictionaryService(), nodeContext.getImporter());
        this.nodeContext = nodeContext;
    }
    
    /**
     * Gets the Node Context
     */
    public NodeContext getNodeContext()
    {
        return nodeContext;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "NodeItemContext[nodeContext=" + nodeContext.toString() + "]";
    }
 

}
