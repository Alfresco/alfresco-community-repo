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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to add RM custom reference instances
 * to a node.
 *
 * @author Neil McErlean
 */
public class CustomRefPost extends AbstractRmWebScript
{
    private static final String TO_NODE = "toNode";
    private static final String REF_ID = "refId";

    /** RM Admin Service */
    private RecordsManagementAdminService rmAdminService;

    /** Rule Service */
    private RuleService ruleService;

    /**
     * @param rmAdminService    RM Admin Service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * @param ruleService   Rule Service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject json = null;
        Map<String, Object> ftlModel = null;
        try
        {
            ruleService.disableRuleType(RuleType.INBOUND);

            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            ftlModel = addCustomReferenceInstance(req, json);
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
        finally
        {
            ruleService.enableRuleType(RuleType.INBOUND);
        }

        return ftlModel;
    }

    /**
     * Applies custom reference.
     */
    protected Map<String, Object> addCustomReferenceInstance(WebScriptRequest req, JSONObject json) throws JSONException
    {
        NodeRef fromNode = parseRequestForNodeRef(req);

        Map<String, Object> result = new HashMap<String, Object>();

        String toNodeStg = json.getString(TO_NODE);
        NodeRef toNode = new NodeRef(toNodeStg);

        String clientsRefId = json.getString(REF_ID);
        QName qn = rmAdminService.getQNameForClientId(clientsRefId);
        if (qn == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            		"Unable to find reference type: " + clientsRefId);
        }

        rmAdminService.addCustomReference(fromNode, toNode, qn);

        result.put("success", true);

        return result;
    }
}