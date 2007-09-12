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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.simple.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.avm.util.RawServices;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.simple.permission.ACL;
import org.alfresco.service.simple.permission.CapabilityRegistry;

/**
 * Basic implementation of a simple ACL.
 * @author britt
 */
public class ACLImpl implements ACL
{
    private static final long serialVersionUID = -8720314753104805631L;
    
    /**
     * Map of capabilities to authorities allowed.
     */
    private Map<String, Set<String>> fAllowed;
    
    /**
     * Map of capabilities to authorities denied.
     */
    private Map<String, Set<String>> fDenied;

    /**
     * Should this ACL be inherited.
     */
    private boolean fInherit;
    
    /**
     * String (compact) representation of ACL.
     */
    private String fStringRep;
    
    /**
     * Reference to the authority service.
     */
    private transient AuthorityService fAuthorityService;
    
    /**
     * Reference to the capability registry.
     */
    private transient CapabilityRegistry fCapabilityRegistry;
    
    /**
     * Initialize a brand new one.
     * @param inherit Should this ACL be inherited.
     */
    public ACLImpl(boolean inherit)
    {
        fInherit = inherit;
        fAuthorityService = RawServices.Instance().getAuthorityService();
        fCapabilityRegistry = RawServices.Instance().getCapabilityRegistry();
        fAllowed = new HashMap<String, Set<String>>();
        fDenied = new HashMap<String, Set<String>>();
        fStringRep = null;
    }
    
    /**
     * Initialize from an external string representation.
     * @param rep
     */
    public ACLImpl(String rep)
    {
        this(true);
        fStringRep = rep;
    }
    
    public ACLImpl(ACL other)
    {
        this(true);
        fStringRep = other.getStringRepresentation();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#allow(java.lang.String, java.lang.String[])
     */
    public void allow(String capability, String... authorities)
    {
        digest();
        // First remove any explicit denies.
        Set<String> denied = fDenied.get(capability);
        if (denied != null)
        {
            for (String authority : authorities)
            {
                denied.remove(authority);
            }
        }
        // Add the authorities to the allowed list.
        Set<String> allowed = fAllowed.get(capability);
        if (allowed == null)
        {
            allowed = new HashSet<String>();
            fAllowed.put(capability, allowed);
        }
        for (String authority : authorities)
        {
            allowed.add(authority);
        }
    }

    /**
     * Helper to decode from the string representation.
     */
    private void digest()
    {
        if (fStringRep == null)
        {
            return;
        }
        String[] segments = fStringRep.split("\\|");
        fInherit = segments[0].equals("i");
        digestMap(segments[1], fAllowed);
        digestMap(segments[2], fDenied);
        fStringRep = null;
    }

    /**
     * Sub helper for decoding string representation.
     * @param string The partial string representation.
     * @param map The map to update.
     */
    private void digestMap(String rep, Map<String, Set<String>> map)
    {
        String[] segments = rep.split(":");
        if (segments.length == 0 || segments[0].equals(""))
        {
            // This means there are no explicit entries.
            return;
        }
        for (String entryRep : segments)
        {
            String[] entryRegs = entryRep.split(";");
            String capability = fCapabilityRegistry.getCapabilityName(Integer.parseInt(entryRegs[0], 16));
            Set<String> authorities = new HashSet<String>();
            map.put(capability, authorities);
            for (int i = 1; i < entryRegs.length; ++i)
            {
                authorities.add(entryRegs[i]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#can(java.lang.String, boolean, java.lang.String)
     */
    public boolean can(String authority, boolean isOwner, String capability)
    {
        digest();
        AuthorityType type = AuthorityType.getAuthorityType(authority);
        // Admin trumps.
        if (type == AuthorityType.ADMIN)
        {
            return true;
        }
        // Look for denies first.
        Set<String> denied = fDenied.get(capability);
        if (denied != null)
        {
            if (denied.contains(authority))
            {
                return false;
            }
            for (String auth : denied)
            {
                if (fAuthorityService.getContainedAuthorities(null, auth, false).contains(authority))
                {
                    return false;
                }
            }
        }
        // Now look for allows.
        Set<String> allowed = fAllowed.get(capability);
        if (allowed != null)
        {
            if (allowed.contains(authority))
            {
                return true;
            }
            for (String auth : allowed)
            {
                if (fAuthorityService.getContainedAuthorities(null, auth, false).contains(authority))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#deny(java.lang.String, java.lang.String[])
     */
    public void deny(String capability, String ... authorities)
    {
        digest();
        // Remove corresponding explicit allows.
        Set<String> allowed = fAllowed.get(capability);
        if (allowed != null)
        {
            for (String authority : authorities)
            {
                allowed.remove(authority);
            }
        }
        // Now add denies.
        Set<String> denied = fDenied.get(capability);
        if (denied == null)
        {
            denied = new HashSet<String>();
            fDenied.put(capability, denied);
        }
        for (String authority : authorities)
        {
            if (AuthorityType.getAuthorityType(authority) == AuthorityType.ADMIN)
            {
                continue;
            }
            denied.add(authority);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#getAllowed(java.lang.String)
     */
    public Set<String> getAllowed(String capability)
    {
        digest();
        Set<String> allowed = new HashSet<String>();
        allowed.add(AuthorityType.ADMIN.getFixedString());
        // Add the explicitly allowed.
        Set<String> expAllowed = fAllowed.get(capability);
        if (expAllowed == null)
        {
            return allowed;
        }
        allowed.addAll(expAllowed);
        for (String authority : expAllowed)
        {
            allowed.addAll(fAuthorityService.getContainedAuthorities(null, authority, false));
        }
        // Now remove based on denials.
        Set<String> denied = fDenied.get(capability);
        if (denied == null)
        {
            return allowed;
        }
        allowed.removeAll(denied);
        // Now those that are indirectly denied.
        for (String authority : denied)
        {
            allowed.removeAll(fAuthorityService.getContainedAuthorities(null, authority, false));
        }
        return allowed;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#getCapabilities(java.lang.String, boolean)
     */
    public Set<String> getCapabilities(String authority, boolean isOwner)
    {
        digest();
        AuthorityType type = AuthorityType.getAuthorityType(authority);
        if (type == AuthorityType.ADMIN)
        {
            return fCapabilityRegistry.getAll();
        }
        Set<String> capabilities = new HashSet<String>();
        // First run through the allowed entries.
        Set<String> containers = null;
        for (Map.Entry<String, Set<String>> entry : fAllowed.entrySet())
        {
            if (entry.getValue().contains(authority))
            {
                capabilities.add(entry.getKey());
                continue;
            }
            if (containers == null)
            {
                containers = fAuthorityService.getContainingAuthorities(null, authority, false);
            }
            for (String auth : containers)
            {
                if (entry.getValue().contains(auth))
                {
                    capabilities.add(entry.getKey());
                    break;
                }
            }
        }
        // Now go through the denials.
        for (Map.Entry<String, Set<String>> entry : fDenied.entrySet())
        {
            if (!capabilities.contains(entry.getKey()))
            {
                continue;
            }
            Set<String> denied = entry.getValue();
            if (denied.contains(authority))
            {
                capabilities.remove(entry.getKey());
                continue;
            }
            if (containers == null)
            {
                containers = fAuthorityService.getContainingAuthorities(null, authority, false);
            }
            for (String auth : containers)
            {
                if (denied.contains(auth))
                {
                    capabilities.remove(entry.getKey());
                    break;
                }
            }
        }
        return capabilities;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#getStringRepresentation()
     */
    public String getStringRepresentation()
    {
        if (fStringRep != null)
        {
            return fStringRep;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(fInherit ? 'i' : 'n');
        builder.append('|');
        int count = 0;
        for (Map.Entry<String, Set<String>> entry : fAllowed.entrySet())
        {
            builder.append(Integer.toString(fCapabilityRegistry.getCapabilityID(entry.getKey()), 16));
            for (String authority : entry.getValue())
            {
                builder.append(';');
                builder.append(authority);
            }
            if (count++ < fAllowed.size() - 1)
            {
                builder.append(':');
            }
        }
        builder.append('|');
        count = 0;
        for (Map.Entry<String, Set<String>> entry : fDenied.entrySet())
        {
            builder.append(Integer.toString(fCapabilityRegistry.getCapabilityID(entry.getKey()), 16));
            for (String authority : entry.getValue())
            {
                builder.append(';');
                builder.append(authority);
            }
            if (count++ < fDenied.size() - 1)
            {
                builder.append(':');
            }
        }
        return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#inherits()
     */
    public boolean inherits()
    {
        digest();
        return fInherit;
    }
}
