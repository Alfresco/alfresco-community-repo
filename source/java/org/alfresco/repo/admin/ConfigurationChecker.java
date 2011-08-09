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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptorImpl;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Component to perform a bootstrap check of the alignment of the
 * database, Lucene indexes and content store.
 * <p>
 * The algorithm is:
 * <ul>
 *   <li>Get all stores from the NodeService</li>
 *   <li>Get each root node</li>
 *   <li>Perform a Lucene query for each root node</li>
 *   <li>Query Lucene for a small set of content nodes</li>
 *   <li>Get content readers for each node</li>
 * </ul>
 * If any of the steps fail then the bootstrap bean will fail, except if
 * the indexes are marked for full recovery.  In this case, the Lucene
 * checks are not required as the indexes will be due for a rebuild.  
 * 
 * @author Derek Hulley
 */
public class ConfigurationChecker extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(ConfigurationChecker.class);
    
    private static final String WARN_RELATIVE_DIR_ROOT = "system.config_check.warn.dir_root";
    private static final String MSG_DIR_ROOT = "system.config_check.msg.dir_root";
    static final String ERR_MISSING_INDEXES = "system.config_check.err.missing_index";
    private static final String ERR_MISSING_CONTENT = "system.config_check.err.missing_content";
    static final String ERR_FIX_DIR_ROOT = "system.config_check.err.fix_dir_root";
    static final String MSG_HOWTO_INDEX_RECOVER = "system.config_check.msg.howto_index_recover";
    static final String WARN_STARTING_WITH_ERRORS = "system.config_check.warn.starting_with_errors";

    private boolean strict;
    private RecoveryMode indexRecoveryMode;
    private String dirRoot;

    private ImporterBootstrap systemBootstrap;
    private TransactionService transactionService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private IndexConfigurationChecker indexConfigurationChecker;
    
    public ConfigurationChecker()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("ConfigurationChecker")
          .append("[indexRecoveryMode=").append(indexRecoveryMode)
          .append("]");
        return sb.toString();
    }

    /**
     * This flag controls the behaviour of the component in the event of problems being found.
     * Generally, the system should be <b>strict</b>, but this can be changed if indexes are
     * going to be recovered, or if missing content is acceptable.
     * 
     * @param strict <code>true</code> to prevent system startup if problems are found, otherwise
     *      <code>false</code> to allow the system to startup regardless.
     */
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

    /**
     * Set the index recovery mode.  If this is
     * {@link org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode#VALIDATE FULL}
     * then the index checks are ignored as the indexes will be scheduled for a rebuild
     * anyway.
     * 
     * @see org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode
     */
    public void setIndexRecoveryMode(String indexRecoveryMode)
    {
        this.indexRecoveryMode = RecoveryMode.valueOf(indexRecoveryMode);
    }

    public void setDirRoot(String dirRoot)
    {
        this.dirRoot = dirRoot;
    }

    public void setSystemBootstrap(ImporterBootstrap systemBootstrap)
    {
        this.systemBootstrap = systemBootstrap;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setIndexConfigurationChecker(IndexConfigurationChecker indexConfigurationChecker)
    {
        this.indexConfigurationChecker = indexConfigurationChecker;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        RetryingTransactionCallback<Object> checkWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                check();
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(checkWork, true);
    }
    
    /**
     * Performs the check work.
     */
    private void check()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting bootstrap configuration check: " + this);
        }
        
        // check the dir.root
        boolean isRelativeRoot = dirRoot.startsWith(".");
        if (isRelativeRoot)
        {
            String msg = I18NUtil.getMessage(WARN_RELATIVE_DIR_ROOT, dirRoot);
            logger.warn(msg);
        }
        File dirRootFile = new File(dirRoot);
        String msgDirRoot = I18NUtil.getMessage(MSG_DIR_ROOT, dirRootFile);
        logger.info(msgDirRoot);

        List<StoreRef> missingIndexStoreRefs = indexConfigurationChecker.checkIndexConfiguration();
        // check for the system version properties content snippet
        boolean versionPropertiesContentAvailable = true;
        NodeRef descriptorNodeRef = getSystemDescriptor();
        if (descriptorNodeRef != null)
        {
            // get the version properties
            ContentReader reader = contentService.getReader(
                    descriptorNodeRef,
                    ContentModel.PROP_SYS_VERSION_PROPERTIES);
            if (reader != null && !reader.exists())
            {
                // the property is there, but the content is not
                versionPropertiesContentAvailable = false;
            }
        }
            
        // check for missing indexes
        int missingStoreIndexes = missingIndexStoreRefs.size();
        if (missingStoreIndexes > 0)
        {
            String msg = I18NUtil.getMessage(ERR_MISSING_INDEXES, missingStoreIndexes);
            logger.error(msg);
            String msgRecover = I18NUtil.getMessage(MSG_HOWTO_INDEX_RECOVER);
            logger.info(msgRecover);
        }
        // check for missing content
        if (!versionPropertiesContentAvailable)
        {
            String msg = I18NUtil.getMessage(ERR_MISSING_CONTENT);
            logger.error(msg);
        }
        // handle either content or indexes missing
        if (missingStoreIndexes > 0 || !versionPropertiesContentAvailable)
        {
            String msg = I18NUtil.getMessage(ERR_FIX_DIR_ROOT, dirRootFile);
            logger.error(msg);
            
            // Now determine the failure behaviour
            if (strict)
            {
                throw new AlfrescoRuntimeException(msg);
            }
            else
            {
                String warn = I18NUtil.getMessage(WARN_STARTING_WITH_ERRORS);
                logger.warn(warn);
            }
        }
    }

    
    
    /**
     * @return Returns the system descriptor node or null
     */
    public NodeRef getSystemDescriptor()
    {
        StoreRef systemStoreRef = systemBootstrap.getStoreRef();
        List<NodeRef> nodeRefs = null;
        if (nodeService.exists(systemStoreRef))
        {
            Properties systemProperties = systemBootstrap.getConfiguration();
            String path = systemProperties.getProperty("system.descriptor.current.childname");
            String searchPath = "/" + path;
            NodeRef rootNodeRef = nodeService.getRootNode(systemStoreRef);
            nodeRefs = searchService.selectNodes(rootNodeRef, searchPath, null, namespaceService, false);
            if (nodeRefs.size() > 0)
            {
                NodeRef descriptorNodeRef = nodeRefs.get(0);
                return descriptorNodeRef;
            }
        }
        return null;
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing here
    }
}
