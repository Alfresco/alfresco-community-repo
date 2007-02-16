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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

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
