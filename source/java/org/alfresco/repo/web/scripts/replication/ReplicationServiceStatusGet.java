package org.alfresco.repo.web.scripts.replication;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.replication.ReplicationService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Neil Mc Erlean
 * @since 3.5
 */
public class ReplicationServiceStatusGet extends DeclarativeWebScript
{
    public static final String ENABLED = "enabled";

    private ReplicationService replicationService;
    
    /**
     * Used to inject the {@link ReplicationService}.
     * 
     * @param replicationService ReplicationService
     */
    public void setReplicationService(ReplicationService replicationService)
    {
        this.replicationService = replicationService;
    }
    
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        boolean serviceEnabled = replicationService.isEnabled();
        
        model.put(ENABLED, serviceEnabled);
        
        return model;
    }
}