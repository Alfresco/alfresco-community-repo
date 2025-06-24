/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.quickshare;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.web.context.ServletContextAware;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * QuickShare/PublicView
 * 
 * GET web script to get stream "shared" content (ie. enabled for public/unauthenticated access) from the repository
 *
 * WARNING: **unauthenticated** web script (equivalent to authenticated version - see ContentGet.java)
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class QuickShareContentGet extends ContentGet implements ServletContextAware
{
    private static final Log logger = LogFactory.getLog(QuickShareContentGet.class);

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private QuickShareService quickShareSerivce;

    private boolean enabled = true;

    public void setServletContext(ServletContext servletContext)
    {}

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
        super.setNodeService(nodeService);
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareSerivce = quickShareService;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    protected boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        if (!isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }

        // create map of template vars (params)
        final Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final String sharedId = params.get("shared_id");
        if (sharedId == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "A valid sharedId must be specified !");
        }

        try
        {
            Pair<String, NodeRef> pair = quickShareSerivce.getTenantNodeRefFromSharedId(sharedId);
            final String tenantDomain = pair.getFirst();
            final NodeRef nodeRef = pair.getSecond();

            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>() {
                public Void doWork() throws Exception
                {
                    if (!nodeService.getAspects(nodeRef).contains(QuickShareModel.ASPECT_QSHARE))
                    {
                        throw new InvalidNodeRefException(nodeRef);
                    }

                    // MNT-21118 (XSS prevention)
                    // Force the attachment in case of asking for the content file only
                    // (will be overridden for thumbnails)
                    executeImpl(nodeRef, params, req, res, null, true);

                    return null;
                }
            }, tenantDomain);

            if (logger.isDebugEnabled())
            {
                logger.debug("QuickShare - retrieved content: " + sharedId + " [" + nodeRef + "]");
            }

        }
        catch (InvalidSharedIdException ex)
        {
            logger.error("Unable to find: " + sharedId);
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: " + sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find: " + sharedId + " [" + inre.getNodeRef() + "]");
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: " + sharedId);
        }
    }

    protected void executeImpl(NodeRef nodeRef, Map<String, String> templateVars, WebScriptRequest req, WebScriptResponse res, Map<String, Object> model, boolean attach) throws IOException
    {
        // render content
        QName propertyQName = ContentModel.PROP_CONTENT;
        String contentPart = templateVars.get("property");
        if (contentPart.length() > 0 && contentPart.charAt(0) == ';')
        {
            if (contentPart.length() < 2)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Content property malformed");
            }
            String propertyName = contentPart.substring(1);
            if (propertyName.length() > 0)
            {
                propertyQName = QName.createQName(propertyName, namespaceService);
            }
        }

        // Stream the content
        streamContentLocal(req, res, nodeRef, attach, propertyQName, model);
    }
}
