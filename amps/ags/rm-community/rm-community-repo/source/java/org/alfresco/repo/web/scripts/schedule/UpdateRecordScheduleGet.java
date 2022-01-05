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

package org.alfresco.repo.web.scripts.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.model.rma.aspect.FrozenAspect;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
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
 * Webscript used to update records that are missing their schedule information
 *
 * @author Roy Wetherall
 */
public class UpdateRecordScheduleGet extends AbstractWebScript implements RecordsManagementModel
{
    /**
     * logger
     */
    private static Log logger = LogFactory.getLog(UpdateRecordScheduleGet.class);

    /**
     * parameters
     */
    private static final String PARAM_MAX_RECORD_FOLDERS = "maxRecordFolders";
    private static final String PARAM_RECORD_FOLDER = "recordFolder";

    private static final String SUCCESS_STATUS = "success";
    private static final String MODEL_STATUS = "responsestatus";
    private static final String MODEL_MESSAGE = "message";
    private static final String MESSAGE_ALL_TEMPLATE = "Updated {0} records from {1} folders with updated disposition instructions.";
    private static final String MESSAGE_FOLDER_TEMPLATE = "Updated records in folder {0} with updated disposition instructions.";

    /**
     * services
     */
    private NodeService nodeService;
    private DispositionService dispositionService;
    private RecordService recordService;
    private TransactionService transactionService;
    private RecordsManagementQueryDAO recordsManagementQueryDAO;
    private BehaviourFilter behaviourFilter;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private FrozenAspect frozenAspect;
    private RecordsManagementSearchBehaviour recordsManagementSearchBehaviour;
    /**
     * service setters
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRecordsManagementQueryDAO(RecordsManagementQueryDAO recordsManagementQueryDAO)
    {
        this.recordsManagementQueryDAO = recordsManagementQueryDAO;
    }

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setFrozenAspect(FrozenAspect frozenAspect)
    {
        this.frozenAspect = frozenAspect;
    }

    public void setRecordsManagementSearchBehaviour(RecordsManagementSearchBehaviour recordsManagementSearchBehaviour)
    {
        this.recordsManagementSearchBehaviour = recordsManagementSearchBehaviour;
    }

    /**
     * Build web script model
     */
    protected Map<String, Object> buildModel(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        Map<String, Object> model = new HashMap<>();
        transactionService.getRetryingTransactionHelper()
            .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    qnameDAO.getOrCreateQName(ASPECT_DISPOSITION_PROCESSED);
                    return null;
                }

            }, false, true);

        int maxRecordFolders = getMaxRecordFolders(req);
        NodeRef recordFolder = getRecordFolder(req);

        int processedRecords = 0;
        String message;
        if (recordFolder != null)
        {
            // Process the specified record folder
            updateRecordFolder(recordFolder);
            message = MessageFormat.format(MESSAGE_FOLDER_TEMPLATE, recordFolder);
        }
        else
        {
            int processedRecordFolders = 0;
            int queryBatchSize = 10000;
            Long maxNodeId = nodeDAO.getMaxNodeId();
            for (Long i = 0L; i < maxNodeId; i += queryBatchSize)
            {
                List<NodeRef> folders = recordsManagementQueryDAO.getRecordFoldersWithSchedules(i, i + queryBatchSize);
                for (NodeRef folder : folders)
                {
                    processedRecords = processedRecords + updateRecordFolder(folder);
                    processedRecordFolders++;

                    if (processedRecordFolders >= maxRecordFolders)
                    {
                        // stop processing since we have meet our limit
                        break;
                    }
                }

                if (processedRecordFolders >= maxRecordFolders)
                {
                    // stop processing since we have meet our limit
                    break;
                }
            }
            message = MessageFormat.format(MESSAGE_ALL_TEMPLATE, processedRecords, processedRecordFolders);
        }

        model.put(MODEL_STATUS, SUCCESS_STATUS);
        model.put(MODEL_MESSAGE, message);
        logger.info(message);

        return model;
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
            String mimetype = getContainer().getFormatRegistry()
                .getMimeType(req.getAgent(), format);
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
                if (logger.isDebugEnabled())
                    logger.debug("Setting location to " + location);
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
                if (logger.isDebugEnabled())
                    logger.debug(
                        "Rendering JSON callback response: content type=" + Format.JAVASCRIPT.mimetype() + ", status="
                            + statusCode + ", callback=" + callback);

                // NOTE: special case for wrapping JSON results in a javascript function callback
                res.setContentType(Format.JAVASCRIPT.mimetype() + ";charset=UTF-8");
                res.getWriter()
                    .write((callback + "("));
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
                res.getWriter()
                    .write(")");
            }
        }
        catch (Throwable e)
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

        if (logger.isDebugEnabled())
            logger.debug("Rendering template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }

    protected int getMaxRecordFolders(WebScriptRequest req)
    {
        String valueStr = req.getParameter(PARAM_MAX_RECORD_FOLDERS);
        int value = Integer.MAX_VALUE;
        if (StringUtils.isNotBlank(valueStr))
        {
            try
            {
                value = Integer.parseInt(valueStr);
            }
            catch (NumberFormatException ex)
            {
                //do nothing here, the value will remain 0L in this case
            }
        }
        return value;
    }

    protected NodeRef getRecordFolder(WebScriptRequest req)
    {
        String valueStr = req.getParameter(PARAM_RECORD_FOLDER);
        NodeRef value = null;
        if (StringUtils.isNotBlank(valueStr))
        {
            value = new NodeRef(valueStr);
        }

        return value;
    }

    private int updateRecordFolder(final NodeRef recordFolder)
    {
        return transactionService.getRetryingTransactionHelper()
            .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
            {
                public Integer execute() throws Throwable
                {
                    int recordCount = 0;
                    frozenAspect.disableOnPropUpdateFrozenAspect();
                    try
                    {
	                    if (logger.isDebugEnabled())
	                    {
	                        logger.info("Checking folder: " + recordFolder);
	                    }
	                    recordCount = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Integer>()
	                    {
	                        @Override
	                        public Integer doWork() throws Exception
	                        {
	                            DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordFolder);
	                            int innerRecordCount = 0;
	                            if (schedule != null && schedule.isRecordLevelDisposition())
	                            {
	
	                                List<NodeRef> records = recordService.getRecords(recordFolder);
	                                for (NodeRef record : records)
	                                {
	                                    if (!nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE))
	                                    {
	                                        if (recordFolder.equals(nodeService.getPrimaryParent(record).getParentRef()))
	                                        {
	                                            if (logger.isDebugEnabled())
	                                            {
	                                                logger.info("updating record: " + record);
	                                            }

	                                            // update record disposition information
	                                            dispositionService.updateNextDispositionAction(record, schedule);
                                                recordsManagementSearchBehaviour.onAddDispositionLifecycleAspect(record,null);
	                                            innerRecordCount++;
	                                        }
	                                    }
	                                }
	                            }
	                            return innerRecordCount;
	                        }
	                    });
	                    nodeService.addAspect(recordFolder, ASPECT_DISPOSITION_PROCESSED, null);
                    }
                    finally
                    {
                        frozenAspect.enableOnPropUpdateFrozenAspect();
                    }
                    return recordCount;
                }
            }, false, true);
    }
}
