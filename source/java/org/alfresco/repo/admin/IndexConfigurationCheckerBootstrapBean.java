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

                log.info("Checking/Recovering indexes ...");
                check();

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
