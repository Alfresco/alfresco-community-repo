package org.alfresco.repo.node.index;

import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.repo.admin.RepositoryState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

public class IndexRecoveryBootstrapBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(IndexRecoveryBootstrapBean.class);

    IndexRecovery indexRecoveryComponent;
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // reindex

        log.info("Checking/Recovering indexes ...");
        indexRecoveryComponent.reindex();

    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

    public IndexRecovery getIndexRecoveryComponent()
    {
        return indexRecoveryComponent;
    }

    public void setIndexRecoveryComponent(IndexRecovery indexRecoveryComponent)
    {
        this.indexRecoveryComponent = indexRecoveryComponent;
    }
}
