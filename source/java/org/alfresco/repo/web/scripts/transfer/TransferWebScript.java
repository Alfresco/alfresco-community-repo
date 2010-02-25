/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.web.scripts.transfer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.transfer.TransferException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * @author brian
 *
 */
public class TransferWebScript extends AbstractWebScript
{
    private static final Log log = LogFactory.getLog(TransferWebScript.class);
    
    private boolean enabled = true;
    private Map<String, CommandProcessor> processors = new TreeMap<String, CommandProcessor>();

    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    public void setCommandProcessors(Map<String, CommandProcessor> processors) 
    {
        this.processors = new TreeMap<String,CommandProcessor>(processors);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        if (enabled) 
        {
            log.debug("Transfer webscript invoked by user: " + AuthenticationUtil.getFullyAuthenticatedUser() + 
                    " running as " + AuthenticationUtil.getRunAsAuthentication().getName());
            processCommand(req.getServiceMatch().getTemplateVars().get("command"), req, res);
        }
        else
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
        }
    }

    /**
     * @param command
     * @param req
     * @param res
     */
    private void processCommand(String command, WebScriptRequest req, WebScriptResponse res)
    {
        log.debug("Received request to process transfer command: " + command);
        if (command == null || (command = command.trim()).length() == 0)
        {
            log.warn("Empty or null command received by the transfer script. Returning \"Not Found\"");
            res.setStatus(Status.STATUS_NOT_FOUND);
        }
        else
        {
            CommandProcessor processor = processors.get(command);
            if (processor != null) 
            {
                log.debug("Found appropriate command processor: " + processor);
                try 
                {
                    processor.process(req, res);
                    log.debug("command processed");
                } 
                catch (TransferException ex) 
                {
                    try 
                    {
                        log.debug("transfer exception caught", ex);
                        res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
                        String error = writeError(ex);
                        
                        res.setContentType("application/json");
                        res.setContentEncoding("UTF-8");
                        int length = error.getBytes("UTF-8").length;
                        res.addHeader("Content-Length", "" + length);

                        res.getWriter().write(error);
                    } 
                    catch (Exception e) 
                    {
                        //nothing to do at this point really.
                    }
                }
            }
            else
            {
                log.warn("No processor found for requested command: " + command + ". Returning \"Not Found\"");
                res.setStatus(Status.STATUS_NOT_FOUND);
            }
        }
    }

    /**
     * @param ex
     * @return
     */
    private String writeError(TransferException ex) throws IOException
    {
        StringWriter stringWriter = new StringWriter(300);
        JSONWriter jsonWriter = new JSONWriter(stringWriter);
        jsonWriter.startObject();
        jsonWriter.writeValue("errorId", ex.getMsgId());
        jsonWriter.startValue("errorParams");
        jsonWriter.startArray();
        writeErrorParams(stringWriter, ex.getMsgParams());
        jsonWriter.endArray();
        jsonWriter.endObject();
        return stringWriter.toString();
    }

    /**
     * @param stringWriter
     * @param msgParams
     */
    private void writeErrorParams(StringWriter writer, Object[] msgParams)
    {
        if (msgParams == null) return;
        
        boolean first = true;
        for (Object param : msgParams) {
            if (!first) {
                writer.write(",");
            }
            if (param != null) {
                writer.write("\"");
                writer.write(JSONWriter.encodeJSONString(param.toString()));
                writer.write("\"");
            } else {
                writer.write("null");
            }
            first = false;
        }
    }

}
