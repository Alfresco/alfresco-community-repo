/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.site;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Site Information Class
 * 
 * @author Roy Wetherall
 */
public class SiteInfo
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
    
    /** Indicates whether the site is public or not */
    private boolean isPublic;
   
    /**
     * Constructor
     * 
     * @param sitePreset    site preset
     * @param shortName     short name
     * @param title         title
     * @param description   description
     * @param isPublic      is site public
     * @param nodeRef       site node reference
     */
    /*package*/ SiteInfo(String sitePreset, String shortName, String title, String description, boolean isPublic, NodeRef nodeRef)
    {
        this(sitePreset, shortName, title, description, isPublic);
        this.nodeRef = nodeRef;
    }
    
    /**
     * Constructor
     * 
     * @param sitePreset    site preset
     * @param shortName     short name
     * @param title         title
     * @param description   description
     * @param isPublic      is site public
     */
    /*package*/ SiteInfo(String sitePreset, String shortName, String title, String description, boolean isPublic)
    {
        this.sitePreset = sitePreset;
        this.shortName = shortName;
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
    }
    
    /**
     * Get the site node reference
     * 
     * @return  NodeRef     site node reference, null if not set
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * Get the site preset
     * 
     * @return  String  site preset
     */
    public String getSitePreset()
    {
        return sitePreset;
    }
    
    /**
     * Get the short name
     * 
     * @return  String  short name
     */
    public String getShortName()
    {
        return shortName;
    }
    
    /**
     * Get the title
     * 
     * @return  String  site title
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * Set the title
     * 
     * @param title site title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    /**
     * Get the description
     * 
     * @return  String  site description
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * Set the description
     * 
     * @param description   site description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * Sets whether the site is public or not
     * 
     * @param isPublic  true if the site is public, false otherwise
     */
    public void setIsPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }
    
    /**
     * Indicates wehther the site is public
     * 
     * @return  boolean true if public false otherwise
     */
    public boolean getIsPublic()
    {
        return this.isPublic;
    }
}
