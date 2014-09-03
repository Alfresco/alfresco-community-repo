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
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API to set the recorded version config for a document
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigPost extends AbstractRmWebScript implements RecordableVersionModel
{
    // Constant for recorded version parameter
    public static final String RECORDED_VERSION = "recordedVersion";

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        NodeRef nodeRef = parseRequestForNodeRef(req);
        RecordableVersionPolicy recordableVersionPolicy = getRecordableVersionPolicy(req);
        nodeService.setProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY, recordableVersionPolicy);
        return new HashMap<String, Object>(1);
    }

    /**
     * Gets the recordable version policy from the request
     *
     * @param The webscript request
     * @return The recordable version policy
     */
    private RecordableVersionPolicy getRecordableVersionPolicy(WebScriptRequest req)
    {
        String recordedVersion = getRecordedVersion(req);
        return RecordableVersionPolicy.valueOf(recordedVersion);
    }

    /**
     * Gets the recorded version parameter value from the request
     *
     * @param req The webscript request
     * @return The recorded version parameter value
     */
    private String getRecordedVersion(WebScriptRequest req)
    {
        try
        {
            // Convert the request content to JSON
            String content = req.getContent().getContent();
            JSONObject jsonObject = new JSONObject(new JSONTokener(content));
            checkMandatoryJsonParam(jsonObject, RECORDED_VERSION);
            return jsonObject.getString(RECORDED_VERSION);
        }
        catch (JSONException | IOException ex)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not parse JSON from req.", ex);
        }
    }
}
