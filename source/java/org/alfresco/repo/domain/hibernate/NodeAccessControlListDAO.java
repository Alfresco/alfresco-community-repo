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

import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Node implementation for getting and setting ACLs.
 * @author britt
 */
public class NodeAccessControlListDAO extends HibernateDaoSupport implements AccessControlListDAO
{
    /**
     * The DAO for Nodes.
     */
    private NodeDaoService fNodeDAOService;
    
    /**
     * Default constructor.
     */
    public NodeAccessControlListDAO()
    {
    }

    public void setNodeDaoService(NodeDaoService nodeDAOService)
    {
        fNodeDAOService = nodeDAOService;
    }
    
    /**
     * Get the ACL from a node.
     * @param nodeRef The reference to the node.
     * @return The ACL.
     * @throws InvalidNodeRefException
     */
    public DbAccessControlList getAccessControlList(NodeRef nodeRef)
    {
        Node node = fNodeDAOService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        return node.getAccessControlList();
    }
    
    /**
     * Set the ACL on a node.
     * @param nodeRef The reference to the node.
     * @param acl The ACL.
     * @throws InvalidNodeRefException
     */
    public void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl)
    {
        Node node = fNodeDAOService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        node.setAccessControlList(acl);
    }
}
