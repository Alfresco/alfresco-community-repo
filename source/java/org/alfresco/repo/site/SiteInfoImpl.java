/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.site;

import java.io.Serializable;
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
    
    /** Set of custom properties that have been defined for site */
    private Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>(1);
   
    /**
     * Constructor
     * 
     * @param sitePreset    site preset
     * @param shortName     short name
     * @param title         title
     * @param description   description
     * @param visibility    site visibility
     * @param nodeRef       site node reference
     */
    /*package*/ SiteInfoImpl(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, Map<QName, Serializable> customProperties, NodeRef nodeRef)
    {
        this(sitePreset, shortName, title, description, visibility, customProperties);
        this.nodeRef = nodeRef;
    }
    
    /**
     * Constructor
     * 
     * @param sitePreset    site preset
     * @param shortName     short name
     * @param title         title
     * @param description   description
     * @param visibility    site visibility
     */
    /*package*/ SiteInfoImpl(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, Map<QName, Serializable> customProperties)
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
     * @see org.alfresco.repo.site.SiteInfo#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#getSitePreset()
     */
    public String getSitePreset()
    {
        return sitePreset;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#getShortName()
     */
    public String getShortName()
    {
        return shortName;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#getTitle()
     */
    public String getTitle()
    {
        // title can be used for sorting, so ensure it's not null
        return (title != null ? title : "");
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#setTitle(java.lang.String)
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#getDescription()
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#setIsPublic(boolean)
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
     * @see org.alfresco.repo.site.SiteInfo#getIsPublic()
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
     * @see org.alfresco.repo.site.SiteInfo#getCustomProperties()
     */
    public Map<QName, Serializable> getCustomProperties()
    {
        return this.customProperties;
    }
    
    /**
     * @see org.alfresco.repo.site.SiteInfo#getCustomProperty(org.alfresco.service.namespace.QName)
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
