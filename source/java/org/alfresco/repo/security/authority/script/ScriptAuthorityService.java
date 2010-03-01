/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.authority.script;

import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Script object representing the authority service.
 * 
 * Provides Script access to groups and may in future be extended for roles and people.
 * 
 * @author Mark Rogers
 */
public class ScriptAuthorityService extends BaseScopableProcessorExtension
{    
    /** The service */
    private AuthorityService authorityService;

	public void setAuthorityService(AuthorityService authorityService)
	{
		this.authorityService = authorityService;
	}

	public AuthorityService getAuthorityService()
	{
		return authorityService;
	}
	
	/**
	 * Search the root groups, those without a parent group.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] searchRootGroupsInZone(String shortNamePattern, String zone)
	{
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>(0);
		Set<String> authorities = authorityService.findAuthoritiesByShortNameInZone(AuthorityType.GROUP, shortNamePattern, zone);
		for (String authority : authorities)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			if (group.isRootGroup())
			{
				groups.add(group);
			}
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
	
	/**
	 * Search the root groups, those without a parent group.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] searchRootGroups(String shortNamePattern)
	{
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		Set<String> authorities = authorityService.findAuthoritiesByShortName(AuthorityType.GROUP, shortNamePattern);
		for (String authority : authorities)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			if (group.isRootGroup())
			{
				groups.add(group);
			}
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
	
	/**
	 * Search the root groups, those without a parent group.   Searches in all zones.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] getAllRootGroups()
	{
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		Set<String> authorities = authorityService.getAllRootAuthorities(AuthorityType.GROUP);
		for (String authority : authorities)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
	
	/**
	 * Get the root groups, those without a parent group.
	 * @param zone zone to search in.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] getAllRootGroupsInZone(String zone)
	{
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		Set<String> authorities = authorityService.getAllRootAuthoritiesInZone(zone, AuthorityType.GROUP);
		for (String authority : authorities)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
    
	/**
	 * Get a group given its short name
	 * @param shortName, the shortName of the group
	 * @return the authority or null if it can't be found
	 */
	public ScriptGroup getGroup(String shortName)
	{
		String fullName = authorityService.getName(AuthorityType.GROUP, shortName);
		
		if (authorityService.authorityExists(fullName))
		{
		    ScriptGroup group = new ScriptGroup(fullName, authorityService);
		    return group;		
		}
		// group not found.
		return null;
	}
	
	/**
	 * Get a group given it full authority name (Which must begin with 'GROUP_'
	 * @param fullAuthorityName, the shortName of the group
	 * @return the authority or null if it can't be found
	 */
	public ScriptGroup getGroupForFullAuthorityName(String fullAuthorityName)
	{
		if (authorityService.authorityExists(fullAuthorityName))
		{
		    ScriptGroup group = new ScriptGroup(fullAuthorityName, authorityService);
		    return group;		
		}
		// group not found.
		return null;
	}
	
	/**
	 * Create a new root group in the default application zones
	 * 
	 * @return the new root group.
	 */
	public ScriptGroup createRootGroup(String shortName, String displayName)
	{
		authorityService.createAuthority(AuthorityType.GROUP, shortName, displayName, authorityService.getDefaultZones());
		return getGroup(shortName);
	}
	
	/**
	 * Search for groups in all zones.
	 * 
	 * @param shortNameFilter partial match on shortName (* and ?) work.  If empty then matches everything.
	 * @return the groups matching the query
	 */
	public ScriptGroup[] searchGroups(String shortNameFilter)
	{
		String filter = shortNameFilter;
		
		/**
		 * Modify shortNameFilter to be "shortName*"
		 */
		if (shortNameFilter.length() != 0)
		{
			filter = filter.replace("\"", "") + "*";
		}
		
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		Set<String> authorities = authorityService.findAuthoritiesByShortName(AuthorityType.GROUP, filter);
		for(String authority : authorities)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
	
	/**
	 * Search for groups in a specific zone
	 * 
	 * @param shortNameFilter partial match on shortName (* and ?) work.  If empty then matches everything.
	 * @param zone zone to search in.
	 * @return the groups matching the query
	 */
	public ScriptGroup[] searchGroupsInZone(String shortNameFilter, String zone)
	{
		String filter = shortNameFilter;
		
		/**
		 * Modify shortNameFilter to be "shortName*"
		 */
		if (shortNameFilter.length() != 0)
		{
			filter = filter.replace("\"", "") + "*";
		}
		
		Set<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
		Set<String> authorities = authorityService.findAuthoritiesByShortNameInZone(AuthorityType.GROUP, filter, zone);
		for(String authority : authorities)
		{
			ScriptGroup group = new ScriptGroup(authority, authorityService);
			groups.add(group);
		}
		return groups.toArray(new ScriptGroup[groups.size()]);
	}
}