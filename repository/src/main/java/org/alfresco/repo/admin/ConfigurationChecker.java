/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.admin;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Component to perform a bootstrap check of the alignment of the database and content store.
 * <p>
 * The algorithm is:
 * <ul>
 * <li>Checks that an absolute path is used</li>
 * <li>Ensures that the system descriptor content is available (created at bootstrap)</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class ConfigurationChecker extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(ConfigurationChecker.class);

    private static final String WARN_RELATIVE_DIR_ROOT = "system.config_check.warn.dir_root";
    private static final String MSG_DIR_ROOT = "system.config_check.msg.dir_root";
    private static final String ERR_MISSING_CONTENT = "system.config_check.err.missing_content";
    static final String ERR_FIX_DIR_ROOT = "system.config_check.err.fix_dir_root";
    static final String WARN_STARTING_WITH_ERRORS = "system.config_check.warn.starting_with_errors";

    private boolean strict;
    private String dirRoot;

    private ImporterBootstrap systemBootstrap;
    private TransactionService transactionService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;

    public ConfigurationChecker()
    {}

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("ConfigurationChecker");
        return sb.toString();
    }

    /**
     * This flag controls the behaviour of the component in the event of problems being found. Generally, the system should be <b>strict</b>, but this can be changed if indexes are going to be recovered, or if missing content is acceptable.
     * 
     * @param strict
     *            <code>true</code> to prevent system startup if problems are found, otherwise <code>false</code> to allow the system to startup regardless.
     */
    public void setStrict(boolean strict)
    {
        this.strict = strict;
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

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        RetryingTransactionCallback<Object> checkWork = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable
            {
                // run as System on bootstrap
                return AuthenticationUtil.runAs(new RunAsWork<Object>() {
                    public Object doWork()
                    {
                        check();
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
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
                String msg = I18NUtil.getMessage(ERR_MISSING_CONTENT, reader.getContentUrl());
                logger.error(msg);
            }
        }

        // handle content missing
        if (!versionPropertiesContentAvailable)
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
