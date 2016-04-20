package org.alfresco.service.cmr.webdav;

import org.alfresco.service.cmr.repository.NodeRef;

public interface WebDavService
{
    /**
     * Get the WebDavUrl for the specified nodeRef
     * @param nodeRef the node that the webdav URL (or null)
     * @return the URL of the node in webdav or "" if a URL cannot be built.
     */
    public String getWebdavUrl(NodeRef nodeRef);
    
    /**
     * Determines whether activity post generation is enabled for WebDAV. When enabled,
     * file creation, modification and deletion will create activities that can be viewed
     * in the Share web client.
     * 
     * @return true if activity generation is enabled.
     */
    public boolean activitiesEnabled();
    
    /**
     * Is the web dav service enabled?
     * @return true, is enabled
     */
    public boolean getEnabled();
}
