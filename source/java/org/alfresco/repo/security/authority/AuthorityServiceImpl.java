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
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.person.UserNameMatcher;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.InitializingBean;

/**
 * The default implementation of the authority service.
 * 
 * @author Andy Hind
 */
public class AuthorityServiceImpl implements AuthorityService, InitializingBean
{
    private static Set<String> DEFAULT_ZONES = new HashSet<String>();
    
    private PersonService personService;

    private NodeService nodeService;
    
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

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
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

    public boolean hasAdminAuthority()
    {
        String currentUserName = AuthenticationUtil.getRunAsUser();
        
        // Determine whether the administrator role is mapped to this user or one of their groups
        return ((currentUserName != null) && getAuthoritiesForUser(currentUserName).contains(PermissionService.ADMINISTRATOR_AUTHORITY));
    }

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

    public boolean hasGuestAuthority()
    {
        String currentUserName = AuthenticationUtil.getRunAsUser();
        
        // Determine whether the guest role is mapped to this user or one of their groups
        return ((currentUserName != null) && getAuthoritiesForUser(currentUserName).contains(PermissionService.GUEST_AUTHORITY));
    }

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

    public Set<String> getAuthorities()
    {
        String currentUserName = AuthenticationUtil.getRunAsUser();
        return getAuthoritiesForUser(currentUserName);
    }

    public Set<String> getAuthoritiesForUser(String currentUserName)
    {
        Set<String> authorities = new HashSet<String>(64);

        authorities.addAll(getContainingAuthorities(null, currentUserName, false));
        
        // Work out mapped roles
        
        // Check named guest and admin users
        Set<String> adminUsers = this.authenticationService.getDefaultAdministratorUserNames();
        
        Set<String> guestUsers = this.authenticationService.getDefaultGuestUserNames();
        
        String defaultGuestName = AuthenticationUtil.getGuestUserName();
        if (defaultGuestName != null && defaultGuestName.length() > 0)
        {
            guestUsers.add(defaultGuestName);
        }
        
        // Check for name matches using MT + case sensitivity rules
        boolean isAdminUser = containsMatch(adminUsers, currentUserName);
        boolean isGuestUser = containsMatch(guestUsers, currentUserName);
        
        // Check if any of the user's groups are listed as admin groups
        if (!isAdminUser && !adminGroups.isEmpty())
        {
            for (String authority : authorities)
            {
                if (adminGroups.contains(authority) || adminGroups.contains(tenantService.getBaseNameUser(authority)))
                {
                    isAdminUser = true;
                    break;
                }
            }
        }
        // Check if any of the user's groups are listed as guest groups
        if (!isAdminUser && !isGuestUser && !guestGroups.isEmpty())
        {
            for (String authority : authorities)
            {
                if (guestGroups.contains(authority) || guestGroups.contains(tenantService.getBaseNameUser(authority)))
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
            authorities.addAll(authorityDAO.getAllAuthorities(type));
            break;
        case OWNER:
             break;
        case ROLE:
            authorities.addAll(authorityDAO.getAllAuthorities(type));
            break;
        case USER:
            for (NodeRef personRef : personService.getAllPeople())
            {
                authorities.add(DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(personRef,
                        ContentModel.PROP_USERNAME)));
            }
            break;
        default:
            break;
        }
        return authorities;
    }
    
    public void addAuthority(String parentName, String childName)
    {
        addAuthority(Collections.singleton(parentName), childName);
    }

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
    
    public String createAuthority(AuthorityType type, String shortName)
    {
        return createAuthority(type, shortName, shortName, getDefaultZones());
    }

    public void deleteAuthority(String name)
    {
        deleteAuthority(name, false);
    }

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

    public Set<String> getAllRootAuthorities(AuthorityType type)
    {
        return getAllRootAuthoritiesInZone(null, type);
    }

    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return authorityDAO.getContainedAuthorities(type, name, immediate);
    }

    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return authorityDAO.getContainingAuthorities(type, name, immediate);
    }
    
    public NodeRef getAuthorityNodeRef(String name)
    {
        return authorityDAO.getAuthorityNodeRefOrNull(name);
    }

    public void removeAuthority(String parentName, String childName)
    {
        authorityDAO.removeAuthority(parentName, childName);
    }

    public boolean authorityExists(String name)
    {
       return authorityDAO.authorityExists(name);
    }

    public String createAuthority(AuthorityType type, String shortName, String authorityDisplayName,
            Set<String> authorityZones)
    {
        checkTypeIsMutable(type);
        String name = getName(type, shortName);
        authorityDAO.createAuthority(name, authorityDisplayName, authorityZones);
        return name;
    }

    public String getAuthorityDisplayName(String name)
    {
        String displayName = authorityDAO.getAuthorityDisplayName(name);
        if(displayName == null)
        {
            displayName = getShortName(name);
        }
        return displayName;
    }

    public void setAuthorityDisplayName(String authorityName, String authorityDisplayName)
    {
        AuthorityType type = AuthorityType.getAuthorityType(authorityName);
        checkTypeIsMutable(type);
        authorityDAO.setAuthorityDisplayName(authorityName, authorityDisplayName);
    }

    public Set<String> getAuthorityZones(String name)
    {
        return authorityDAO.getAuthorityZones(name);
    }

    public NodeRef getOrCreateZone(String zoneName)
    {
        return authorityDAO.getOrCreateZone(zoneName);
    }
    
    public NodeRef getZone(String zoneName)
    {
        return authorityDAO.getZone(zoneName);
    }

    public Set<String> getAllAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        return authorityDAO.getAllAuthoritiesInZone(zoneName, type);
    }

    public void addAuthorityToZones(String authorityName, Set<String> zones)
    {
        authorityDAO.addAuthorityToZones(authorityName,  zones);
        
    }

    public void removeAuthorityFromZones(String authorityName, Set<String> zones)
    {
        authorityDAO.removeAuthorityFromZones(authorityName,  zones);   
    }

    public Set<String> getDefaultZones()
    {
      return DEFAULT_ZONES;
    }

    public Set<String> getAllRootAuthoritiesInZone(String zoneName, AuthorityType type)
    {
        return authorityDAO.getRootAuthorities(type, zoneName);
    }

    public Set<String> findAuthorities(AuthorityType type, String parentAuthority, boolean immediate,
            String displayNamePattern, String zoneName)
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

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthorityService#getName(org.alfresco.service.cmr.security.AuthorityType, java.lang.String)
     */
    public String getName(AuthorityType type, String shortName)
    {
        return authorityDAO.getName(type, shortName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthorityService#getShortName(java.lang.String)
     */
    public String getShortName(String name)
    {
        return authorityDAO.getShortName(name);
    }        
}
