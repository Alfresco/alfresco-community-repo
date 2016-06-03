package org.alfresco.opencmis;

import org.alfresco.service.cmr.activities.ActivityInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * OpenCMIS methods can ActivityPoster to create entries in the activity feed.
 * 
 * @author sglover
 */
public interface CmisActivityPoster
{
	void postFileFolderAdded(NodeRef nodeRef);
    
    void postFileFolderUpdated(boolean isFolder, NodeRef nodeRef);
    
    void postFileFolderDeleted(ActivityInfo activityInfo);
    
    ActivityInfo getActivityInfo(NodeRef nodeRef);
}
