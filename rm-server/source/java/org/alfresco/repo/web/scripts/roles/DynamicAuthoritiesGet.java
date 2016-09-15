/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.roles;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedWriterDynamicAuthority;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript used for removing dynamic authorities from the records.
 *
 * @author Silviu Dinuta
 * @since 2.3.0.7
 */
@SuppressWarnings("deprecation")
public class DynamicAuthoritiesGet extends DeclarativeWebScript implements RecordsManagementModel
{
    private static final String MESSAGE_PARAMETER_BATCHSIZE_GREATER_THAN_ZERO = "Parameter batchsize should be a number greater than 0.";
    private static final String MESSAGE_PROCESSING_BEGIN = "Processing - BEGIN";
    private static final String MESSAGE_PROCESSING_END = "Processing - END";
    private static final String MESSAGE_PROCESSING_RECORD_END_TEMPLATE = "Processing record {0} - END";
    private static final String MESSAGE_PROCESSING_RECORD_BEGIN_TEMPLATE = "Processing record {0} - BEGIN";
    private static final String MESSAGE_BATCHSIZE_IS_INVALID = "Parameter batchsize is invalid.";
    private static final String MESSAGE_BATCHSIZE_IS_MANDATORY = "Parameter batchsize is mandatory";
    private static final String SUCCESS_STATUS = "success";
    private static final String FAILED_STATUS = "failed";
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(DynamicAuthoritiesGet.class);
    private static final String BATCH_SIZE = "batchsize";
    private static final String TOTAL_NUMBER_TO_PROCESS = "maxProcessedRecords";
    private static final String MODEL_STATUS = "responsestatus";
    private static final String MODEL_MESSAGE = "message";
    private static final String MESSAGE_ALL_TEMPLATE = "Processed {0} records.";
    private static final String MESSAGE_PARTIAL_TEMPLATE = "Processed first {0} records.";
    private static final String MESSAGE_NO_RECORDS_TO_PROCESS = "There where no records to be processed.";


    /** services */
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private NodeService nodeService;
    private PermissionService permissionService;
    private ExtendedSecurityService extendedSecurityService;
    private TransactionService transactionService;

    /** service setters */
    public void setPatchDAO(PatchDAO patchDAO) { this.patchDAO = patchDAO; }
    public void setNodeDAO(NodeDAO nodeDAO) { this.nodeDAO = nodeDAO; }
    public void setQnameDAO(QNameDAO qnameDAO) { this.qnameDAO = qnameDAO; }
    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setPermissionService(PermissionService permissionService) { this.permissionService = permissionService; }
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService) { this.extendedSecurityService = extendedSecurityService; }
    public void setTransactionService(TransactionService transactionService) { this.transactionService = transactionService; }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        String batchSizeStr = req.getParameter(BATCH_SIZE);
        String totalToBeProcessedRecordsStr = req.getParameter(TOTAL_NUMBER_TO_PROCESS);

        Long size = 0L;
        if (StringUtils.isBlank(batchSizeStr))
        {
            model.put(MODEL_STATUS, FAILED_STATUS);
            model.put(MODEL_MESSAGE, MESSAGE_BATCHSIZE_IS_MANDATORY);
            logger.info(MESSAGE_BATCHSIZE_IS_MANDATORY);
            return model;
        }
        try
        {
            size = Long.parseLong(batchSizeStr);
            if(size <= 0)
            {
                model.put(MODEL_STATUS, FAILED_STATUS);
                model.put(MODEL_MESSAGE, MESSAGE_PARAMETER_BATCHSIZE_GREATER_THAN_ZERO);
                logger.info(MESSAGE_PARAMETER_BATCHSIZE_GREATER_THAN_ZERO);
                return model;
            }
        }
        catch(NumberFormatException ex)
        {
            model.put(MODEL_STATUS, FAILED_STATUS);
            model.put(MODEL_MESSAGE, MESSAGE_BATCHSIZE_IS_INVALID);
            logger.info(MESSAGE_BATCHSIZE_IS_INVALID);
            return model;
        }
        final Long batchSize = size;
        // get the max node id and the extended security aspect
        Long maxNodeId = patchDAO.getMaxAdmNodeID();
        final Pair<Long, QName> recordAspectPair = qnameDAO.getQName(ASPECT_EXTENDED_SECURITY);
        if(recordAspectPair == null)
        {
            model.put(MODEL_STATUS, SUCCESS_STATUS);
            model.put(MODEL_MESSAGE, MESSAGE_NO_RECORDS_TO_PROCESS);
            logger.info(MESSAGE_NO_RECORDS_TO_PROCESS);
            return model;
        }

        //default total number of records to be processed to batch size value
        Long totalNumberOfRecordsToProcess = batchSize;
        if (StringUtils.isNotBlank(totalToBeProcessedRecordsStr))
        {
            try
            {
                totalNumberOfRecordsToProcess = Long.parseLong(totalToBeProcessedRecordsStr);
            }
            catch(NumberFormatException ex)
            {
                //do nothing here, the value will remain 0L in this case
            }
        }

        final Long maxRecordsToProcess = totalNumberOfRecordsToProcess;
        final List<NodeRef> processedNodes = new ArrayList<NodeRef>();
        logger.info(MESSAGE_PROCESSING_BEGIN);
        // by batch size
        for (Long i = 0L; i < maxNodeId; i+=batchSize)
        {
            if(maxRecordsToProcess != 0 && processedNodes.size() >= maxRecordsToProcess)
            {
                break;
            }
            final Long currentIndex = i;

            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    // get the nodes with the extended security aspect applied
                    List<Long> nodeIds = patchDAO.getNodesByAspectQNameId(recordAspectPair.getFirst(), currentIndex, currentIndex + batchSize);

                    // process each one
                    for (Long nodeId : nodeIds)
                    {
                        if(maxRecordsToProcess != 0 && processedNodes.size() >= maxRecordsToProcess)
                        {
                            break;
                        }
                        NodeRef record = nodeDAO.getNodePair(nodeId).getSecond();
                        String recordName = (String) nodeService.getProperty(record, ContentModel.PROP_NAME);
                        logger.info(MessageFormat.format(MESSAGE_PROCESSING_RECORD_BEGIN_TEMPLATE, recordName));
                        processNode(record);
                        logger.info(MessageFormat.format(MESSAGE_PROCESSING_RECORD_END_TEMPLATE, recordName));
                        processedNodes.add(record);
                    }

                    return null;
                }
            },
            false,  // read only
            true); // requires new
        }
        logger.info(MESSAGE_PROCESSING_END);
        int processedNodesSize = processedNodes.size();
        String message = "";
        if(totalNumberOfRecordsToProcess == 0 || (totalNumberOfRecordsToProcess > 0 && processedNodesSize < totalNumberOfRecordsToProcess))
        {
            message = MessageFormat.format(MESSAGE_ALL_TEMPLATE, processedNodesSize);
        }
        if (totalNumberOfRecordsToProcess > 0 && totalNumberOfRecordsToProcess == processedNodesSize)
        {
            message = MessageFormat.format(MESSAGE_PARTIAL_TEMPLATE, totalNumberOfRecordsToProcess);
        }
        model.put(MODEL_STATUS, SUCCESS_STATUS);
        model.put(MODEL_MESSAGE, message);
        logger.info(message);
        return model;
    }

    /**
     * Process each node
     *
     * @param nodeRef
     */
    @SuppressWarnings({ "unchecked"})
    private void processNode(NodeRef nodeRef)
    {
        // get the reader/writer data
        Map<String, Integer> readers = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
        Map<String, Integer> writers = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_WRITERS);

        // remove extended security aspect
        nodeService.removeAspect(nodeRef, ASPECT_EXTENDED_SECURITY);

        // remove dynamic authority permissions
        permissionService.clearPermission(nodeRef, ExtendedReaderDynamicAuthority.EXTENDED_READER);
        permissionService.clearPermission(nodeRef, ExtendedWriterDynamicAuthority.EXTENDED_WRITER);

        // if record then ...
        if (nodeService.hasAspect(nodeRef, ASPECT_RECORD))
        {
            // re-set extended security via API
            extendedSecurityService.set(nodeRef, readers.keySet(), writers.keySet());
        }
    }
}
