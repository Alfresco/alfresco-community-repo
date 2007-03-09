/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.scripts.AbstractWebScript;
import org.alfresco.web.scripts.WebScript;
import org.alfresco.web.scripts.WebScriptDescription;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;


/**
 * Retrieves a Web Script Description Document 
 * 
 * @author davidc
 */
public class ServiceDescription extends AbstractWebScript
{

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // extract web script id
        String extensionPath = req.getExtensionPath();
        if (extensionPath == null || extensionPath.length() == 0)
        {
            throw new WebScriptException("Web Script Id not provided");
        }
        String scriptId = extensionPath.replace("/", ".");
        
        // locate web script
        WebScript script = getWebScriptRegistry().getWebScript(scriptId);
        if (script == null)
        {
            throw new WebScriptException("Web Script Id '" + scriptId + "' not found");
        }

        // retrieve description document
        WebScriptDescription desc = script.getDescription();
        InputStream serviceDescIS = null;
        try
        {
            serviceDescIS = desc.getSourceDocument();
            OutputStream out = res.getOutputStream();
            res.setContentType(MimetypeMap.MIMETYPE_XML + ";charset=UTF-8");
            byte[] buffer = new byte[2048];
            int read = serviceDescIS.read(buffer);
            while (read != -1)
            {
                out.write(buffer, 0, read);
                read = serviceDescIS.read(buffer);
            }
        }
        catch(IOException e)
        {
            throw new WebScriptException("Failed to read Web Script description document for '" + scriptId + "'", e);
        }
        finally
        {
            try
            {
                if (serviceDescIS != null) serviceDescIS.close();
            }
            catch(IOException e)
            {
                // NOTE: ignore close exception
            }
        }
    }

}
