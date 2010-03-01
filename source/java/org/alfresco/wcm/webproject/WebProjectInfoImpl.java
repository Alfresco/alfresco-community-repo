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
import org.alfresco.wcm.util.WCMUtil;

/**
 * Web Project Information
 * 
 * @author janv
 */
public class WebProjectInfoImpl implements WebProjectInfo
{
	/** Web Project node reference */    
    private NodeRef nodeRef;
    
    /** Web Project name */
    private String name;
    
    /** Web Project title */
    private String title;
    
    /** Web Project description */
    private String description;
    
    /** Web Project store id (aka DNS name) */
    private String wpStoreId;
    
    /** Web Project default webApp name */
    private String defaultWebApp;
    
    /** Web Project true if the web project can also be used as template */
    private boolean isTemplate;
    
    /** Web Project preview provider name (if null then default preview provider will be used) */
    private String previewURIServiceProviderName;
    
    /**
     * Constructor
     */
    public WebProjectInfoImpl(String wpStoreId, 
                              String name, 
                              String title, 
                              String description, 
                              String defaultWebApp, 
                              boolean isTemplate, 
                              NodeRef nodeRef, 
                              String previewProvider)
    {
        this.wpStoreId = wpStoreId;
        this.name = name;
        this.title = title;
        this.description = description;
        this.defaultWebApp = defaultWebApp;
        this.isTemplate = isTemplate;
        this.nodeRef = nodeRef;
        this.previewURIServiceProviderName = previewProvider;
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
     * Get the name
     * 
     * @return  String  name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) 
    {
        this.name = name;
    }
    
	/**
	 * @return the dnsName
	 */
	public String getStoreId() 
	{
		return wpStoreId;
	}
	
    /**
     * Get the staging store name
     * 
     * @return  String  staging store name
     */
	public String getStagingStoreName()
	{
	    return WCMUtil.buildStagingStoreName(getStoreId());
	}
    
    /**
     * Get the description
     * 
     * @return  String  description
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * Set the description
     * 
     * @param description   description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
	 * @return the title
	 */
	public String getTitle() 
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) 
	{
		this.title = title;
	}

	/**
	 * @return the webApp
	 */
	public String getDefaultWebApp() 
	{
		return defaultWebApp;
	}

	/**
	 * @param webApp the webApp to set
	 */
	public void setDefaultWebApp(String defaultWebApp) 
	{
		this.defaultWebApp = defaultWebApp;
	}

	/**
	 * @return the useAsTemplate
	 */
	public boolean isTemplate()
	{
		return isTemplate;
	}

	/**
	 * @param isTemplate the isTemplate to set
	 */
	public void setIsTemplate(boolean isTemplate)
	{
		this.isTemplate = isTemplate;
	}
	
    public String getPreviewProviderName()
    {
        return previewURIServiceProviderName;
    }
    
    public void setPreviewProviderName(String previewURIServiceProviderName)
    {
        this.previewURIServiceProviderName = previewURIServiceProviderName;
    }
}
