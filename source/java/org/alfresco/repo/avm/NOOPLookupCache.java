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
package org.alfresco.repo.avm;

import org.alfresco.repo.avm.util.SimplePath;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;

/**
 * A NO-OP implementation of AVM path lookup cache
 */
public class NOOPLookupCache implements LookupCache
{
    /**
     * Make one up.
     */
    public NOOPLookupCache()
    {
    }
    
    /**
     * Lookup a path.
     * 
     * @param store The AVMStore.
     * @param version The versions.
     * @param path The path we are looking up.
     * @param write Whether this is a write lookup.
     * @param includeDeleted
     * @return
     */
    public Lookup lookup(AVMStore store, int version, SimplePath path,
                         boolean write, boolean includeDeleted)
    {
        // Make up a Lookup to hold the results.
        if (path.size() == 0)
        {
            return null;
        }
        Lookup result = new Lookup(store, store.getName(), version);
        // Grab the root node to start the lookup.
        DirectoryNode dir = null;
        // Versions less than 0 mean get current.
        if (version < 0)
        {
            dir = store.getRoot();
        }
        else
        {
            VersionRoot vRoot = AVMDAOs.Instance().fVersionRootDAO.getByVersionID(store, version);
            if (vRoot != null)
            {
                dir = vRoot.getRoot();
            }
//            dir = fAVMNodeDAO.getAVMStoreRoot(store, version);
        }
        if (dir == null)
        {
            return null;
        }
        // Add an entry for the root.
        result.add(dir, "", true, write);
        dir = (DirectoryNode)result.getCurrentNode();
        if (path.size() == 1 && path.get(0).equals(""))
        {
            return result;
        }
        // Now look up each path element in sequence up to one
        // before the end.
        DirectoryNode prevDir = null;
        for (int i = 0; i < path.size() - 1; i++)
        {
            if (!AVMRepository.GetInstance().can(null, dir, PermissionService.READ_CHILDREN, result.getDirectlyContained()))
            {
                throw new AccessDeniedException("Not allowed to read children: " + path.get(i) + " ("+store.getName()+")");
            }
            Pair<ChildEntry, Boolean> childEntryResult = dir.lookupChildEntry(result, path.get(i), includeDeleted);
            if (childEntryResult == null)
            {
                return null;
            }
            AVMNode child = childEntryResult.getFirst().getChild();
            // Every element that is not the last needs to be a directory.
            if (child.getType() != AVMNodeType.PLAIN_DIRECTORY &&
                child.getType() != AVMNodeType.LAYERED_DIRECTORY)
            {
                return null;
            }
            
            prevDir = (DirectoryNode)child;
            String lookupPathElementName = childEntryResult.getFirst().getKey().getName();
            result.add(child, lookupPathElementName, childEntryResult.getSecond(), write);
            dir = (DirectoryNode)result.getCurrentNode();
        }
        // Now look up the last element.
        if (!AVMRepository.GetInstance().can(null, dir, PermissionService.READ_CHILDREN, result.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read children: " + path.get(path.size() - 1) + " ("+store.getName()+")");
        }
        Pair<ChildEntry, Boolean> childEntryResult = dir.lookupChildEntry(result, path.get(path.size() - 1), includeDeleted);
        if (childEntryResult == null)
        {
            if (write && (dir.getType() == AVMNodeType.LAYERED_DIRECTORY))
            {
                // stale ?
                ChildKey key = new ChildKey(prevDir, path.get(path.size() - 1));
                ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
                if (entry != null)
                {
                    if (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE)
                    {
                        return null;
                    }
                    childEntryResult = new Pair<ChildEntry, Boolean>(entry, true);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        AVMNode child = childEntryResult.getFirst().getChild();
        String lookupPathElementName = childEntryResult.getFirst().getKey().getName();
        result.add(child, lookupPathElementName, childEntryResult.getSecond(), write);
        return result;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.LookupCache#onWrite(java.lang.String)
     */
    public void onWrite(String storeName)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.LookupCache#onDelete(java.lang.String)
     */
    public void onDelete(String storeName)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.LookupCache#onSnapshot(java.lang.String)
     */
    public void onSnapshot(String storeName)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.LookupCache#reset()
     */
    public void reset()
    {
    }
    
}
