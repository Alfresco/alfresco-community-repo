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
package org.alfresco.web.api;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.alfresco.service.cmr.repository.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Script based implementation of an API Service
 *
 * @author davidc
 */
public class ScriptedAPIService extends AbstractAPIService 
{
    // Logger
    private static final Log logger = LogFactory.getLog(ScriptedAPIService.class);

    private String baseTemplatePath;
    
    
    @Override
    public void init(APIRegistry apiRegistry)
    {
        super.init(apiRegistry);
        baseTemplatePath = getDescription().getId().replace('.', '/');     
        
        // TODO: Test for .js script
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    final public void execute(APIRequest req, APIResponse res) throws IOException
    {
        // construct data model for template
        Map<String, Object> model = createAPIModel(req, res);
        model = createModel(req, res, model); 
        
        // TODO: execute script if it exists

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
            renderFormatTemplate(format, model, res.getWriter());
        }
        catch(TemplateException e)
        {
            throw new APIException("Failed to process format '" + format + "'", e);
        }
    }
    

    /**
     * Create a custom service model
     * 
     * @param req  API request
     * @param res  API response
     * @param model  basic API model
     * @return  custom service model
     */
    protected Map<String, Object> createModel(APIRequest req, APIResponse res, Map<String, Object> model)
    {
        return model;
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
