/**
 * 
 */
package org.alfresco.service.cmr.site;

/**
 * Enumeration representing the different site visibilities.
 * 
 * @author Roy Wetherall
 */
public enum SiteVisibility
{
    PUBLIC,             // Public site.  Visible and accessible by all
    MODERATED,          // Moderated site.  Visible to all, but only accessible via moderated invitation.
    PRIVATE             // Private site.  Visible and accessible only to members of the site.
}
