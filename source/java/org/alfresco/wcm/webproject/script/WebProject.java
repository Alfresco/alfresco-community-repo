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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.sandbox.script.Sandbox;


/**
 * WebProject object to expose via JavaScript
 */
public class WebProject implements Serializable
{
    public static final String ROLE_CONTENT_MANAGER     = "ContentManager";
    public static final String ROLE_CONTENT_PUBLISHER   = "ContentPublisher";
    public static final String ROLE_CONTENT_REVIEWER    = "ContentReviewer";
    public static final String ROLE_CONTENT_CONTRIBUTOR = "ContentContributor";
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -2194205151549790079L;
	
	WebProjectInfo info;
	
	private String name;
	private String title;
	private String description;
	private boolean isTemplate;
	private String webProjectRef;
	private WebProjects webprojects;
	
	/*
	 * Constructor for Outbound WebProjects
	 */
	public WebProject(WebProjects webprojects, WebProjectInfo info)
	{
		this.info = info;
		this.name = info.getName();
		this.title = info.getTitle();
		this.description = info.getDescription();
		this.isTemplate = info.isTemplate();
		this.webProjectRef = info.getStoreId();
		this.webprojects = webprojects;
	}
	
	public void setName(String name) {
		this.name = name;
		if(info != null) {
		    info.setName(name);	
		}
	}
	public String getName() {
		return name;
	}
	
	public void setTitle(String title) {
		this.title = title;
		if(info != null) {
		    info.setTitle(title);	
		}
	}
	public String getTitle() {
		return title;
	}
	
	public void setDescription(String description) {
		this.description = description;
		if(info != null) {
		    info.setDescription(description);	
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	
	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
		if(info != null) {
			info.setIsTemplate(isTemplate);
		}
	}
	
    public void setTemplate(Boolean isTemplate) {
        if (isTemplate != null)
        {
            this.setTemplate(isTemplate.booleanValue());
        }
    }
    
	public boolean isTemplate() {
		return isTemplate;
	}

	// read-only property
	public void setWebProjectRef(String webProjectRef) {
		this.webProjectRef = webProjectRef;
	}
	
	// 
	public String getWebProjectRef() 
	{
		return webProjectRef;
	}
	
	// read-only property
	public NodeRef getNodeRef() 
	{
		return info.getNodeRef();
	}
	
	/**
	 * delete this web project
	 */
	public void deleteWebProject()
	{
		getWebProjectService().deleteWebProject(webProjectRef);
	}
	
	/**
	 * update this web project
	 */ 
	public void save()
	{
		getWebProjectService().updateWebProject(info);
	}
	
	/**
	 * getSandboxes
	 * @param userName
	 * @return the sandboxes or an empty map if there are none.
	 */
	public  ScriptableHashMap<String, Sandbox> getSandboxes(String userName)
	{
		ScriptableHashMap<String, Sandbox> result = new ScriptableHashMap<String, Sandbox>();
		
		// TODO at the moment the user can only have one sandbox - this will change in future
		SandboxInfo si = getSandboxService().getAuthorSandbox(webProjectRef, userName);
		if(si != null)
		{
			Sandbox sandbox = new Sandbox(this, si);
			result.put(userName, sandbox);
		}
	    return result;
	}
	
	
	/**
	 * Create a user sandbox, if the user already has a sandbox does nothing.
	 * @param userName
	 * @return the newly created sandbox details
	 */
	public Sandbox createSandbox(String userName)
	{
		SandboxInfo si = getSandboxService().createAuthorSandbox(webProjectRef, userName);
		Sandbox sandbox = new Sandbox(this, si);
		return sandbox;	
	}
	
	/**
	 * Get a single sandbox by its unique reference
	 * @param sandboxRef
	 * @return the sandbox or null if it is not found.
	 */
	public Sandbox getSandbox(String sandboxRef)
	{
		SandboxInfo si = getSandboxService().getSandbox(sandboxRef);
		if(si != null)
		{
			Sandbox sandbox = new Sandbox(this, si);
			return sandbox;
		}
		return null;		
	}
	
	/**
	 * getSandboxes for this web project
	 * @return the sandboxes
	 */
	public ScriptableHashMap<String, Sandbox> getSandboxes()
	{
		List<SandboxInfo> si = getSandboxService().listSandboxes(webProjectRef);

   	 	ScriptableHashMap<String, Sandbox> result = new ScriptableHashMap<String, Sandbox>();
   	 	
		for(SandboxInfo s : si)
		{
			Sandbox b = new Sandbox(this, s);
			result.put(b.getSandboxRef(), b);
		}
		
        return result;
	}
	
    /**
     * Gets a user's role on this site.
     * <p>
     * If the user is not a member of the site then null is returned.
     * 
     * @param userName  user name
     * @return String   user's role or null if not a member
     */
    public String getMembersRole(String userName)
    {
    	return getWebProjectService().getWebUserRole(webProjectRef, userName);
    }
	
    /**
     * Sets the membership details for a user.
     * <p>
     * If the user is not already a member of the web project then they are invited with the role
     * given. 
     * <p>
     * Only a content manager can modify memberships and there must be at least one conttent manager at
     * all times.
     * 
     * @param userName  user name
     * @param role      site role
     */
    public void addMembership(String userName, String role)
    {
    	getWebProjectService().inviteWebUser(webProjectRef, userName, role);
    }
    
    /**
     * Removes a users membership of the web project.
     * 
     * Note: this will cascade delete the user's sandboxes without warning (even if there are modified items)
     * <p>
     * 
     * @param userName  user name
     */
    public void removeMembership(String userName)
    {
    	getWebProjectService().uninviteWebUser(webProjectRef, userName);
    }
    
    /**
     * Gets a map of members of the web project with their role within the web project.  
     * <p>
     * @return ScriptableHashMap<String, String>    list of members of site with their roles
     */
    public ScriptableHashMap<String, String> listMembers()
    {
    	Map<String, String> members = getWebProjectService().listWebUsers(webProjectRef);
        
        ScriptableHashMap<String, String> result = new ScriptableHashMap<String, String>();
        result.putAll(members);
        
        return result;
    }
    
    /**
     * List the role (name) for a WCM project
     * @return a map of roles for a WCM project (value, name)
     */
    public ScriptableHashMap<String, String> getRoles()
    {
    	//TODO Role names should be I811N from webclient.properties
    	//ContentManager=Content Manager
    	//ContentPublisher=Content Publisher
    	//ContentContributor=Content Contributor
    	//ContentReviewer=Content Reviewer
    	 ScriptableHashMap<String, String> result = new ScriptableHashMap<String, String>();
    	 result.put(ROLE_CONTENT_MANAGER, "Content Manager");
    	 result.put(ROLE_CONTENT_PUBLISHER, "Content Publisher");
    	 result.put(ROLE_CONTENT_REVIEWER, "Content Reviewer");
    	 result.put(ROLE_CONTENT_CONTRIBUTOR, "Content Contributor");
    	 
    	 return result;
    }
    
    public WebProjects getWebProjects()
    {
    	return this.webprojects;
    }
    
    public SandboxService getSandboxService()
    {
    	return getWebProjects().getSandboxService();
    }
    
    public WebProjectService getWebProjectService()
    {
    	return getWebProjects().getWebProjectService();
    }
}
