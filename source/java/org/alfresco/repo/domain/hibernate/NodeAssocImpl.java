/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Hibernate-specific implementation of the generic node association
 * 
 * @author Derek Hulley
 */
public class NodeAssocImpl implements NodeAssoc
{
    private long id;
    private Node source;
    private Node target;
    private QName typeQName;
    private transient AssociationRef nodeAssocRef;

    public NodeAssocImpl()
    {
    }

    public void buildAssociation(Node sourceNode, Node targetNode)
    {
        // add the forward associations
        this.setTarget(targetNode);
        this.setSource(sourceNode);
        // Force initialization of the inverse collections
        // so that we don't queue additions to them.
        // This can go if we move to set-based collections
        sourceNode.getSourceNodeAssocs().size();
        targetNode.getTargetNodeAssocs().size();
        // add the inverse associations
        sourceNode.getTargetNodeAssocs().add(this);
        targetNode.getSourceNodeAssocs().add(this);
    }
    
    public void removeAssociation()
    {
        // maintain inverse assoc from source node to this instance
        this.getSource().getTargetNodeAssocs().remove(this);
        // maintain inverse assoc from target node to this instance
        this.getTarget().getSourceNodeAssocs().remove(this);
    }
    
    public synchronized AssociationRef getNodeAssocRef()
    {
        if (nodeAssocRef == null)
        {
            nodeAssocRef = new AssociationRef(getSource().getNodeRef(),
                    this.typeQName,
                    getTarget().getNodeRef());
        }
        return nodeAssocRef;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(32);
        sb.append("NodeAssoc")
          .append("[ source=").append(source)
          .append(", target=").append(target)
          .append(", name=").append(getTypeQName())
          .append("]");
        return sb.toString();
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof NodeAssoc))
        {
            return false;
        }
        NodeAssoc that = (NodeAssoc) obj;
        return (EqualsHelper.nullSafeEquals(this.getTypeQName(), that.getTypeQName())
                && EqualsHelper.nullSafeEquals(this.getTarget(), that.getTarget())
                && EqualsHelper.nullSafeEquals(this.getSource(), that.getSource()));
    }
    
    public int hashCode()
    {
        return (typeQName == null ? 0 : typeQName.hashCode());
    }

    public long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(long id)
    {
        this.id = id;
    }

    public Node getSource()
    {
        return source;
    }

    /**
     * For internal use
     */
    private void setSource(Node source)
    {
        this.source = source;
    }

    public Node getTarget()
    {
        return target;
    }

    /**
     * For internal use
     */
    private void setTarget(Node target)
    {
        this.target = target;
    }

    public QName getTypeQName()
    {
        return typeQName;
    }

    public void setTypeQName(QName typeQName)
    {
        this.typeQName = typeQName;
    }
}
