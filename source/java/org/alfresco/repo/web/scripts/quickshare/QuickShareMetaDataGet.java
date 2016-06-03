package org.alfresco.repo.web.scripts.quickshare;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * QuickShare/PublicView
 * 
 * GET web script to get limited metadata (including thumbnail defs) for some "shared" content
 * 
 * WARNING: **unauthenticated** web script (equivalent to authenticated version - see MetaDataGet.java)
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class QuickShareMetaDataGet extends MetaDataGet
{
    private static final Log logger = LogFactory.getLog(QuickShareMetaDataGet.class);
    
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, Status status, Cache cache)
    {
        if (! isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }
        
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final String sharedId = params.get("shared_id");
        if (sharedId == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "A valid sharedId must be specified !");
        }
        
        try
        {
            return quickShareService.getMetaData(sharedId);
        }
        catch (InvalidSharedIdException ex)
        {
            logger.error("Unable to find: "+sharedId);
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: "+sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: "+sharedId);
        }
    }
}