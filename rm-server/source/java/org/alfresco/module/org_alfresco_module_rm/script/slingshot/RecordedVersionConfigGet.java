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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API to get the recorded version config for a document
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigGet extends AbstractRmWebScript implements RecordableVersionModel
{
    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        RecordableVersionPolicy[] recordableVersionPolicies = RecordableVersionPolicy.values();
        List<Map<String, Object>> recordableVersions = new ArrayList<Map<String,Object>>(recordableVersionPolicies.length);
        NodeRef documentNodeRef = parseRequestForNodeRef(req);

        for (RecordableVersionPolicy recordableVersionPolicy : recordableVersionPolicies)
        {
            recordableVersions.add(buildRecordableVersionData(recordableVersionPolicy, documentNodeRef));
        }

        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("recordableVersions", recordableVersions);
        return model;
    }

    /**
     * Builds the recordable version data
     *
     * @param recordableVersionPolicy The recordable version policy
     * @param nodeRef Node reference of the document
     * @return A map containing the information about recordable version policy and if this policy is selected for the document
     */
    private Map<String, Object> buildRecordableVersionData(RecordableVersionPolicy recordableVersionPolicy, NodeRef nodeRef)
    {
        Map<String, Object> recordableVersionData = new HashMap<String, Object>(2);
        recordableVersionData.put("policy", recordableVersionPolicy.toString());
        recordableVersionData.put("selected", isVersionPolicySelected(recordableVersionPolicy, nodeRef));
        return recordableVersionData;
    }

    /**
     * Checks if the specified recordable version policy has been selected for the document
     *
     * @param recordableVersionPolicy The recordable version policy
     * @param nodeRef Node reference of the document
     * @return <code>true</code> if the specified recordable version policy has been selected for the document, <code>false</code> otherwise
     */
    private boolean isVersionPolicySelected(RecordableVersionPolicy recordableVersionPolicy, NodeRef nodeRef)
    {
        boolean isVersionPolicySelected = false;
        String policy = (String) getNodeService().getProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY);
        if (StringUtils.isNotBlank(policy))
        {
            if (RecordableVersionPolicy.valueOf(policy).equals(recordableVersionPolicy))
            {
                isVersionPolicySelected = true;
            }
        }
        else
        {
            if (recordableVersionPolicy.equals(RecordableVersionPolicy.NONE))
            {
                isVersionPolicySelected = true;
            }
        }
        return isVersionPolicySelected;
    }
}
