package org.alfresco.repo.web.scripts.quickshare;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.QuickShareModel;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * QuickShare/PublicView
 * 
 * DELETE web script to "Unshare" access to some content (ie. disable unauthenticated access to this node)
 * 
 * Note: authenticated web script
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class UnshareContentDelete extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(ShareContentPost.class);

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
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
            NodeRef nodeRef = quickShareService.getTenantNodeRefFromSharedId(sharedId).getSecond();

            String sharedBy = (String) nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDBY);
            if (!quickShareService.canDeleteSharedLink(nodeRef, sharedBy))
            {
                throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Can't perform unshare action: " + sharedId);
            }
            quickShareService.unshareContent(sharedId);

            Map<String, Object> model = new HashMap<>(1);
            model.put("success", Boolean.TRUE);
            return model;
        }
        catch (InvalidSharedIdException ex)
        {
            logger.error("Unable to find: " + sharedId);
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: " + sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find: " + sharedId + " [" + inre.getNodeRef() + "]");
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: " + sharedId);
        }
    }
}