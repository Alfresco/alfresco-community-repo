
package org.alfresco.repo.web.scripts.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ScriptPagingDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the deletednodes.delete web script.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class ArchivedNodesDelete extends AbstractArchivedNodeWebScript
{
    private static Log log = LogFactory.getLog(ArchivedNodesDelete.class);
    
    public static final String PURGED_NODES = "purgedNodes";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // Current user
        String userID = AuthenticationUtil.getFullyAuthenticatedUser();
        if (userID == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script ["
                        + req.getServiceMatch().getWebScript().getDescription()
                        + "] requires user authentication.");
        }

        StoreRef storeRef = parseRequestForStoreRef(req);
        NodeRef nodeRef = parseRequestForNodeRef(req);
        
        List<NodeRef> nodesToBePurged = new ArrayList<NodeRef>();
        if (nodeRef != null)
        {
            // check if the current user has the permission to purge the node
            validatePermission(nodeRef, userID);
            
            // If there is a specific NodeRef, then that is the only Node that should be purged.
            // In this case, the NodeRef points to the actual node to be purged i.e. the node in
            // the archive store.
            nodesToBePurged.add(nodeRef);
        }
        else
        {
            // But if there is no specific NodeRef and instead there is only a StoreRef, then
            // all nodes which were originally in that StoreRef should be purged.
            // Create paging
            ScriptPagingDetails paging = new ScriptPagingDetails(maxSizeView, 0);
            PagingResults<NodeRef> result = getArchivedNodesFrom(storeRef, paging, null);
            nodesToBePurged.addAll(result.getPage());            
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("Purging " + nodesToBePurged.size() + " nodes");
        }
        
        // Now having identified the nodes to be purged, we simply have to do it.
        nodeArchiveService.purgeArchivedNodes(nodesToBePurged);

        model.put(PURGED_NODES, nodesToBePurged);
        
        return model;
    }
}
