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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.util.Pair;

/**
 * The default implementation of the authority service.
 * 
 * @author Andy Hind
 */
public class SimpleAuthorityServiceImpl implements AuthorityService
{
    private PersonService personService;

    private Set<String> adminSet = Collections.singleton(PermissionService.ADMINISTRATOR_AUTHORITY);

    private Set<String> guestSet = Collections.singleton(PermissionService.GUEST_AUTHORITY);

    private Set<String> allSet = Collections.singleton(PermissionService.ALL_AUTHORITIES);

    private Set<String> adminUsers;

    private AuthenticationContext authenticationContext;

    private Set<String> guestUsers;
    
    private TenantService tenantService;
    

    public SimpleAuthorityServiceImpl()
    {
        super();
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    

    public boolean hasAdminAuthority()
    {
        String currentUserName = authenticationContext.getCurrentUserName();

        // note: for MT, this currently relies on a naming convention which assumes that all tenant admins will 
        // have the same base name as the default non-tenant specific admin. Typically "admin" is the default required admin user, 
        // although, if for example "bob" is also listed as an admin then all tenant-specific bob's will also have admin authority 

        return ((currentUserName != null) && (adminUsers.contains(currentUserName) || adminUsers.contains(tenantService.getBaseNameUser(currentUserName))));
    }

    public boolean isAdminAuthority(String authorityName)
    {
        String canonicalName = personService.getUserIdentifier(authorityName);
        if (canonicalName == null)
        {
            canonicalName = authorityName;
        }
        return adminUsers.contains(canonicalName);
    }

    public boolean hasGuestAuthority()
    {
        String currentUserName = authenticationContext.getCurrentUserName();

        // note: for MT, this currently relies on a naming convention which assumes that all tenant admins will 
        // have the same base name as the default non-tenant specific guest. 

        return ((currentUserName != null) && (guestUsers.contains(currentUserName) || guestUsers.contains(tenantService.getBaseNameUser(currentUserName))));
    }

    public boolean isGuestAuthority(String authorityName)
    {
        String canonicalName = personService.getUserIdentifier(authorityName);
        if (canonicalName == null)
        {
            canonicalName = authorityName;
        }
        return guestUsers.contains(canonicalName);
    }

    // IOC

    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public void setAdminUsers(Set<String> adminUsers)
    {
        this.adminUsers = adminUsers;
    }
 
    public void setGuestUsers(Set<String> guestUsers)
    {
        this.guestUsers = guestUsers;
    }
 
    public Set<String> getAuthorities()
    {
        Set<String> authorities = new HashSet<String>();
        String currentUserName = authenticationContext.getCurrentUserName();
        if (adminUsers.contains(currentUserName))
        {
            authorities.addAll(adminSet);
        }
        else if (!guestUsers.contains(currentUserName))
        {
            authorities.addAll(allSet);
        }
        return authorities;
    }

    public Set<String> getAllAuthorities(AuthorityType type)
    {
        Set<String> authorities = new HashSet<String>();
        switch (type)
        {
        case ADMIN:
            authorities.addAll(adminSet);
            break;
        case EVERYONE:
            authorities.addAll(allSet);
            break;
        case GUEST:
            authorities.addAll(guestSet);
            break;
        case GROUP:
            authorities.addAll(allSet);
            break;
        case OWNER:
             break;
        case ROLE:
            break;
        case USER:
            for (PersonInfo person : personService.getPeople(null, true, null, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage())
            {
                authorities.add(person.getUserName());
            }
            break;
        default:
            break;
        }
        return authorities;
    }
    
    public PagingResults<String> getAuthorities(AuthorityType type, String zoneName, String displayNameFilter, boolean sortByDisplayName, boolean sortAscending, PagingRequest pagingRequest)
    {
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
                return Collections.<String>emptyList();
            }
            @Override
            public boolean hasMoreItems()
            {
                return false;
            }
            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return null;
            }
            @Override
            public boolean permissionsApplied()
            {
                return true;
            }
        };
    }
    
    public void addAuthority(String parentName, String childName)
    {
        
    }

    public void addAuthority(Collection<String> parentNames, String childName)
    {

    }

    public String createAuthority(AuthorityType type, String shortName)
    {
       return "";
    }

    
    public void deleteAuthority(String name)
    {
      
    }

    public void deleteAuthority(String name, boolean cascade)
    {
        
    }

    public Set<String> getAllRootAuthorities(AuthorityType type)
    {
        return getAllAuthorities(type);
    }

    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return Collections.<String>emptySet();
    }

    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return Collections.<String>emptySet();
    }

    public String getName(AuthorityType type, String shortName)
    {
        if (type.isFixedString())
        {
            return type.getFixedString();
        }
        else if (type.isPrefixed())
        {
            return type.getPrefixString() + shortName;
        }
        else
        {
            return shortName;
        }
    }

    public String getShortName(String name)
    {
        AuthorityType type = AuthorityType.getAuthorityType(name);
        if (type.isFixedString())
        {
            return "";
        }
        else if (type.isPrefixed())
        {
            return name.substring(type.getPrefixString().length());
        }
        else
        {
            return name;
        }

    }

    public void removeAuthority(String parentName, String childName)
    {
        
    }

    public boolean authorityExists(String name)
    {
        return false;
    }

    public Set<String> getAuthoritiesForUser(String currentUserName)
    {
        Set<String> authorities = new HashSet<String>();
        if (adminUsers.contains(currentUserName))
        {
            authorities.addAll(adminSet);
        }
        if(AuthorityType.getAuthorityType(currentUserName) != AuthorityType.GUEST)
        {
           authorities.addAll(allSet);
        }
        return authorities;
    }

    public String getAuthorityDisplayName(String name)
    {
        return "";
    }

    public void setAuthorityDisplayName(String authorityName, String authorityDisplayName)
    {
        
    }

    public Set<String> getAllAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        return Collections.<String>emptySet();
    }

    public NodeRef getOrCreateZone(String zoneName)
    {
        return null;
    }

    public void addAuthorityToZones(String authorityName, Set<String> zones)
    {
        
    }

    public String createAuthority(AuthorityType type, String shortName, String authorityDisplayName, Set<String> authorityZones)
    {
       return "";
    }

    public Set<String> getAllRootAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        return Collections.<String>emptySet();
    }

    public Set<String> getAuthorityZones(String name)
    {
        return Collections.<String>emptySet();
    }

    public Set<String> getDefaultZones()
    {
        return Collections.<String>emptySet();
    }

    public void removeAuthorityFromZones(String authorityName, Set<String> zones)
    {
        
    }

    public NodeRef getZone(String zoneName)
    {
        return null;
    }

    public NodeRef getAuthorityNodeRef(String name)
    {
        return null;
    }

    public Set<String> findAuthorities(AuthorityType type, String parentAuthority, boolean immediate,
            String displayNamePattern, String zoneName)
    {
        return Collections.emptySet();
    }        
}
