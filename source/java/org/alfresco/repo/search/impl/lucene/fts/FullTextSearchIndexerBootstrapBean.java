package org.alfresco.repo.search.impl.lucene.fts;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

public class FullTextSearchIndexerBootstrapBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(FullTextSearchIndexerBootstrapBean.class);

    private FullTextSearchIndexer fullTextSearchIndexer;

    private NodeService nodeService;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        String majorVersion = I18NUtil.getMessage("version.major");
        String minorVersion = I18NUtil.getMessage("version.minor");
        
        // Internationalizes the message
        String errorMsg = I18NUtil.getMessage("system.err.lucene_not_supported", majorVersion + "." + minorVersion);
        log.error(errorMsg);

        List<StoreRef> storeRefs = nodeService.getStores();
        for (StoreRef storeRef : storeRefs)
        {
            fullTextSearchIndexer.requiresIndex(storeRef);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

    public FullTextSearchIndexer getFullTextSearchIndexer()
    {
        return fullTextSearchIndexer;
    }

    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer)
    {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    
    
}
