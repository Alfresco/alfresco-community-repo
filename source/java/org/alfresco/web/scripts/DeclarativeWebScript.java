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
package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.jscript.Node;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Wrapper;


/**
 * Script/template driven based implementation of an Web Script
 *
 * @author davidc
 */
public class DeclarativeWebScript extends AbstractWebScript 
{
    // Logger
    private static final Log logger = LogFactory.getLog(DeclarativeWebScript.class);

    private String basePath;
    private ScriptLocation executeScript;
    

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractWebScript#init(org.alfresco.web.scripts.WebScriptRegistry)
     */
    @Override
    public void init(WebScriptRegistry apiRegistry)
    {
        super.init(apiRegistry);
        basePath = getDescription().getId();
        
        // Test for "execute" script
        String scriptPath = basePath + ".js";
        executeScript = getWebScriptRegistry().getScriptProcessor().findScript(scriptPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    final public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // retrieve requested format
        String format = req.getFormat();
        if (format == null || format.length() == 0)
        {
            format = getDescription().getDefaultFormat();
        }

        try
        {
            // establish mimetype from format
            String mimetype = getWebScriptRegistry().getFormatRegistry().getMimeType(req.getAgent(), format);
            if (mimetype == null)
            {
                throw new WebScriptException("Web Script format '" + format + "' is not registered");
            }
            
            // construct data model for template
            WebScriptStatus status = new WebScriptStatus();
            Map<String, Object> model = executeImpl(req, status);
            if (model == null)
            {
                model = new HashMap<String, Object>(7, 1.0f);
            }
            model.put("status", status);
            
            // execute script if it exists
            if (executeScript != null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Executing script " + executeScript);
                
                Map<String, Object> scriptModel = createScriptModel(req, res, model);
                // add return model allowing script to add items to template model
                Map<String, Object> returnModel = new ScriptableHashMap<String, Object>();
                scriptModel.put("model", returnModel);
                executeScript(executeScript, scriptModel);
                mergeScriptModelIntoTemplateModel(returnModel, model);
            }
    
            // create model for template rendering
            Map<String, Object> templateModel = createTemplateModel(req, res, model);            
            
            // is a redirect to a status specific template required?
            if (status.getRedirect())
            {
                sendStatus(req, res, status, format, templateModel);
            }
            else
            {
                // render output
                int statusCode = status.getCode();
                if (statusCode != HttpServletResponse.SC_OK && !req.forceSuccessStatus())
                {
                    logger.debug("Force success status header in response: " + req.forceSuccessStatus());
                    logger.debug("Setting status " + statusCode);
                    res.setStatus(statusCode);
                }
                
                String callback = req.getJSONCallback();
                if (format.equals(WebScriptResponse.JSON_FORMAT) && callback != null)
                {
                    
                    if (logger.isDebugEnabled())
                        logger.debug("Rendering JSON callback response: content type=" + MimetypeMap.MIMETYPE_TEXT_JAVASCRIPT + ", status=" + statusCode + ", callback=" + callback);
                    
                    // NOTE: special case for wrapping JSON results in a javascript function callback
                    res.setContentType(MimetypeMap.MIMETYPE_TEXT_JAVASCRIPT + ";charset=UTF-8");
                    res.getOutputStream().write((callback + "(").getBytes());
                }
                else
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Rendering response: content type=" + mimetype + ", status=" + statusCode);

                    res.setContentType(mimetype + ";charset=UTF-8");
                }
            
                // render response according to requested format
                renderFormatTemplate(format, templateModel, res.getWriter());
                
                if (format.equals(WebScriptResponse.JSON_FORMAT) && callback != null)
                {
                    // NOTE: special case for wrapping JSON results in a javascript function callback
                    res.getOutputStream().write(")".getBytes());
                }
            }
        }
        catch(Throwable e)
        {
            // extract status code, if specified
            int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            if (e instanceof WebScriptException)
            {
                statusCode = ((WebScriptException)e).getStatus();
            }

            // send status
            WebScriptStatus status = new WebScriptStatus();
            status.setCode(statusCode);
            status.setMessage(e.getMessage());
            status.setException(e);
            Map<String, Object> customModel = new HashMap<String, Object>();
            customModel.put("status", status);
            Map<String, Object> templateModel = createTemplateModel(req, res, customModel);
            sendStatus(req, res, status, format, templateModel);
        }
    }
    
    /**
     * Merge script generated model into template-ready model
     * 
     * @param scriptModel  script model
     * @param templateModel  template model
     */
    @SuppressWarnings("unchecked")
    final private void mergeScriptModelIntoTemplateModel(Map<String, Object> scriptModel, Map<String, Object> templateModel)
    {
        for (Map.Entry<String, Object> entry : scriptModel.entrySet())
        {
            // retrieve script model value
            Object value = entry.getValue();
            
            // convert from js to java, if required
            if (value instanceof Wrapper)
            {
                value = ((Wrapper)value).unwrap();
            }
            else if (value instanceof NativeArray)
            {
                value = Context.jsToJava(value, Object[].class);
            }
            
            // convert script node to template node, if required
            if (value instanceof Node)
            {
                value = new TemplateNode(((Node)value).getNodeRef(), getServiceRegistry(), getWebScriptRegistry().getTemplateImageResolver());
            }
            else if (value instanceof Collection)
            {
                Collection coll = (Collection)value;
                Collection templateColl = new ArrayList(coll.size());
                for (Object object : coll)
                {
                    if (value instanceof Node)
                    {
                        templateColl.add(new TemplateNode(((Node)object).getNodeRef(), getServiceRegistry(), getWebScriptRegistry().getTemplateImageResolver()));
                    }
                    else
                    {
                        templateColl.add(object);
                    }
                }
                value = templateColl;
            }
            else if (value instanceof Node[])
            {
                Node[] nodes = (Node[])value;
                TemplateNode[] templateNodes = new TemplateNode[nodes.length];
                int i = 0;
                for (Node node : nodes)
                {
                    templateNodes[i++] = new TemplateNode(node.getNodeRef(), getServiceRegistry(), getWebScriptRegistry().getTemplateImageResolver());
                }
                value = templateNodes;
            }
            templateModel.put(entry.getKey(), value);
        }
    }

    /**
     * Execute custom Java logic
     * 
     * @param req  Web Script request
     * @return  custom service model
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status)
    {
        return null;
    }
    
    /**
     * Render a template (of given format) to the Web Script Response
     * 
     * @param format  template format (null, default format)  
     * @param model  data model to render
     * @param writer  where to output
     */
    final protected void renderFormatTemplate(String format, Map<String, Object> model, Writer writer)
    {
        format = (format == null) ? "" : format;
        String templatePath = basePath + "." + format + ".ftl";

        if (logger.isDebugEnabled())
            logger.debug("Rendering template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }

}
