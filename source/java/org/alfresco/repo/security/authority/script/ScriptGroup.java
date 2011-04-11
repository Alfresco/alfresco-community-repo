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
package org.alfresco.repo.security.authority.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	    return getChildGroups(-1, -1);
	}

	/**
	 * Get child groups of this group
	 */
	public ScriptGroup[] getChildGroups(int maxItems, int skipCount)
	{
		if(childGroups == null)
		{
			Set<String> children = getChildNamesOfType(AuthorityType.GROUP);
			Set<ScriptGroup> groups =makeScriptGroups(children); 
			childGroups = groups.toArray(new ScriptGroup[groups.size()]);
		}
		return makePagedGroups(maxItems, skipCount, childGroups);
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
        return getParentGroups(-1, -1);
    }
    
	/**
	 * Get the immediate parents of this group
	 * @return the immediate parents of this group
	 */
	public ScriptGroup[] getParentGroups(int maxItems, int skipCount)
	{
		if(parentCache == null)
		{
			Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, fullName, true);
			Set<ScriptGroup> groups = makeScriptGroups(parents);
			parentCache = groups.toArray(new ScriptGroup[groups.size()]);
		}
		return makePagedGroups(maxItems, skipCount, parentCache);
	}

    private ScriptGroup[] makePagedGroups(int maxItems, int skipCount, ScriptGroup[] groups)
    {
        boolean invalidSkipCount = skipCount <1 || skipCount>= groups.length;
		skipCount = invalidSkipCount ? 0 : skipCount;
		int maxSize = groups.length - skipCount;
		boolean invalidMaxItems = maxItems <1 || maxItems>=maxSize;

		if(invalidMaxItems && invalidSkipCount)
		{
		    return groups;
		}
		
		maxItems = invalidMaxItems ? maxSize : maxItems;
		ScriptGroup[] results = new ScriptGroup[maxItems];
		System.arraycopy(groups, skipCount, results, 0, maxItems);
        return results;
    }

    private Set<ScriptGroup> makeScriptGroups(Set<String> parents)
    {
        ArrayList<String> sortedParents = new ArrayList<String>(parents);
        Collections.sort(sortedParents);
        LinkedHashSet<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
        for(String authority : sortedParents)
        {
        	ScriptGroup group = new ScriptGroup(authority, authorityService);
        	groups.add(group);
        
        }
        return groups;
    }
	
	/**
	 * Get all the parents of this this group
	 * @return all the parents of this group
	 */
	public ScriptGroup[] getAllParentGroups()
	{
	    return getAllParentGroups(-1, -1);
	}

    /**
     * Get all the parents of this this group
     * 
     * @param maxItems Maximum number of groups to return.
     * @param skipCount number of groups to skip before returning the first result.
     * @return all the parents of this group
     */
    public ScriptGroup[] getAllParentGroups(int maxItems, int skipCount)
    {
		Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, fullName, false);
		return makeScriptGroups(parents, maxItems, skipCount, authorityService);
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
	public ScriptGroup createGroup(String newShortName, String newDisplayName)
	{
		String authorityName = authorityService.createAuthority(AuthorityType.GROUP, newShortName, displayName, authorityService.getDefaultZones());
		authorityService.addAuthority(fullName, authorityName);
		ScriptGroup childGroup = new ScriptGroup(authorityName, authorityService);
		clearCaches();
		return childGroup;
	}
		
	/**
	 * remove sub group from this group
	 * @param newShortName the shortName of the sub group to remove from this group.
	 */
	public void removeGroup(String newShortName)
	{
		String fullAuthorityName = authorityService.getName(AuthorityType.GROUP, newShortName);
		
		authorityService.removeAuthority(fullName, fullAuthorityName);
		clearCaches();
	}
	
	/**
	 * Remove child user from this group
	 * @param newShortName the shortName of the user to remove from this group.
	 */
	public void removeUser(String newShortName)
	{
		String fullAuthorityName = authorityService.getName(AuthorityType.USER, newShortName);
		
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
	
    public static ScriptGroup[] makeScriptGroups(Collection<String> authorities,
                int maxItems, int skipCount, AuthorityService authorityService)
    {
        ArrayList<String> authList = new ArrayList<String>(authorities);
        Collections.sort(authList);
        int totalItems = authList.size();
        if(maxItems<1 || maxItems>totalItems)
        {
            maxItems = totalItems;
        }
        if(skipCount<0)
        {
            skipCount = 0;
        }
        int endPoint = skipCount + maxItems;
        if(endPoint > totalItems)
        {
            endPoint = totalItems;
        }
        int size = skipCount > endPoint ? 0 : endPoint - skipCount;
        ScriptGroup[] groups = new ScriptGroup[size];
        for (int i = skipCount; i < endPoint; i++)
        {
            String authority = authList.get(i);
            ScriptGroup group = new ScriptGroup(authority, authorityService);
            groups[i-skipCount] = group;
        }
        return groups;
    }
	
}
