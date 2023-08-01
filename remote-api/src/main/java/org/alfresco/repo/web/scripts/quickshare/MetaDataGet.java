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

import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * QuickShare/PublicView
 * 
 * GET web script to get limited metadata (including thumbnail defs) => authenticated web script (using a nodeRef)
 * 
 * Note: authenticated web script (equivalent to unauthenticated version - see QuickShareMetaDataGet)
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class MetaDataGet extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(QuickShareMetaDataGet.class);
    
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, Status status, Cache cache)
    {
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final NodeRef nodeRef = WebScriptUtil.getNodeRef(params);
        if (nodeRef == null)
        {
            String msg = "A valid NodeRef must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        
        try
        {
            Map<String, Object> model = quickShareService.getMetaData(nodeRef);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Retrieved limited metadata: "+nodeRef+" ["+model+"]");
            }
            
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find node: "+inre.getNodeRef());
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find nodeRef: "+inre.getNodeRef());
        }
    }
}