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
package org.alfresco.repo.admin.patch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch to break the link between {@link ContentModel#ASPECT_WORKING_COPY working copies}
 * and {@link ContentModel#ASPECT_COPIEDFROM copies}.
 * <p/>
 * Formerly, when a document was copied, it was given a <b>cm:source</b> property on the
 * <b>cm:copiedfrom</b> aspect - a <b>d:noderef</b> property.  During checkout, the
 * working copy was given the <b>cm:workingcopy</b> aspect and the <b>cm:copiedfrom</b>
 * aspect was assumed to be present.  However, the ordinality of the <b>cm:copiedfrom</b>'s
 * <b>cm:source</b> property didn't match up with the checkin-checkout 1:1 relationship.
 * <p/>
 * This patch works in two parts:
 * <p/>
 * <u><b>cm:copiedfrom</b></u><br/>
 * <ul>
 *    <li><b>cm:source</b> is transformed into a peer association, <b>cm:original</b></li>
 *    <li>The aspect is removed where the source no longer exists</li>
 * </ul>
 * <p/>
 * <u><b>cm:workingcopy</b></u><br/>
 * <ul>
 *    <li><b>cm:source</b> is transformed into a peer association, <b>cm:workingcopylink</b></li>
 *    <li>The original is given aspect <b>cm:checkedout</b></li>
 *    <li>The copy keeps <b>cm:workingcopy</b></li>
 * </ul>
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CopiedFromAspectPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.copiedFromAspect.result";
    
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private DictionaryService dictionaryService;
    
    private int batchThreads = 2;
    private int batchSize = 1000;
    private int batchMaxQueryRange = 10000;
    
    private static Log logger = LogFactory.getLog(CopiedFromAspectPatch.class);
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);
    
    public CopiedFromAspectPatch()
    {
    }

    /**
     * @param patchDAO              additional queries
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param nodeDAO               provides query support
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param dictionaryService     type and aspect resolution
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

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

    /**
     * @param batchMaxQueryRange        the largest ID range that the work provider can query for.
     *                                  Lower this if the DB resultset retrieval causes memory issues
     *                                  prior to the {@link #setBatchQuerySize(int) query limit} being
     *                                  applied.
     */
    public void setBatchMaxQueryRange(int batchMaxQueryRange)
    {
        this.batchMaxQueryRange = batchMaxQueryRange;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
        checkPropertyNotNull(dictionaryService, "dictionaryService");
        checkPropertyNotNull(applicationEventPublisher, "applicationEventPublisher");
    }

    private CopiedFromAspectPatch write(FileChannel file, Object obj)
    {
        try
        {
            file.write(ByteBuffer.wrap(obj.toString().getBytes("UTF-8")));
        }
        catch (IOException e)
        {
            logger.error("Failed to write object to file: " + obj.toString());
        }
        return this;
    }
    private CopiedFromAspectPatch writeLine(FileChannel file, Object obj)
    {
        write(file, obj);
        write(file, "\n");
        return this;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // put the log file into a long life temp directory
        File tempDir = TempFileProvider.getLongLifeTempDir("patches");
        File logFile = new File(tempDir, "CopiedFromAspectPatch.log");
        
        // open the file for appending
        RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
        FileChannel file = outputFile.getChannel();
        try
        {
            // move to the end of the file
            file.position(file.size());
            // add a newline and it's ready
            writeLine(file, "").writeLine(file, "");
            writeLine(file, "CopiedFromAspectPatch.log executing on " + new Date());
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting CopiedFromAspectPatch.  [Q=Query; P=Process]");
            }
            
            int updated = process(file);
            // done
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
        finally
        {
            try { file.close(); } catch (IOException e) {}
        }
    }
    
    /**
     * Does the actual work, writing results to the given file channel

     * @return              Returns a status message after completion
     */
    private int process(final FileChannel file)
    {
        // Authentication
        final String user = AuthenticationUtil.getRunAsUser();
        
        Set<QName> qnames = new HashSet<QName>();
        qnames.add(ContentModel.ASPECT_COPIEDFROM);
        qnames.add(ContentModel.ASPECT_WORKING_COPY);
        
        // Instance to provide raw data to process
        BatchProcessWorkProvider<Pair<Long, NodeRef>> workProvider = new WorkProvider(qnames);
        
        // Instance to handle each item of work
        BatchProcessWorker<Pair<Long, NodeRef>> worker = new BatchProcessWorkerAdaptor<Pair<Long, NodeRef>>()
        {
            @Override
            public void beforeProcess() throws Throwable
            {
                AuthenticationUtil.setRunAsUser(user);
            }
            @Override
            public void process(Pair<Long, NodeRef> entry) throws Throwable
            {
                CopiedFromAspectPatch.this.process(file, entry);
            }
            @Override
            public void afterProcess() throws Throwable
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        };
        
        BatchProcessor<Pair<Long, NodeRef>> batchProcessor = new BatchProcessor<Pair<Long, NodeRef>>(
                "CopiedFromAspectPatch",
                transactionService.getRetryingTransactionHelper(),
                workProvider,
                this.batchThreads, this.batchSize,
                null,
                progress_logger,
                1000);
        int updated = batchProcessor.process(worker, true);
        return updated;
    }
    
    /**
     * Work provider that performs incremental queries to find nodes with the
     * required aspects.
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    private class WorkProvider implements BatchProcessWorkProvider<Pair<Long, NodeRef>>
    {
        private long maxId = Long.MAX_VALUE;
        private long workCount = Long.MAX_VALUE;
        private long currentId = 0L;
        private final Set<QName> aspectQNames;
        
        private WorkProvider(Set<QName> aspectQNames)
        {
            this.aspectQNames = aspectQNames;
        }

        @Override
        public synchronized int getTotalEstimatedWorkSize()
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
                workCount = patchDAO.getCountNodesWithAspects(aspectQNames);
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tQ: Work count: " + workCount);
                }
            }
            return (int) workCount;
        }

        @Override
        public synchronized Collection<Pair<Long, NodeRef>> getNextWork()
        {
            // Record the results
            final List<Pair<Long, NodeRef>> results = new ArrayList<Pair<Long, NodeRef>>(batchMaxQueryRange);
            // Record the node IDs for bulk loading
            final List<Long> nodeIds = new ArrayList<Long>(batchMaxQueryRange);
            
            NodeRefQueryCallback callback = new NodeRefQueryCallback()
            {
                @Override
                public boolean handle(Pair<Long, NodeRef> nodePair)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\tQ: Recording node work: " + nodePair);
                    }
                    results.add(nodePair);
                    nodeIds.add(nodePair.getFirst());
                    return true;
                }
            };
            // Keep querying until we have enough results to give back
            while (currentId <= maxId)
            {
                nodeDAO.getNodesWithAspects(
                        aspectQNames,
                        currentId,
                        currentId + batchMaxQueryRange,
                        callback);
                // Increment the minimum ID
                currentId += batchMaxQueryRange;
            }
            // Preload the nodes for quicker access
            nodeDAO.cacheNodesById(nodeIds);
            // Done
            return results;
        }
    }
    
    private static final QName PROP_SOURCE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "source");
    /**
     * Does the per-node manipulation as stated in the class's docs
     * 
     * @param file                  the file to write output to
     * @param nodePair              the node to operate on
     */
    private void process(FileChannel file, Pair<Long, NodeRef> nodePair)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("\tP: Processing node: " + nodePair);
        }
        NodeRef nodeRef = nodePair.getSecond();
        // First check if the source property is present and valid
        NodeRef sourceNodeRef = DefaultTypeConverter.INSTANCE.convert(
                NodeRef.class,
                nodeService.getProperty(nodeRef, PROP_SOURCE));
        // Does the source exist?
        if (sourceNodeRef == null || !nodeService.exists(sourceNodeRef))
        {
            boolean isNewModel = true;
            if (
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_COPIEDFROM) &&
                    nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL).size() == 0)
            {
                // There is no association pointing back to the original and the source node is invalid
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tP: Removing cm:copiedfrom: " + nodePair);
                }
                writeLine(file, "Removing cm:copiedfrom from node: " + nodePair);
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_COPIEDFROM);
                isNewModel = false;
            }
            if (
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) &&
                    nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK).size() == 0)
            {
                // There is no association from the checked out node and the source node is invalid
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tP: Removing cm:workingcopy: " + nodePair);
                }
                writeLine(file, "Removing cm:workingcopy from node: " + nodePair);
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
                isNewModel = false;
            }
            // If nothing was done, then it's a node with the new data model and we can leave it
            if (isNewModel)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tP: Ignoring data with new model: " + nodePair);
                }
                writeLine(file, "Ignoring data with new model: " + nodePair);
            }
        }
        else
        {
            // The cm:source property points to a valid node.
            // This needs to be fixed up to use the new model.
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_COPIEDFROM))
            {
                if (nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL).size() > 0)
                {
                    // The association is already present, so just remove the property (we'll do that later)
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\tP: Adding association cm:original: " + nodePair);
                    }
                    writeLine(file, "Adding association cm:original: " + nodePair);
                    // Create the association
                    nodeService.createAssociation(nodeRef, sourceNodeRef, ContentModel.ASSOC_ORIGINAL);
                }
            }
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
            {
                if (nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK).size() > 0)
                {
                    // The association is already present, so just remove the property (we'll do that later)
                }
                else if (nodeService.hasAspect(sourceNodeRef, ContentModel.ASPECT_CHECKED_OUT))
                {
                    // ALF-9569: copiedFromAspect patch does not take documents with
                    //           more than one working copy into account
                    // So there are multiple working copies
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\tP: Found node with multiple working copies: " + sourceNodeRef);
                        logger.debug("\tP: Removing cm:workingcopy: " + nodePair);
                    }
                    writeLine(file, "Found node with multiple working copies: " + nodePair);
                    writeLine(file, "Removing cm:workingcopy from node: " + nodePair);
                    nodeService.removeAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\tP: Adding aspect cm:checkedout: " + sourceNodeRef);
                        logger.debug("\tP: Adding association cm:workingcopylink: " + nodePair);
                    }
                    writeLine(file, "Adding aspect cm:checkedout: " + sourceNodeRef);
                    writeLine(file, "Adding association cm:workingcopylink to " + nodePair);
                    // Add aspect to source
                    nodeService.addAspect(sourceNodeRef, ContentModel.ASPECT_CHECKED_OUT, null);
                    // Create the association
                    nodeService.createAssociation(sourceNodeRef, nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
                }
            }
        }
        // Remove the property if it exists
        nodeService.removeProperty(nodeRef, PROP_SOURCE);
    }
}
