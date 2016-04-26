package org.alfresco.repo.web.scripts.download;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.download.DownloadService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Base class for download related webscripts.
 *
 * @author Alex Miller
 */
abstract class AbstractDownloadWebscript extends DeclarativeWebScript
{
    // Shared dependencies
    protected DownloadService downloadService;
    
    public void setDownloadService(DownloadService downloadSerivce)
    {
        this.downloadService = downloadSerivce;
    }
    
    /**
     * Helper method to embed error informaion in a map.
     */
    protected Map<String,Object> buildError(String message)
    {
       HashMap<String, Object> result = new HashMap<String, Object>();
       result.put("error", message);
       
       HashMap<String, Object> model = new HashMap<String, Object>();
       model.put("error", message);
       model.put("result", result);
       
       return model;
    }

}
