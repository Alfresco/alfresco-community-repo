 
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.util.WebScriptUtils.getRequestContentAsJsonObject;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API to set the recorded version config for a document
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigPost extends AbstractRmWebScript
{
    /** Constant for recorded version parameter */
    public static final String RECORDED_VERSION = "recordedVersion";

    /** Recordable version config service */
    private RecordableVersionConfigService recordableVersionConfigService;

    /**
     * Gets the recordable version config service
     *
     * @return The recordable version config service
     */
    protected RecordableVersionConfigService getRecordableVersionConfigService()
    {
        return this.recordableVersionConfigService;
    }

    /**
     * Sets the recordable version config service
     *
     * @param recordableVersionConfigService The recordable version config service
     */
    public void setRecordableVersionConfigService(RecordableVersionConfigService recordableVersionConfigService)
    {
        this.recordableVersionConfigService = recordableVersionConfigService;
    }

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        NodeRef nodeRef = parseRequestForNodeRef(req);
        String policy = getRecordableVersionPolicy(req);
        getRecordableVersionConfigService().setVersion(nodeRef, policy);
        return new HashMap<String, Object>(1);
    }

    /**
     * Gets the recordable version policy from the request
     *
     * @param The webscript request
     * @return The recordable version policy
     */
    private String getRecordableVersionPolicy(WebScriptRequest req)
    {
        JSONObject requestContent = getRequestContentAsJsonObject(req);
        return getStringValueFromJSONObject(requestContent, RECORDED_VERSION);
    }
}
