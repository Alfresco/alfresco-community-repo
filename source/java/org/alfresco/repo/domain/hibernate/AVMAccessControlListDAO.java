/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.repo.domain.hibernate;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * The AVM implementation for getting and setting ACLs.
 * @author britt
 */
public class AVMAccessControlListDAO implements AccessControlListDAO
{
    /**
     * Reference to the AVM Repository instance.
     */
    private AVMRepository fAVMRepository;

    /**
     * Default constructory.
     */
    public AVMAccessControlListDAO()
    {
    }

    public void setAvmRepository(AVMRepository repository)
    {
        fAVMRepository = repository;
    }
    
    /**
     * Get the ACL from a node.
     * @param nodeRef The reference to the node.
     * @return The ACL.
     * @throws InvalidNodeRefException
     */
    public DbAccessControlList getAccessControlList(NodeRef nodeRef)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
        try
        {
            return fAVMRepository.getACL(version, path);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }
    
    /**
     * Set the ACL on a node.
     * @param nodeRef The reference to the node.
     * @param acl The ACL.
     * @throws InvalidNodeRefException
     */
    public void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", nodeRef);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            fAVMRepository.setACL(path, acl);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }
}
