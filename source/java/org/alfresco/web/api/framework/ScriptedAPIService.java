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
package org.alfresco.web.api.framework;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.Node;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Wrapper;


/**
 * Script/template driven based implementation of an API Service
 *
 * @author davidc
 */
public class ScriptedAPIService extends AbstractAPIService 
{
    // Logger
    private static final Log logger = LogFactory.getLog(ScriptedAPIService.class);

    private String baseTemplatePath;
    private ScriptLocation executeScript;
    

    /* (non-Javadoc)
     * @see org.alfresco.web.api.AbstractAPIService#init(org.alfresco.web.api.APIRegistry)
     */
    @Override
    public void init(APIRegistry apiRegistry)
    {
        super.init(apiRegistry);
        baseTemplatePath = getDescription().getId().replace('.', '/');     
        
        // Test for "execute" script
        String scriptPath = baseTemplatePath + ".js";
        executeScript = getAPIRegistry().getScriptProcessor().findScript(scriptPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    final public void execute(APIRequest req, APIResponse res) throws IOException
    {
        // construct data model for template
        Map<String, Object> model = executeImpl(req, res);
        if (model == null)
        {
            model = new HashMap<String, Object>(7, 1.0f);
        }
        
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

        // process requested format
        String format = req.getFormat();
        if (format == null || format.length() == 0)
        {
            format = getDescription().getDefaultFormat();
        }
        
        String mimetype = getAPIRegistry().getFormatRegistry().getMimeType(req.getAgent(), format);
        if (mimetype == null)
        {
            throw new APIException("API format '" + format + "' is not registered");
        }

        // render output
        res.setContentType(mimetype + ";charset=UTF-8");
        
        if (logger.isDebugEnabled())
            logger.debug("Response content type: " + mimetype);
        
        try
        {
            Map<String, Object> templateModel = createTemplateModel(req, res, model);            
            renderFormatTemplate(format, templateModel, res.getWriter());
        }
        catch(TemplateException e)
        {
            throw new APIException("Failed to process format '" + format + "'", e);
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
                value = new TemplateNode(((Node)value).getNodeRef(), getServiceRegistry(), null);
            }
            else if (value instanceof Collection)
            {
                Collection coll = (Collection)value;
                Collection templateColl = new ArrayList(coll.size());
                for (Object object : coll)
                {
                    if (value instanceof Node)
                    {
                        templateColl.add(new TemplateNode(((Node)object).getNodeRef(), getServiceRegistry(), null));
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
                    templateNodes[i++] = new TemplateNode(node.getNodeRef(), getServiceRegistry(), null);
                }
                value = templateNodes;
            }
            templateModel.put(entry.getKey(), value);
        }
    }

    /**
     * Execute custom Java logic
     * 
     * @param req  API request
     * @param res  API response
     * @param model  basic API model
     * @return  custom service model
     */
    protected Map<String, Object> executeImpl(APIRequest req, APIResponse res)
    {
        return null;
    }
    
    /**
     * Render a template (of given format) to the API Response
     * 
     * @param format  template format (null, default format)  
     * @param model  data model to render
     * @param writer  where to output
     */
    final protected void renderFormatTemplate(String format, Map<String, Object> model, Writer writer)
    {
        format = (format == null) ? "" : format;
        String templatePath = baseTemplatePath + "_" + format + ".ftl";

        if (logger.isDebugEnabled())
            logger.debug("Rendering service template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }

}
