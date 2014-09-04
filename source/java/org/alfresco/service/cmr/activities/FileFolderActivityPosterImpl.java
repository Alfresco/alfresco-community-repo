package org.alfresco.service.cmr.activities;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.Client;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A consolidated services for posting file folder activities.
 * Some code was moved from webdav.ActivityPosterImpl and
 * opencmis.ActivityPosterImpl.
 *
 * @author Gethin James
 */
public class FileFolderActivityPosterImpl implements ActivityPoster
{
    private ActivityService activityService;
 
    @Override
    public void postFileFolderActivity(
                String activityType,
                String path,
                String tenantDomain,
                String siteId,
                NodeRef parentNodeRef,
                NodeRef nodeRef,
                String fileName,
                String appTool,
                Client client,
                FileInfo fileInfo)
    {

        JSONObject json;
        try
        {
            json = createActivityJSON(tenantDomain, path, parentNodeRef, nodeRef, fileName);
        }
        catch (JSONException jsonError)
        {
            throw new AlfrescoRuntimeException("Unabled to create activities json", jsonError);
        }
        
        activityService.postActivity(
                    activityType,
                    siteId,
                    appTool,
                    json.toString(),
                    client,
                    fileInfo);
    }
    
    /**
     * Create JSON suitable for create, modify or delete activity posts.
     * 
     * @param tenantDomain
     * @param path
     * @param parentNodeRef
     * @param nodeRef
     * @param fileName
     * @throws JSONException
     * @return JSONObject
     */
    protected JSONObject createActivityJSON(
                String tenantDomain,
                String path,
                NodeRef parentNodeRef,
                NodeRef nodeRef,
                String fileName) throws JSONException
    {
            JSONObject json = new JSONObject();

            json.put("nodeRef", nodeRef);
            
            if (parentNodeRef != null)
            {
                // Used for deleted files.
                json.put("parentNodeRef", parentNodeRef);
            }
            
            if (path != null)
            {
                // Used for deleted files and folders (added or deleted)
                json.put("page", "documentlibrary?path=" + path);
            }
            else
            {
                // Used for added or modified files.
                json.put("page", "document-details?nodeRef=" + nodeRef);
            }
            json.put("title", fileName);
            
            if (tenantDomain!= null && !tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                // Only used in multi-tenant setups.
                json.put("tenantDomain", tenantDomain);
            }
        
        return json;
    }

    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
}
