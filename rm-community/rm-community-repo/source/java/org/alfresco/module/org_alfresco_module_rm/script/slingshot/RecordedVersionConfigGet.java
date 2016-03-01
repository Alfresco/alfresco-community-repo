 
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API to get the recorded version config for a document
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordedVersionConfigGet extends AbstractRmWebScript
{
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
        Map<String, Object> model = new HashMap<String, Object>(1);
        NodeRef nodeRef = parseRequestForNodeRef(req);
        List<Version> recordableVersions = getRecordableVersionConfigService().getVersions(nodeRef);
        model.put("recordableVersions", recordableVersions);
        return model;
    }
}
