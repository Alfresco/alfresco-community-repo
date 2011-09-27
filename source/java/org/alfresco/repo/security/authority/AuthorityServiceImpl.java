/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.person.UserNameMatcher;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * The default implementation of the authority service.
 * 
 * @author Andy Hind
 */
public class AuthorityServiceImpl implements AuthorityService, InitializingBean
{
    private static Set<String> DEFAULT_ZONES = new HashSet<String>();
    
    private PersonService personService;
    
    private TenantService tenantService;

    private AuthorityDAO authorityDAO;
    
    private UserNameMatcher userNameMatcher;
	
    private AuthenticationService authenticationService;
    
    private PermissionServiceSPI permissionServiceSPI;
    
    private Set<String> adminSet = Collections.singleton(PermissionService.ADMINISTRATOR_AUTHORITY);

    private Set<String> guestSet = Collections.singleton(PermissionService.GUEST_AUTHORITY);

    private Set<String> allSet = Collections.singleton(PermissionService.ALL_AUTHORITIES);

    private Set<String> adminGroups = Collections.emptySet();
    
    private Set<String> guestGroups = Collections.emptySet();
    
    static
    {
        DEFAULT_ZONES.add(AuthorityService.ZONE_APP_DEFAULT);
        DEFAULT_ZONES.add(AuthorityService.ZONE_AUTH_ALFRESCO);
    }
    
    public AuthorityServiceImpl()
    {
        super();
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }        
        
    public void setUserNameMatcher(UserNameMatcher userNameMatcher)
    {
        this.userNameMatcher = userNameMatcher;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setPermissionServiceSPI(PermissionServiceSPI permissionServiceSPI)
    {
        this.permissionServiceSPI = permissionServiceSPI;
    }

    public void setAdminGroups(Set<String> adminGroups)
    {
        this.adminGroups = adminGroups;
    }

    public void setGuestGroups(Set<String> guestGroups)
    {
        this.guestGroups = guestGroups;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        // Fully qualify the admin group names
        if (!this.adminGroups.isEmpty())
        {
            Set<String> adminGroups = new HashSet<String>(this.adminGroups.size());
            for (String group : this.adminGroups)
            {
                adminGroups.add(getName(AuthorityType.GROUP, group));
            }
            this.adminGroups = adminGroups;
        }
        // Fully qualify the guest group names
        if (!this.guestGroups.isEmpty())
        {
            Set<String> guestGroups = new HashSet<String>(this.guestGroups.size());
            for (String group : this.guestGroups)
            {
                guestGroups.add(getName(AuthorityType.GROUP, group));
            }
            this.guestGroups = guestGroups;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasAdminAuthority()
    {
        String currentUserName = AuthenticationUtil.getRunAsUser();
        
        // Determine whether the administrator role is mapped to this user or one of their groups
        return ((currentUserName != null) && getAuthoritiesForUser(currentUserName).contains(PermissionService.ADMINISTRATOR_AUTHORITY));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isAdminAuthority(String authorityName)
    {
        String canonicalName = personService.getUserIdentifier(authorityName);
        if (canonicalName == null)
        {
            canonicalName = authorityName;
        }
        
        // Determine whether the administrator role is mapped to this user or one of their groups
        return getAuthoritiesForUser(canonicalName).contains(PermissionService.ADMINISTRATOR_AUTHORITY);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasGuestAuthority()
    {
        String currentUserName = AuthenticationUtil.getRunAsUser();
        
        // Determine whether the guest role is mapped to this user or one of their groups
        return ((currentUserName != null) && getAuthoritiesForUser(currentUserName).contains(PermissionService.GUEST_AUTHORITY));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isGuestAuthority(String authorityName)
    {
        String canonicalName = personService.getUserIdentifier(authorityName);
        if (canonicalName == null)
        {
            canonicalName = authorityName;
        }
        
        // Determine whether the administrator role is mapped to this user or one of their groups
        return getAuthoritiesForUser(canonicalName).contains(PermissionService.GUEST_AUTHORITY);
    }
    
    /**
     * Checks if the {@code authority} (normally a username) is the same as or is contained
     * within the {@code parentAuthority}.
     * @param authority
     * @param parentAuthority a normalized, case sensitive authority name
     * @return {@code true} if does, {@code false} otherwise.
     */
    private boolean hasAuthority(String authority, String parentAuthority)
    {
        if (parentAuthority.equals(authority))
        {
            return true;
        }
        // Even users are matched case sensitively in ACLs
        if (AuthorityType.getAuthorityType(parentAuthority) == AuthorityType.USER)
        {
            return false;
        }
        NodeRef nodeRef = authorityDAO.getAuthorityNodeRefOrNull(parentAuthority);
        if (nodeRef == null)
        {
            return false;
        }
        return authorityDAO.isAuthorityContained(nodeRef, authority);        
    }
    
    /**
     * {@inheritDoc}
     */
    // note: could be renamed (via deprecation) to getAuthoritiesForUser()
    public Set<String> getAuthorities()
    {
        String currentUserName = AuthenticationUtil.getRunAsUser();
        return getAuthoritiesForUser(currentUserName);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getAuthoritiesForUser(String currentUserName)
    {
        return new UserAuthoritySet(currentUserName);
    }

    // Return mapped roles
    private Set<String> getRoleAuthorities(String currentUserName)
    {
        Set<String> authorities = new TreeSet<String>();
        
        // Check named guest and admin users
        Set<String> adminUsers = authenticationService.getDefaultAdministratorUserNames();
        Set<String> guestUsers = authenticationService.getDefaultGuestUserNames();
        
        String defaultGuestName = AuthenticationUtil.getGuestUserName();
        if (defaultGuestName != null && defaultGuestName.length() > 0)
        {
            guestUsers.add(defaultGuestName);
        }
        
        // Check for name matches using MT + case sensitivity rules
        boolean isAdminUser = containsMatch(adminUsers, currentUserName);
        boolean isGuestUser = containsMatch(guestUsers, currentUserName);
        
        // Check if any of the user's groups are listed as admin groups
        if (!isAdminUser)
        {
            for (String authority : adminGroups)
            {
                if (hasAuthority(currentUserName, authority) || hasAuthority(currentUserName, tenantService.getBaseNameUser(authority)))
                {
                    isAdminUser = true;
                    break;
                }
            }
        }
        
        // Check if user name matches (ignore case) "ROLE_GUEST", if so its a guest. Code originally in PermissionService. 
        if (!isAdminUser && !isGuestUser &&
            tenantService.getBaseNameUser(currentUserName).equalsIgnoreCase(AuthenticationUtil.getGuestUserName()))
        {
            isGuestUser = true;

        }
        
        // Check if any of the user's groups are listed as guest groups
        if (!isAdminUser && !isGuestUser)
        {
            for (String authority : guestGroups)
            {
                if (hasAuthority(currentUserName, authority) || hasAuthority(currentUserName, tenantService.getBaseNameUser(authority)))
                {
                    isGuestUser = true;
                    break;
                }
            }
        }
        
        // Give admin user's the ADMINISTRATOR authorities
        if (isAdminUser)
        {
            authorities.addAll(adminSet);
        }
        // Give all non-guest users the ALL authorities
        if (!isGuestUser)
        {
           authorities.addAll(allSet);
        }
        else
        {
            authorities.addAll(guestSet);
        }
        
        return authorities;
    }
    
    /**
     * {@inheritDoc}
     */
    // see getAuthorities (paged)
    public Set<String> getAllAuthorities(AuthorityType type)
    {
        List<String> auths = getAuthorities(type, null, null, false, false, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage();
        Set<String> authorities = new HashSet<String>(auths.size());
        authorities.addAll(auths);
        return authorities;
    }
    
    /**
     * {@inheritDoc}
     */
    public PagingResults<String> getAuthorities(AuthorityType type, String zoneName, String displayNameFilter, boolean sortByDisplayName, boolean sortAscending, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        if ((type == null) && (zoneName == null))
        {
            throw new IllegalArgumentException("Type and/or zoneName required - both cannot be null");
        }
        if (type != null)
        {
            switch (type)
            {
            case USER:
            case GROUP:
            case ROLE:
                return authorityDAO.getAuthorities(type, zoneName, displayNameFilter, sortByDisplayName, sortAscending, pagingRequest);
            case ADMIN:
            case EVERYONE:
            case GUEST:
            case OWNER:
            default:
                 // others
                 return getOtherAuthorities(type); // either singletons or empty - hence ignore zone/filter/sort/paging
            }
        }
        
        // type is null
        return authorityDAO.getAuthorities(type, zoneName, displayNameFilter, sortByDisplayName, sortAscending, pagingRequest);
    }
    
    private PagingResults<String> getOtherAuthorities(AuthorityType type)
    {
        final List<String> auths = new ArrayList<String>();
        
        switch (type)
        {
        case USER:
        case GROUP:
        case ROLE:
            throw new UnsupportedOperationException("Unexpected authority type: "+type);
        case ADMIN:
            auths.addAll(adminSet);
            break;
        case EVERYONE:
            auths.addAll(allSet);
            break;
        case GUEST:
            auths.addAll(guestSet);
            break;
        case OWNER:
             break;
        default:
            break;
        }
        
        // spoof the page
        return new PagingResults<String>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
            @Override
            public List<String> getPage()
            {
                return auths;
            }
            @Override
            public boolean hasMoreItems()
            {
                return false;
            }
            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return new Pair<Integer, Integer>(auths.size(), auths.size());
            }
        };
    }
    
    /**
     * {@inheritDoc}
     */
    public void addAuthority(String parentName, String childName)
    {
        addAuthority(Collections.singleton(parentName), childName);
    }
    
    /**
     * {@inheritDoc}
     */
    public void addAuthority(Collection<String> parentNames, String childName)
    {
        authorityDAO.addAuthority(parentNames, childName);
    }
    
    private boolean containsMatch(Set<String> names, String name)
    {
        String baseName = this.tenantService.getBaseNameUser(name);
        if (this.tenantService.isEnabled())
        {
            // note: for multi-tenancy, this currently relies on a naming convention which assumes that all tenant
            // admins will have the same base name as the default non-tenant specific admin. Typically "admin" is the
            // default required admin user, although, if for example "bob" is also listed as an admin then all
            // tenant-specific bob's will also have admin authority
            for (String candidate : names)
            {
                if (this.userNameMatcher.matches(candidate, name)
                        || this.userNameMatcher.matches(this.tenantService.getBaseNameUser(candidate), baseName))
                {
                    return true;
                }
            }
        }
        else
        {
            for (String candidate : names)
            {
                if (this.userNameMatcher.matches(candidate, name) || this.userNameMatcher.matches(candidate, baseName))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void checkTypeIsMutable(AuthorityType type)
    {
        if((type == AuthorityType.GROUP) || (type == AuthorityType.ROLE))
        {
            return;
        }
        else
        {
            throw new AuthorityException("Trying to modify a fixed authority");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String createAuthority(AuthorityType type, String shortName)
    {
        return createAuthority(type, shortName, shortName, getDefaultZones());
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(String name)
    {
        deleteAuthority(name, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(String name, boolean cascade)
    {
        AuthorityType type = AuthorityType.getAuthorityType(name);
        checkTypeIsMutable(type);
        if (cascade)
        {
            for (String child : getContainedAuthorities(type, name, true))
            {
                deleteAuthority(child, true);
            }
        }
        authorityDAO.deleteAuthority(name);
        permissionServiceSPI.deletePermissions(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getAllRootAuthorities(AuthorityType type)
    {
        return getAllRootAuthoritiesInZone(null, type);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return authorityDAO.getContainedAuthorities(type, name, immediate);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return authorityDAO.getContainingAuthorities(type, name, immediate);
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getAuthorityNodeRef(String name)
    {
        return authorityDAO.getAuthorityNodeRefOrNull(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getContainingAuthoritiesInZone(AuthorityType type, String authority, final String zoneName, AuthorityFilter filter, int size)
    {
        return authorityDAO.getContainingAuthoritiesInZone(type, authority, zoneName, filter, size);
    }

    @Override
    public void removeAuthority(String parentName, String childName)
    {
        authorityDAO.removeAuthority(parentName, childName);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean authorityExists(String name)
    {
       return authorityDAO.authorityExists(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public String createAuthority(AuthorityType type, String shortName, String authorityDisplayName,
            Set<String> authorityZones)
    {
        checkTypeIsMutable(type);
        String name = getName(type, shortName);
        authorityDAO.createAuthority(name, authorityDisplayName, authorityZones);
        return name;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getAuthorityDisplayName(String name)
    {
        String displayName = authorityDAO.getAuthorityDisplayName(name);
        if(displayName == null)
        {
            displayName = getShortName(name);
        }
        return displayName;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setAuthorityDisplayName(String authorityName, String authorityDisplayName)
    {
        AuthorityType type = AuthorityType.getAuthorityType(authorityName);
        checkTypeIsMutable(type);
        authorityDAO.setAuthorityDisplayName(authorityName, authorityDisplayName);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getAuthorityZones(String name)
    {
        return authorityDAO.getAuthorityZones(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getOrCreateZone(String zoneName)
    {
        return authorityDAO.getOrCreateZone(zoneName);
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef getZone(String zoneName)
    {
        return authorityDAO.getZone(zoneName);
    }
    
    /**
     * {@inheritDoc}
     */
    // see getAuthorities (paged)
    public Set<String> getAllAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        return authorityDAO.getAllAuthoritiesInZone(zoneName, type);
    }
    
    /**
     * {@inheritDoc}
     */
    public void addAuthorityToZones(String authorityName, Set<String> zones)
    {
        authorityDAO.addAuthorityToZones(authorityName, zones);
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeAuthorityFromZones(String authorityName, Set<String> zones)
    {
        authorityDAO.removeAuthorityFromZones(authorityName, zones);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getDefaultZones()
    {
      return DEFAULT_ZONES;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getAllRootAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        return authorityDAO.getRootAuthorities(type, zoneName);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> findAuthorities(AuthorityType type, String parentAuthority, boolean immediate, String displayNamePattern, String zoneName)
    {
        if (type == null || type == AuthorityType.GROUP || type == AuthorityType.USER)
        {
            return authorityDAO.findAuthorities(type, parentAuthority, immediate, displayNamePattern, zoneName);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName(AuthorityType type, String shortName)
    {
        return authorityDAO.getName(type, shortName);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getShortName(String name)
    {
        return authorityDAO.getShortName(name);
    }


    /**
     * Lazy load set of authorities. Try not to iterate or ask for the size. Needed for the case where there
     * is a large number of sites/groups.
     * 
     * @author David Ward, Alan Davis
     */
    public final class UserAuthoritySet extends AbstractSet<String>
    {
        private final String username;
        private Set<String> positiveHits;
        private Set<String> negativeHits;
        private boolean allAuthoritiesLoaded;

        /**
         * @param username
         * @param auths
         */
        public UserAuthoritySet(String username)
        {
            this.username = username;
            positiveHits = getRoleAuthorities(username);
            negativeHits = new TreeSet<String>();
        }

        // Try to avoid evaluating the full set unless we have to!
        private Set<String> getAllAuthorities()
        {
            if (!allAuthoritiesLoaded)
            {
                allAuthoritiesLoaded = true;
                Set<String> tmp = positiveHits;  // must add role authorities back in.
                positiveHits = getContainingAuthorities(null, username, false);
                positiveHits.addAll(tmp);
                negativeHits = null;
            }
            return positiveHits;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(String e)
        {
            return positiveHits.add(e);
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o)
        {
            if (!(o instanceof String))
            {
                return false;
            }
            if (positiveHits.contains(o))
            {
                return true;
            }
            if (allAuthoritiesLoaded || negativeHits.contains(o))
            {
                return false;
            }
            // Remember positive and negative hits for next time
            if (hasAuthority(username, (String) o))
            {
                positiveHits.add((String) o);
                return true;
            }
            else
            {
                negativeHits.add((String)o);
                return false;
            }
        }

        @Override
        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<String> iterator()
        {
            return getAllAuthorities().iterator();
        }

        @Override
        public int size()
        {
            return getAllAuthorities().size();
        }

        public Object getUsername()
        {
            return username;
        }
    }
}
