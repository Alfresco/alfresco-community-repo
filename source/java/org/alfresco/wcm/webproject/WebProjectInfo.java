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
package org.alfresco.wcm.webproject;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Web Project Information
 * 
 * @author janv
 */
public interface WebProjectInfo
{
    /**
     * Get the site node reference
     * 
     * @return  NodeRef     site node reference, null if not set
     */
    public NodeRef getNodeRef();
    
    /**
     * Get the name
     * 
     * @return  String  name
     */
    public String getName();
    
    /**
     * Set the name
     *     
     * @param name the name to set
     */
    public void setName(String name);
    
    /**
     * Get the web project store id
     * 
     * @return the web project store id (generated from the passed DNS name)
     */
    public String getStoreId();
	
    /**
     * Get the staging store name
     * 
     * @return  String  staging store name
     */
	public String getStagingStoreName();
    
    /**
     * Get the description
     * 
     * @return  String  description
     */
    public String getDescription();
    
    /**
     * Set the description
     * 
     * @param description   description
     */
    public void setDescription(String description);
    
    /**
     * Get the title
     * 
     * @return the title
     */
    public String getTitle();
    
    /**
     * Set the title
     * 
     * @param title the title to set
     */
    public void setTitle(String title);
    
    /**
     * Get the default webapp
     * 
     * @return the webapp name
     */
    public String getDefaultWebApp();
    
    /**
     * Set the default webapp
     * 
     * @param webApp the webapp name to set
     */
    public void setDefaultWebApp(String defaultWebApp);
    
    /**
     * @return <tt>true</tt> if this web project can also be used as a template
     */
    public boolean isTemplate();
    
    /**
     * @param isTemplate set to <tt>true</tt> if this web project can also be used as a template
     */
    public void setIsTemplate(boolean isTemplate);
    
    /**
     * Get the preview URI service provider name
     * 
     * @since 3.2
     * 
     * @return the preview URI service provider name
     * 
     */
    public String getPreviewProviderName();
    
    /**
     * Set the preview URI service provider name
     * 
     * @since 3.2
     * 
     * @param previewURIServiceProviderName the preview URI service provider name to set
     */
    public void setPreviewProviderName(String previewURIServiceProviderName);
}
