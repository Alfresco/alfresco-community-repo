/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.site;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;

/**
 * Site Information Class
 * 
 * @author Roy Wetherall
 */
public class SiteInfoImpl implements SiteInfo
{
    /** Site node reference */
    private NodeRef nodeRef;

    /** Site preset */
    private String sitePreset;

    /** Site short name */
    private String shortName;

    /** Site title */
    private String title;

    /** Site description */
    private String description;

    /** Site visibility */
    private SiteVisibility visibility;

    /** Site created date */
    private Date createdDate;

    /** Site last modified date */
    private Date lastModifiedDate;

    /** Set of custom properties that have been defined for site */
    private Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>(1);

    /**
     * Constructor
     * 
     * @param sitePreset
     *            site preset
     * @param shortName
     *            short name
     * @param title
     *            title
     * @param description
     *            description
     * @param visibility
     *            site visibility
     * @param nodeRef
     *            site node reference
     */
    /* package */ SiteInfoImpl(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, Map<QName, Serializable> customProperties, NodeRef nodeRef)
    {
        this(sitePreset, shortName, title, description, visibility, customProperties);
        this.nodeRef = nodeRef;
    }

    /**
     * Constructor
     * 
     * @param sitePreset
     *            site preset
     * @param shortName
     *            short name
     * @param title
     *            title
     * @param description
     *            description
     * @param visibility
     *            site visibility
     */
    /* package */ SiteInfoImpl(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, Map<QName, Serializable> customProperties)
    {
        this.sitePreset = sitePreset;
        this.shortName = shortName;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        if (customProperties != null)
        {
            this.customProperties = customProperties;
        }
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getSitePreset()
     */
    public String getSitePreset()
    {
        return sitePreset;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getShortName()
     */
    public String getShortName()
    {
        return shortName;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getTitle()
     */
    public String getTitle()
    {
        // title can be used for sorting, so ensure it's not null
        return (title != null ? title : "");
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#setTitle(java.lang.String)
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#setIsPublic(boolean)
     */
    public void setIsPublic(boolean isPublic)
    {
        if (isPublic == true)
        {
            setVisibility(SiteVisibility.PUBLIC);
        }
        else
        {
            setVisibility(SiteVisibility.PRIVATE);
        }
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getIsPublic()
     */
    public boolean getIsPublic()
    {
        boolean result = false;
        if (SiteVisibility.PUBLIC.equals(this.visibility) == true)
        {
            result = true;
        }
        return result;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getVisibility()
     */
    public SiteVisibility getVisibility()
    {
        return this.visibility;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#setVisibility(org.alfresco.service.cmr.site.SiteVisibility)
     */
    public void setVisibility(SiteVisibility visibility)
    {
        this.visibility = visibility;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getCustomProperties()
     */
    public Map<QName, Serializable> getCustomProperties()
    {
        return this.customProperties;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getCustomProperty(org.alfresco.service.namespace.QName)
     */
    public Serializable getCustomProperty(QName name)
    {
        Serializable result = null;
        if (this.customProperties != null)
        {
            result = this.customProperties.get(name);
        }
        return result;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getCreatedDate()
     */
    public Date getCreatedDate()
    {
        return this.createdDate;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#setCreatedDate(java.util.Date)
     */
    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#getLastModifiedDate()
     */
    public Date getLastModifiedDate()
    {
        return this.lastModifiedDate;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteInfo#setLastModifiedDate(java.util.Date)
     */
    public void setLastModifiedDate(Date lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Override equals for this ref type
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof SiteInfoImpl)
        {
            SiteInfoImpl that = (SiteInfoImpl) obj;
            return (this.shortName.equals(that.shortName));
        }
        else
        {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return this.shortName.hashCode();
    }

    public String toString()
    {
        return visibility.name() + " Site " + shortName + " (" + title + ")" +
                " @ " + nodeRef.toString();
    }
}
