package org.alfresco.repo.web.scripts.tenant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API - create tenant
 * 
 * @author janv
 * @since 4.2
 */
public class TenantPost extends AbstractTenantAdminWebScript
{
    protected static final Log logger = LogFactory.getLog(TenantPost.class);
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String tenantDomain = null;
        String tenantAdminPassword = null;
        String contentStoreRoot = null;
        
        try
        {
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            if (! json.has(TENANT_DOMAIN))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'tenantDomain' parameter");
            }
            tenantDomain = json.getString(TENANT_DOMAIN);
            
            if (! json.has(TENANT_ADMIN_PASSWORD))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'tenantAdminPassword' parameter");
            }
            tenantAdminPassword = json.getString(TENANT_ADMIN_PASSWORD);
            
            if (json.has(TENANT_CONTENT_STORE_ROOT))
            {
                contentStoreRoot = json.getString(TENANT_CONTENT_STORE_ROOT);
            }
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }
        
        tenantAdminService.createTenant(tenantDomain, tenantAdminPassword.toCharArray(), contentStoreRoot);
        
        Map<String, Object> model = new HashMap<String, Object>(0);
        return model;
    }
}
