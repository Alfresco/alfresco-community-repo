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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.simple.permission.AuthorityCapabilityRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a registry for Authorities and Capabilities.
 * @author britt
 */
public class AuthorityCapabilityRegistryImpl implements
        AuthorityCapabilityRegistry, TransactionListener
{
    private static Log fgLogger = LogFactory.getLog(AuthorityCapabilityRegistryImpl.class);

    private Map<String, Integer> fAuthorityToID;
    
    private Map<Integer, String> fIDToAuthority;
    
    private Map<String, Set<String>> fAuthorityToChild;
    
    private Map<String, Set<String>> fChildToAuthority;
    
    private Map<String, Integer> fCapabilityToID;
    
    private Map<Integer, String> fIDToCapability;
    
    private AuthorityEntryDAO fAuthorityEntryDAO;
    
    private CapabilityEntryDAO fCapabilityEntryDAO;
    
    private Set<String> fInitialCapabilities;
    
    private RetryingTransactionHelper fTransactionHelper;
    
    private AuthorityService fAuthorityService;
    
    public AuthorityCapabilityRegistryImpl()
    {
        fAuthorityToID = new HashMap<String, Integer>();
        fIDToAuthority = new HashMap<Integer, String>();
        fAuthorityToChild = new HashMap<String, Set<String>>();
        fChildToAuthority = new HashMap<String, Set<String>>();
        fCapabilityToID = new HashMap<String, Integer>();
        fIDToCapability = new HashMap<Integer, String>();
    }
    
    public void setAuthorityEntryDAO(AuthorityEntryDAO dao)
    {
        fAuthorityEntryDAO = dao;
    }
    
    public void setCapabilityEntryDAO(CapabilityEntryDAO dao)
    {
        fCapabilityEntryDAO = dao;
    }
    
    public void setCapabilities(Set<String> capabilities)
    {
        fInitialCapabilities = capabilities;
    }
    
    public void setRetryingTransactionHelper(RetryingTransactionHelper helper)
    {
        fTransactionHelper = helper;
    }
    
    public void setAuthorityService(AuthorityService service)
    {
        fAuthorityService = service;
    }

    public void bootstrap()
    {
        fTransactionHelper.doInTransaction(
        new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                init();
                return null;
            }
        });    
    }
    
    public void init()
    {
        List<CapabilityEntry> entries = fCapabilityEntryDAO.getAll();
        for (CapabilityEntry entry : entries)
        {
            String capability = entry.getName().toLowerCase();
            fCapabilityToID.put(capability, entry.getId());
            fIDToCapability.put(entry.getId(), capability);
        }
        for (String entry : fInitialCapabilities)
        {
            entry = entry.toLowerCase();
            if (!fCapabilityToID.containsKey(entry))
            {
                CapabilityEntry newEntry = new CapabilityEntryImpl(entry);
                fCapabilityEntryDAO.save(newEntry);
                fCapabilityToID.put(entry, newEntry.getId());
                fIDToCapability.put(newEntry.getId(), entry);
            }
        }
        List<AuthorityEntry> authorities = fAuthorityEntryDAO.get();
        for (AuthorityEntry entry : authorities)
        {
            String name = normalizeAuthority(entry.getName());
            Integer id = entry.getId();
            fAuthorityToID.put(name, id);
            fIDToAuthority.put(id, name);
            for (AuthorityEntry child : entry.getChildren())
            {
                String childName = normalizeAuthority(child.getName());
                Set<String> children = fAuthorityToChild.get(name);
                if (children == null)
                {
                    children = new HashSet<String>();
                    fAuthorityToChild.put(name, children);
                }
                children.add(childName);
                Set<String> parents = fChildToAuthority.get(childName);
                if (parents == null)
                {
                    parents = new HashSet<String>();
                    fChildToAuthority.put(childName, parents);
                }
                parents.add(name);
            }
        }
        // Now go to AuthorityService to fill anything that might be missing.
        AuthorityType[] types = AuthorityType.values();
        for (AuthorityType type : types)
        {
            Set<String> auths = fAuthorityService.getAllAuthorities(type);
            for (String auth : auths)
            {
                auth = normalizeAuthority(auth);
                if (fAuthorityToID.containsKey(auth))
                {
                    continue;
                }
                AuthorityEntry entry = new AuthorityEntryImpl(auth);
                fAuthorityEntryDAO.save(entry);
                fAuthorityToID.put(auth, entry.getId());
                fIDToAuthority.put(entry.getId(), auth);
            }
        }
        for (AuthorityType type : types)
        {
            Set<String> auths = fAuthorityService.getAllAuthorities(type);
            for (String auth : auths)
            {
                AuthorityType aType = AuthorityType.getAuthorityType(auth);
                if (aType == AuthorityType.ROLE || aType == AuthorityType.EVERYONE ||
                    aType == AuthorityType.GUEST)
                {
                    continue;
                }
                Set<String> children = fAuthorityService.getContainedAuthorities(null, auth, true);
                auth = normalizeAuthority(auth);
                Set<String> found = fAuthorityToChild.get(auth);
                if (found == null)
                {
                    found = new HashSet<String>();
                    fAuthorityToChild.put(auth, found);
                }
                AuthorityEntry entry = null;
                if (!fAuthorityToID.containsKey(auth))
                {
                    entry = new AuthorityEntryImpl(auth);
                    fAuthorityEntryDAO.save(entry);
                    fAuthorityToID.put(auth, entry.getId());
                    fIDToAuthority.put(entry.getId(), auth);
                }
                else
                {
                    entry = fAuthorityEntryDAO.get(fAuthorityToID.get(auth));
                }
                for (String child : children)
                {
                    child = normalizeAuthority(child);
                    if (found.contains(child))
                    {
                        continue;
                    }
                    AuthorityEntry childEntry = null;
                    if (!fAuthorityToID.containsKey(child))
                    {
                        childEntry = new AuthorityEntryImpl(child);
                        fAuthorityEntryDAO.save(childEntry);
                        fAuthorityToID.put(child, childEntry.getId());
                        fIDToAuthority.put(childEntry.getId(), child);
                    }
                    else
                    {
                        childEntry = fAuthorityEntryDAO.get(fAuthorityToID.get(child));
                    }
                    entry.getChildren().add(childEntry);
                    found.add(child);
                    Set<String> parents = fChildToAuthority.get(child);
                    if (parents == null)
                    {
                        parents = new HashSet<String>();
                        fChildToAuthority.put(child, parents);
                    }
                    parents.add(auth);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#addAuthority(java.lang.String, java.lang.String)
     */
    public synchronized void addAuthority(String authority, String parent)
    {
        authority = normalizeAuthority(authority);
        parent = normalizeAuthority(parent);
        AlfrescoTransactionSupport.bindListener(this);
        AuthorityEntry entry = null;
        if (!fAuthorityToID.containsKey(authority))
        {
            entry = new AuthorityEntryImpl(authority);
            fAuthorityEntryDAO.save(entry);
            fAuthorityToID.put(authority, entry.getId());
            fIDToAuthority.put(entry.getId(), authority);
        }
        if (parent != null)
        {
            if (entry == null)
            {
                Integer id = fAuthorityToID.get(authority);
                if (id == null)
                {
                    fgLogger.error("Authority Doesn't exist: " + authority, new Exception());
                    return;
                }
                entry = fAuthorityEntryDAO.get(id);
            }
            Integer id = fAuthorityToID.get(parent);
            if (id == null)
            {
                fgLogger.error("Authority Doesn't exist: " + authority, new Exception());
                return;
            }
            AuthorityEntry pEntry = fAuthorityEntryDAO.get(id);
            pEntry.getChildren().add(entry);
            Set<String> children = fAuthorityToChild.get(parent);
            if (children == null)
            {
                children = new HashSet<String>();
                fAuthorityToChild.put(parent, children);
            }
            children.add(authority);
            Set<String> parents = fChildToAuthority.get(authority);
            if (parents == null)
            {
                parents = new HashSet<String>();
                fChildToAuthority.put(authority, parents);
            }
            parents.add(parent);
        }
    }

    /**
     * Get case normalized authority.
     */
    public String normalizeAuthority(String authority)
    {
        if (authority == null)
        {
            return null;
        }
        AuthorityType type = AuthorityType.getAuthorityType(authority);
        switch (type)
        {
            case ADMIN :
            {
                return authority;
            }
            case EVERYONE :
            {
                return PermissionService.ALL_AUTHORITIES;   
            }
            case GROUP :
            {
                return PermissionService.GROUP_PREFIX + authority.substring(PermissionService.GROUP_PREFIX.length()).toLowerCase();
            }
            case USER :
            case GUEST :
            {
                return authority.toLowerCase();
            }
            case OWNER :
            {
                return PermissionService.OWNER_AUTHORITY;
            }
            case ROLE :
            {
                return PermissionService.ROLE_PREFIX + authority.substring(PermissionService.ROLE_PREFIX.length()).toLowerCase();
            }
            default :
            {
                return null;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#removeAuthority(java.lang.String)
     */
    public synchronized void removeAuthority(String authority)
    {
        authority = normalizeAuthority(authority);
        AlfrescoTransactionSupport.bindListener(this);
        Integer id = fAuthorityToID.get(authority);
        if (id == null)
        {
            return;
        }
        AuthorityEntry entry = fAuthorityEntryDAO.get(id);
        if (entry == null)
        {
            fgLogger.error("Authority Doesn't exist: " + authority, new Exception());
            return;
        }
        List<AuthorityEntry> parents = fAuthorityEntryDAO.getParents(entry);
        for (AuthorityEntry parent : parents)
        {
            parent.getChildren().remove(entry);
        }
        fAuthorityEntryDAO.delete(entry);
        Set<String> pNames = fChildToAuthority.get(authority);
        if (pNames != null)
        {
            for (String parent : pNames)
            {
                fAuthorityToChild.get(parent).remove(authority);
            }
        }
        fChildToAuthority.remove(authority);
        id = fAuthorityToID.remove(authority);
        fIDToAuthority.remove(id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#removeAuthorityChild(java.lang.String, java.lang.String)
     */
    public synchronized void removeAuthorityChild(String parent, String child)
    {
        parent = normalizeAuthority(parent);
        child = normalizeAuthority(child);
        AlfrescoTransactionSupport.bindListener(this);
        Integer id = fAuthorityToID.get(child);
        if (id == null)
        {
            return;
        }
        AuthorityEntry cEntry = fAuthorityEntryDAO.get(id);
        id = fAuthorityToID.get(parent);
        if (id == null)
        {
            return;
        }
        AuthorityEntry cParent = fAuthorityEntryDAO.get(parent);
        cParent.getChildren().remove(cEntry);
        fAuthorityToChild.get(parent).remove(child);
        fChildToAuthority.get(child).remove(parent);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#addCapability(java.lang.String)
     */
    public synchronized void addCapability(String capability)
    {
        capability = capability.toLowerCase();
        AlfrescoTransactionSupport.bindListener(this);
        CapabilityEntry entry = fCapabilityEntryDAO.get(capability);
        if (entry != null)
        {
            return;
        }
        entry = new CapabilityEntryImpl(capability);
        fCapabilityEntryDAO.save(entry);
        entry = fCapabilityEntryDAO.get(capability);
        fCapabilityToID.put(capability, entry.getId());
        fIDToCapability.put(entry.getId(), capability);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getAllAuthorities()
     */
    public synchronized Set<String> getAllAuthorities()
    {
        return new HashSet<String>(fAuthorityToID.keySet());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getAllCapabilities()
     */
    public synchronized Set<String> getAllCapabilities()
    {
        return new HashSet<String>(fCapabilityToID.keySet());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getAuthorityID(java.lang.String)
     */
    public synchronized int getAuthorityID(String authority)
    {
        authority = normalizeAuthority(authority);
        Integer id = fAuthorityToID.get(authority);
        if (id == null)
        {
            return -1;
        }
        return id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getAuthorityName(int)
     */
    public synchronized String getAuthorityName(int id)
    {
        return fIDToAuthority.get(id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getCapabilityID(java.lang.String)
     */
    public synchronized int getCapabilityID(String capability)
    {
        capability = capability.toLowerCase();
        Integer id = fCapabilityToID.get(capability);
        if (id == null)
        {
            return -1;
        }
        return id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getCapabilityName(int)
     */
    public synchronized String getCapabilityName(int id)
    {
        return fIDToCapability.get(id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getContainedAuthorities(java.lang.String)
     */
    public synchronized Set<String> getContainedAuthorities(String authority)
    {
        authority = normalizeAuthority(authority);
        Set<String> contained = new HashSet<String>();
        contained.add(authority);
        int count = 1;
        int oldCount = -1;
        while (count != oldCount)
        {
            Set<String> more = new HashSet<String>();
            for (String auth : contained)
            {
                Set<String> children = fAuthorityToChild.get(auth);
                if (children != null)
                {
                    more.addAll(children);
                }
            }
            contained.addAll(more);
            oldCount = count;
            count = contained.size();
        }
        contained.remove(authority);
        return contained;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.AuthorityCapabilityRegistry#getContainerAuthorities(java.lang.String)
     */
    public Set<String> getContainerAuthorities(String authority)
    {
        authority = normalizeAuthority(authority);
        Set<String> containers = new HashSet<String>();
        containers.add(authority);
        int count = 1;
        int oldCount = -1;
        while (count != oldCount)
        {
            Set<String> more = new HashSet<String>();
            for (String auth : containers)
            {
                Set<String> parents = fChildToAuthority.get(auth);
                if (parents != null)
                {
                    more.addAll(parents);
                }
            }
            containers.addAll(more);
            oldCount = count;
            count = containers.size();
        }
        containers.remove(authority);
        return containers;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
     */
    public void afterCommit()
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
     */
    public synchronized void afterRollback()
    {
        fAuthorityToID.clear();
        fIDToAuthority.clear();
        fAuthorityToChild.clear();
        fChildToAuthority.clear();
        fCapabilityToID.clear();
        fIDToCapability.clear();
        bootstrap();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
     */
    public void beforeCommit(boolean readOnly)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
     */
    public void beforeCompletion()
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#flush()
     */
    public void flush()
    {
    }
}
