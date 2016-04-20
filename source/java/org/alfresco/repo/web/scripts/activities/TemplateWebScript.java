package org.alfresco.repo.web.scripts.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
/**
 * Java-backed WebScript to get a Template from a Template Store
 */
public class TemplateWebScript extends DeclarativeWebScript
{
    // Logger
    protected static final Log logger = LogFactory.getLog(TemplateWebScript.class);
    
    private SearchPath searchPath;
    
    public void setSearchPath(SearchPath searchPath)
    {
        this.searchPath = searchPath;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // process extension
        String path = req.getExtensionPath(); // required
        
        if ((path == null) || (path.length() == 0))
        {
            String msg = "Failed to getTemplate: missing {path}";
            logger.error(msg);
            throw new AlfrescoRuntimeException(msg);
        }
        else
        {        
            if (path.endsWith(".ftl"))
            {    
                try
                {
                    InputStream is = searchPath.getDocument(path);
                    if (is != null)
                    {         
                        BufferedReader br = null;
                        try
                        {
                            br = new BufferedReader(new InputStreamReader(is));
                            String line = null;
                            StringBuffer sb = new StringBuffer();
                            while(((line = br.readLine()) !=null)) 
                            {
                                sb.append(line);
                            }
                    
                            model.put("template", sb.toString());
                        }
                        finally
                        {
                            if (br != null) { br.close(); };
                        }
                    }
                }
                catch (IOException ioe)
                {
                    logger.error("Failed to getTemplate: " + ioe);
                }
           }
        }
        return model;
    }
}
