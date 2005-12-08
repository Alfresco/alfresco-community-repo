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
package org.alfresco.repo.domain;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;

/**
 * Represents a generic association between two nodes.  The association is named
 * and bidirectional by default.
 * 
 * @author Derek Hulley
 */
public interface NodeAssoc
{
    public long getId();

    /**
     * Wires up the necessary bits on the source and target nodes so that the association
     * is immediately bidirectional.
     * <p>
     * The association attributes still have to be set.
     * 
     * @param sourceNode
     * @param targetNode
     * 
     * @see #setName(String)
     */
    public void buildAssociation(Node sourceNode, Node targetNode);

    /**
     * Performs the necessary work on the {@link #getSource()() source} and
     * {@link #getTarget()() target} nodes to maintain the inverse association sets
     */
    public void removeAssociation();

    public AssociationRef getNodeAssocRef();
    
    public Node getSource();

    public Node getTarget();

    /**
     * @return Returns the qualified name of this association type 
     */
    public QName getTypeQName();

    /**
     * @param qname the qualified name of the association type
     */
    public void setTypeQName(QName qname);
}
