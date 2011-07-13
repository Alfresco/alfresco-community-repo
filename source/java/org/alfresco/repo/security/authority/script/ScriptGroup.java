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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.ScriptPagingDetails;
import org.mozilla.javascript.Scriptable;

/**
 * The Script group is a GROUP authority exposed to the scripting API.
 * It provides access to the properties of the group including the children of the group which may be groups or users.
 * 
 * @author mrogers
 */
public class ScriptGroup implements Authority, Serializable
{
    private static final long serialVersionUID = 6073732221341647273L;
    private transient ServiceRegistry serviceRegistry;
    private transient AuthorityService authorityService;
    private ScriptAuthorityType authorityType = ScriptAuthorityType.GROUP;
    private String shortName;
    private String fullName;
    private String displayName;
    private Set<String> childAuthorityNames;
    private Boolean isAdmin; 
    private NodeRef groupNodeRef;
    private Scriptable scope;
    
    /**
     * New script group
     * @param fullName
     * @param serviceRegistry
     */
    public ScriptGroup(String fullName, ServiceRegistry serviceRegistry, Scriptable scope)
    {
       this(fullName, serviceRegistry, serviceRegistry.getAuthorityService(), scope);
    }
    
    /**
     * New script group
     * @param fullName
     * @param authorityService
     * @deprecated Use {@link #ScriptGroup(String, ServiceRegistry)} instead
     */
    public ScriptGroup(String fullName, AuthorityService authorityService)
    {
       this(fullName, null, authorityService, null);
    }
    
    /**
     * New script group
     * @param fullName
     * @param authorityService
     */
    private ScriptGroup(String fullName, ServiceRegistry serviceRegistry, AuthorityService authorityService, Scriptable scope)
    {
       this.authorityService = authorityService;
       this.serviceRegistry = serviceRegistry;
       this.fullName = fullName;
       this.scope = scope;
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
	
    public void setAuthorityType(ScriptAuthorityType authorityType) 
    {
        this.authorityType = authorityType;
    }

    public ScriptAuthorityType getAuthorityType() 
    {
        return authorityType;
    }

    public void setShortName(String shortName) 
    {
        this.shortName = shortName;
    }

    /**
     * Get the short name
     */
    public String getShortName() 
    {
        return shortName;
    }

    public void setFullName(String fullName) 
    {
        this.fullName = fullName;
    }

    /**
     * Get the full internal name, also known
     *  as the Authority Name
     */
    public String getFullName() 
    {
        return fullName;
    }

    /**
     * Change the display name for this group.    Need administrator permission to call this method to change a display name.
     * @param displayName
     */
    public void setDisplayName(String displayName) 
    {
        if (this.displayName != null && !this.displayName.equals(displayName))
        {
            authorityService.setAuthorityDisplayName(fullName, displayName);
        }
        this.displayName = displayName;
    }

    public String getDisplayName() 
    {
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
        for (String authority : children)
        {
            ScriptUser user = new ScriptUser(authority, null, serviceRegistry, this.scope);
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
        for (String authority : children)
        {
            ScriptGroup group = new ScriptGroup(authority, serviceRegistry, authorityService, this.scope);
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

    /**
     * Get child users of this group
     */
    public ScriptUser[] getChildUsers()
    {
        return getChildUsers(new ScriptPagingDetails(), null);
    }
    
    /**
     * Get child users of this group
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     */
    public ScriptUser[] getChildUsers(ScriptPagingDetails paging, String sortBy)
    {
        if (childUsers == null)
        {
            Set<String> children = getChildNamesOfType(AuthorityType.USER);
            Set<ScriptUser> users = new LinkedHashSet<ScriptUser>();
            for (String authority : children)
            {
                ScriptUser user = new ScriptUser(authority, null, serviceRegistry, this.scope);
                users.add(user);
            }
            childUsers = users.toArray(new ScriptUser[users.size()]);
        }
        return makePagedAuthority(paging, sortBy, childUsers);
    }
	
    /**
     * Get child groups of this group
     */
    private ScriptGroup[] childGroups; 
    public ScriptGroup[] getChildGroups()
    {
        return getChildGroups(new ScriptPagingDetails(), null);
    }

    /**
     * Get child groups of this group
     * @param maxItems Maximum number of groups to return.
     * @param skipCount number of groups to skip before returning the first result.
     */
    public ScriptGroup[] getChildGroups(int maxItems, int skipCount)
    {
        return getChildGroups(new ScriptPagingDetails(maxItems, skipCount), null);
    }
	
    /**
     * Get child groups of this group
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     */
    public ScriptGroup[] getChildGroups(ScriptPagingDetails paging, String sortBy)
    {
        if (childGroups == null)
        {
            Set<String> children = getChildNamesOfType(AuthorityType.GROUP);
            Set<ScriptGroup> groups =makeScriptGroups(children); 
            childGroups = groups.toArray(new ScriptGroup[groups.size()]);
        }
        return makePagedAuthority(paging, sortBy, childGroups);
    }
    
    /**
     * Get all the children of this group, regardless of type
     */
    public Authority[] getChildAuthorities()
    {
        return getChildAuthorities(new ScriptPagingDetails(), null);
    }
    
    /**
     * Get all the children of this group, regardless of type
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     */
    public Authority[] getChildAuthorities(ScriptPagingDetails paging, String sortBy)
    {
        Authority[] groups = getChildGroups();
        Authority[] users = getChildUsers();

        Authority[] ret = new Authority[groups.length + users.length];
        System.arraycopy(groups, 0, ret, 0, groups.length);
        System.arraycopy(users, 0, ret, groups.length, users.length);
        
        return makePagedAuthority(paging, sortBy, ret);
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
        return getParentGroups(new ScriptPagingDetails(), null);
    }

    /**
     * Get the immediate parents of this group
     * @param maxItems Maximum number of groups to return.
     * @param skipCount number of groups to skip before returning the first result.
     * @return the immediate parents of this group
     */
    public ScriptGroup[] getParentGroups(int maxItems, int skipCount)
    {
        return getParentGroups(new ScriptPagingDetails(maxItems, skipCount), null);
    }

    /**
     * Get the immediate parents of this group
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     * @return the immediate parents of this group
     */
    public ScriptGroup[] getParentGroups(ScriptPagingDetails paging, String sortBy)
    {
        if (parentCache == null)
        {
            Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, fullName, true);
            Set<ScriptGroup> groups = makeScriptGroups(parents);
            parentCache = groups.toArray(new ScriptGroup[groups.size()]);
        }
        return makePagedAuthority(paging, sortBy, parentCache);
    }

    private <T extends Authority> T[] makePagedAuthority(ScriptPagingDetails paging, String sortBy, T[] groups)
    {
        // Sort the groups
        Arrays.sort(groups, new AuthorityComparator(sortBy));

        // Now page
        int maxItems = paging.getMaxItems(); 
        int skipCount = paging.getSkipCount();
        paging.setTotalItems(groups.length);
        return ModelUtil.page(groups, maxItems, skipCount);
    }

    private Set<ScriptGroup> makeScriptGroups(Set<String> parents)
    {
        ArrayList<String> sortedParents = new ArrayList<String>(parents);
        Collections.sort(sortedParents);
        LinkedHashSet<ScriptGroup> groups = new LinkedHashSet<ScriptGroup>();
        for (String authority : sortedParents)
        {
            ScriptGroup group = new ScriptGroup(authority, serviceRegistry, authorityService, this.scope);
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
        return getAllParentGroups(new ScriptPagingDetails(), null);
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
        return getAllParentGroups(new ScriptPagingDetails(maxItems, skipCount), null);
    }
	
    /**
     * Get all the parents of this this group
     * 
     * @param paging Paging object with max number to return, and items to skip
     * @param sortBy What to sort on (authorityName, shortName or displayName)
     * @return all the parents of this group
     */
    public ScriptGroup[] getAllParentGroups(ScriptPagingDetails paging, String sortBy)
    {
        Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, fullName, false);
        return makeScriptGroups(parents, paging, sortBy, serviceRegistry, this.scope);
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
        String authorityName = authorityService.createAuthority(AuthorityType.GROUP, newShortName, newDisplayName, authorityService.getDefaultZones());
        authorityService.addAuthority(fullName, authorityName);
        ScriptGroup childGroup = new ScriptGroup(authorityName, serviceRegistry, authorityService, this.scope);
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
     * Return the NodeRef of the group
     * 
     * @since 4.0
     */
    public NodeRef getGroupNodeRef()
    {
        if (groupNodeRef == null)
        {
            // Lazy lookup for Authority based creation
            groupNodeRef = authorityService.getAuthorityNodeRef(fullName);
        }
        
        return groupNodeRef;
    }

    /**
     * Return a ScriptNode wrapping the group
     * 
     * @since 4.0
     */
    public ScriptNode getGroupNode()
    {
       return new ScriptNode(getGroupNodeRef(), serviceRegistry, this.scope);
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
                ScriptPagingDetails paging, ServiceRegistry serviceRegistry,
                Scriptable scope)
    {
        return makeScriptGroups(authorities, paging, null, serviceRegistry, scope);
    }
    
    public static ScriptGroup[] makeScriptGroups(Collection<String> authorities,
            ScriptPagingDetails paging, final String sortBy, ServiceRegistry serviceRegistry, 
            Scriptable scope)
    {
        
        final ArrayList<String> authList = new ArrayList<String>(authorities);
        final Map<String,ScriptGroup> scriptGroupCache = new HashMap<String, ScriptGroup>();
        
        // Depending on what we're sorting on, we may
        //  need to get the details now
        if ("shortName".equals(sortBy) || "displayName".equals(sortBy))
        {
            for (String authority : authorities)
            {
                scriptGroupCache.put(authority, new ScriptGroup(authority, serviceRegistry, scope));
            }
            final AuthorityComparator c2 = new AuthorityComparator(sortBy);
            Collections.sort(authList, new Comparator<String>() {
                @Override
                public int compare(String g1, String g2)
                {
                    ScriptGroup sg1 = scriptGroupCache.get(g1);
                    ScriptGroup sg2 = scriptGroupCache.get(g2);
                    return c2.compare(sg1, sg2);
                }
            });
        }
        else
        {
            // Default is to sort by authority name,
            //  in lower case, so it's a case insensitive sort
            Collections.sort(authList, new Comparator<String>() {
                @Override
                public int compare(String g1, String g2)
                {
                    return g1.toLowerCase().compareTo( g2.toLowerCase() );
                }
            });
        }
        
        // Do the paging
        List<String> paged = ModelUtil.page(authList, paging);
        ScriptGroup[] groups = new ScriptGroup[paged.size()];
        for (int i=0; i<groups.length; i++)
        {
            String authority = paged.get(i);
            ScriptGroup group;
            if (scriptGroupCache.containsKey(authority))
            {
                group = scriptGroupCache.get(authority);
            } 
            else 
            {
                group = new ScriptGroup(authority, serviceRegistry, scope);
            }
            groups[i] = group;
        }
        return groups;
    }
    
    /**
     * Returns an array of ScriptGroup objects representing the given paged results.
     * 
     * @param groups The paged results
     * @param paging Object representing the paging details
     * @param serviceRegistry
     * @param scope
     * @return Array of ScriptGroup objects
     * 
     * @since 4.0
     */
    public static ScriptGroup[] makeScriptGroups(PagingResults<String> pagedGroups, ScriptPagingDetails paging, 
                ServiceRegistry serviceRegistry, Scriptable scope)
    {
        // set the total on the paging object
        paging.setTotalItems(pagedGroups);
        
        // retrive the page of results and create a ScriptGroup for each one
        List<String> groupNames = pagedGroups.getPage();
        ScriptGroup[] groups = new ScriptGroup[groupNames.size()];
        for (int i=0; i<groups.length; i++)
        {
            groups[i] = new ScriptGroup(groupNames.get(i), serviceRegistry, scope);
        }
        
        return groups;
    }
}
