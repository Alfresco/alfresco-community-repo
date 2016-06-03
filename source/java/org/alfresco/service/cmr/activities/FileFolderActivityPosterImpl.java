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
package org.alfresco.service.cmr.activities;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.Client;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A consolidated services for posting file folder activities.
 * Some code was moved from webdav.ActivityPosterImpl and
 * opencmis.ActivityPosterImpl.
 *
 * @author Gethin James
 */
public class FileFolderActivityPosterImpl implements ActivityPoster
{
    private ActivityService activityService;
 
    @Override
    public void postFileFolderActivity(
                String activityType,
                String path,
                String tenantDomain,
                String siteId,
                NodeRef parentNodeRef,
                NodeRef nodeRef,
                String fileName,
                String appTool,
                Client client,
                FileInfo fileInfo)
    {

        JSONObject json;
        try
        {
            json = createActivityJSON(tenantDomain, path, parentNodeRef, nodeRef, fileName);
        }
        catch (JSONException jsonError)
        {
            throw new AlfrescoRuntimeException("Unabled to create activities json", jsonError);
        }
        
        activityService.postActivity(
                    activityType,
                    siteId,
                    appTool,
                    json.toString(),
                    client,
                    fileInfo);
    }

    /**
     * Create JSON suitable for create, modify or delete activity posts.
     * 
     * @param tenantDomain String
     * @param path String
     * @param parentNodeRef NodeRef
     * @param nodeRef NodeRef
     * @param fileName String
     * @throws JSONException
     * @return JSONObject
     */
    protected JSONObject createActivityJSON(
                String tenantDomain,
                String path,
                NodeRef parentNodeRef,
                NodeRef nodeRef,
                String fileName) throws JSONException
    {
            JSONObject json = new JSONObject();

            json.put("nodeRef", nodeRef);
            
            if (parentNodeRef != null)
            {
                // Used for deleted files.
                json.put("parentNodeRef", parentNodeRef);
            }
            
            if (path != null)
            {
                // Used for deleted files and folders (added or deleted)
                json.put("page", "documentlibrary?path=" + path);
            }
            else
            {
                // Used for added or modified files.
                json.put("page", "document-details?nodeRef=" + nodeRef);
            }
            json.put("title", fileName);
            
            if (tenantDomain!= null && !tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                // Only used in multi-tenant setups.
                json.put("tenantDomain", tenantDomain);
            }
        
        return json;
    }

    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
}
