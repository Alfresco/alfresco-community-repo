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

import org.alfresco.service.namespace.QName;

/**
 * Interface to Aspect names on AVM nodes.
 * @author britt
 */
public interface AVMAspectName
{
    /**
     * Set the node that has the Aspect. 
     * @param node The node.
     */
    public void setNode(AVMNode node);
    
    /**
     * Get the node that has this Aspect name.
     * @return The AVM Node.
     */
    public AVMNode getNode();
    
    /**
     * Set the name of the Aspect.
     * @param name The QName of the Aspect.
     */
    public void setName(QName name);
    
    /**
     * Get the name of this Aspect.
     * @return The QName of this aspect.
     */
    public QName getName();
}
