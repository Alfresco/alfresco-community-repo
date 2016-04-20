package org.alfresco.service.cmr.site;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Enumeration representing the different site visibilities.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public enum SiteVisibility
{
    PUBLIC,             // Public site.  Visible and accessible by all
    MODERATED,          // Moderated site.  Visible to all, but only accessible via moderated invitation.
    PRIVATE             // Private site.  Visible and accessible only to members of the site.
}
