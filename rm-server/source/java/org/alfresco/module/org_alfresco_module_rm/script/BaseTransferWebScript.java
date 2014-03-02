/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.web.scripts.content.StreamACP;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Abstract base class for transfer related web scripts.
 *
 * @author Gavin Cornwell
 */
public abstract class BaseTransferWebScript extends StreamACP
                                            implements RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(BaseTransferWebScript.class);

    protected FilePlanService filePlanService;

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File tempFile = null;
        try
        {
            // retrieve requested format
            String format = req.getFormat();

            // construct model for template
            Status status = new Status();
            Cache cache = new Cache(getDescription().getRequiredCache());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("status", status);
            model.put("cache", cache);

            // get the parameters that represent the NodeRef, we know they are present
            // otherwise this webscript would not have matched
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String nodeId = templateVars.get("id");
            String transferId = templateVars.get("transfer_id");

            // create and return the file plan NodeRef
            NodeRef filePlan = new NodeRef(new StoreRef(storeType, storeId), nodeId);

            if (logger.isDebugEnabled())
            {
                logger.debug("Retrieving transfer '" + transferId + "' from file plan: " + filePlan);
            }

            // ensure the file plan exists
            if (!this.nodeService.exists(filePlan))
            {
                status.setCode(HttpServletResponse.SC_NOT_FOUND,
                            "Node " + filePlan.toString() + " does not exist");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return;
            }

            // ensure the node is a filePlan object
            if (!TYPE_FILE_PLAN.equals(this.nodeService.getType(filePlan)))
            {
                status.setCode(HttpServletResponse.SC_BAD_REQUEST,
                            "Node " + filePlan.toString() + " is not a file plan");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return;
            }

            // attempt to find the transfer node
            NodeRef transferNode = findTransferNode(filePlan, transferId);

            // send 404 if the transfer is not found
            if (transferNode == null)
            {
                status.setCode(HttpServletResponse.SC_NOT_FOUND,
                            "Could not locate transfer with id: " + transferId);
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return;
            }

            // execute the transfer operation
            tempFile = executeTransfer(transferNode, req, res, status, cache);
        }
        catch (Throwable e)
        {
            throw createStatusException(e, req, res);
        }
        finally
        {
           // try and delete the temporary file (if not in debug mode)
           if (tempFile != null)
           {
               if (logger.isDebugEnabled())
               {
                   logger.debug("Transfer report saved to temporary file: " + tempFile.getAbsolutePath());
               }
               else
               {
                   tempFile.delete();
               }
           }
        }
    }

    /**
     * Abstract method subclasses implement to perform the actual logic required.
     *
     * @param transferNode The transfer node
     * @param req The request
     * @param res The response
     * @param status Status object
     * @param cache Cache object
     * @return File object representing the file containing the JSON of the report
     * @throws IOException
     */
    protected abstract File executeTransfer(NodeRef transferNode,
                WebScriptRequest req, WebScriptResponse res,
                Status status, Cache cache) throws IOException;

    /**
     * Finds a transfer object with the given id in the given file plan.
     * This method returns null if a transfer with the given id is not found.
     *
     * @param filePlan The file plan to search
     * @param transferId The id of the transfer being requested
     * @return The transfer node or null if not found
     */
    protected NodeRef findTransferNode(NodeRef filePlan, String transferId)
    {
        NodeRef transferNode = null;

        // get all the transfer nodes and find the one we need
        NodeRef transferContainer = filePlanService.getTransferContainer(filePlan);
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(transferContainer,
                                                                           ContentModel.ASSOC_CONTAINS,
                                                                           RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef child : assocs)
        {
            if (child.getChildRef().getId().equals(transferId))
            {
                transferNode = child.getChildRef();
                break;
            }
        }

        return transferNode;
    }

    /**
     * Returns an array of NodeRefs representing the items to be transferred.
     *
     * @param transferNode The transfer object
     * @return Array of NodeRefs
     */
    protected NodeRef[] getTransferNodes(NodeRef transferNode)
    {
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(transferNode,
                    RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        NodeRef[] itemsToTransfer = new NodeRef[assocs.size()];
        for (int idx = 0; idx < assocs.size(); idx++)
        {
            itemsToTransfer[idx] = assocs.get(idx).getChildRef();
        }

        return itemsToTransfer;
    }
}