package org.alfresco.repo.web.scripts.quickshare;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * QuickShare/PublicView
 * 
 * GET web script to get limited metadata (including thumbnail defs) => authenticated web script (using a nodeRef)
 * 
 * Note: authenticated web script (equivalent to unauthenticated version - see QuickShareMetaDataGet)
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class MetaDataGet extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(QuickShareMetaDataGet.class);
    
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, Status status, Cache cache)
    {
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final NodeRef nodeRef = WebScriptUtil.getNodeRef(params);
        if (nodeRef == null)
        {
            String msg = "A valid NodeRef must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        
        try
        {
            Map<String, Object> model = quickShareService.getMetaData(nodeRef);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Retrieved limited metadata: "+nodeRef+" ["+model+"]");
            }
            
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find node: "+inre.getNodeRef());
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find nodeRef: "+inre.getNodeRef());
        }
    }
}