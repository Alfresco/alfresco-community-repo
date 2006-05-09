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


public interface PlainDirectoryNodeBean extends DirectoryNodeBean
{
    /**
     * Set the child map.
     * @param children The Map to set.
     */
    public void setChildren(Map<String, DirectoryEntry> children);

    /**
     * Get the child map.
     * @return The map of child names to IDs.
     */
    public Map<String, DirectoryEntry> getChildren();
    
    /**
     * Set whether this node is a root directory.
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot);
    
    /**
     * Get whether this node is a root directory.
     * @return Whether it is.
     */
    public boolean getIsRoot();
}