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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API to get the recorded version config for a document
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigGet extends AbstractRmWebScript
{
    /** Recordable version config service */
    private RecordableVersionConfigService recordableVersionConfigService;

    /**
     * Gets the recordable version config service
     *
     * @return The recordable version config service
     */
    protected RecordableVersionConfigService getRecordableVersionConfigService()
    {
        return this.recordableVersionConfigService;
    }

    /**
     * Sets the recordable version config service
     *
     * @param recordableVersionConfigService The recordable version config service
     */
    public void setRecordableVersionConfigService(RecordableVersionConfigService recordableVersionConfigService)
    {
        this.recordableVersionConfigService = recordableVersionConfigService;
    }

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);
        NodeRef nodeRef = parseRequestForNodeRef(req);
        List<Version> recordableVersions = getRecordableVersionConfigService().getVersions(nodeRef);
        model.put("recordableVersions", recordableVersions);
        return model;
    }
}
