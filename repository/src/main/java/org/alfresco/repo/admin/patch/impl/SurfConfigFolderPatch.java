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

package org.alfresco.repo.admin.patch.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AsynchronousPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Patch to add <i>cm:indexControl</i> aspect to sites' surf-config folders and
 * their children as well as to the shared surf-config folder(s) and its/their children.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SurfConfigFolderPatch extends AsynchronousPatch
{
    private static final Log logger = LogFactory.getLog(SurfConfigFolderPatch.class);
    private static final Log progress_logger = LogFactory.getLog(PatchExecuter.class);

    private static final String MSG_SUCCESS = "patch.surfConfigFolderPatch.result";

    // name of the surf config folder
    private static final String SURF_CONFIG = "surf-config";
    private static final String COMPONENTS = "components"; // cm:surf-config/cm:components
    private static final String PAGES = "pages"; // cm:surf-config/cm:pages
    private static final String SITE = "site"; // cm:surf-config/cm:pages/cm:site

    private static final int SITE_BATCH_THREADS = 2;
    private static final int SHARED_SURF_CONFIG_BATCH_THREADS = 2;
    private static final int BATCH_SIZE = 1000;
    private static final int SHARED_SURF_CONFIG_BATCH_MAX_QUERY_RANGE = 1000;
    private static final int SITE_BATCH_MAX_QUERY_RANGE = 1000;

    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private BehaviourFilter behaviourFilter;
    private RuleService ruleService;

    /**
     * @param patchDAO the patchDAO to set
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param nodeDAO the nodeDAO to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param qnameDAO the qnameDAO to set
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param ruleService the ruleService to set
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(ruleService, "ruleService");
        checkPropertyNotNull(behaviourFilter, "behaviourFilter");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        long start = System.currentTimeMillis();

        // get user names that will be used within RunAs
        final String systemUser = AuthenticationUtil.getSystemUserName();

        // Instance to provide raw data to process
        BatchProcessWorkProvider<Long> siteWorkProvider = new SiteWorkProvider();

        // Instance to handle each item of work
        BatchProcessWorker<Long> siteWorker = new BatchProcessWorkerAdaptor<Long>()
        {
            @Override
            public void beforeProcess() throws Throwable
            {
                // Run as the systemuser
                AuthenticationUtil.setRunAsUser(systemUser);
            }

            @Override
            public void process(Long entry) throws Throwable
            {
                // Disable auditable aspect
                behaviourFilter.disableBehaviour();
                // Disable rules
                ruleService.disableRules();
                try
                {
                    SurfConfigFolderPatch.this.process(entry);
                }
                finally
                {
                    ruleService.enableRules();
                    behaviourFilter.enableBehaviour();
                }
            }

            @Override
            public void afterProcess() throws Throwable
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        };

        BatchProcessor<Long> siteBatchProcessor = new BatchProcessor<Long>("SurfConfigFolderPatch",
                    transactionService.getRetryingTransactionHelper(), siteWorkProvider, SITE_BATCH_THREADS, BATCH_SIZE, null,
                    progress_logger, 1000);

        int updatedSiteSurfConfig = siteBatchProcessor.process(siteWorker, true);

        // shared surf-config folder
        // Instance to provide raw data to process
        BatchProcessWorkProvider<NodeRef> surfConfigWorkProvider = new SharedSurfConfigWorkProvider();

        // Instance to handle each item of work
        BatchProcessWorker<NodeRef> surfConfigWorker = new BatchProcessWorkerAdaptor<NodeRef>()
        {
            @Override
            public void beforeProcess() throws Throwable
            {
                // Run as the systemuser
                AuthenticationUtil.setRunAsUser(systemUser);
            }

            @Override
            public void process(NodeRef entry) throws Throwable
            {
                // Disable auditable aspect
                behaviourFilter.disableBehaviour();
                // Disable rules
                ruleService.disableRules();
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\tP: Processing [company_home/sites/surf-config/pages/user] children");
                    }
                    // add aspect to
                    // app:company_home/st:sites/cm:surf-config/cm:pages/cm:user/{userName}
                    addIndexControlAspectIfNotExist(entry);
                }
                finally
                {
                    ruleService.enableRules();
                    behaviourFilter.enableBehaviour();
                }
            }

            @Override
            public void afterProcess() throws Throwable
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        };

        BatchProcessor<NodeRef> surfConfigBatchProcessor = new BatchProcessor<NodeRef>("SurfConfigFolderPatch",
                    transactionService.getRetryingTransactionHelper(), surfConfigWorkProvider, SHARED_SURF_CONFIG_BATCH_THREADS,
                    BATCH_SIZE, null, progress_logger, 1000);

        surfConfigBatchProcessor.process(surfConfigWorker, true);

        int numOfSites = updatedSiteSurfConfig / 12;
        String msg = I18NUtil.getMessage(MSG_SUCCESS, numOfSites);

        long end = System.currentTimeMillis();
        logger.info(msg + " in [" + (end - start) + " ms]");
        return msg;
    }

    private void process(long siteId)
    {
        String siteName = (String) nodeDAO.getNodeProperty(siteId, ContentModel.PROP_NAME);

        if (logger.isDebugEnabled())
        {
            logger.debug("\tP: Processing surf-config folder for the site: [" + siteName + ']');
        }

        // {siteName}/cm:surf-config/
        Pair<Long, ChildAssociationRef> surfConfigPair = nodeDAO.getChildAssoc(siteId, ContentModel.ASSOC_CONTAINS, SURF_CONFIG);

        if (surfConfigPair == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to find surf-config folder for site: [" + siteName + ']');
            }
            return;
        }
        NodeRef surfConfigNodeRef = surfConfigPair.getSecond().getChildRef();
        // apply the aspect to suef-config folder
        addIndexControlAspectIfNotExist(surfConfigNodeRef);

        // cm:surf-config/cm:components
        NodeRef componentsNodeRef = nodeService.getChildByName(surfConfigNodeRef, ContentModel.ASSOC_CONTAINS, COMPONENTS);
        if (componentsNodeRef != null)
        {
            // {siteName}/cm:surf-config/cm:components nodeRef
            addIndexControlAspectIfNotExist(componentsNodeRef);

            List<ChildAssociationRef> listOfComponents = nodeService.getChildAssocs(componentsNodeRef,
                        ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            // apply the aspect to all of the children (6 in total)
            for (ChildAssociationRef comp : listOfComponents)
            {
                addIndexControlAspectIfNotExist(comp.getChildRef());
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to find surf-config/components folder for site: [" + siteName + ']');
            }
        }

        // cm:surf-config/cm:pages folder
        NodeRef pagesNodeRef = nodeService.getChildByName(surfConfigNodeRef, ContentModel.ASSOC_CONTAINS, PAGES);
        if (pagesNodeRef == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to find surf-config/pages folder for site: [" + siteName + ']');
            }
            return;
        }
        // add aspect to cm:pages
        addIndexControlAspectIfNotExist(pagesNodeRef);

        // cm:surf-config/cm:pages/cm:site folder
        NodeRef siteNodeRef = nodeService.getChildByName(pagesNodeRef, ContentModel.ASSOC_CONTAINS, SITE);
        if (siteNodeRef == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to find surf-config/pages/site folder for site: [" + siteName + ']');
            }
            return;
        }
        // add aspect to cm:pages/cm:site folder
        addIndexControlAspectIfNotExist(siteNodeRef);

        // cm:surf-config/cm:pages/cm:site/{siteName}
        NodeRef siteChildNodeRef = nodeService.getChildByName(siteNodeRef, ContentModel.ASSOC_CONTAINS, siteName);

        if (siteChildNodeRef == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to find surf-config/pages/site/" + siteName + " folder for site: [" + siteName + ']');
            }
            return;
        }
        // add aspect to cm:surf-config/cm:pages/cm:site/{siteName}
        addIndexControlAspectIfNotExist(siteChildNodeRef);

        List<ChildAssociationRef> listOfComponents = nodeService.getChildAssocs(siteChildNodeRef, ContentModel.ASSOC_CONTAINS,
                    RegexQNamePattern.MATCH_ALL);
        // apply the aspect to all of the children
        for (ChildAssociationRef comp : listOfComponents)
        {
            addIndexControlAspectIfNotExist(comp.getChildRef());
        }
    }

    private void addIndexControlAspectIfNotExist(NodeRef nodeRef)
    {
        // We need to check the property rather than the aspect, as the node
        // might have the aspect but not the correct property.
        Serializable indexProp = nodeService.getProperty(nodeRef, ContentModel.PROP_IS_INDEXED);
        if (indexProp == null || ((Boolean) indexProp))
        {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL,
                        Collections.singletonMap(ContentModel.PROP_IS_INDEXED, (Serializable) false));

            if (logger.isDebugEnabled())
            {
                logger.debug("\tP: Adding cm:indexControl aspect to node: [" + nodeRef + ']');
            }
        }
    }

    /**
     * Work provider which performs incremental queries to find site nodes.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    private class SiteWorkProvider implements BatchProcessWorkProvider<Long>
    {
        private long maxId = Long.MAX_VALUE;
        private long workCount = Long.MAX_VALUE;
        private long currentId = 0L;
        private final Pair<Long, QName> siteTypeQNameId;

        private SiteWorkProvider()
        {
            this.siteTypeQNameId = qnameDAO.getQName(SiteModel.TYPE_SITE);
        }

        @Override public synchronized int getTotalEstimatedWorkSize()
        {
            return (int) getTotalEstimatedWorkSizeLong();
        }

        @Override public synchronized long getTotalEstimatedWorkSizeLong()
        {
            if (maxId == Long.MAX_VALUE)
            {
                maxId = patchDAO.getMaxAdmNodeID();
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tQ: Max node id: " + maxId);
                }
            }
            if (workCount == Long.MAX_VALUE)
            {
                // get the sites count
                workCount = patchDAO.getCountNodesWithTypId(SiteModel.TYPE_SITE);
                // Each site has 12 children (we care only about surf-config
                // itself and its children)
                workCount *= 12;
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tQ: Work count: " + workCount);
                }
            }
            return workCount;
        }

        @Override
        public synchronized Collection<Long> getNextWork()
        {
            // Record the site node IDs
            final List<Long> siteNodeIDs = new ArrayList<Long>(SITE_BATCH_MAX_QUERY_RANGE);
            // ACE-2981
            if (siteTypeQNameId == null)
            {
                return siteNodeIDs;
            }
            // Keep querying until we have enough results to give back
            int minResults = SITE_BATCH_MAX_QUERY_RANGE / 2;
            while (currentId <= maxId && siteNodeIDs.size() < minResults)
            {
                List<Long> nodeIds = patchDAO.getNodesByTypeQNameId(siteTypeQNameId.getFirst(), currentId, currentId + SITE_BATCH_MAX_QUERY_RANGE);
                siteNodeIDs.addAll(nodeIds);
                // Increment the minimum ID
                currentId += SITE_BATCH_MAX_QUERY_RANGE;
            }
            // Done
            return siteNodeIDs;
        }
    }

    /**
     * Work provider which performs incremental queries to find shared
     * Surf-Config folders and their children.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    private class SharedSurfConfigWorkProvider implements BatchProcessWorkProvider<NodeRef>
    {
        private long maxId = Long.MAX_VALUE;
        private long currentId = 0L;

        private SharedSurfConfigWorkProvider()
        {
        }

        @Override
        public synchronized int getTotalEstimatedWorkSize()
        {
            return (int)getTotalEstimatedWorkSizeLong();
        }

        @Override
        public synchronized long getTotalEstimatedWorkSizeLong()
        {
            if (maxId == Long.MAX_VALUE)
            {
                maxId = patchDAO.getMaxAdmNodeID();
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tQ: Max node id: " + maxId);
                }
            }
            return 0;

        }

        @Override
        public synchronized Collection<NodeRef> getNextWork()
        {
            // Record the user folder node IDs
            final List<NodeRef> folderNodes = new ArrayList<NodeRef>(SHARED_SURF_CONFIG_BATCH_MAX_QUERY_RANGE);

            // Keep querying until we have enough results to give back
            int minResults = SHARED_SURF_CONFIG_BATCH_MAX_QUERY_RANGE / 2;
            while (currentId <= maxId && folderNodes.size() < minResults)
            {

                List<NodeRef> nodeIds = patchDAO.getChildrenOfTheSharedSurfConfigFolder(currentId, currentId + SHARED_SURF_CONFIG_BATCH_MAX_QUERY_RANGE);
                folderNodes.addAll(nodeIds);
                // Increment the minimum ID
                currentId += SHARED_SURF_CONFIG_BATCH_MAX_QUERY_RANGE;
            }
            // Preload the nodes for quicker access
            nodeDAO.cacheNodes(folderNodes);
            // Done
            return folderNodes;
        }
    }
}
