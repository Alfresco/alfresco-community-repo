/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.transfer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.util.json.ExceptionJsonSerializer;
import org.alfresco.util.json.JsonSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author brian
 *
 */
public class TransferWebScript extends AbstractWebScript
{
    private static final Log log = LogFactory.getLog(TransferWebScript.class);
    
    private boolean enabled = true;
    private Map<String, CommandProcessor> processors = new TreeMap<String, CommandProcessor>();
    private JsonSerializer<Throwable, JSONObject> errorSerializer = new ExceptionJsonSerializer();
    
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
                        JSONObject errorObject = errorSerializer.serialize(ex);
                        String error = errorObject.toString();
                        
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

}
