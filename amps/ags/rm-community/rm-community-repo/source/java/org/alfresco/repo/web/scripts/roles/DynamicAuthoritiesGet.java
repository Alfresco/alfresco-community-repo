/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedWriterDynamicAuthority;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Format;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Webscript used for removing dynamic authorities from the records.
 *
 * @author Silviu Dinuta
 * @since 2.3.0.7
 */
@SuppressWarnings("deprecation")
public class DynamicAuthoritiesGet extends AbstractWebScript implements RecordsManagementModel
{
    private static final String MESSAGE_PARAMETER_BATCHSIZE_GREATER_THAN_ZERO = "Parameter batchsize should be a number greater than 0.";
    private static final String MESSAGE_PROCESSING_BEGIN = "Processing - BEGIN";
    private static final String MESSAGE_PROCESSING_END = "Processing - END";
    private static final String MESSAGE_PROCESSING_RECORD_END_TEMPLATE = "Processing record {0} - END";
    private static final String MESSAGE_PROCESSING_RECORD_BEGIN_TEMPLATE = "Processing record {0} - BEGIN";
    private static final String MESSAGE_BATCHSIZE_IS_INVALID = "Parameter batchsize is invalid.";
    private static final String MESSAGE_BATCHSIZE_IS_MANDATORY = "Parameter batchsize is mandatory";
    private static final String MESSAGE_NODE_REF_DOES_NOT_EXIST_TEMPLATE = "Parameter parentNodeRef = {0} does not exist.";
    private static final String SUCCESS_STATUS = "success";
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(DynamicAuthoritiesGet.class);
    private static final String BATCH_SIZE = "batchsize";
    private static final String TOTAL_NUMBER_TO_PROCESS = "maxProcessedRecords";
    private static final String PARAM_EXPORT = "export";
    private static final String PARAM_PARENT_NODE_REF = "parentNodeRef";
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
    /** Content Streamer */
    protected ContentStreamer contentStreamer;
    private FileFolderService fileFolderService;

    /** service setters */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    protected Map<String, Object> buildModel(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        Map<String, Object> model = new HashMap<>();
        final Long batchSize = getBatchSizeParameter(req);
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

        Long totalNumberOfRecordsToProcess = getMaxToProccessParameter(req, batchSize);

        boolean attach = getExportParameter(req);

        File file = TempFileProvider.createTempFile("processedNodes_", ".csv");
        FileWriter writer = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(writer);
        List<NodeRef> processedNodes = new ArrayList<>();
        try
        {
            NodeRef parentNodeRef = getParentNodeRefParameter(req);
            if (parentNodeRef != null)
            {
                processedNodes = processChildrenNodes(parentNodeRef, batchSize.intValue(), recordAspectPair,
                            totalNumberOfRecordsToProcess.intValue(), out, attach);
            }
            else
            {
                processedNodes = processNodes(batchSize, maxNodeId, recordAspectPair, totalNumberOfRecordsToProcess,
                            out, attach);
            }
        }
        finally
        {
            out.close();
        }

        int processedNodesSize = processedNodes.size();

        String message = "";
        if (totalNumberOfRecordsToProcess == 0
                    || (totalNumberOfRecordsToProcess > 0 && processedNodesSize < totalNumberOfRecordsToProcess))
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

        if (attach)
        {
            try
            {
                String fileName = file.getName();
                contentStreamer.streamContent(req, res, file, null, attach, fileName, model);
                model = null;
            }
            finally
            {
                if (file != null)
                {
                    file.delete();
                }
            }
        }
        return model;
    }

    /**
     * Get export parameter from the request
     *
     * @param req
     * @return
     */
    protected boolean getExportParameter(WebScriptRequest req)
    {
        boolean attach = false;
        String export = req.getParameter(PARAM_EXPORT);
        if (Boolean.parseBoolean(export))
        {
            attach = true;
        }
        return attach;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.content.StreamContent#execute(org.springframework.extensions.webscripts.
     * WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // retrieve requested format
        String format = req.getFormat();

        try
        {
            String mimetype = getContainer().getFormatRegistry().getMimeType(req.getAgent(), format);
            if (mimetype == null) 
            { 
                throw new WebScriptException("Web Script format '" + format + "' is not registered");
            }

            // construct model for script / template
            Status status = new Status();
            Cache cache = new Cache(getDescription().getRequiredCache());
            Map<String, Object> model = buildModel(req, res);
            if (model == null) { return; }
            model.put("status", status);
            model.put("cache", cache);

            Map<String, Object> templateModel = createTemplateParameters(req, res, model);

            // render output
            int statusCode = status.getCode();
            if (statusCode != HttpServletResponse.SC_OK && !req.forceSuccessStatus())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Force success status header in response: " + req.forceSuccessStatus());
                    logger.debug("Setting status " + statusCode);
                }
                res.setStatus(statusCode);
            }

            // apply location
            String location = status.getLocation();
            if (location != null && location.length() > 0)
            {
                if (logger.isDebugEnabled()) logger.debug("Setting location to " + location);
                res.setHeader(WebScriptResponse.HEADER_LOCATION, location);
            }

            // apply cache
            res.setCache(cache);

            String callback = null;
            if (getContainer().allowCallbacks())
            {
                callback = req.getJSONCallback();
            }
            if (format.equals(WebScriptResponse.JSON_FORMAT) && callback != null)
            {
                if (logger.isDebugEnabled()) logger.debug("Rendering JSON callback response: content type="
                            + Format.JAVASCRIPT.mimetype() + ", status=" + statusCode + ", callback=" + callback);

                // NOTE: special case for wrapping JSON results in a javascript function callback
                res.setContentType(Format.JAVASCRIPT.mimetype() + ";charset=UTF-8");
                res.getWriter().write((callback + "("));
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Rendering response: content type=" + mimetype + ", status=" + statusCode);

                res.setContentType(mimetype + ";charset=UTF-8");
            }

            // render response according to requested format
            renderFormatTemplate(format, templateModel, res.getWriter());

            if (format.equals(WebScriptResponse.JSON_FORMAT) && callback != null)
            {
                // NOTE: special case for wrapping JSON results in a javascript function callback
                res.getWriter().write(")");
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                logger.debug("Caught exception; decorating with appropriate status template : " + stack.toString());
            }

            throw createStatusException(e, req, res);
        }
    }

    protected void renderFormatTemplate(String format, Map<String, Object> model, Writer writer)
    {
        format = (format == null) ? "" : format;

        String templatePath = getDescription().getId() + "." + format;

        if (logger.isDebugEnabled()) logger.debug("Rendering template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }

    /**
     * Obtain maximum of the records to be processed from the request if it is specified or bachsize value otherwise
     *
     * @param req
     * @return maximum of the records to be processed from the request if it is specified or bachsize value otherwise
     */
    protected Long getMaxToProccessParameter(WebScriptRequest req, final Long batchSize)
    {
        String totalToBeProcessedRecordsStr = req.getParameter(TOTAL_NUMBER_TO_PROCESS);
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
        return totalNumberOfRecordsToProcess;
    }

    /**
     * Obtain batchsize parameter from the request.
     *
     * @param req
     * @return batchsize parameter from the request
     */
    protected Long getBatchSizeParameter(WebScriptRequest req)
    {
        String batchSizeStr = req.getParameter(BATCH_SIZE);
        Long size;
        if (StringUtils.isBlank(batchSizeStr))
        {
            logger.info(MESSAGE_BATCHSIZE_IS_MANDATORY);
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, MESSAGE_BATCHSIZE_IS_MANDATORY);
        }
        try
        {
            size = Long.parseLong(batchSizeStr);
            if (size <= 0)
            {
                logger.info(MESSAGE_PARAMETER_BATCHSIZE_GREATER_THAN_ZERO);
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, MESSAGE_PARAMETER_BATCHSIZE_GREATER_THAN_ZERO);
            }
        }
        catch (NumberFormatException ex)
        {
            logger.info(MESSAGE_BATCHSIZE_IS_INVALID);
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, MESSAGE_BATCHSIZE_IS_INVALID);
        }
        return size;
    }

    /**
     * Get parentNodeRef parameter from the request
     *
     * @param req
     * @return
     */
    protected NodeRef getParentNodeRefParameter(WebScriptRequest req)
    {
        String parentNodeRefStr = req.getParameter(PARAM_PARENT_NODE_REF);
        NodeRef parentNodeRef = null;
        if (StringUtils.isNotBlank(parentNodeRefStr))
        {
            parentNodeRef = new NodeRef(parentNodeRefStr);
            if(!nodeService.exists(parentNodeRef))
            {
                String message = MessageFormat.format(MESSAGE_NODE_REF_DOES_NOT_EXIST_TEMPLATE, parentNodeRef.toString());
                logger.info(message);
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, message);
            }
        }
        return parentNodeRef;
    }

    /**
     * Process nodes all nodes or the maximum number of nodes specified by batchsize or totalNumberOfRecordsToProcess
     * parameters
     *
     * @param batchSize
     * @param maxNodeId
     * @param recordAspectPair
     * @param totalNumberOfRecordsToProcess
     * @return the list of processed nodes
     */
    protected List<NodeRef> processNodes(final Long batchSize, Long maxNodeId, final Pair<Long, QName> recordAspectPair,
                Long totalNumberOfRecordsToProcess, final BufferedWriter out, final boolean attach)
    {
        final Long maxRecordsToProcess = totalNumberOfRecordsToProcess;
        final List<NodeRef> processedNodes = new ArrayList<>();
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
                    List<Long> nodeIds = patchDAO.getNodesByAspectQNameId(recordAspectPair.getFirst(), currentIndex,
                                currentIndex + batchSize);

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
                        if (attach)
                        {
                            out.write(recordName);
                            out.write(",");
                            out.write(record.toString());
                            out.write("\n");
                        }
                    }

                    return null;
                }
            }, false, // read only
            true); // requires new
        }
        logger.info(MESSAGE_PROCESSING_END);
        return processedNodes;
    }

    protected List<NodeRef> processChildrenNodes(NodeRef parentNodeRef, final int batchSize,
                final Pair<Long, QName> recordAspectPair, final int maxRecordsToProcess, final BufferedWriter out,
                final boolean attach)
    {
        final List<NodeRef> processedNodes = new ArrayList<>();
        final List<FileInfo> children = fileFolderService.search(parentNodeRef, "*", /*filesSearch*/true, /*folderSearch*/true, /*includeSubfolders*/true);
        logger.info(MESSAGE_PROCESSING_BEGIN);
        // by batch size
        for (int i = 0; i < children.size(); i += batchSize)
        {
            if (maxRecordsToProcess != 0 && processedNodes.size() >= maxRecordsToProcess)
            {
                break;
            }
            final int currentIndex = i;

            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    List<FileInfo> nodes = children.subList(currentIndex, Math.min(currentIndex + batchSize, children.size()));
                    // process each one
                    for (FileInfo node : nodes)
                    {
                        if (maxRecordsToProcess != 0 && processedNodes.size() >= maxRecordsToProcess)
                        {
                            break;
                        }
                        NodeRef record = node.getNodeRef();
                        if (nodeService.hasAspect(record, recordAspectPair.getSecond()))
                        {
                            String recordName = (String) nodeService.getProperty(record, ContentModel.PROP_NAME);
                            logger.info(MessageFormat.format(MESSAGE_PROCESSING_RECORD_BEGIN_TEMPLATE, recordName));
                            processNode(record);
                            logger.info(MessageFormat.format(MESSAGE_PROCESSING_RECORD_END_TEMPLATE, recordName));
                            processedNodes.add(record);
                            if (attach)
                            {
                                out.write(recordName);
                                out.write(",");
                                out.write(record.toString());
                                out.write("\n");
                            }
                        }
                    }

                    return null;
                }
            }, false, // read only
                        true); // requires new
        }
        logger.info(MESSAGE_PROCESSING_END);
        return processedNodes;
    }

    /**
     * Process each node
     *
     * @param nodeRef
     */
    @SuppressWarnings({ "unchecked"})
    protected void processNode(NodeRef nodeRef)
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
            Set<String> readersKeySet = null;
            if (readers != null)
        {
                readersKeySet = readers.keySet();
            }
            Set<String> writersKeySet = null;
            if (writers != null)
            {
                writersKeySet = writers.keySet();
            }
            // re-set extended security via API
            extendedSecurityService.set(nodeRef, readersKeySet, writersKeySet);
        }
    }
}
