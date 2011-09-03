/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.publishing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * @author Brian
 * @since 4.0
 */
public class ChannelTypeIconGetWebScript extends AbstractWebScript
{
    private ChannelService channelService;
    private MimetypeService mimetypeService;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        res.setContentType("text/html");
        res.setContentEncoding("UTF-8");
        
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String channelTypeId = templateVars.get("channelType");
        String iconSize = templateVars.get("iconSize");
        
        if (channelTypeId == null || iconSize == null)
        {
            res.setStatus(400);  //Bad request
            return;
        }
        ChannelType channelType = channelService.getChannelType(channelTypeId);
        if (channelType == null)
        {
            res.setStatus(404);  // Not found
            return;
        }
        
        Resource iconFile = channelType.getIcon(iconSize);
        if (iconFile == null || !iconFile.exists())
        {
            res.setStatus(404);  //Not found
            return;
        }
        
        res.setHeader("Content-Length", "" + iconFile.contentLength());
        String mimeType = mimetypeService.getMimetype(channelType.getIconFileExtension());
        res.setContentType(mimeType);
        OutputStream out = res.getOutputStream();
        InputStream in = iconFile.getInputStream();  
        FileCopyUtils.copy(in, out);
        in.close();
    }
}
