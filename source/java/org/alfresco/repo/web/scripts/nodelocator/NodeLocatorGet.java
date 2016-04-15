
package org.alfresco.repo.web.scripts.nodelocator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class NodeLocatorGet extends DeclarativeWebScript
{
    private static final String NODE_ID = "node_id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";
    private static final String NODE_LOCATOR_NAME = "node_locator_name";
    private NodeLocatorService locatorService;
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        // getting task id from request parameters
        String locatorName = vars.get(NODE_LOCATOR_NAME);

        // No locatorname specified -> return 404
        if (locatorName == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "No NodeLocator strategy was specified!");
        }

        NodeRef source = null;
        String storeType = vars.get(STORE_TYPE);
        String storeId= vars.get(STORE_ID);
        String nodeId= vars.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null)
        {
            source = new NodeRef(storeType, storeId, nodeId);
        }
        
        Map<String, Serializable> params = mapParams(req);
        
        NodeRef node = locatorService.getNode(locatorName, source, params);
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("nodeRef", node==null ? null : node.toString());
        return model;
    }

    private Map<String, Serializable> mapParams(WebScriptRequest req)
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        for (String key: req.getParameterNames())
        {
            String value = req.getParameter(key);
            if (value != null)
            {
                String decodedValue = URLDecoder.decode(value);
                // TODO Handle type conversions here.
                params.put(key, decodedValue);
            }
        }
        return params;
    }

    /**
     * @param locatorService the locatorService to set
     */
    public void setNodeLocatorService(NodeLocatorService locatorService)
    {
        this.locatorService = locatorService;
    }
}
