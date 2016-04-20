package org.alfresco.repo.node.index;

import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Passes index information to the index services.
 * 
 * @author Derek Hulley
 */
public class NodeIndexer extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(NodeIndexer.class);
    
    /** the component to index the node hierarchy */
    private Indexer indexer;
    /** ready to use or not */
    private volatile boolean started;
    /** enabled or disabled */
    private boolean enabled;
    
    public NodeIndexer()
    {
        started = false;
        enabled = true;
    }
    
    /**
     * Explicit property to disable in-transaction indexing.
     * 
     * @param disabled      <tt>true</tt> to index nothing in-line
     */
    public void setDisabled(boolean disabled)
    {
        this.enabled = !disabled;
    }

    /* package */ void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Dig the indexer out of the context
        indexer = (Indexer) super.getApplicationContext().getBean("indexerComponent");
        started = true;
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        started = false;
    }
    
    public void indexDeleteStore(StoreRef storeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("indexDeleteNode", new Exception("Stack Trace"));
        }
        indexer.deleteIndex(storeRef);
    }

    public void indexCreateNode(ChildAssociationRef childAssocRef)
    {
        if (enabled && started)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexCreateNode: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.createNode(childAssocRef);
        }
    }

    public void indexUpdateNode(NodeRef nodeRef)
    {
        if (enabled && started)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexUpdateNode: " + nodeRef, new Exception("Stack Trace"));
            }
            indexer.updateNode(nodeRef);
        }
    }

    public void indexDeleteNode(ChildAssociationRef childAssocRef)
    {
        if (enabled && started)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexDeleteNode: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.deleteNode(childAssocRef);
        }
    }

    public void indexCreateChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (enabled && started)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexCreateChildAssociation: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.createChildRelationship(childAssocRef);
        }
    }

    public void indexDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (enabled && started)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexDeleteChildAssociation: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.deleteChildRelationship(childAssocRef);
        } 
    }
    
    public void indexUpdateChildAssociation(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (enabled && started)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexUpdateChildAssociation: " + oldChildAssocRef + " -> " + newChildAssocRef, new Exception("Stack Trace"));
            }
            indexer.updateChildRelationship(oldChildAssocRef, newChildAssocRef);
        }
    }
}
