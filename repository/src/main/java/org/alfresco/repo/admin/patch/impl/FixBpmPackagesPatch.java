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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch that updates workflow package type and package items associations
 * 
 * @see <a href=https://issues.alfresco.com/jira/browse/ETHREEOH-3613>ETHREEOH-3613</a>
 * @see <a href=https://issues.alfresco.com/jira/browse/ALF-11499>ALF-11499</a>
 * @author Arseny Kovalchuk
 * @since 3.4.7
 */
public class FixBpmPackagesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixBpmPackages.result";

    private static final String ERR_MSG_INVALID_BOOTSTRAP_STORE = "patch.fixBpmPackages.invalidBootsrapStore";
    private static final String ERR_MSG_EMPTY_CONTAINER = "patch.fixBpmPackages.emptyContainer";

    private static final Log logger = LogFactory.getLog(FixBpmPackagesPatch.class);
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);

    private int batchThreads = 4;
    private int batchSize = 1000;

    private ImporterBootstrap importerBootstrap;
    
    private BehaviourFilter policyFilter;

    /**
     * @param batchThreads              the number of threads that will write child association changes
     */
    public void setBatchThreads(int batchThreads)
    {
        this.batchThreads = batchThreads;
    }

    /**
     * @param batchSize                 the number of child associations that will be modified per transaction
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setPolicyFilter(BehaviourFilter policyFilter) 
    {
        this.policyFilter = policyFilter;
    }

	@Override
    protected String applyInternal() throws Exception
    {

        FixBpmPackagesPatchHelper helper = new FixBpmPackagesPatchHelper();
        try
        {
            // disable auditable behavior. MNT-9538 fix
            policyFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
            
            StoreRef store = importerBootstrap.getStoreRef();
            if (store == null)
            {
                throw new PatchException(ERR_MSG_INVALID_BOOTSTRAP_STORE);
            }

            // Get root node for store
            NodeRef rootRef = nodeService.getRootNode(store);

            if (logger.isDebugEnabled())
                logger.debug("StoreRef:" + store + " RootNodeRef: " + rootRef);

            // Get /sys:system container path, if it doesn't exist there is something wrong with the repo
            String sysContainer = importerBootstrap.getConfiguration().getProperty("system.system_container.childname");
            QName sysContainerQName = QName.createQName(sysContainer, namespaceService);

            List<ChildAssociationRef> refs = nodeService.getChildAssocs(rootRef, ContentModel.ASSOC_CHILDREN, sysContainerQName);

            if (refs == null || refs.size() == 0)
                throw new PatchException(ERR_MSG_EMPTY_CONTAINER, sysContainer);

            NodeRef sysNodeRef = refs.get(0).getChildRef();

            // Get /sys:system/sys:workflow container, if it doesn't exist there is something wrong with the repo
            String sysWorkflowContainer = importerBootstrap.getConfiguration().getProperty("system.workflow_container.childname");
            QName sysWorkflowQName = QName.createQName(sysWorkflowContainer, namespaceService);

            refs = nodeService.getChildAssocs(sysNodeRef, ContentModel.ASSOC_CHILDREN, sysWorkflowQName);

            if (refs == null || refs.size() == 0)
                throw new PatchException(ERR_MSG_EMPTY_CONTAINER, sysWorkflowContainer);

            NodeRef workflowContainerRef = refs.get(0).getChildRef();

            // Try to get /sys:system/sys:workflow/cm:packages, if there is no such node, then it wasn't created yet,
            // so there is nothing to convert
            refs = nodeService.getChildAssocs(workflowContainerRef, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL);

            if (refs == null || refs.size() == 0)
            {
                if (logger.isDebugEnabled())
                    logger.debug("There are no any packages in the container " + sysWorkflowContainer);
                return I18NUtil.getMessage(MSG_SUCCESS, 0);
            }
            // Get /sys:system/sys:workflow/cm:packages container NodeRef
            NodeRef packagesContainerRef = refs.get(0).getChildRef();
            // Get workflow packages to be converted
            refs = nodeService.getChildAssocs(packagesContainerRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

            if (logger.isDebugEnabled())
                logger.debug("Found " + refs.size() + " packages to convert");

            return helper.fixBpmPackages(refs);
        }
        finally
        {
            // enable auditable behavior. MNT-9538 fix
            policyFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            
            helper.closeWriter();
        }
    }

    private class FixBpmPackagesPatchHelper
    {
        private File logFile;
        private FileChannel channel;
        private Integer assocCount;
        private int skipCount = 0;
        private List<ChildAssociationRef> refs;

        private FixBpmPackagesPatchHelper() throws IOException
        {
            // put the log file into a long life temp directory
            File tempDir = TempFileProvider.getLongLifeTempDir("patches");
            logFile = new File(tempDir, "FixBpmPackagesPatch.log");

            // open the file for appending
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();
            // move to the end of the file
            channel.position(channel.size());
            // add a newline and it's ready
            writeLine("").writeLine("");
            writeLine("FixBpmPackagesPatch executing on " + new Date());
        }

        private FixBpmPackagesPatchHelper write(Object obj) throws IOException
        {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes("UTF-8")));
            return this;
        }

        private FixBpmPackagesPatchHelper writeLine(Object obj) throws IOException
        {
            write(obj);
            write("\n");
            return this;
        }

        private void closeWriter()
        {
            try
            {
                channel.close();
            }
            catch (IOException ioe)
            {
                // Nothing we can do
            }
        }

        public String fixBpmPackages(List<ChildAssociationRef> references) throws Exception
        {
            this.refs = references;
            this.assocCount = references.size();
            BatchProcessWorkProvider<ChildAssociationRef> workProvider = new BatchProcessWorkProvider<ChildAssociationRef>()
            {
                @Override
                public synchronized int getTotalEstimatedWorkSize()
                {
                    return assocCount;
                }

                @Override
                public synchronized Collection<ChildAssociationRef> getNextWork()
                {
                    int nextMaxSize = skipCount + batchSize;
                    List<ChildAssociationRef> result;
                    if (assocCount < skipCount)
                    {
                        // we are finished, return empty list
                        result = Collections.emptyList();
                    }
                    else if (assocCount >= nextMaxSize)
                    {
                        // more jobs are available with full batch
                        result = refs.subList(skipCount, nextMaxSize);
                    }
                    else
                    {
                        // there are less items than batch size
                        result = refs.subList(skipCount, assocCount);
                    }
                    // increment the counter of batches
                    skipCount += batchSize;
                    return result;
                }
            };

            // get the association types to check
            BatchProcessor<ChildAssociationRef> batchProcessor = new BatchProcessor<ChildAssociationRef>(
                    "FixBpmPackagesPatch",
                    transactionHelper,
                    workProvider,
                    batchThreads, batchSize,
                    applicationEventPublisher,
                    progress_logger, 1000);

            BatchProcessor.BatchProcessWorker<ChildAssociationRef> worker = new BatchProcessor.BatchProcessWorker<ChildAssociationRef>()
            {
                @Override
                public String getIdentifier(ChildAssociationRef entry)
                {
                    return entry.toString();
                }

                @Override
                public void beforeProcess() throws Throwable
                {
                }

                @Override
                public void process(ChildAssociationRef assocRef) throws Throwable
                {
                    NodeRef packageRef = assocRef.getChildRef();
                    QName typeQname = nodeService.getType(packageRef);
                    String name = (String) nodeService.getProperty(packageRef, ContentModel.PROP_NAME);
                    if (logger.isDebugEnabled())
                        logger.debug("Package " + name + " type " + typeQname);

                    if (!nodeService.getType(packageRef).equals(WorkflowModel.TYPE_PACKAGE))
                    {
                        // New type of the package is bpm:package
                        nodeService.setType(packageRef, WorkflowModel.TYPE_PACKAGE);
                    }
                    // Get all package items
                    List<ChildAssociationRef> packageItemsAssocs = nodeService.getChildAssocs(packageRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

                    for (ChildAssociationRef itemAssoc : packageItemsAssocs)
                    {
                        NodeRef parentRef = itemAssoc.getParentRef();
                        NodeRef childRef = itemAssoc.getChildRef();
                        String itemName = (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME);
                        // To avoid unnecessary deletion of the child item, we check if the association is not primary
                        // For the package item it should be not primary association.
                        if (itemAssoc.isPrimary())
                        {
                            logger.error("Association between package: " + name + " and item: " + itemName + " is primary association, so removing this assiciation will result in child node deletion");
                            continue;
                        }

                        if (itemAssoc.getTypeQName().equals(WorkflowModel.ASSOC_PACKAGE_CONTAINS))
                        {
                            continue;
                        }

                        boolean assocRemoved = nodeService.removeChildAssociation(itemAssoc);
                        if (assocRemoved)
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("Association between package: " + name + " and item: " + itemName + " was removed");
                        }
                        else
                        {
                            if (logger.isErrorEnabled())
                                logger.error("Association between package: " + name + " and item: " + itemName + " doesn't exist");
                            // If there is no association we won't create a new one
                            continue;
                        }
                        // Recreate new association between package and particular item as bpm:packageContains
                        /* ChildAssociationRef newChildAssoc = */nodeService.addChild(parentRef, childRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(itemName)));
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("New association has been created between package: " + name + " and item: " + itemName);
                        }
                    }
                }

                @Override
                public void afterProcess() throws Throwable
                {
                }
            };

            int updated = batchProcessor.process(worker, true);

            return I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
        }
    }
}
