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
package org.alfresco.repo.admin;

import java.io.File;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

public class IndexConfigurationCheckerBootstrapBean extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(IndexConfigurationCheckerBootstrapBean.class);
    
    private IndexConfigurationChecker indexConfigurationChecker;
    
    private RepositoryState repositoryState;
    
    private TransactionService transactionService;

    private boolean strict;

    private String dirRoot;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        RetryingTransactionCallback<Object> checkWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
             // reindex
                if((repositoryState == null) || (false == repositoryState.isBootstrapping()))
                {
                    log.info("Checking/Recovering indexes ...");
                    check();
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(checkWork, true);
        
        
        
    }

    private void check()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting index configuration check: " + this);
        }
        
      
        File dirRootFile = new File(dirRoot);

        
        List<StoreRef> missingIndexStoreRefs = indexConfigurationChecker.checkIndexConfiguration();
            
        // check for missing indexes
        int missingStoreIndexes = missingIndexStoreRefs.size();
        if (missingStoreIndexes > 0)
        {
            String msg = I18NUtil.getMessage(ConfigurationChecker.ERR_MISSING_INDEXES, missingStoreIndexes);
            logger.error(msg);
            String msgRecover = I18NUtil.getMessage(ConfigurationChecker.MSG_HOWTO_INDEX_RECOVER);
            logger.info(msgRecover);
        }
        
        // handle either content or indexes missing
        if (missingStoreIndexes > 0)
        {
            String msg = I18NUtil.getMessage(ConfigurationChecker.ERR_FIX_DIR_ROOT, dirRootFile);
            logger.error(msg);
            
            // Now determine the failure behaviour
            if (strict)
            {
                throw new AlfrescoRuntimeException(msg);
            }
            else
            {
                String warn = I18NUtil.getMessage(ConfigurationChecker.WARN_STARTING_WITH_ERRORS);
                logger.warn(warn);
            }
        }
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

   

    public IndexConfigurationChecker getIndexConfigurationChecker()
    {
        return indexConfigurationChecker;
    }

    public void setIndexConfigurationChecker(IndexConfigurationChecker indexConfigurationChecker)
    {
        this.indexConfigurationChecker = indexConfigurationChecker;
    }

    public RepositoryState getRepositoryState()
    {
        return repositoryState;
    }

    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }

    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }
    
    public void setDirRoot(String dirRoot)
    {
        this.dirRoot = dirRoot;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    
}
