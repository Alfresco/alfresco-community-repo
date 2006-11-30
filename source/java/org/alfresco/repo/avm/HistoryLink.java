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

/**
 * Interface for the ancestor-descendent relationship.
 * @author britt
 */
public interface HistoryLink
{
    /**
     * Set the ancestor part of this.
     * @param ancestor
     */
    public void setAncestor(AVMNode ancestor);
    
    /**
     * Get the ancestor part of this.
     * @return The ancestor.
     */
    public AVMNode getAncestor();

    /**
     * Set the descendent part of this.
     * @param descendent
     */
    public void setDescendent(AVMNode descendent);
    
    /**
     * Get the descendent part of this.
     * @return The descendent of this link.
     */
    public AVMNode getDescendent();
}
