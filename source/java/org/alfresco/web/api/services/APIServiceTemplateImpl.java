/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.api.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.web.api.APIException;
import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Template based implementation of an API Service
 *
 * @author davidc
 */
public abstract class APIServiceTemplateImpl extends APIServiceImpl 
{
    // Logger
    private static final Log logger = LogFactory.getLog(APIServiceTemplateImpl.class);

    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    public void execute(APIRequest req, APIResponse res) throws IOException
    {
        // construct data model for template
        Map<String, Object> model = createAPIModel(req, res);
        model = createModel(req, res, model); 

        // process requested format
        String format = req.getFormat();
        if (format == null || format.length() == 0)
        {
            format = getDefaultFormat();
        }
        String mimetype = getFormatRegistry().getMimeType(req.getAgent(), format);
        if (mimetype == null)
        {
            throw new APIException("API format '" + format + "' does not exist");
        }

        // render output
        res.setContentType(mimetype + ";charset=UTF-8");
        
        if (logger.isDebugEnabled())
            logger.debug("Response content type: " + mimetype);
        
        try
        {
            renderTemplate(null, format, model, res.getWriter());
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
    protected abstract Map<String, Object> createModel(APIRequest req, APIResponse res, Map<String, Object> model);

    
    /**
     * Render a template to the API Response
     * 
     * @param type  type of template (null defaults to type VIEW)
     * @param format  template format (null, default format)  
     * @param model  data model to render
     * @param writer  where to output
     */
    protected void renderTemplate(String type, String format, Map<String, Object> model, Writer writer)
    {
        type = (type == null) ? "view" : type;
        format = (format == null) ? "" : format;
        String templatePath = (this.getClass().getSimpleName() + "_" + type + "_" + format).replace(".", "/") + ".ftl";

        if (logger.isDebugEnabled())
            logger.debug("Rendering service template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }

    
    /**
     * Simple test that can be executed outside of web context
     */
    /*package*/ void test(String format)
        throws IOException
    {
        // create test model
        Map<String, Object> model = createTestModel();
        
        // render service template to string
        StringWriter rendition = new StringWriter();
        PrintWriter writer = new PrintWriter(rendition);
        renderTemplate(null, format, model, writer);
        System.out.println(rendition.toString());
    }
    
}
