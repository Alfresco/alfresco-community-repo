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


/**
 * The interface to support persistence of node access control entries in hibernate
 * 
 * @author andyh
 */
public interface DbAccessControlList
{
    public long getId();

    public Node getNode();
    
    public void setNode(Node node);
    
    /**
     * Get inheritance behaviour
     * @return
     */
    public boolean getInherits();
    
    /**
     * Set inheritance behaviour
     * @param inherits
     */
    public void setInherits(boolean inherits);
    
    /**
     * Delete the entries related to this access control list
     * 
     * @return Returns the number of entries deleted
     */
    public int deleteEntries();
}
