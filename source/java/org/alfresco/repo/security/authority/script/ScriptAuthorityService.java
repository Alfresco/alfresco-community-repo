/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import static org.alfresco.repo.security.authority.script.ScriptGroup.makeScriptGroups;
import static org.alfresco.repo.security.authority.script.ScriptGroup.makeScriptGroupsInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.security.authority.AuthorityInfo;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ScriptPagingDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Script object representing the authority service.
 * 
 * Provides Script access to groups and may in future be extended for roles and people.
 * 
 * @author Mark Rogers
 */
public class ScriptAuthorityService extends BaseScopableProcessorExtension
{
    /** RegEx to split a String on the first space. */
    public static final String ON_FIRST_SPACE = " +";
    
    private static final Log logger = LogFactory.getLog(ScriptAuthorityService.class);
    
    /** The group/authority service */
    private AuthorityService authorityService;
    /** The person service */
    private PersonService personService;
    
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
       this.serviceRegistry = serviceRegistry;
       this.authorityService = serviceRegistry.getAuthorityService();
       this.personService = serviceRegistry.getPersonService();
    }

    public AuthorityService getAuthorityService()
    {
       return authorityService;
    }
	
	/**
	 * Search the root groups, those without a parent group.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] searchRootGroupsInZone(String displayNamePattern, String zone)
    {
	    return searchRootGroupsInZone(displayNamePattern, zone, -1, -1);
    }
	    
    /**
     * Search the root groups, those without a parent group.
     * 
     * @param maxItems Maximum number of items returned.
     * @param skipCount number of items to skip.
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] searchRootGroupsInZone(String displayNamePattern, String zone, int maxItems, int skipCount)
    {
        return searchRootGroupsInZone(displayNamePattern, zone, new ScriptPagingDetails(maxItems, skipCount), null);
    }
	
    /**
     * Search the root groups, those without a parent group.
     * 
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] searchRootGroupsInZone(String displayNamePattern, String zone, ScriptPagingDetails paging, String sortBy)
    {
        Set<String> authorities;
        try 
        {
            authorities = authorityService.findAuthorities(AuthorityType.GROUP,
                    null, true, displayNamePattern, zone);
        }
        catch (UnknownAuthorityException e)
        {
            authorities = Collections.emptySet();
        }
        return makeScriptGroups(authorities, paging, sortBy, serviceRegistry, this.getScope());
    }
    
    /**
     * Search the root groups, those without a parent group.
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] searchRootGroups(String displayNamePattern)
    {
       return searchRootGroupsInZone(displayNamePattern, null);
    }

    /**
     * Search the root groups, those without a parent group.
     * 
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] searchRootGroups(String displayNamePattern, ScriptPagingDetails paging, String sortBy)
    {
       return searchRootGroupsInZone(displayNamePattern, null, paging, sortBy);
    }

    /**
     * Search the root groups, those without a parent group.   Searches in all zones.
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] getAllRootGroups()
    {
        return getAllRootGroups(-1, -1);
    }
	
	/**
	 * Search the root groups, those without a parent group.   Searches in all zones.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] getAllRootGroups(int maxItems, int skipCount)
	{
	    return getAllRootGroups(new ScriptPagingDetails(maxItems, skipCount));
	}
	
    /**
     * Search the root groups, those without a parent group.   Searches in all zones.
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] getAllRootGroups(ScriptPagingDetails paging)
    {
        Set<String> authorities;
        try
        {
            authorities = authorityService.getAllRootAuthorities(AuthorityType.GROUP);
        }
        catch(UnknownAuthorityException e)
        {
            authorities = Collections.emptySet();
        }
        return makeScriptGroups(authorities, paging, serviceRegistry, this.getScope());
    }
    
    /**
     * Get the root groups, those without a parent group.
     * @param zone zone to search in.
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] getAllRootGroupsInZone(String zone)
    {
        return getAllRootGroupsInZone(zone, -1, -1);
    }
        
	/**
	 * Get the root groups, those without a parent group.
	 * @param zone zone to search in.
     * @param maxItems Maximum number of items returned.
     * @param skipCount number of items to skip.
	 * @return The root groups (empty if there are no root groups)
	 */
	public ScriptGroup[] getAllRootGroupsInZone(String zone, int maxItems, int skipCount)
	{
		Set<String> authorities;
		try
		{
		    authorities= authorityService.getAllRootAuthoritiesInZone(zone, AuthorityType.GROUP);
        }
        catch(UnknownAuthorityException e)
        {
            authorities = Collections.emptySet();
        }

		return makeScriptGroups(authorities, new ScriptPagingDetails(maxItems, skipCount), 
		            null, serviceRegistry, this.getScope());
	}
    
    /**
     * Get the root groups, those without a parent group.
     * @param zone zone to search in.
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     * @return The root groups (empty if there are no root groups)
     */
    public ScriptGroup[] getAllRootGroupsInZone(String zone, ScriptPagingDetails paging, String sortBy)
    {
        Set<String> authorities;
        try
        {
            authorities= authorityService.getAllRootAuthoritiesInZone(zone, AuthorityType.GROUP);
        }
        catch(UnknownAuthorityException e)
        {
            authorities = Collections.emptySet();
        }

        return makeScriptGroups(authorities, paging, sortBy, serviceRegistry, this.getScope());
    }
    
    /**
     * Retreives groups matching the given filter from all zones.
     * 
     * NOTE: If the filter is null, an empty string or * all groups found will be returned.
     * If the filter starts with * or contains a ? character results returned could be inconsistent.
     * 
     * @param filter Pattern to filter groups by
     * @param paging Paging details
     * @return Array of matching groups
     * @since 4.0
     */
    public ScriptGroup[] getGroups(String filter, ScriptPagingDetails paging)
    {
        return getGroupsInZone(filter, null, paging, null);
    }
    
    /**
     * Retreives groups matching the given filter from all zones.
     * 
     * NOTE: If the filter is null, an empty string or * all groups found will be returned.
     * If the filter starts with * or contains a ? character results returned could be inconsistent.
     * 
     * @param filter Pattern to filter groups by
     * @param paging Paging details
     * @param sortBy Field to sort by, can be <code>shortName</code> or <code>displayName</code> otherwise 
     *        the results are ordered by the authorityName
     * @return Array of matching groups
     * @since 4.0
     */
    public ScriptGroup[] getGroups(String filter, ScriptPagingDetails paging, String sortBy)
    {
        return getGroupsInZone(filter, null, paging, sortBy);
    }
    
    /**
     * @deprecated
     * @since 4.0
     */
    public ScriptGroup[] getGroupsInZone(String filter, String zone, ScriptPagingDetails paging, String sortBy)
    {
        return getGroupsInZone(filter, zone, paging, sortBy, true);
    }
    
    /**
     * Retrieves groups matching the given filter from the given zone.
     * 
     * NOTE: If the filter is null, an empty string or * all groups found will be returned.
     * 
     * @param filter Pattern to filter groups by
     * @param zone The zone in which to search for groups
     * @param paging Paging details
     * @param sortBy Field to sort by, can be <code>shortName</code>, <code>displayName</code> or
     *        <code>authorityName</code>, the default is displayName
     * @param sortAsc sort ascending or not
     * @return Array of matching groups
     * @since 4.1.4
     */
    public ScriptGroup[] getGroupsInZone(String filter, String zone, ScriptPagingDetails paging, String sortBy, boolean sortAsc)
    {
        if (sortBy == null)
        {
            sortBy = "displayName";
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Start getGroupsInZone("+(filter == null ? "null" : '"'+filter+'"')+", "+zone+", {"+paging.getMaxItems()+" "+paging.getSkipCount()+"}, "+sortBy+")");
        }
        
        // reset filter if necessary
        if (filter != null && (filter.length() == 0 || filter.equals("*")))
        {
            filter = null;
        }
        
        ScriptGroup[] scriptGroups = null;
        try
        {
            // for backwards compatibility request a total count of found items, once the UI can deal with
            // results that do not specify the total number of results this can be removed
            paging.setRequestTotalCountMax(10000);

            // get the paged results (NOTE: we can only sort by display name currently)
            PagingResults<AuthorityInfo> groups = authorityService.getAuthoritiesInfo(AuthorityType.GROUP, zone, filter, sortBy, sortAsc, paging);
            
            // create ScriptGroup array from paged results
            scriptGroups = makeScriptGroupsInfo(groups, paging, sortBy, sortAsc, serviceRegistry, this.getScope());
        }
        catch (UnknownAuthorityException e)
        {
            scriptGroups = new ScriptGroup[] {};
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("End   getGroupsInZone("+(filter == null ? "null" : '"'+filter+'"')+", "+zone+", {"+paging.getMaxItems()+" "+paging.getSkipCount()+"}, "+sortBy+") returns "+scriptGroups.length+"\n");
        }
        return scriptGroups;
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
		    ScriptGroup group = new ScriptGroup(fullName, serviceRegistry, this.getScope());
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
		    ScriptGroup group = new ScriptGroup(fullAuthorityName, serviceRegistry, this.getScope());
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
		return searchGroupsInZone(shortNameFilter, null);
	}
	
   /**
    * Search for groups in all zones.
    * 
    * @param shortNameFilter partial match on shortName (* and ?) work.  If empty then matches everything.
    * @param paging Paging object with max number to return, and items to skip
    * @param sortBy What to sort on (authorityName, shortName or displayName)
    * @return the groups matching the query
    */
   public ScriptGroup[] searchGroups(String shortNameFilter, ScriptPagingDetails paging, String sortBy)
   {
      return searchGroupsInZone(shortNameFilter, null, paging, sortBy);
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
		return searchGroupsInZone(shortNameFilter, zone, -1, -1);
	}
    
    /**
     * Search for groups in a specific zone
     * Includes paging parameters to limit size of results returned.
     * 
     * @param shortNameFilter partial match on shortName (* and ?) work.  If empty then matches everything.
     * @param zone zone to search in.
     * @param maxItems Maximum number of items returned.
     * @param skipCount number of items to skip.
     * @return the groups matching the query
     */
    public ScriptGroup[] searchGroupsInZone(String shortNameFilter, String zone, int maxItems, int skipCount)
    {
        return searchGroupsInZone(shortNameFilter, zone, new ScriptPagingDetails(maxItems, skipCount), null);
    }
	
    /**
     * Search for groups in a specific zone
     * Includes paging parameters to limit size of results returned.
     * 
     * @param shortNameFilter partial match on shortName (* and ?) work.  If empty then matches everything.
     * @param zone zone to search in.
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     * @return the groups matching the query
     */
    public ScriptGroup[] searchGroupsInZone(String shortNameFilter, String zone, ScriptPagingDetails paging, String sortBy)
    {
        String filter = shortNameFilter;
        
        if (shortNameFilter.length() != 0)
        {
            filter = filter.replace("\"", "");
        }
        
        Set<String> authorities;
        try 
        {
            authorities = authorityService.findAuthorities(AuthorityType.GROUP, null, false, filter, zone);
        }
        catch(UnknownAuthorityException e)
        {
            // Return an empty set if unrecognised authority.
            authorities = Collections.emptySet();
        }
        return makeScriptGroups(authorities, paging, sortBy, serviceRegistry, this.getScope());
    }
    
    /**
     * Get a user given their username
     * @param username, the username of the user
     * @return the user or null if they can't be found
     */
    public ScriptUser getUser(String username)
    {
       try
       {
          NodeRef person = personService.getPerson(username, false);
          return new ScriptUser(username, person, serviceRegistry, this.getScope());
       }
       catch (NoSuchPersonException e)
       {
          return null;
       }
    }
    
    /**
     * Search for users
     * Includes paging parameters to limit size of results returned.
     * 
     * @param nameFilter partial match of the name (username, first name, last name). If empty then matches everyone.
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (firstName, lastName or userName)
     * @return the users matching the query
     * 
     * @deprecated see People.getPeople(String filter, ScriptPagingDetails pagingRequest, String sortBy)
     */
    public ScriptUser[] searchUsers(String nameFilter, ScriptPagingDetails paging, String sortBy)
    {
       // Build the filter
       List<Pair<QName,String>> filter = new ArrayList<Pair<QName,String>>();
       filter.add(new Pair<QName, String>(ContentModel.PROP_FIRSTNAME, nameFilter));
       filter.add(new Pair<QName, String>(ContentModel.PROP_LASTNAME, nameFilter));
       filter.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, nameFilter));
       
       // In order to support queries for "Alan Smithee", we'll parse these tokens
       // and add them in to the query.
       final Pair<String, String> tokenisedName = tokeniseName(nameFilter);
       if (tokenisedName != null)
       {
           filter.add(new Pair<QName, String>(ContentModel.PROP_FIRSTNAME, tokenisedName.getFirst()));
           filter.add(new Pair<QName, String>(ContentModel.PROP_LASTNAME, tokenisedName.getSecond()));
       }
       
       // Build the sorting. The user controls the primary sort, we supply
       // additional ones automatically
       List<Pair<QName,Boolean>> sort = new ArrayList<Pair<QName,Boolean>>();
       if ("lastName".equals(sortBy))
       {
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, true));
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, true));
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, true));
       }
       else if ("firstName".equals(sortBy))
       {
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, true));
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, true));
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, true));
       }
       else
       {
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, true));
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, true));
          sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, true));
       }
       
       // Do the search
       List<PersonInfo> people = personService.getPeople(filter, true, sort, paging).getPage();
       
       // Record the size of the results
       paging.setTotalItems(people.size());
       
       // Now wrap up the users
       ScriptUser[] users = new ScriptUser[people.size()];
       for (int i=0; i<users.length; i++)
       {
          PersonInfo person = people.get(i);
          users[i] = new ScriptUser(person.getUserName(), person.getNodeRef(), serviceRegistry, this.getScope());
       }
       
       return users;
    }
    
    /**
     * This method will tokenise a name string in order to extract first name, last name - if possible.
     * The split is simple - it's made on the first whitespace within the trimmed nameFilter String. So
     * <p/>
     * "Luke Skywalker" becomes ["Luke", "Skywalker"].
     * <p/>
     * "Jar Jar Binks" becomes ["Jar", "Jar Binks"].
     * <p/>
     * "C-3PO" becomes null.
     * 
     * @param nameFilter
     * @return A Pair<firstName, lastName> if the String is valid, else <tt>null</tt>.
     */
    private Pair<String, String> tokeniseName(String nameFilter)
    {
        Pair<String, String> result = null;
        
        if (nameFilter != null)
        {
            final String trimmedNameFilter = nameFilter.trim();
            
            // We can only have a first name and a last name if we have at least 3 characters e.g. "A B".
            if (trimmedNameFilter.length() > 3)
            {
                final String[] tokens = trimmedNameFilter.split(ON_FIRST_SPACE, 2);
                if (tokens.length == 2)
                {
                    result = new Pair<String, String>(tokens[0], tokens[1]);
                }
            }
        }
        
        return result;
    }
}