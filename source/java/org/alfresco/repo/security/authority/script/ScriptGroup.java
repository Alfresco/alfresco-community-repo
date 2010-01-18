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
import java.util.TreeSet;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * The Script group is a GROUP authority exposed to the scripting API.
 * It provides access to the properties of the group including the children of the group which may be groups or users.
 * 
 * @author mrogers
 */
public class ScriptGroup implements Authority, Serializable
{
	private static final long serialVersionUID = 6073732221341647273L;
	private transient AuthorityService authorityService;
    private ScriptAuthorityType authorityType = ScriptAuthorityType.GROUP;
    private String shortName;
    private String fullName;
    private String displayName;
    private Set<String> childAuthorityNames;
    private Boolean isAdmin; 
    
    /**
     * New script group
     * @param fullName
     * @param authorityService
     */
    public ScriptGroup(String fullName, AuthorityService authorityService)
    {
    	this.authorityService = authorityService;
        this.fullName = fullName;	
        shortName = authorityService.getShortName(fullName);
        displayName = authorityService.getAuthorityDisplayName(fullName);
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

	/**
	 * Change the display name for this group.    Need administrator permission to call this method to change a display name.
	 * @param displayName
	 */
	public void setDisplayName(String displayName) {
		if(this.displayName != null && !this.displayName.equals(displayName))
		{
			authorityService.setAuthorityDisplayName(fullName, displayName);
		}
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Get all users contained in this group
	 * @return
	 */
	public ScriptUser[] getAllUsers()
	{
		Set<String> children = authorityService.getContainedAuthorities(AuthorityType.USER, fullName, false);
		Set<ScriptUser> users = new LinkedHashSet<ScriptUser>();
		for(String authority : children)
		{
			ScriptUser user = new ScriptUser(authority, authorityService);
			users.add(user);
		}
	    return users.toArray(new ScriptUser[users.size()]);
	}
	
	/**
	 * Get all sub groups (all decendants)
	 * @return the descenants of this group
	 */
	public ScriptGroup[] getAllGroups()
	{
		Set<String> children = authorityService.getContainedAuthorities(AuthorityType.GROUP, fullName, false);
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		for(String authority : children)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);	
		}
		return groups.toArray(new ScriptGroup[groups.size()]);

	}
	
	/**
	 * Get child groups of this group
	 */
	private ScriptUser[] childUsers;
	
	private Set<String> getChildAuthorityNames()
	{
	    if (childAuthorityNames == null)
	    {
	        childAuthorityNames = authorityService.getContainedAuthorities(null, fullName, true);
	    }
	    return childAuthorityNames;
	}

	private Set<String> getChildNamesOfType(AuthorityType type)
	{
	    Set<String> authorities = getChildAuthorityNames();
	    Set<String> result = new TreeSet<String>();
	    for (String authority : authorities)
	    {
	        if (AuthorityType.getAuthorityType(authority) == type)
	        {
	            result.add(authority);
	        }
	    }
	    return result;
	}

	public ScriptUser[] getChildUsers()
	{
		if(childUsers == null)
		{
			Set<String> children = getChildNamesOfType(AuthorityType.USER);
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
	private ScriptGroup[] childGroups; 
	public ScriptGroup[] getChildGroups()
	{
		if(childGroups == null)
		{
			Set<String> children = getChildNamesOfType(AuthorityType.GROUP);
			Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
			for(String authority : children)
			{
				ScriptGroup group = new ScriptGroup(authority, authorityService);
				groups.add(group);
			
			}
			childGroups = groups.toArray(new ScriptGroup[groups.size()]);
		}
		return childGroups;
	}
	
	/**
	 * Get the parents of this this group
	 */
	private ScriptGroup[] parentCache;
	
	/**
	 * Get the immediate parents of this group
	 * @return the immediate parents of this group
	 */
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
	 * @return all the parents of this group
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
	public Authority[] getChildAuthorities()
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
        if (this.isAdmin == null)
        {
            this.isAdmin = authorityService.isAdminAuthority(fullName);
        }
        return this.isAdmin;
    }	
	
	/**
	 * Get the number of users contained within this group.
	 * @return the number of users contained within this group.
	 */
	public int getUserCount()
	{
		return getChildNamesOfType(AuthorityType.USER).size();
	}
	
	/**
	 * Get the number of child groups contained within this group.
	 * @return the number of child groups contained within this group.
	 */
	public int getGroupCount()
	{
        return getChildNamesOfType(AuthorityType.GROUP).size();
	}
	
	/**
	 * Create a new group as a child of this group.
	 * @return the new group
	 */
	public ScriptGroup createGroup(String shortName, String displayName)
	{
		String authorityName = authorityService.createAuthority(AuthorityType.GROUP, shortName, displayName, authorityService.getDefaultZones());
		authorityService.addAuthority(fullName, authorityName);
		ScriptGroup childGroup = new ScriptGroup(authorityName, authorityService);
		clearCaches();
		return childGroup;
	}
		
	/**
	 * remove sub group from this group
	 * @param shortName the shortName of the sub group to remove from this group.
	 */
	public void removeGroup(String shortName)
	{
		String fullAuthorityName = authorityService.getName(AuthorityType.GROUP, shortName);
		
		authorityService.removeAuthority(fullName, fullAuthorityName);
		clearCaches();
	}
	
	/**
	 * Remove child user from this group
	 * @param shortName the shortName of the user to remove from this group.
	 */
	public void removeUser(String shortName)
	{
		String fullAuthorityName = authorityService.getName(AuthorityType.USER, shortName);
		
		authorityService.removeAuthority(fullName, fullAuthorityName);
		clearCaches();
	}
	
	/**
	 * AddAuthority as a child of this group
	 * @param fullAuthorityName the full name of the authority to add to this group.
	 */
	public void addAuthority(String fullAuthorityName)
	{
		authorityService.addAuthority(fullName, fullAuthorityName);
		clearCaches();
	}
	
	/**
	 * Remove child Authority from this group
	 * @param fullAuthorityName the full name of the authority to remove from this group.
	 */
	public void removeAuthority(String fullAuthorityName)
	{
		authorityService.removeAuthority(fullName, fullAuthorityName);
		clearCaches();
	}
	
	/**
	 * clear the caches
	 */
	private void clearCaches()
	{
		childUsers = null;
		childGroups = null;
		childAuthorityNames = null;
	}
	
}
