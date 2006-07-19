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

package org.alfresco.repo.avm;

import java.io.Serializable;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * @author britt
 *
 */
public class AVMNodePropertyImpl implements AVMNodeProperty, Serializable
{
    private static final long serialVersionUID = -7194228119659288619L;

    /**
     * The node that owns this.
     */
    private AVMNode fNode;
    
    /**
     * The QName of this property.
     */
    private QName fName;
    
    /**
     * The PropertyValue.
     */
    private PropertyValue fValue;

    /**
     * Default constructor.
     */
    public AVMNodePropertyImpl()
    {
    }
    
    /**
     * Get the owning node.
     * @return The AVMNode.
     */
    public AVMNode getNode()
    {
        return fNode;
    }

    /**
     * Set the owning node.
     * @param node The AVMNode to set.
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }

    /**
     * Get the name, a QName
     * @return A QName.
     */
    public QName getName()
    {
        return fName;
    }

    /**
     * Set the name, a QName.
     * @param name The QName.
     */
    public void setName(QName name)
    {
        fName = name;
    }

    /**
     * Get the value.
     * @return A PropertyValue
     */
    public PropertyValue getValue()
    {
        return fValue;
    }

    /**
     * Set the value.
     * @param value A PropertyValue.
     */
    public void setValue(PropertyValue value)
    {
        fValue = value;
    }
}
