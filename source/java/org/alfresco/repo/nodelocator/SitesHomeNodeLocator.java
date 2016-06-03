
package org.alfresco.repo.nodelocator;


import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.site.SiteServiceInternal;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Locates the Sites Home {@link NodeRef}.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class SitesHomeNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "siteshome"; 

    SiteServiceInternal siteService;
    
    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        return siteService.getSiteRoot();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getName()
    {
        return NAME;
    }

    /**
     * @param siteService the siteService to set
     */
    public void setSiteService(SiteServiceInternal siteService)
    {
        this.siteService = siteService;
    }
}
