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

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;

/**
 * QuickShare/PublicView
 * 
 * GET web script that returns whether or not a user can read the shared content.
 * 
 * @author Alex Miller
 */
public class ReadGet extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(ReadGet.class);

    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, Status status, Cache cache)
    {
        if (!isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }

        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final String sharedId = params.get("shared_id");
        if (sharedId == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "A valid sharedId must be specified !");
        }

        try
        {
            boolean canRead = quickShareService.canRead(sharedId);
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("canRead", canRead);
            return result;
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
}
