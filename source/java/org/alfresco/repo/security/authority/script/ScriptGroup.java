/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority.script;

import java.io.Serializable;

import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.security.authority.script.Authority.ScriptAuthorityType;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * The Script group is a GROUP authority exposed to the scripting API
 * @author mrogers
 */
public class ScriptGroup implements Authority, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6073732221341647273L;
	/**
	 * 
	 */
	private transient AuthorityService authorityService;
    private ScriptAuthorityType authorityType = ScriptAuthorityType.GROUP;
    private String shortName;
    private String fullName;
    private String displayName;
    private boolean isAdmin; 
    // how to calculate this private boolean isInternal;
    
    public ScriptGroup(String fullName, AuthorityService authorityService)
    {
    	this.authorityService = authorityService;
        this.fullName = fullName;	
        shortName = authorityService.getShortName(fullName);
        displayName = authorityService.getAuthorityDisplayName(fullName);
        isAdmin = authorityService.isAdminAuthority(fullName);
    }
    
	/**
	 * Delete this group
	 */
	public void deleteGroup()
	{
		authorityService.deleteAuthority(fullName);
	}
	
	public void setAuthorityType(ScriptAuthorityType authorityType) {
		this.authorityType = authorityType;
	}

	public ScriptAuthorityType getAuthorityType() {
		return authorityType;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Get child groups of this group
	 */
	private ScriptUser[] childUsers; 
	public ScriptUser[] getChildUsers()
	{
		if(childUsers == null)
		{
			Set<String> children = authorityService.getContainedAuthorities(AuthorityType.USER, fullName, true);
			Set<ScriptUser> users = new LinkedHashSet<ScriptUser>();
			for(String authority : children)
			{
				ScriptUser user = new ScriptUser(authority, authorityService);
				users.add(user);
			}
			childUsers = users.toArray(new ScriptUser[users.size()]);
		}
		return childUsers;
	}
	
	/**
	 * Get child groups of this group
	 */
	public ScriptGroup[] getChildGroups()
	{
		Set<String> children = authorityService.getContainedAuthorities(AuthorityType.GROUP, fullName, true);
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		for(String authority : children)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);
			
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
	
	/**
	 * Get the parents of this this group
	 */
	private ScriptGroup[] parentCache;
	
	public ScriptGroup[] getParentGroups()
	{
		if(parentCache == null)
		{
			Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, fullName, true);
			Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
			for(String authority : parents)
			{
				ScriptGroup group = new ScriptGroup(authority, authorityService);
				groups.add(group);
			
			}
			parentCache = groups.toArray(new ScriptGroup[groups.size()]);
		}
		return parentCache;
	}
	
	/**
	 * Get all the parents of this this group
	 */
	public ScriptGroup[] getAllParentGroups()
	{
		Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, fullName, false);
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		for(String authority : parents)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);
			
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
	
	/**
	 * Get all the children of this group, regardless of type
	 */
	public Authority[] getAllChildren()
	{
		Authority[] groups = getChildGroups();
		Authority[] users = getChildUsers();
		
	    Authority[] ret = new Authority[groups.length + users.length];
		System.arraycopy(groups, 0, ret, 0, groups.length);
		System.arraycopy(users, 0, ret, groups.length, users.length);
		return ret;

	}
	
	/**
	 * Is this a root group?
	 * @return
	 */
	public boolean isRootGroup()
	{
		ScriptGroup[] groups = getParentGroups();
		return (groups.length == 0);
	}
	
	/**
	 * Is this an admin group?
	 * @return
	 */
	public boolean isAdminGroup()
	{
		return this.isAdmin;
	}
	
	/**
	 * Is this an internal group?
	 * @return
	 */
	public boolean isInternalGroup()
	{
		//TODO Not yet implemeted
		return true;
	}
	
	/**
	 * Get the number of users contained within this group.
	 * @return the number of users contained within this group.
	 */
	public int getUserCount()
	{
		ScriptUser[] users = getChildUsers();
		return users.length;
	}
	
	/**
	 * Get the number of child groups contained within this group.
	 * @return the number of child groups contained within this group.
	 */
	public int getGroupCount()
	{
		ScriptGroup[] groups = getChildGroups();
		return groups.length;
	}
}
