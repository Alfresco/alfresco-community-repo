
package org.alfresco.repo.site;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;

/**
 * Internal interface used to expose the getSiteRoot() method without polluting the public interface {@link SiteService}.
 * @author Nick Smith
 * @since 4.0
 *
 */
public interface SiteServiceInternal extends SiteService
{
    
    /**
     * Get the node reference that is the site root
     * 
     * @return NodeRef node reference
     */
    NodeRef getSiteRoot();
}
