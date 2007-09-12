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
 * Basic implementation of an ACL
 * @author britt
 */
public class ACLImpl implements ACL
{
    private static final long serialVersionUID = 5184311729355811095L;

    /**
     * The allowed entries for users.
     */
    private Map<String, Set<String>> fUserAllows;
    
    /**
     * The allowed entries for groups.
     */
    private Map<String, Set<String>> fGroupAllows;
    
    /**
     * The denied entries for users.
     */
    private Map<String, Set<String>> fUserDenies;

    /**
     * The denied entries for groups.
     */
    private Map<String, Set<String>> fGroupDenies;
    
    /**
     * Bit indicating whether a child (however that is defined)
     * should inherit these permissions.
     */
    private boolean fInherit;
    
    private transient AuthorityService fAuthorityService;
    
    private transient CapabilityRegistry fCapabilityRegistry;
    
    private transient String fStringRep;
    
    public ACLImpl(boolean inherit)
    {
        fAuthorityService = RawServices.Instance().getAuthorityService();
        fCapabilityRegistry = RawServices.Instance().getCapabilityRegistry();
        fUserAllows = new HashMap<String, Set<String>>();
        fGroupAllows = new HashMap<String, Set<String>>();
        fUserDenies = new HashMap<String, Set<String>>();
        fGroupDenies = new HashMap<String, Set<String>>();
        fInherit = inherit;
        fStringRep = null;
    }
    
    public ACLImpl(String stringRep)
    {
        this(true);
        fStringRep = stringRep;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#allow(java.lang.String, java.lang.String[])
     */
    public void allow(String agent, String... capabilities)
    {
        digest();
        AuthorityType type = AuthorityType.getAuthorityType(agent);
        if (type == AuthorityType.ADMIN)
        {
            return;
        }
        Set<String> capDenied = null;
        Set<String> capAllowed = null;
        switch (type)
        {
            case EVERYONE :
            case GROUP :
            {
                capDenied = fGroupDenies.get(agent);
                capAllowed = fGroupAllows.get(agent);
                if (capAllowed == null)
                {
                    capAllowed = new HashSet<String>();
                    fGroupAllows.put(agent, capAllowed);
                }
                break;
            }
            case ADMIN :
            case USER :
            case OWNER :
            {
                capDenied = fUserDenies.get(agent);
                capAllowed = fUserAllows.get(agent);
                if (capAllowed == null)
                {
                    capAllowed = new HashSet<String>();
                    fUserAllows.put(agent, capAllowed); 
                }
                break;
            }
            default :
            {
                // ignore.
                return;
            }
        }
        if (capDenied != null)
        {
            for (String cap : capabilities)
            {
                capDenied.remove(cap);
                capAllowed.add(cap);
            }
        }
        else
        {
            for (String cap : capabilities)
            {
                capAllowed.add(cap);
            }
        }
    }

    private void digestMap(String mapRep, Map<String, Set<String>> map)
    {
        String[] segments = mapRep.split(":");
        if (segments.length == 0 || segments[0].equals(""))
        {
            return;
        }
        for (String segment : segments)
        {
            String[] entrySeg = segment.split(";");
            if (entrySeg.length == 0 || entrySeg[0].equals(""))
            {
                continue;
            }
            Set<String> caps = new HashSet<String>();
            map.put(entrySeg[0], caps);
            for (int i = 1; i < entrySeg.length; ++i)
            {
                String cap = fCapabilityRegistry.getCapabilityName(Integer.parseInt(entrySeg[i], 16));
                if (cap == null)
                {
                    continue;
                }
                caps.add(cap);
            }
        }
    }
    
    private void digest()
    {
        if (fStringRep == null)
        {
            return;
        }
        String[] segments = fStringRep.split("\\|");
        fInherit = segments[0].equals("i") ? true : false;
        digestMap(segments[1], fUserAllows);
        digestMap(segments[2], fUserDenies);
        digestMap(segments[3], fGroupAllows);
        digestMap(segments[4], fGroupDenies);
        fStringRep = null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#can(java.lang.String, java.lang.String)
     */
    public boolean can(String agent, String capability)
    {
        digest();
        AuthorityType type = AuthorityType.getAuthorityType(agent);
        // Admin trumps all.
        if (type == AuthorityType.ADMIN)
        {
            return true;
        }
        // Next check for denied entries that apply.
        Set<String> denied = fUserDenies.get(agent);
        if (denied == null)
        {
            denied = new HashSet<String>();
        }
        Set<String> containing = fAuthorityService.getContainingAuthorities(null, agent, false);
        Set<String> found = fGroupDenies.get(agent);
        if (found != null)
        {
            denied.addAll(found);
        }
        for (String container : containing)
        {
            found = fGroupDenies.get(container);
            if (found != null)
            {
                denied.addAll(found);
            }
        }
        if (denied.contains(capability))
        {
            return false;
        }
        // Now go look for the alloweds.
        Set<String> allowed = fUserAllows.get(agent);
        if (allowed == null)
        {
            allowed = new HashSet<String>();
        }
        found = fGroupAllows.get(agent);
        if (found != null)
        {
            allowed.addAll(found);
        }
        for (String container : containing)
        {
            found = fGroupAllows.get(container);
            if (found != null)
            {
                allowed.addAll(found);
            }
        }
        return allowed.contains(capability);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#deny(java.lang.String, java.lang.String[])
     */
    public void deny(String agent, String ... capabilities)
    {
        digest();
        AuthorityType type = AuthorityType.getAuthorityType(agent);
        if (type == AuthorityType.ADMIN)
        {
            return;
        }
        Set<String> allowed = fUserAllows.get(agent);
        if (allowed != null)
        {
            for (String cap : capabilities)
            {
                allowed.remove(cap);
            }
        }
        allowed = fGroupAllows.get(agent);
        if (allowed != null)
        {
            for (String cap : capabilities)
            {
                allowed.remove(cap);
            }
        }
        Set<String> denied = null;
        switch (type)
        {
            case EVERYONE :
            case GROUP :
            {
                denied = fGroupDenies.get(agent);
                if (denied == null)
                {
                    denied = new HashSet<String>();
                    fGroupDenies.put(agent, denied);
                }
                break;
            }
            case OWNER :
            case USER :
            case GUEST :
            {
                denied = fUserDenies.get(agent);
                if (denied == null)
                {
                    denied = new HashSet<String>();
                    fUserDenies.put(agent, denied);
                }
                break;
            }
            default :
            {
                // Cop Out!
                return;
            }
        }
        for (String cap : capabilities)
        {
            denied.add(cap);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.ACL#getCapabilities(java.lang.String)
     */
    public Set<String> getCapabilities(String agent)
    {
        digest();
        AuthorityType type = AuthorityType.getAuthorityType(agent);
        if (type == AuthorityType.ADMIN)
        {
            return fCapabilityRegistry.getAll();
        }
        // First add in all the possible capabilities from the allow sets.
        Set<String> capabilities = new HashSet<String>();
        Set<String> found = fUserAllows.get(agent);
        if (found != null)
        {
            capabilities.addAll(found);
        }
        found = fGroupAllows.get(agent);
        if (found != null)
        {
            capabilities.addAll(found);
        }
        Set<String> containers = fAuthorityService.getContainingAuthorities(null, agent, false);
        for (String container : containers)
        {
            found = fGroupAllows.get(container);
            if (found != null)
            {
                capabilities.addAll(found);
            }
        }
        // Now remove everything that's denied.
        found = fUserDenies.get(agent);
        if (found != null)
        {
            capabilities.removeAll(found);
        }
        found = fGroupDenies.get(agent);
        if (found != null)
        {
            capabilities.removeAll(found);
        }
        for (String container : containers)
        {
            found = fGroupDenies.get(container);
            if (found != null)
            {
                capabilities.removeAll(found);
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
        for (Map.Entry<String, Set<String>> entry : fUserAllows.entrySet())
        {
            builder.append(entry.getKey());
            if (entry.getValue().size() != 0)
            {
                for (String cap : entry.getValue())
                {
                    builder.append(';');
                    builder.append(Integer.toString(fCapabilityRegistry.getCapabilityID(cap), 16));
                }
            }
            if (count++ < fUserAllows.size() - 1)
            {
                builder.append(':');
            }
        }
        builder.append('|');
        count = 0;
        for (Map.Entry<String, Set<String>> entry : fUserDenies.entrySet())
        {
            builder.append(entry.getKey());
            if (entry.getValue().size() != 0)
            {
                for (String cap : entry.getValue())
                {
                    builder.append(';');
                    builder.append(Integer.toString(fCapabilityRegistry.getCapabilityID(cap), 16));
                }
            }
            if (count++ < fUserDenies.size() - 1)
            {
                builder.append(':');
            }
        }
        builder.append('|');
        count = 0;
        for (Map.Entry<String, Set<String>> entry : fGroupAllows.entrySet())
        {
            builder.append(entry.getKey());
            if (entry.getValue().size() != 0)
            {
                for (String cap : entry.getValue())
                {
                    builder.append(';');
                    builder.append(Integer.toString(fCapabilityRegistry.getCapabilityID(cap), 16));
                }
            }
            if (count++ < fGroupAllows.size() - 1)
            {
                builder.append(':');
            }
        }
        builder.append('|');
        count = 0;
        for (Map.Entry<String, Set<String>> entry : fGroupDenies.entrySet())
        {
            builder.append(entry.getKey());
            if (entry.getValue().size() != 0)
            {
                for (String cap : entry.getValue())
                {
                    builder.append(';');
                    builder.append(Integer.toString(fCapabilityRegistry.getCapabilityID(cap), 16));
                }
            }
            if (count++ < fGroupDenies.size() - 1)
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
        return fInherit;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[" + getStringRepresentation() + "]";
    }
}
