/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.doclink;

import java.io.StringWriter;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.DocumentLinkService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * This class contains common code for doclink webscripts controllers
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public abstract class AbstractDocLink extends DeclarativeWebScript
{
    private static String PARAM_STORE_TYPE = "store_type";
    private static String PARAM_STORE_ID = "store_id";
    private static String PARAM_ID = "id";
    private static String PARAM_SITE = "site";
    private static String PARAM_CONTAINER = "container";
    private static String PARAM_PATH = "path";

    private static final String ACTIVITY_TOOL = "documentLinkService";

    protected NodeService nodeService;
    protected SiteService siteService;
    protected DocumentLinkService documentLinkService;
    protected ActivityService activityService;
    protected ServiceRegistry serviceRegistry;

    private static Log logger = LogFactory.getLog(AbstractDocLink.class);

    protected NodeRef parseNodeRefFromTemplateArgs(Map<String, String> templateVars)
    {
        if (templateVars == null)
        {
            return null;
        }

        String storeTypeArg = templateVars.get(PARAM_STORE_TYPE);
        String storeIdArg = templateVars.get(PARAM_STORE_ID);
        String idArg = templateVars.get(PARAM_ID);

        if (storeTypeArg != null)
        {
            ParameterCheck.mandatoryString("storeTypeArg", storeTypeArg);
            ParameterCheck.mandatoryString("storeIdArg", storeIdArg);
            ParameterCheck.mandatoryString("idArg", idArg);

            /*
             * NodeRef based request
             * <url>URL_BASE/{store_type}/{store_id}/{id}</url>
             */
            return new NodeRef(storeTypeArg, storeIdArg, idArg);
        }
        else
        {
            String siteArg = templateVars.get(PARAM_SITE);
            String containerArg = templateVars.get(PARAM_CONTAINER);
            String pathArg = templateVars.get(PARAM_PATH);

            if (siteArg != null)
            {
                ParameterCheck.mandatoryString("siteArg", siteArg);
                ParameterCheck.mandatoryString("containerArg", containerArg);

                /*
                 * Site based request <url>URL_BASE/{site}/{container}</url> or
                 * <url>URL_BASE/{site}/{container}/{path}</url>
                 */
                SiteInfo site = siteService.getSite(siteArg);
                PropertyCheck.mandatory(this, "site", site);

                NodeRef node = siteService.getContainer(site.getShortName(), containerArg);
                if (node == null)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid 'container' variable");
                }

                if (pathArg != null)
                {
                    // <url>URL_BASE/{site}/{container}/{path}</url>
                    StringTokenizer st = new StringTokenizer(pathArg, "/");
                    while (st.hasMoreTokens())
                    {
                        String childName = st.nextToken();
                        node = nodeService.getChildByName(node, ContentModel.ASSOC_CONTAINS, childName);
                        if (node == null)
                        {
                            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid 'path' variable");
                        }
                    }
                }

                return node;
            }
        }
        return null;
    }

    /**
     * Generates an activity entry for the link
     */

    protected void addActivityEntry(String activityType, String title, String nodeRef, String site)
    {
       try
       {
          StringWriter activityJson = new StringWriter();
          JSONWriter activity = new JSONWriter(activityJson);
          activity.startObject();
          activity.writeValue("title", title);
          activity.writeValue("nodeRef", nodeRef);
          activity.writeValue("page", "document-details?nodeRef=" + nodeRef);
          activity.endObject();

          activityService.postActivity(
                activityType,
                site,
                ACTIVITY_TOOL,
                activityJson.toString());
       }
       catch (Exception e)
       {
          // Warn, but carry on
          logger.warn("Error adding link event to activities feed", e);
       }
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setDocumentLinkService(DocumentLinkService documentLinkService)
    {
        this.documentLinkService = documentLinkService;
    }

    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

}
