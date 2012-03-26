/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebDAV methods may use an instance of this class to post activity data.
 * 
 * @see ActivityPoster
 * @author Matt Ward
 */
public class ActivityPosterImpl implements ActivityPoster
{
    private static final String FILE_ADDED = "org.alfresco.documentlibrary.file-added";
    private static final String FILE_UPDATED = "org.alfresco.documentlibrary.file-updated";
    private static final String FILE_DELETED = "org.alfresco.documentlibrary.file-deleted";
    private static final String APP_TOOL = "WebDAV";
    private final ActivityService activityService;
    private final NodeService nodeService;
    private final PersonService personService;
    
    
    /**
     * Constructor
     * 
     * @param activityService
     * @param nodeService
     * @param personService
     */
    public ActivityPosterImpl(ActivityService activityService, NodeService nodeService, PersonService personService)
    {
        this.activityService = activityService;
        this.nodeService = nodeService;
        this.personService = personService;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileAdded(
                String siteId,
                String tenantDomain,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        postFileActivity(FILE_ADDED, siteId, tenantDomain, null, contentNodeInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileUpdated(
                String siteId,
                String tenantDomain,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        postFileActivity(FILE_UPDATED, siteId, tenantDomain, null, contentNodeInfo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileDeleted(
                String siteId,
                String tenantDomain,
                String parentPath,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        postFileActivity(FILE_DELETED, siteId, tenantDomain, parentPath, contentNodeInfo);
    }
    
    
    private void postFileActivity(
                String activityType,
                String siteId,
                String tenantDomain,
                String parentPath,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        Pair<String, String> personName = getPersonName();
        final String firstName = personName.getFirst();
        final String lastName = personName.getSecond();
        final String fileName = contentNodeInfo.getName();
        final NodeRef nodeRef = contentNodeInfo.getNodeRef();
        JSONObject json = createActivityJSON(tenantDomain, parentPath, nodeRef, firstName, lastName, fileName);
        
        activityService.postActivity(
                    activityType,
                    siteId,
                    APP_TOOL,
                    json.toString());
    }
    
    /**
     * Create JSON suitable for create, modify or delete activity posts. Returns a new JSONObject
     * containing appropriate key/value pairs.
     * 
     * @param tenantDomain
     * @param nodeRef
     * @param firstName
     * @param lastName
     * @param fileName
     * @throws WebDAVServerException
     * @return JSONObject
     */
    private JSONObject createActivityJSON(
                String tenantDomain,
                String parentPath,
                NodeRef nodeRef,
                String firstName,
                String lastName,
                String fileName) throws WebDAVServerException
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("nodeRef", nodeRef);
            if (parentPath != null)
            {
                // Used for deleted files.
                json.put("page", "documentlibrary?path=" + parentPath);                
            }
            else
            {
                // Used for added or modified files.
                json.put("page", "document-details?nodeRef=" + nodeRef);
            }
            json.put("title", fileName);
            json.put("firstName", firstName);
            json.put("lastName", lastName);
            if (!tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                // Only used in multi-tenant setups.
                json.put("tenantDomain", tenantDomain);
            }
        }
        catch (JSONException error)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return json;
    }

    /**
     * Creates the tuple (firstName, lastName) for the current user.
     * 
     * @return Pair&lt;String, String&gt;
     */
    private Pair<String, String> getPersonName()
    {
        String firstName = "";
        String lastName = "";
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef person = personService.getPerson(userName);
        if (person != null)
        {
            firstName = (String) nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
            lastName = (String) nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
        }
        
        return new Pair<String, String>(firstName, lastName);
    }
}
