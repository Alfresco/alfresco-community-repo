/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeVersionKey;
import org.alfresco.repo.domain.node.StoreEntity;
import org.alfresco.repo.domain.node.TransactionEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.util.Pair;

class TestNode implements Node
{
    NodeRef nodeRef;
    
    TestNode(String id)
    {
        nodeRef = new NodeRef("test://store/" + id);
    }
    
    @Override
    public Long getId()
    {
        return null;
    }

    @Override
    public Long getAclId()
    {
        return null;
    }

    @Override
    public NodeVersionKey getNodeVersionKey()
    {
        return null;
    }

    @Override
    public void lock()
    {
    }

    @Override
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    @Override
    public Status getNodeStatus(QNameDAO qnameDAO)
    {
        return null;
    }

    @Override
    public Pair<Long, NodeRef> getNodePair()
    {
        return null;
    }

    @Override
    public boolean getDeleted(QNameDAO qnameDAO)
    {
        return false;
    }

    @Override
    public Long getVersion()
    {
        return null;
    }

    @Override
    public StoreEntity getStore()
    {
        return null;
    }

    @Override
    public String getUuid()
    {
        return null;
    }

    @Override
    public Long getTypeQNameId()
    {
        return null;
    }

    @Override
    public Long getLocaleId()
    {
        return null;
    }

    @Override
    public TransactionEntity getTransaction()
    {
        return null;
    }

    @Override
    public AuditablePropertiesEntity getAuditableProperties()
    {
        return null;
    }

    @Override
    public String getShardKey()
    {
        return null;
    }

}
