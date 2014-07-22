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
package org.alfresco.slingshot.web.scripts;

import java.io.IOException;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * Share specific ContentGet implementation.
 * <p>
 * Checks to see if:
 * a) the request is an explicit download (attachment)
 * b) the requested NodeRef within the context of a Share Site
 * <p>
 * If both tests are true then generates an Activity feed item to record the Download request.
 * All other requests and any further processing is performed by the super class.
 * 
 * @author Kevin Roast
 */
public class SlingshotContentGet extends ContentGet
{
    protected SiteService siteService;
    protected ActivityService activityService;
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.content.ContentGet#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        // are we downloading content as an attachment?
        if (Boolean.valueOf(req.getParameter("a")))
        {
            // is this node part of a Site context?
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String nodeId = templateVars.get("id");
            
            // create the NodeRef and ensure it is valid
            if (storeType != null && storeId != null && nodeId != null)
            {
                final NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
                final SiteInfo site = this.siteService.getSite(nodeRef);
                if (site != null)
                {
                    // found a valid parent Site - gather the details to post an Activity
                    String filename = templateVars.get("filename");
                    if (filename == null || filename.length() == 0)
                    {
                        filename = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                    }
                    StringBuilderWriter out = new StringBuilderWriter(256);
                    final JSONWriter json = new JSONWriter(out);
                    json.startObject();
                    json.writeValue("title",   filename);
                    json.writeValue("nodeRef", nodeRef.toString());
                    json.writeValue("page",    "document-details?nodeRef=" + nodeRef.toString());
                    json.endObject();
                    
                    // post an activity - mirror the mechanism as if from the Share application
                    this.activityService.postActivity(
                            "org.alfresco.documentlibrary.file-downloaded",
                            site.getShortName(),
                            "documentlibrary",
                            out.toString());
                }
            }
        }
        super.execute(req, res);
    }
}