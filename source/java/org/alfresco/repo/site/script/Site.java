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
package org.alfresco.repo.site.script;

import java.io.Serializable;

import org.alfresco.repo.site.SiteInfo;
import org.alfresco.repo.site.SiteService;

/**
 * @author Roy Wetherall
 */
public class Site implements Serializable
{
    private static final long serialVersionUID = 8013569574120957923L;
 
    /** Site information */
    private SiteInfo siteInfo;
    
    /** Site service */
    private SiteService siteService;
    
    /** Indicates whether there are any outstanding changes that need to be saved */
    private boolean isDirty = false;
    
    /**
     * Constructor 
     * 
     * @param siteInfo      site information
     */
    /*package*/ Site(SiteService siteService, SiteInfo siteInfo)
    {
        this.siteService = siteService;
        this.siteInfo = siteInfo;
    }
   
    /**
     * Get the site preset
     * 
     * @return  String  the site preset
     */
    public String getSitePreset()
    {
        return this.siteInfo.getSitePreset();
    }
    
    /**
     * Set the short name
     * 
     * @return  String  the short name
     */
    public String getShortName()
    {
        return this.siteInfo.getShortName();
    }
    
    /**
     * Get the title
     * 
     * @return  String  the site title
     */
    public String getTitle()
    {
        return this.siteInfo.getTitle();
    }

    /**
     * Set the title
     * 
     * @param title     the title
     */
    public void setTitle(String title)
    {
        this.isDirty = true;
        this.siteInfo.setTitle(title);
    }
    
    /**
     * Get the description
     * 
     * @return  String  the description
     */
    public String getDescription()
    {
        return this.siteInfo.getDescription();
    }
    
    /**
     * Set the description
     * 
     * @param description   the description
     */
    public void setDescription(String description)
    {
        this.isDirty = true;
        this.siteInfo.setDescription(description);
    }
    
    /**
     * Gets whether the site is public or not
     * 
     * @return  true is public false otherwise
     */
    public boolean getIsPublic()
    {
        return this.siteInfo.getIsPublic();
    }
    
    /**
     * Set whether the site is public or not
     * 
     * @param isPublic  true the site is public false otherwise
     */
    public void setIsPublic(boolean isPublic)
    {
        this.isDirty = true;
        this.siteInfo.setIsPublic(isPublic);
    }
    
    /**
     * Saves any outstanding updates to the site details.  
     * <p>
     * If properties of the site are changed and save is not called, those changes will be lost.
     */
    public void save()
    {
        if (this.isDirty == true)
        {
            // Update the site details
            this.siteService.updateSite(this.siteInfo);
            
            // Reset the dirty flag
            this.isDirty = false;
        }
    }
    
    /**
     * Deletes the site
     */
    public void deleteSite()
    {
        // Delete the site
        this.siteService.deleteSite(this.siteInfo.getShortName());
    }
}
