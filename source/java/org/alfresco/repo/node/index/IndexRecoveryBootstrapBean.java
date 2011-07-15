/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
    
    RepositoryState repositoryState;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // reindex
        if((repositoryState == null) || (false == repositoryState.isBootstrapping()))
        {
            log.info("Checking/Recovering indexes ...");
            indexRecoveryComponent.reindex();
        }
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

    public RepositoryState getRepositoryState()
    {
        return repositoryState;
    }

    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }

    
}
