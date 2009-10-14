/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
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
                throw new AccessDeniedException("Not allowed to read children: " + path.get(i));
            }
            Pair<AVMNode, Boolean> child = dir.lookupChild(result, path.get(i), includeDeleted);
            if (child == null)
            {
                return null;
            }
            // Every element that is not the last needs to be a directory.
            if (child.getFirst().getType() != AVMNodeType.PLAIN_DIRECTORY &&
                child.getFirst().getType() != AVMNodeType.LAYERED_DIRECTORY)
            {
                return null;
            }
            
            prevDir = (DirectoryNode)child.getFirst();
            result.add(child.getFirst(), path.get(i), child.getSecond(), write);
            dir = (DirectoryNode)result.getCurrentNode();
        }
        // Now look up the last element.
        if (!AVMRepository.GetInstance().can(null, dir, PermissionService.READ_CHILDREN, result.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read children: " + path.get(path.size() - 1));
        }
        Pair<AVMNode, Boolean> child = dir.lookupChild(result, path.get(path.size() - 1),
                                        includeDeleted);
        if (child == null)
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
                    child = new Pair<AVMNode, Boolean>(AVMNodeUnwrapper.Unwrap(entry.getChild()), true);
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
        result.add(child.getFirst(), path.get(path.size() - 1), child.getSecond(), write);
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
