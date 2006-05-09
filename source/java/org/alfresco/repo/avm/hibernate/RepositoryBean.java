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

package org.alfresco.repo.avm.hibernate;

import java.util.Map;
import java.util.Set;

/**
 * This is responsible for keeping track of the root 
 * directories for different versions.
 * @author britt
 */
public interface RepositoryBean
{
    /**
     * Set the name of this Repository.
     * @param name The name of the respository.
     */
    public void setName(String name);
    
    /**
     * Get the name of this Repository.
     * @return The name.
     */
    public String getName();
    
    /**
     * Set the current root.
     * @param root The root to set.
     */
    public void setRoot(DirectoryNodeBean root);
    
    /**
     * Get the current root.
     * @return The current root.
     */
    public DirectoryNodeBean getRoot();
    
    /**
     * Set the roots map.
     * @param roots The Map of version ids to roots.
     */
    public void setRoots(Map<Long, DirectoryNodeBean> roots);
    
    /**
     * Get the roots map.
     * @return The roots map.
     */
    public Map<Long, DirectoryNodeBean> getRoots();
    
    /**
     * Set the next version id.
     * @param nextVersionID The value to set.
     */
    public void setNextVersionID(long nextVersionID);
    
    /**
     * Get the next version id.
     * @return The next version id.
     */
    public long getNextVersionID();
    
    /**
     * Set the new nodes.
     * @param newNodes The new nodes Set to set.
     */
    public void setNewNodes(Set<AVMNodeBean> newNodes);
    
    /**
     * Get the new nodes.
     * @return The new nodes associated with this Repository.
     */
    public Set<AVMNodeBean> getNewNodes();
    
    /**
     * Set the version (for concurrency control).
     * @param vers The version to set.
     */
    public void setVers(long vers);
    
    /**
     * Get the version (for concurrency control).
     * @return The version.
     */
    public long getVers();
}
