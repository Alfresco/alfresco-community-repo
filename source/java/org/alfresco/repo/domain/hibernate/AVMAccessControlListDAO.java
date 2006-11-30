/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
