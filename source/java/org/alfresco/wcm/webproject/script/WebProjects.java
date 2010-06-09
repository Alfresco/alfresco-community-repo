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
package org.alfresco.wcm.webproject.script;

import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;


/**
 * Script object representing the wcm web projects script object.
 * 
 * This class is the root for the script api to use wcm web projects.  
 */
public class WebProjects extends BaseScopableProcessorExtension
{
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
    /** The web projects service */
    private WebProjectService webProjectService;

    /** The sandbox service */
    private SandboxService sandboxService;
    
    /** The asset service */
    private AssetService assetService;
    
    /** The namespace service */
    private NamespaceService namespaceService;
    
    /**
     * Sets the Service Registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /**
     * Set the wcm web project service 
     * 
     * @param webProjectService   the wcm web project service
     */
    public void setWebProjectService(WebProjectService webProjectService)
    {
        this.webProjectService = webProjectService;
    }
    
    public WebProjectService getWebProjectService()
    {
    	return this.webProjectService;
    }
    
    /**
     * Set the wcm sandbox service 
     * 
     * @param webProjectService   the wcm web project service
     */
    public void setSandboxService(SandboxService sandboxService)
    {
        this.sandboxService = sandboxService;
    }
    
    public SandboxService getSandboxService()
    {
    	return this.sandboxService;
    }
    
    /**
     * Set the wcm asset service 
     * 
     * @param assetService   the wcm asset service
     */
    public void setAssetService(AssetService assetService)
    {
        this.assetService = assetService;
    }
    
    public AssetService getAssetService()
    {
    	return this.assetService;
    }
    
    /**
     * Set the alfresco namespace service 
     * 
     * @param namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public NamespaceService getNamespaceService()
    {
    	return this.namespaceService;
    }
    
    /**
     * create web project
     * @param name
     * @param title
     * @param description
     * @return
     */
    public WebProject createWebProject(String dnsName, String name, String title, String description )
    {
    	WebProjectInfo info = webProjectService.createWebProject(dnsName, name, title, description);    	
    	return new WebProject(this, info);
    }
    
    /**
     * Get an existing wcm web project
     * @param webProjectRef
     * @return the wcm web project
     */
    public WebProject getWebProject(String webProjectRef) 
    {	
    	WebProjectInfo info = webProjectService.getWebProject(webProjectRef);
    	
    	if(info != null){
    		WebProject retVal = new WebProject(this, info);   	
    		return retVal;
    	}
    	return null;
    }
    
    /*
     * list All Web Projects
     */
    public WebProject[] listWebProjects() 
    {
   	
    	List<WebProjectInfo> projects = webProjectService.listWebProjects();
    	
    	WebProject[] ret = new WebProject[projects.size()];
    	
    	int i= 0;
    	for(WebProjectInfo info : projects)
    	{
    		ret[i++] = new WebProject(this, info);   
    	}	
    	return ret;
    }
    
    /*
     * list All Web Projects which have the userName as a member
     */
    public WebProject[] listWebProjects(String userName) 
    {
   	
    	List<WebProjectInfo> projects = webProjectService.listWebProjects(userName);
    	
    	WebProject[] ret = new WebProject[projects.size()];
    	
    	int i= 0;
    	for(WebProjectInfo info : projects)
    	{
    		ret[i++] = new WebProject(this, info);   
    	}	
    	return ret;
    }
}
