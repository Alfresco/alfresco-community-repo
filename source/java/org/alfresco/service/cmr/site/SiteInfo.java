package org.alfresco.service.cmr.site;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

@AlfrescoPublicApi
public interface SiteInfo extends PermissionCheckValue
{
    /**
     * Get the site node reference
     * 
     * @return  NodeRef     site node reference, null if not set
     */
    public abstract NodeRef getNodeRef();

    /**
     * Get the site preset
     * 
     * @return  String  site preset
     */
    public abstract String getSitePreset();

    /**
     * Get the short name
     * 
     * @return  String  short name
     */
    public abstract String getShortName();

    /**
     * Get the title
     * 
     * @return  String  site title
     */
    public abstract String getTitle();

    /**
     * Set the title
     * 
     * @param title site title
     */
    public abstract void setTitle(String title);

    /**
     * Get the description
     * 
     * @return  String  site description
     */
    public abstract String getDescription();

    /**
     * Set the description
     * 
     * @param description   site description
     */
    public abstract void setDescription(String description);

    /**
     * Sets whether this site is public or not.  If true the visibility is set to "public", if false
     * the visibility is set to "private"
     * 
     * @param isPublic  true public, false private
     * @deprecated      as of version 3.2, replaced by {@link #setVisibility(SiteVisibility)}
     */
    public abstract void setIsPublic(boolean isPublic);
    
    /**
     * Indicates whether the site is public.
     * 
     * @return      boolean true if public, false either private or moderated
     * @deprecated  as of version 3.2, replaced by {@link #getVisibility()}
     */
    public abstract boolean getIsPublic();
    
    /**
     * Get the sites visibility
     * 
     * @return  SiteVisibility  site visibility
     */
    public abstract SiteVisibility getVisibility();

    /**
     * Set the sites visibility
     * 
     * @param visibility    site visibility
     */
    public abstract void setVisibility(SiteVisibility visibility);
    
    /**
     * Get the custom property values
     * 
     * @return  map of custom property names and values
     */
    public abstract Map<QName, Serializable> getCustomProperties();

    /**
     * Get the value of a custom property
     * 
     * @param  name             name of custom property
     * @return Serializable     value of the property, null if not set or doesn't exist    
     */
    public abstract Serializable getCustomProperty(QName name);

    /**
     * Get the site created date
     * 
     * @return <code>Date</code> site created date
     */
    public abstract Date getCreatedDate();

    /**
     * Set the site created date
     * 
     * @param createdDate site created date
     */
    public abstract void setCreatedDate(Date createdDate);

    /**
     * Get the site last modified date
     * 
     * @return <code>Date</code> site last modified date
     */
    public abstract Date getLastModifiedDate();

    /**
     * Set the site last modified date
     * 
     * @param lastModifiedDate site last modified date
     */
    public abstract void setLastModifiedDate(Date lastModifiedDate);

}
