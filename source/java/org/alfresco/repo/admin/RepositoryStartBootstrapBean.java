package org.alfresco.repo.admin;

import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Track repo bootstrap so sub systems do not duplciate stuff or do it too early ... eg index rebuild/check
 *  
 * @author andyh
 */
public class RepositoryStartBootstrapBean extends AbstractLifecycleBean
{
    private RepositoryState repositoryState; 

    public RepositoryState getRepositoryState()
    {
        return repositoryState;
    }

    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        repositoryState.setBootstrapping(true);
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}