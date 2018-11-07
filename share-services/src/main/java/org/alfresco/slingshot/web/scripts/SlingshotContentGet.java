/*
 * #%L
 * Alfresco Share Services AMP
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
package org.alfresco.slingshot.web.scripts;

import java.io.IOException;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.sync.repo.Client;
import org.alfresco.sync.repo.Client.ClientType;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.activities.ActivityPoster;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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
    private ActivityPoster poster;
    private RetryingTransactionHelper transactionHelper;

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setPoster(ActivityPoster poster)
    {
        this.poster = poster;
    }

    public void setTransactionHelper(RetryingTransactionHelper transactionHelper)
    {
        this.transactionHelper = transactionHelper;
    }


    @Override
    public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        // are we downloading content as an attachment?
        if (Boolean.valueOf(req.getParameter("a")))
        {
            // is this    private ActivityPoster poster; node part of a Site context?
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String nodeId = templateVars.get("id");

            // create the NodeRef and ensure it is valid
            if (storeType != null && storeId != null && nodeId != null)
            {
                // MNT-16380
                String nodeIdTmp = nodeId;
                if (nodeId.contains("/"))
                {
                    nodeIdTmp = nodeId.substring(0, nodeId.indexOf('/'));
                }
                final NodeRef nodeRef = new NodeRef(storeType, storeId, nodeIdTmp);
                SiteInfo site = null;
                try
                {
                    site = this.siteService.getSite(nodeRef);
                }
                catch (AccessDeniedException ade)
                {
                    // We don't have access to the site, don't post any permissions
                }
                if (site != null)
                {
                    // found a valid parent Site - gather the details to post an Activity
                    String filename = templateVars.get("filename");
                    if (filename == null || filename.length() == 0)
                    {
                        filename = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                        if (nodeId.contains("/"))
                        {
                            filename = nodeId.substring(nodeId.lastIndexOf("/") + 1);
                        }
                    }
                    final String strFilename = filename;
                    final String siteName = site.getShortName();
                    transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                            // post an activity - mirror the mechanism as if from the Share application
                            poster.postFileFolderActivity(ActivityPoster.DOWNLOADED, null, null,
                                    siteName, null, nodeRef, strFilename, "documentlibrary", Client.asType(ClientType.webclient), null);
                            return null;
                        }
                    }, false, true);
                }
            }
        }
        super.execute(req, res);
    }
}
