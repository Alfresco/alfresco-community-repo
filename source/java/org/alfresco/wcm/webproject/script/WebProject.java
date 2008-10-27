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
package org.alfresco.wcm.webproject.script;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;


/**
 * WebProject object to expose via JavaScript
 * 
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

	WebProjectService service;
	
	WebProjectInfo info;
	
	private String name;
	private String title;
	private String description;
	private boolean isTemplate;
	private String webProjectRef;
	
	/*
	 * Constructor for Outbound WebProjects
	 */
	public WebProject(WebProjectInfo info, WebProjectService service)
	{
		this.info = info;
		this.name = info.getName();
		this.title = info.getTitle();
		this.description = info.getDescription();
		this.isTemplate = info.isTemplate();
		this.webProjectRef = info.getStoreId();
		this.service = service;	
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
	
	public boolean isTemplate() {
		return isTemplate;
	}

	// read-only property
	public void setWebProjectRef(String webProjectRef) {
		this.webProjectRef = webProjectRef;
	}
	
	// 
	public String getWebProjectRef() {
		return webProjectRef;
	}
	
	/**
	 * delete this web project
	 */
	public void deleteWebProject()
	{
		service.deleteWebProject(webProjectRef);
	}
	
	/**
	 * update this web project
	 */ 
	public void save()
	{
		service.updateWebProject(info);
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
    	return service.getWebUserRole(webProjectRef, userName);
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
    	service.inviteWebUser(webProjectRef, userName, role);
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
        service.uninviteWebUser(webProjectRef, userName);
    }
    
    /**
     * Gets a map of members of the web project with their role within the web project.  
     * <p>
     * @return ScriptableHashMap<String, String>    list of members of site with their roles
     */
    public ScriptableHashMap<String, String> listMembers()
    {
    	Map<String, String> members = service.listWebUsers(webProjectRef);
        
        ScriptableHashMap<String, String> result = new ScriptableHashMap<String, String>();
        result.putAll(members);
        
        return result;
    }
    
    /**
     * List the role (name) for a WCM project
     * @return the roles for a WCM project
     */
    public ScriptableHashMap<String, String> getRoles()
    {
    	// TODO Not yet implemented.
    	 ScriptableHashMap<String, String> result = new ScriptableHashMap<String, String>();
    	 return result;
    }
}
